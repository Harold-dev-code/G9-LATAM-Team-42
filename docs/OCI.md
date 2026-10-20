# ☁️ JouleAI – Configuración y Despliegue en OCI

**Hackathon – G9 LATAM Team 42**

## Resumen

Se implementó la configuración de una instancia en **Oracle Cloud Infrastructure (OCI)** y se realizó el empaquetamiento del Frontend, Backend y Data Science a través de **Docker** para el despliegue de los servicios del proyecto, garantizando la alta disponibilidad y la gestión segura de la información.

---

## Componentes OCI configurados

### Base de Datos Autónoma (Autonomous DB)

Persistencia relacional de usuarios, autenticación y consultas de registros procesados mediante `primary_key` por el backend.

- **Tablas:** `ANALISIS_ENERGETICO`, `USUARIO`
- **Seguridad por Red (ACL & TLS):** Autenticación mediante **TLS estándar** combinada con **Access Control List (ACL)**. Se configuró una regla de control de acceso estricta que solo permite peticiones de conexión originadas desde la IP pública de la instancia de Cómputo (`129.80.145.212`), bloqueando cualquier otro intento de acceso no autorizado a nivel de red.

### Cómputo (Compute Instance)

- **OS:** Ubuntu Linux
- **Función:** Host principal encargado de la ejecución y mantenimiento en línea de los contenedores (Docker) del sistema.

### Redes (VCN & Security Lists)

- **VCN & Subred Pública:** Enrutamiento de tráfico desde Internet hacia la instancia de cómputo.
- **Reglas de Entrada (Ingress Rules):** Apertura estricta en Security List e `iptables` para los puertos operativos:
  - `80/TCP` → Interfaz Web (Frontend React/Nginx)
  - `8080/TCP` → API REST (Backend Spring Boot)
- **IP Pública:** `129.80.145.212`

---

## Arquitectura de Despliegue (Docker Compose)

**Multi-container:** ejecución de los servicios Frontend, Backend y Data Science aislados e interconectados mediante la red interna de Docker.

### `docker-compose.yml`

El archivo cumple múltiples propósitos dentro de la configuración de la infraestructura declarativa:

| Propósito | Descripción |
|---|---|
| **Rutas de compilación** (`context`) | Establece el enrutamiento hacia el empaquetador (`Dockerfile`) de cada servicio. |
| **Construcción de imágenes** (`build` / `args`) | Genera la compilación del paquete aplicando las configuraciones del `Dockerfile` e inyectando variables de construcción (Build Time). |
| **Identificación del contenedor** (`container_name`) | Asigna un nombre estático a cada contenedor para su supervisión en tiempo de ejecución. |
| **Exposición de red** (`ports`) | Establece el mapeo de puertos entre la instancia host de OCI y los servicios del contenedor. |
| **Variables de entorno** (`env_file`) | Inyecta y conecta las variables del archivo sensible `.env` con los servicios. |
| **Orquestación de arranque** (`depends_on`) | Define el orden secuencial de inicio de los servicios. |
| **Alta disponibilidad** (`restart: always`) | Garantiza que los contenedores se vuelvan a levantar de forma automática ante un reinicio del motor de Docker o de la VM de OCI. |
| **Redes aisladas** (`networks`) | Establece una comunicación privada entre los contenedores del proyecto. |

### Servicios

| Servicio | Imagen / Stack | Puerto | Descripción |
|---|---|---|---|
| `frontend-app` | React/Vite + Nginx | `80` | La variable `VITE_API_URL` es inyectada en la compilación (Build Time). |
| `backend-springboot` | Java 25 / Spring Boot | `8080` | Lógica de negocio, autenticación JWT y persistencia en la Base de Datos Autónoma de OCI. |
| `python-service` | Python (Flask/ML) | `5000` (privado) | Microservicio analítico expuesto solo para consumo interno del backend. |

### Variables de entorno y ejecución local

Hay dos formas de iniciar el backend, y las variables se configuran de manera distinta:

- **Backend ejecutado directamente con Maven (perfil `dev`):** las asignaciones escritas antes de `./mvnw` se heredan como variables de entorno del proceso Spring Boot. No hace falta un archivo `.env` para este modo:

  ```bash
  cd backend
  EXCHANGERATE_API_KEY='<tu-clave>' \
  GEMINI_API_KEY='<tu-clave>' \
  ./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -DskipTests
  ```

- **Servicios ejecutados con Docker Compose:** Compose no hereda las variables de un proceso Maven que se inició en otra terminal. En este caso, crea el archivo `.env` en la raíz del repositorio usando `.env.example` y reemplaza los valores de ejemplo. Compose carga ese archivo para el backend y el servicio de Python; el frontend recibe `VITE_API_URL` como argumento de compilación.

  ```bash
  cp .env.example .env
  # Edita .env y establece tus claves y la URL que corresponda
  docker compose config --quiet
  docker compose up --build -d
  docker compose ps
  docker compose logs --tail=100 servicio-data backend-springboot frontend-app
  ```

  Para probar localmente, usa `SPRING_PROFILES_ACTIVE=dev`, `PUBLIC_IP=http://localhost` y `VITE_API_URL=http://localhost:8080`; abre el frontend en `http://localhost/`. El frontend se compila con `VITE_API_URL`, por lo que cambiar ese valor requiere reconstruir su imagen.

En OCI, configura `.env` directamente en el servidor (no lo subas a Git), usa `SPRING_PROFILES_ACTIVE=prod` y proporciona las variables de Autonomous Database indicadas abajo. `VITE_API_URL` debe apuntar a la URL pública del backend, no a `localhost`; reconstruye las imágenes después de modificarlo. Para detener localmente los contenedores, ejecuta `docker compose down`.

### Gestión de Configuración Sensible

Aislamiento total de credenciales y variables de entorno mediante archivos `.env` locales en la instancia de OCI, evitando la filtración de secretos en el repositorio Git:

```
DB_HOST
DB_USER
DB_PASSWORD
DB_PORT
DB_SERVICE_NAME
GEMINI_API_KEY
EXCHANGERATE_API_KEY
SPRING_PROFILES_ACTIVE
PUBLIC_IP
PYTHON_SERVICE_URL
VITE_API_URL
```

Para el perfil `prod`, establece `SPRING_PROFILES_ACTIVE=prod` y define `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_PORT` y `DB_SERVICE_NAME` con los datos de Autonomous Database. Las claves `GEMINI_API_KEY` y `EXCHANGERATE_API_KEY` deben corresponder a sus proveedores. En local con el perfil `dev`, la base H2 en memoria evita requerir las variables de Oracle.

---

## Diagrama de Procesos

```text
[Cliente / Internet]
        │
   (IP: 129.80.145.212)
        │
┌────────────────▼────────────────────────────────┐
│ OCI Security List (Puertos 80, 8080)             │
└────────────────▼────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ OCI Compute Instance (Ubuntu)                    │
│                                                   │
│ ├─► frontend-app (Nginx)       ──► Puerto :80    │
│ ├─► backend-springboot (Java)  ──► Puerto :8080  │
│ └─► python-service (Python)    ──► Puerto :5000  │
└─────▼───────────────────────────────────────────┘
      │
      │ Conexión TLS Cifrada
      │ (Permitido solo para IP: 129.80.145.212 via ACL)
      │
┌─────▼───────────────────────────────────────────┐
│ OCI Autonomous Database                          │
│ └─ Access Control List (ACL): ACTIVE             │
└─────────────────────────────────────────────────┘
```

---

## Mantenimiento y Actualización

### Acceso Remoto Seguro

Conexión a la VM de OCI mediante túnel SSH protegido por par de llaves RSA/Ed25519 a través del puerto 22:

```bash
ssh -i {Ruta al Archivo SSH.key} ubuntu@129.80.145.212
```

### Actualización de Código Fuente

```bash
cd ~/julesia/G9-LATAM-Team-42
git pull origin main
```

### Reconstrucción y Despliegue de Contenedores

Recompila las imágenes según la configuración de los parámetros establecidos en `docker-compose.yml`:

```bash
docker compose up --build
```

### Verificación de Estado y Logs

Monitoreo en tiempo real del funcionamiento y arranque de los microservicios:

```bash
docker ps
docker logs -f backend-springboot
```

---

## Demo en Vivo

🔗 **URL pública:** [http://129.80.145.212](http://129.80.145.212)

---

⬅️ [Volver al README principal](../README.md)
