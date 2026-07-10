# Profile
## Profile Annotation
- ```@Profile("prod")``
- We can tell SpringBoot to create this bean only when this particular profile is set

## Profiling
Here's a working example of how to set up region-based configurations in a Spring Boot application using `application.properties` and `@Profile`.

### Step-by-Step Example

1. **Create Your Spring Boot Project**:
   You can create a new Spring Boot project using Spring Initializr or your IDE.

2. **Define Region-Specific Property Files**:
   Create the following files in the `src/main/resources` directory:

   - **`application-us.properties`**:
     ```properties
     spring.datasource.url=jdbc:mysql://us-db-server:3306/mydb
     spring.datasource.username=us_user
     spring.datasource.password=us_pass
     ```

   - **`application-eu.properties`**:
     ```properties
     spring.datasource.url=jdbc:mysql://eu-db-server:3306/mydb
     spring.datasource.username=eu_user
     spring.datasource.password=eu_pass
     ```

   - **`application-asia.properties`**:
     ```properties
     spring.datasource.url=jdbc:mysql://asia-db-server:3306/mydb
     spring.datasource.username=asia_user
     spring.datasource.password=asia_pass
     ```

3. **Set the Active Profile**:
   You can set the active profile using `application.properties` or through command-line arguments.

   - **`application.properties`** (optional):
     ```properties
     spring.profiles.active=us
     ```

   To run your application with a specific profile (e.g., EU):
   ```bash
   java -jar myapp.jar --spring.profiles.active=eu
   ```

4. **Create a Configuration Class**:
   Create a configuration class to configure your datasource based on the active profile.

   ```java
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
   import org.springframework.boot.context.properties.ConfigurationProperties;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.context.annotation.Profile;

   import javax.sql.DataSource;

   @Configuration
   public class DataSourceConfig {

       @Bean
       @Profile("us")
       @ConfigurationProperties("spring.datasource")
       public DataSource usDataSource() {
           return DataSourceBuilder.create().build();
       }

       @Bean
       @Profile("eu")
       @ConfigurationProperties("spring.datasource")
       public DataSource euDataSource() {
           return DataSourceBuilder.create().build();
       }

       @Bean
       @Profile("asia")
       @ConfigurationProperties("spring.datasource")
       public DataSource asiaDataSource() {
           return DataSourceBuilder.create().build();
       }
   }
   ```

5. **Run the Application**:
   Depending on which profile you set, the application will load the respective properties from the corresponding `application-*.properties` file.

### Testing

You can test this setup by running the application with different profiles:

```bash
# For US
java -jar myapp.jar --spring.profiles.active=us

# For EU
java -jar myapp.jar --spring.profiles.active=eu

# For Asia
java -jar myapp.jar --spring.profiles.active=asia
```