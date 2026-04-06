# AOP
## Boiler Plate
- Other part of code whoch is not part of business logic, like logging, transaction management
## AOP
- It handles the boiler plate part of the wrok
- It intercepts method, and do some works before and after the method invocation
- @Around, @Before, @After
```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* com.example.service.UserService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed(); // Proceed with the method call
        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        return proceed;
    }
}
```