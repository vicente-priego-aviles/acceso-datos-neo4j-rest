package dev.javacadabra.acceso_datos_neo4j_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableNeo4jRepositories
@SpringBootApplication
public class AccesoDatosNeo4jRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccesoDatosNeo4jRestApplication.class, args);
	}
}
