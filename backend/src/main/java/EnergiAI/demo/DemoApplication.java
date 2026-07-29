package EnergiAI.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/* Se agrega exclusion de bd para realizar pruebas de funcionamiento,
en caso de agregar bd, eliminar las siguentes lineas y descomentar la linea sobre la clase.*/
@SpringBootApplication (exclude = {DataSourceAutoConfiguration.class})

//@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}