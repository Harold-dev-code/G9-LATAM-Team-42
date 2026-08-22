# Etapa 1: Compilación con Node.js
FROM node:20-alpine AS builder
WORKDIR /app

# Copiar archivos de dependencias e instalar
COPY package*.json ./
RUN npm install

ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Copiar el código fuente y compilar
COPY . .
RUN npm run build

# Etapa 2: Servir con Nginx
FROM nginx:alpine

# Copiar el resultado del build a la carpeta de Nginx
# (Si usas Vite/React la carpeta suele ser /app/dist, si es Create React App es /app/build)
COPY --from=builder /app/dist /usr/share/nginx/html

# Configuración opcional para evitar error 404 al recargar páginas con rutas SPA
RUN echo 'server { listen 80; location / { root /usr/share/nginx/html; index index.html; try_files $uri $uri/ /index.html; } }' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
