# Bean and DI

## Bean
An object that is instantiated, assembled, and managed by the Spring IoC (Inversion of Control) container.
### **@Component**
- Convention of configuration.
- Spring boot creates the object by calling its default constructor and manages the life cycle
- It'll fail if some class has a parameterized cunstructor(no default constructor available)
```java
@Component
public class MyBean {
    MyBean(String s){

    }
    public void doSomething() {
        System.out.println("Doing something...");
    }
}
```
### **@Bean**
- @Configuration annotation indicates that a class declares one or more @Bean methods
- Each method return instances of beans to be managed by the Spring container
- @Bean annotation is used on methods to indicate that they return a bean that should be registered in the Spring application context
```java
@Configuration
public class AppConfig {
    @Bean
    public MyBean myBean() {
        return new MyBean("default"); // Manually creating and returning the bean instance
    }
    @Bean
    public MyBean myBean2() {
        return new MyBean("default2"); // Manually creating and returning the bean instance
    }
}
```

* By default, scope is singleton

#### Component Scan ###
- Spring boot will scan the provided package in this component to find Beans
#### @Configuration ###
- Spring boot checks if there is any @Configuartion, then get @Bean defined there

### Bean Initialization
    - Eager
        * Singleton scoped
    - Lazy
        * Only when it is required, it'll get created
### Lifecycle of Bean (using @PostConstruct and @PreDestroy Annotations)
```app start``` => ```ioc container start``` (search for bean definiton) => ```construct bean``` => 

```inject dependency``` => ```@PostConstruct``` => Bean lifecyle =>```@PreDestroy``` => ```destroyed```

### Primary and Qualifier
```java
@Service
@Primary
public class DefaultService implements MyService {
    @Override
    public void performTask() {
        System.out.println("Default Service");
    }
}

@Service
public class AlternativeService implements MyService {
    @Override
    public void performTask() {
        System.out.println("Alternative Service");
    }
}

@Service
public class ClientService {

    private final MyService myService;

    @Autowired
    public ClientService(@Qualifier("alternativeService") MyService myService) {
        this.myService = myService;
    }

    public void useService() {
        myService.performTask();
    }
}
```
### Dynamic Initialized Bean
- @Value is used to inject values from various sources like property file, environment variables
```java
@Configuration
public class AppConfig{
    @Bean
    public Order createOrderBean(@Value("${isOnlineOrder}"boolean isOnlineOrder)){
        if(isOnlineOrder){
            return new OnlineOrder();
        }else{
            return new OfflineOrder();
        }
    }
}
```

application.properties
```properties
isOnlineOrder=true
```
### @ConditionalOnProperty
```java
@Component
class DBConnection{
    @Autowired
    MySQLConnection mySQLConnection;
    @Autowired
    NoSQLConnection noSQLConnection;
    @PostConstruct
    public void init(){
        System.out.println("mysql obje null?: ", Object.IsNull(mySQLConnection));
        System.out.println("nosql obje null?: ", Object.IsNull(noSQLConnection));
    }
}
```
- What if we want to generate only one of it.
```java
@Component
@ConditionalOnProperty(prefix="mysqlconnection", value="enabled", havingValue="true", matchIfMissing=false)
```
```properties
mysqlconnection.enabled=true
```
## Injection ##
It looks like you're breaking down a video or a session into different topics with timestamps related to **Dependency Injection** in Spring or similar frameworks. Let me give you a detailed explanation of each section based on the topics you've mentioned:

### **Problem Exist Today for Which Dependency Injection is Required**
Before Dependency Injection (DI) was introduced, managing dependencies in large applications was complex and prone to issues like tight coupling, poor testability, and hardcoded object creation. Without DI, objects themselves are responsible for creating their dependencies, leading to:
- **Tight Coupling**: Classes are heavily dependent on each other.
- **Difficult to Unit Test**: It's hard to mock or change dependencies without rewriting code.
- **Inflexibility**: Changing a dependency would require changes in the class itself.
DI addresses these problems by shifting the responsibility of dependency creation and management to a container like the **Spring Framework**, improving modularity, testability, and maintainability.

### **What is Dependency Injection and Its Types**
**Dependency Injection** is a design pattern in which an object receives its dependencies from an external source, rather than creating them itself. DI helps in creating loosely coupled, more maintainable, and testable systems.

There are three main types of Dependency Injection:
1. **Constructor Injection**: Dependencies are passed through the constructor.
2. **Setter Injection**: Dependencies are passed through setter methods.
3. **Field Injection**: Dependencies are injected directly into fields of a class.

### **Field Injection and Its Advantages and Disadvantages**
Field injection is when dependencies are directly injected into the fields (variables) of a class using annotations like `@Autowired`.

```java
class A {
    @Autowired
    private B b;

    void setB(B b){
        this.b=b;
    }
}
```
**Advantages**:
- Simple and concise code: Dependencies are automatically injected.
- Reduces boilerplate code, as no constructors or setters are required.

**Disadvantages**:
- **Not recommended for testing**: Harder to mock dependencies because fields are private and final.
- Violates **Inversion of Control**: The class has no control over its dependencies.
- **Circular Dependency** issues can occur more easily.
- Reduces **immutability**: Fields can be modified if not final.


### **Setter Injection and Its Advantages and Disadvantages**
In **Setter Injection**, dependencies are injected through setter methods.

```java
class A {
    private B b;

    @Autowired
    void setB(B b){
        this.b=b;
    }
}
```
**Advantages**:
- **Optional Dependencies**: You can define optional dependencies that may not always need to be injected.
- More flexibility in **changing** or injecting dependencies at runtime.
- **Better Testability**: Dependencies can be set or changed during testing.

**Disadvantages**:
- The class can be instantiated in an **invalid state** if setters are not called.
- Mutability: Dependencies can change during the lifetime of the object, leading to unexpected behavior.

### **Constructor Injection and Its Advantages and Disadvantages**
In **Constructor Injection**, dependencies are provided via a constructor. This is the most recommended form of DI.

```java
class A{
    private B b;
    @Autowired
    A (B b){
        this.b =b;
    }
}
```

**Advantages**:
- Ensures **immutability**: Dependencies are set once and cannot be modified after the object is created.
- Makes the object always **valid**: Dependencies are provided when the object is constructed, ensuring all required fields are initialized.
- **Better for Unit Testing**: Dependencies are explicitly passed, which makes mocking easier.
- Encourages **design clarity**: When there are many dependencies, it becomes apparent through the constructor signature, prompting a reevaluation of class design.

**Disadvantages**:
- May result in large constructors if a class has many dependencies.
- **Circular Dependency** problems can occur.

### **Circular Dependency Problem and Its Solutions**
**Circular Dependency** occurs when two or more classes depend on each other, directly or indirectly, leading to infinite recursion during bean initialization.

For example:
```java
class A {
    @Autowired
    private B b;
}

class B {
    @Autowired
    private A a;
}
```

**Solutions**:
1. **Use Setter Injection** instead of Constructor Injection. This allows Spring to create the objects first and inject dependencies afterward.
2. **Use `@Lazy`**: This defers the initialization of a bean until it's first requested.
3. **Redesign the classes**: Break the circular dependency by introducing an intermediary service or refactor the code to reduce direct dependency.

### **Unsatisfied Dependency Problems and Its Solutions**
An **Unsatisfied Dependency** occurs when Spring can't find a suitable bean to inject into a field, setter, or constructor. This typically results in an `UnsatisfiedDependencyException`.

**Solutions**:
1. **Define the required beans**: Ensure that the beans or components required by the class are available and annotated with `@Component`, `@Service`, `@Repository`, etc., so that Spring can manage them.
2. **Use `@Primary`**: If there are multiple beans of the same type, use `@Primary` to tell Spring which one to inject by default.
3. **Use `@Qualifier`**: If there are multiple beans of the same type and `@Primary` is not suitable, you can use `@Qualifier` to explicitly specify which bean to inject.
4. **Ensure proper configuration**: Double-check configuration files (like `@Configuration`, XML) to ensure the beans are properly defined and available in the context.


## Scope
- 4 type:
    - Singleton
        * Default scope
        - Eagerly initialize
        - Only 1 instance per IOC
    - Prototype
        - A new instance of the bean is created each time it is requested
        - Lazily initialize
    - Request
        - A new bean is created for each HTTP request, and the same bean instance is used throughout the lifecycle of that request.
        - Lazily initialize
        - only available in a web context 
            - Can lead to an error, if some singleton scoped class wants it injection
            - can be solve by proxy_mode
            - @Scope(value="request",proxyMode=ScopedProxyMode.TARGET_CLASS)
            - Will start using the correct bean, once it's created
    - Session
        - A new bean instance is created for each HTTP session and remains active until the session ends.

