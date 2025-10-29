package src.com.patterns.proxy;

/*
 * Client requests a real object, but we introduce a proxy between them
 * Like blocking, caching, pre/post processing, access-restriction
 */

public class Main {
    public static void main(String[] args) {
        try {
            EmployeeDao employeeObj = new EmployeeDaoProxy();
            employeeObj.create("Admin", new EmployeeDo());
            employeeObj.create("USER", new EmployeeDo());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
