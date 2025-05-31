# Acceder a Datos de Neo4j con REST

Este tutorial te guiará a través del proceso de creación de una aplicación que accede a datos basados ​​en grafos a través de un frontend RESTful [basado en hipermedia](https://spring.io/guides/gs/rest-hateoas).

>[!success] Código de GitHub
>Puedes clonar o hacer un fork del código de esta guía desde mi repositorio de [GitHub](https://github.com/vicente-priego-aviles/acceso-datos-neo4j-rest)
>
>`https://github.com/vicente-priego-aviles/acceso-datos-neo4j-rest.git`

## Qué crearás

Crearás una aplicación Spring que te permitirá crear y recuperar objetos `Person` almacenados en una base de datos NoSQL de Neo4j mediante Spring Data REST. Spring Data REST aprovecha las características de [Spring HATEOAS](https://spring.io/projects/spring-hateoas) y [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j) y las combina automáticamente.

## Qué necesitas

- Aproximadamente 15 minutos

- Tu editor de texto o IDE favorito

- Java 17 o posterior

- Gradle 7.5+ o Maven 3.5+

- También puedes importar el código directamente a tu IDE:

	- [IntelliJ IDEA](https://spring.io/guides/gs/intellij-idea)

	- [VSCode](https://spring.io/guides/gs/guides-with-vscode)

	- [Spring Tool Suite (STS)](https://spring.io/guides/gs/sts)

## Puesta en marcha de un servidor Neo4j

Antes de compilar esta aplicación, necesitas configurar un servidor Neo4j.

Neo4j cuenta con un servidor de código abierto que puedes instalar gratis.

### Instalación directa

En una Mac con Homebrew instalado, puedes escribir lo siguiente en una ventana de terminal:

`$ brew install neo4j`

Para otras opciones, consulta https://neo4j.com/download/community-edition/

### Instalación con Docker

Todos los detalles de cómo instalar un contenedor Docker de neo4j está explicado [[Introducción a Neo4j en Docker| aquí]].

### Lanzando el contenedor

Una vez instalado Neo4j, puedes iniciarlo con su configuración predeterminada ejecutando el siguiente comando:

`$ neo4j start`

Deberías ver un mensaje similar al siguiente:

```shell
Starting Neo4j.
Started neo4j (pid 96416). By default, it is available at http://localhost:7474/
There may be a short delay until the server is ready.
See /usr/local/Cellar/neo4j/3.0.6/libexec/logs/neo4j.log for current status.
```

Por defecto, Neo4j tiene el nombre de usuario y la contraseña `neo4j` y `neo4j`. Sin embargo, es necesario cambiar la contraseña de la nueva cuenta. Para ello, ejecute el siguiente comando:

```shell
$ curl -v -u neo4j:neo4j POST localhost:7474/user/neo4j/password -H "Content-type:application/json" -d "{\"password\":\"secret\"}"
```
Esto cambia la contraseña de `neo4j` a `secret` (algo que NO se debe hacer en producción). Una vez completado esto, debería estar listo para ejecutar esta guía.

## Empezando con Spring Initializr

Puede usar [este proyecto preinicializado](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.5.0&packaging=jar&jvmVersion=21&groupId=com.ejemplo&artifactId=acceso-datos-neo4j-rest&name=acceso-datos-neo4j-rest&description=Proyecto%20de%20demostración%20Spring%20Boot%20para%20acceso%20a%20datos%20de%20Neo4j%20con%20Spring%20Data&packageName=com.ejemplo.acceso-datos-neo4j-rest&dependencies=data-rest,data-neo4j)y hacer clic en `Generate` para descargar un archivo ZIP. Este proyecto está configurado para adaptarse a los ejemplos de este tutorial.

Para inicializar el proyecto manualmente:

1. Ve a https://start.spring.io. Este servicio extrae todas las dependencias necesarias para una aplicación y realiza la mayor parte de la configuración automáticamente.

2. Elije **Gradle** o **Maven** y el lenguaje que desees usar. Esta guía asume que elegiste **Java**.

3. Haz clic en **Dependencies** y selecciona **Rest Repositories** y **Spring Data Neo4j**.

4. Haz clic en **Generate**.

Descarga el archivo ZIP resultante, que es un archivo comprimido de una aplicación web configurada con tus preferencias.

>[!note] Nota
Si tu IDE tiene la integración con Spring Initializr, puede completar este proceso desde el IDE.

## Permisos para acceder a Neo4j

*Neo4j Community Edition* requiere credenciales para acceder. Puedes configurar las credenciales configurando las propiedades en `src/main/resources/application.properties`, como se indica a continuación:

```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=neo4j
```

Esto incluye el nombre de usuario predeterminado (`neo4j`) y la contraseña recién configurada (`neo4j`) que estableciste anteriormente.

>[!warning] Cuidado
No guardes las credenciales reales en tu repositorio de código fuente. En su lugar, configúralas en el entorno de ejecución mediante la [externalización de propiedades de Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config).

## Crear un objeto de dominio

Debe crear un nuevo objeto de dominio para representar a una persona, como se muestra en el siguiente ejemplo (en `src/main/java/com/ejemplo/acceso_datos_neo4j_rest/Persona.java`):

```java
package com.ejemplo.acceso_datos_neo4j_rest;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

@Node
public class Persona {
 
	@Id @GeneratedValue private Long id;
	
	private String nombre;
	private String apellido;

	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getApellido() {
		return apellido;
	}
	
	public void setApellidos(String apellido) {
		this.apellido = apellido;
	}
}
```

El objeto `Persona` tiene `nombre` y `apellido`. También hay un objeto ID configurado para generarse automáticamente, por lo que no es necesario hacerlo manualmente.


## Crear un repositorio de `Persona`

A continuación, debes crear un repositorio simple, como se muestra en el siguiente ejemplo (en `src/main/java/com/ejemplo/acceso_datos_neo4j_rest/PersonaRepository.java'):


```java
package com.ejemplo.acceso_datos_neo4j_rest;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "personas", path = "personas")
public interface PersonaRepository extends 
		PagingAndSortingRepository<Persona, Long>, 
		CrudRepository<Persona, Long> {

	List<Persona> findByApellido(@Param("nombre") String nombre);
}
```

Este repositorio es una interfaz que permite realizar diversas operaciones relacionadas con objetos `Person`. Estas operaciones se obtienen extendiendo la interfaz `PagingAndSortingRepository` definida en *Spring Data Commons*.

En tiempo de ejecución, Spring Data REST crea automáticamente una implementación de esta interfaz. Luego, utiliza la anotación `@RepositoryRestResource` para indicar a *Spring MVC* que cree endpoints RESTful en `/personas`.

>[!note] Nota
`@RepositoryRestResource` no es necesario para exportar un repositorio. Solo se usa para cambiar los detalles de la exportación, como usar `/gente` en lugar del valor predeterminado de `/personas`.

Aquí también has definido una consulta personalizada para recuperar una lista de objetos `Persona` según el valor de `apellido`. Puedes ver cómo invocar esta consulta más adelante en esta guía.

## Encontrar la clase de aplicación

*Spring Initializr* crea una clase de aplicación al crear un proyecto. Puedes encontrarla en `src/main/java/com/ejemplo/acceso_datos_neo4j_rest/Application.java`. Ten en cuenta que *Spring Initializr* concatena (y cambia correctamente entre mayúsculas y minúsculas) el nombre del paquete y lo añade a la aplicación para crear el nombre de la aplicación. En este caso, obtenemos `AccesoDatosNeo4jRestApplication`, como se muestra en el siguiente código:

```java
package com.ejemplo.acceso_datos_neo4j_rest;

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
```

Es necesario realizar algún cambio en esta clase de aplicación para este ejemplo.

`@SpringBootApplication` es una anotación conveniente que añade todo lo siguiente:

- `@Configuration`: Etiqueta la clase como fuente de definiciones de beans para el contexto de la aplicación.

- `@EnableAutoConfiguration`: Indica a Spring Boot que empiece a añadir beans según la configuración del *classpath*, otros beans y diversas propiedades de configuración. Por ejemplo, si `spring-webmvc` está en la ruta de clases, esta anotación marca la aplicación como una aplicación web y activa comportamientos clave, como la configuración de un `DispatcherServlet`.

- `@ComponentScan`: Indica a Spring que busque otros componentes, configuraciones y servicios en el paquete `com/example`, lo que le permite encontrar los controladores.

El método `main()` utiliza el método `SpringApplication.run()` de Spring Boot para iniciar una aplicación. ¿Observaste que no hay ni una sola línea de XML? Tampoco hay un archivo `web.xml`. Esta aplicación web está completamente desarrollada en Java y no tienes que configurar ningún componente ni infraestructura.

La anotación `@EnableNeo4jRepositories` activa *Spring Data Neo4j*. Spring Data Neo4j crea una implementación concreta de `PersonRepository` y la configura para comunicarse con una base de datos Neo4j integrada mediante el lenguaje de consulta *Cypher*.

La anotación `@EnableTransactionManagement` es fundamental para aprovechar la potente funcionalidad de gestión transaccional declarativa de Spring. Al usarla, te liberas de la necesidad de escribir manualmente el código de transacción, permitiendo que Spring gestione las transacciones de manera transparente basándose en la presencia de la anotación `@Transactional` en tus servicios y componentes. Esto mejora la legibilidad, mantenibilidad y robustez de tu aplicación, especialmente en escenarios donde la integridad de los datos es crítica.

#### Crea un JAR ejecutable

Puedes ejecutar la aplicación desde la línea de comandos con Gradle o Maven. También puedes crear un único archivo JAR ejecutable que contenga todas las dependencias, clases y recursos necesarios y ejecutarlo. Crear un JAR ejecutable facilita la distribución, versionado y despliegue del servicio como aplicación a lo largo del ciclo de vida del desarrollo, en diferentes entornos, etc.

Si usas **Maven**, puedes ejecutar la aplicación con `./mvnw spring-boot:run`. Como alternativa, puede compilar el archivo JAR con  `./mvnw clean package` y luego ejecutarlo, como se indica a continuación:

```shell
java -jar target/{project_id}-0.1.0.jar
```

Si usas **Gradle**, puedes ejecutar la aplicación con `./gradlew bootRun`. Como alternativa, puedes crear el archivo JAR con `./gradlew build` y luego ejecutarlo, como se indica a continuación:

```shell
java -jar build/libs/{project_id}-0.1.0.jar
```

A continuación se muestran los logs salida. El servicio debería estar listo y funcionando en unos segundos.

```shell
...

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.0)

2025-05-30T19:36:54.284+02:00  INFO 215641 --- [acceso-datos-neo4j-rest] [           main] d.j.a.AccesoDatosNeo4jRestApplication    : Starting AccesoDatosNeo4jRestApplication using Java 21.0.7 with PID 215641 

...

2025-05-30T19:36:55.445+02:00  INFO 215641 --- [acceso-datos-neo4j-rest] [           main] d.j.a.AccesoDatosNeo4jRestApplication    : Started AccesoDatosNeo4jRestApplication in 1.414 seconds (process running for 1.567)
```

## Prueba la aplicación

Ahora que la aplicación está ejecutándose, puedes probarla. Puedes usar cualquier cliente REST que deses. Los siguientes ejemplos usan la herramienta unix/linux llamada `curl`.

Lo primero que puedes hacer es ver el servicio de nivel superior. El siguiente ejemplo muestra cómo hacerlo:

```shell
$ curl http://localhost:8080
```

```json
{
  "_links": {
    "personas": {
      "href": "http://localhost:8080/personas{?page,size,sort*}",
      "templated": true
    },
    "profile": {
      "href": "http://localhost:8080/profile"
    }
  }
}
```

Aquí se muestra un primer vistazo de lo que ofrece este servidor. Hay un enlace de `personas` en [http://localhost:8080/personas](http://localhost:8080/personas). Incluye algunas opciones como `?page`, `?size`, y `?sort`.

>[!info] Nota
Spring Data REST utiliza el [formato HAL](https://stateless.co/hal_specification.html) para la salida JSON. Es flexible y ofrece una forma práctica de proporcionar enlaces adyacentes a los datos entregados.


```shell
$ curl http://localhost:8080/personas
```

```json
{
  "_embedded": {
    "personas": []
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas?page=0&size=20"
    },
    "profile": {
      "href": "http://localhost:8080/profile/personas"
    },
    "search": {
      "href": "http://localhost:8080/personas/search"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "number": 0
  }
}
```

Actualmente no hay elementos ni, por lo tanto, páginas, así que es hora de crear una nueva `Persona`. Para ello, ejecuta el siguiente comando:

```shell
$ curl -i -X POST -H "Content-Type:application/json" -d '{ "nombre" : "Frodo", "apellido" : "Bolson" }' http://localhost:8080/personas
```

```json
HTTP/1.1 201    
Vary: Origin  
Vary: Access-Control-Request-Method  
Vary: Access-Control-Request-Headers  
Location: http://localhost:8080/personas/0  
Content-Type: application/hal+json  
Transfer-Encoding: chunked  
Date: Sat, 31 May 2025 07:58:30 GMT  
  
{
  "nombre": "Frodo",
  "apellido": "Bolson",
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas/0"
    },
    "persona": {
      "href": "http://localhost:8080/personas/0"
    }
  }
}
```

- `-i` garantiza que se pueda ver el mensaje de respuesta, incluidos los encabezados. Se muestra la URI de la `Persona` recién creada.

- `-X POST` indica que se trata de un `POST`, utilizado para crear una nueva entrada.

- `-H "Content-Type:application/json"` establece el tipo de contenido para que la aplicación sepa que la carga útil contiene un objeto JSON.
(mostrado con su salida)
- `-d '{ "firstName": "Frodo", "lastName": "Bolson" }'` son los datos que se envían.

>[!note] Nota
Observa cómo la operación `POST` anterior incluye un encabezado `Location`. Este contiene la URI del recurso recién creado. Spring Data REST también cuenta con el método `RepositoryRestConfiguration.setReturnBodyOnCreate(…)`, que puedes usar para configurar el framework y que devuelva inmediatamente la representación del recurso recién creado.

Desde aquí puedes consultar todas las personas ejecutando el siguiente comando:

```shell
$ curl http://localhost:8080/personas
```

```json
{
  "_embedded": {
    "personas": [
      {
        "nombre": "Frodo",
        "apellido": "Bolson",
        "_links": {
          "self": {
            "href": "http://localhost:8080/personas/0"
          },
          "persona": {
            "href": "http://localhost:8080/personas/0"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas?page=0&size=20"
    },
    "profile": {
      "href": "http://localhost:8080/profile/personas"
    },
    "search": {
      "href": "http://localhost:8080/personas/search"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}
```

El objeto `Personas` contiene una lista con Frodo. Observa cómo incluye un enlace `self`. 

>[!info] Nota
Spring Data REST también utiliza la biblioteca [Evo Inflector](https://www.atteo.org/2011/12/12/EvoInflector.html) para pluralizar el nombre de la entidad en agrupaciones, aunque solo reconoce palabras en inglés.

Puedes consultar directamente el registro individual ejecutando el siguiente comando:

```shell
$ curl http://localhost:8080/personas/0
```

```json
{  
 "nombre" : "Frodo",  
 "apellido" : "Bolson",  
 "_links" : {  
   "self" : {  
     "href" : "http://localhost:8080/personas/0"  
   },  
   "persona" : {  
     "href" : "http://localhost:8080/personas/0"  
   }  
 }  
}
```

Puede parecer que esto es puramente web, pero, en segundo plano, hay una base de datos de grafos de Neo4j integrada. En producción, probablemente se conectaría a un servidor Neo4j independiente.

Puedes usar la siguiente consulta en el [interfaz de usuario de Neo4j](http://localhost:7474/browser/) para comprobar que se ha guardado el objeto `Persona`:

```chyper
MATCH (n) return n
```

![[creacion-servicio-web-neo4j-persona.png]]

En esta guía, solo hay un objeto de dominio. En sistemas más complejos, donde los objetos de dominio están relacionados entre sí, Spring Data REST genera enlaces adicionales para facilitar la navegación a los registros conectados.

Puedes encontrar todas las consultas personalizadas ejecutando el siguiente comando:

```shell
$ curl http://localhost:8080/personas/search
```

```json
{
  "_links": {
    "findByApellido": {
      "href": "http://localhost:8080/personas/search/findByApellido{?nombre}",
      "templated": true
    },
    "self": {
      "href": "http://localhost:8080/personas/search"
    }
  }
}
```

Puedes ver la URL de la consulta, incluyendo el parámetro HTTP de consulta: `nombre`. Ten en cuenta que esto coincide con la anotación `@Param("nombre")` incrustada en la interfaz.

Para usar la consulta `findByApellido`, ejecuta el siguiente comando:

```shell
$ curl http://localhost:8080/personas/search/findByApellido?nombre=Bolson
```

```json
{
  "_embedded": {
    "personas": [
      {
        "nombre": "Frodo",
        "apellido": "Bolson",
        "_links": {
          "self": {
            "href": "http://localhost:8080/personas/0"
          },
          "persona": {
            "href": "http://localhost:8080/personas/0"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas/search/findByApellido?nombre=Bolson"
    }
  }
}
```

Dado que definiste en el código que devolviera una `List<Persona>` , devuelve todos los resultados. Si lo hubieras definido para que devolviera solo `Persona`, seleccionaría uno de los objetos `Persona` para devolver. Dado que esto puede ser impredecible, probablemente no desee hacerlo para consultas que puedan devolver múltiples entradas.

También puede realizar peticiones REST `PUT`, `PATCH` y `DELETE` para reemplazar, actualizar o eliminar registros existentes. 

El siguiente ejemplo muestra una petición `PUT`:

```shell
$ curl -X PUT -H "Content-Type:application/json" -d '{ "nombre": "Bilbo", "apellido": "Bolson" }' http://localhost:8080/personas/0
```

```shell
$ curl http://localhost:8080/personas/0
```

```json
{
  "nombre": "Bilbo",
  "apellido": "Bolson",
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas/0"
    },
    "persona": {
      "href": "http://localhost:8080/personas/0"
    }
  }
}
```

El siguiente ejemplo muestra una llamada `PATCH`:

```shell
$ curl -X PATCH -H "Content-Type:application/json" -d '{ "nombre": "Bilbo Junior" }' http://localhost:8080/personas/0
```

```shell
$ curl http://localhost:8080/personas/0
```

```json
{
  "nombre": "Bilbo Junior",
  "apellido": "Bolson",
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas/0"
    },
    "persona": {
      "href": "http://localhost:8080/personas/0"
    }
  }
}
```

>[!info] Nota
`PUT` reemplaza un registro completo. Los campos que no se proporcionan se reemplazan con valores nulos. 
`PATCH` puede usarse para actualizar un subconjunto de elementos.

También puedes eliminar registros, como lo muestra el siguiente ejemplo:

```shell
$ curl -X DELETE http://localhost:8080/personas/0
```

```shell
$ curl http://localhost:8080/personas
```

```json
{
  "_embedded": {
    "personas": []
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/personas?page=0&size=20"
    },
    "profile": {
      "href": "http://localhost:8080/profile/personas"
    },
    "search": {
      "href": "http://localhost:8080/personas/search"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "number": 0
  }
}
```

Una ventaja de esta interfaz basada en hipermedia es que permite descubrir todos los endpoints RESTful usando curl (o cualquier otro cliente REST que prefieras). No es necesario intercambiar un contrato formal ni un documento de interfaz con tus clientes.

## Resumen

¡Enhorabuena! Acabas de desarrollar una aplicación con un frontend RESTful [[Creación de un Servicio Web RESTful basado en Hipermedia|basado en hipermedia]] y un backend basado en Neo4j.