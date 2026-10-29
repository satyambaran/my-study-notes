package src.com.patterns.proxy;

public class EmployeeDaoProxy implements EmployeeDao {
    EmployeeDao employeeDaoObj;

    EmployeeDaoProxy() {
        employeeDaoObj = new EmployeeDaoImpl();
    }

    @Override
    public void create(String client, EmployeeDo employee) throws Exception {
        if (client.equals("Admin")) {
            employeeDaoObj.create(client, employee);
            return;
        }
        throw new Exception("Access Denied");
    }

    @Override
    public void delete(String client, int employeeId) throws Exception {
        if (client.equals("Admin")) {
            employeeDaoObj.delete(client, employeeId);
            return;
        }
        throw new Exception("Access Denied");
    }

    @Override
    public EmployeeDo get(String client, int employeeId) throws Exception {
        if (client.equals("Admin")) {
            return employeeDaoObj.get(client, employeeId);
        }
        throw new Exception("Access Denied");
    }

}
