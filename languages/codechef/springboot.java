public class springboot {
    
}
/*

Prometheus and grafana

Spring Boot and Tomcat are both integral parts of modern Java development, but they serve different purposes and operate at different levels of the application stack. Here's a comparison and explanation of how Spring Boot works and what advantages it offers over a traditional Tomcat server setup.

### What is Spring Boot?

**Spring Boot** is a framework for building Java-based applications. It is part of the larger Spring ecosystem and is designed to simplify the process of creating stand-alone, production-grade Spring-based applications. Spring Boot offers several key features:

1. **Auto-Configuration**: Spring Boot automatically configures application components based on the classpath, properties, and other settings. This reduces the need for extensive configuration and setup.

2. **Embedded Servers**: Spring Boot includes embedded servers (like Tomcat, Jetty, or Undertow). This means you can package your application as a standalone JAR or WAR file with the server included, which simplifies deployment.

3. **Production-Ready Features**: Spring Boot provides out-of-the-box features such as health checks, metrics, and application monitoring, which are essential for production environments.

4. **Spring Boot Starters**: Starters are pre-configured sets of dependencies that simplify the setup of common tasks, such as connecting to databases, handling security, and integrating with messaging systems.

5. **Spring Boot Actuator**: Provides additional features for managing and monitoring Spring Boot applications, including endpoints for application health, metrics, and more.

### How Does Spring Boot Work?

1. **Auto-Configuration**:
   - **Classpath Scanning**: Spring Boot scans the classpath for available libraries and dependencies. Based on the libraries present, it auto-configures beans and settings for those libraries.
   - **Configuration Classes**: Spring Boot uses `@Configuration` classes and `@Conditional` annotations to conditionally configure beans based on the application environment.

2. **Embedded Server**:
   - **Server Initialization**: Spring Boot includes an embedded server (like Tomcat by default). The application is packaged as an executable JAR/WAR file, and the server is started from within the application itself.
   - **Servlet Container**: When the application starts, it initializes the embedded server and deploys the application in it.

3. **Production-Ready Features**:
   - **Actuator Endpoints**: Provides endpoints like `/actuator/health` and `/actuator/metrics` for monitoring and management.
   - **Metrics**: Collects and exposes metrics about the application’s performance and health.

4. **Spring Boot Starters**:
   - **Dependency Management**: Starters simplify dependency management by including common libraries and their dependencies in a single artifact. For example, `spring-boot-starter-web` includes dependencies for building web applications.

### Comparison with Traditional Tomcat Setup

#### Traditional Tomcat Server

1. **Deployment**:
   - **WAR Files**: Applications are typically packaged as WAR files and deployed to an external Tomcat server.
   - **Configuration**: Requires manual configuration of the server and application settings (e.g., `server.xml`, `web.xml`).

2. **Server Management**:
   - **External Server**: Requires managing and maintaining the Tomcat server separately from the application.
   - **Configuration**: Server-specific configuration is required, such as port settings and resource management.

3. **Application Deployment**:
   - **Complexity**: Deploying and updating applications involves dealing with the external server and potentially complex deployment scripts or processes.

#### Spring Boot with Embedded Tomcat

1. **Deployment**:
   - **Standalone Application**: Applications are packaged as standalone JAR or WAR files with the embedded server. This simplifies deployment as the server is bundled with the application.
   - **Auto-Configuration**: Reduces the need for manual configuration of the server and application components.

2. **Server Management**:
   - **Embedded Server**: The embedded Tomcat server is started and managed within the Spring Boot application. No separate server management is required.
   - **Simplified Configuration**: Configuration is handled through application properties (`application.properties` or `application.yml`).

3. **Application Deployment**:
   - **Ease of Deployment**: Deployment is simplified because the application is self-contained with the server included. No need to separately manage or configure the server.

### Summary

- **Spring Boot**:
  - Provides auto-configuration and embedded servers, simplifying setup and deployment.
  - Includes production-ready features such as monitoring and management.
  - Reduces manual configuration and server management overhead.

- **Traditional Tomcat**:
  - Requires separate server management and configuration.
  - Applications are deployed as WAR files to an external Tomcat server.

Spring Boot’s approach offers a more streamlined development and deployment process, making it easier to build, configure, and deploy Java applications with embedded servers and production-ready features.

What are the different types of advice in Spring AOP?
In Spring AOP, advice is the action taken by an aspect at a particular join point. There are five types of advice:

Before advice: Runs before the method execution.
After advice: Runs after the method execution, regardless of its outcome.
After returning advice: Runs after the method returns successfully.
After throwing advice: Runs after the method throws an exception.
Around advice: Surrounds the method execution, allowing you to control when the method is called.

Annotations: in Java are a powerful mechanism that allows developers to add metadata to their code.
    @RestController = @Controller + @ResponseBody
    ResponseBody:= denotes that reponse is a response
    @RequestMapping(path="", method:"GET")
        := mappes the api details to a function
        Can mentioned it at the top of class
        Mention rest of the path in the methods using GetMapping and PostMapping
    @RequestParam
        Used to bind request params to methods
        @RequestParam(name="firstName" , required=false) String firstName
    @InitBinder
        Checks and do some pre-processing on the data before calling the methods
        Need to write a customEditor init
    @PathVariable
        path = "/name/{pathvar}"
        @PathVariable(value="pathvar") String firstName
    @RequestBody
        Binds the body of hhtp request to to method parameters
        @RequestBody User user
        can set Json_property for custom name in class
    @ResponseEntity
        It contains all the response params that is required to add
        public ResponseEntity<className> methodName...
        return ResponseEntity.status(200).body(output)



@Component: expects default construtor for each class and now if we write a constrictor then default constrictor wont get created. Then program wont start
Then

We have to write a external configuration and write a @Bean annotaion to define what default values we need to use while creating bean

No scope means singleton, eligible for eager Initialization of bean

Autowired: checks if bean for required class is already created, if yes then it'll inject it otherwise it'll create it and the inject it

SetterInjection:
    create a setter method for each injection and make the the setter method as autowired. Injection will be set later after object creation
construtor injection:(highly used)
    dependency gets resolved while object creation itself
    can use final with the dependencies and can make immutable
    fail faster: if it gonna fail then it will fail immidietly
        using @PostConstruct and check if(order==null)
    
    // Order of execution:
    //     Constructor->autowired->PostConstruct->predestroy

    Circular dependecy:
        order is dependent on invoice
        and invoice is dependent on order
            Refactor the code, copy the common code in different class
            @Lazy
            @PostConstruct(gets executed after all autowired is completed)
    If multiple implementation is available for a abstract class, then either make one of them primary or mention @Qualifier("name_of_implmentation")
    Reflection in Spring Boot refers to the process of inspecting and manipulating classes, methods, fields, and annotations at runtime. 

    @Scope(value="singleton")
        Singleton, Prototype, Request, Session, Application
            Singleton: 
                default scope, 1 instance for IOC, Eager instance(if lazy not mentioned explicitly)
            Prototype:
                Each time a new pbject is created
                Lazily initialized
            Request:
                New object is created for each HTTP Request
                One object per request, doesnt matter how many time its being used in different classes
                Lazily initialized
                    If parent is singleton and dependecy is request, then it'll fail during eager Initialization
                    Need to solve it using (request="value", proxyMode = ScopedProxyMode.Target_Class)
                    Create a dummy object for now, later when you create it, the give it to me
            Session:
                New object is created for each HTTP Session
                Lazily initialized
                When user access any endpoint, a session is created
                Remains active till it expires
                One session can have multiple http requests
            Application:
                One object per IOC

    Maven: pom.xml
        Build generation, dependency resolution,
        Phase:
            Validate
            Compile
            Test
            Package the compiled code (JAR/WAR)
            Verify the package
            Install the package
            Deploy in remote repository
        mvn validate
            validate:= all the checks mentioned in execution's phase = validate
        mvn compile = validate, compile
            compile:= convert all the code in ByteClass.code (target class)
            test:= run all the tests
            package:= makes the package from target classes
            verify:= will verify the package (maven pmd plugin)
                like unused variables or packages, empty catch, duplicate code
                we need to mention it explicilty
            install:=
                Installs in the local repository
                    Local repository:=our systems
                    Remote systems:= external systems
            deploy:=
                Installs in remote repo(not used that much)
                // need to provide repository id and remote repository url in distribution manager of pom.xml


        mvn install = validate, compile, test, package, verify, and install
        mvn test = validate, compile, and test

        <project xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            
            <modelVersion>4.0.0</modelVersion>

            <!-- Project Identification -->
            <groupId>com.example</groupId>
            <artifactId>my-app</artifactId>
            <version>1.0-SNAPSHOT</version>
            <packaging>jar</packaging>

            <!-- Project Metadata -->
            <name>My Application</name>
            <description>A simple Maven project</description>
            <url>http://www.example.com</url>

            <!-- Properties -->
            <properties>
                <maven.compiler.source>1.8</maven.compiler.source>
                <maven.compiler.target>1.8</maven.compiler.target>
                <!-- Custom properties can be added here -->
            </properties>

            <!-- Dependencies -->
            <dependencies>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                    <version>2.5.4</version>
                </dependency>
                <!-- Additional dependencies can be listed here -->
            </dependencies>

            <!-- Dependency Management -->
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-dependencies</artifactId>
                        <version>2.5.4</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>
                    <!-- Centralized dependency versions go here -->
                </dependencies>
            </dependencyManagement>

            <!-- Build Configuration -->
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.8.1</version>
                        <configuration>
                            <source>1.8</source>
                            <target>1.8</target>
                        </configuration>
                    </plugin>
                    <!-- Additional plugins can be configured here -->
                </plugins>
            </build>

            <!-- Repositories -->
            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://repo.maven.apache.org/maven2</url>
                </repository>
                <!-- Additional repositories can be listed here -->
            </repositories>

            <!-- Plugin Repositories -->
            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <url>https://repo.maven.apache.org/maven2</url>
                </pluginRepository>
                <!-- Additional plugin repositories can be listed here -->
            </pluginRepositories>

            <!-- Profiles -->
            <profiles>
                <profile>
                    <id>production</id>
                    <activation>
                        <property>
                            <name>env</name>
                            <value>prod</value>
                        </property>
                    </activation>
                    <properties>
                        <!-- Profile-specific properties -->
                    </properties>
                    <build>
                        <!-- Profile-specific build settings -->
                    </build>
                </profile>
                <!-- Additional profiles can be defined here -->
            </profiles>

        </project>

        LifeCycle:




 */