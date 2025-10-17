package src.com.patterns.proxy;

interface EmployeeDao {
    public void create(String client, EmployeeDo employee) throws Exception;

    public void delete(String client, int employeeId) throws Exception;

    public EmployeeDo get(String client, int employeeId) throws Exception;
}

public class EmployeeDaoImpl implements EmployeeDao {

    @Override
    public void create(String client, EmployeeDo employee) throws Exception {
        System.out.println("Created");
    }

    @Override
    public void delete(String client, int employeeId) throws Exception {
        System.out.println("Deleted" + employeeId);
    }

    @Override
    public EmployeeDo get(String client, int employeeId) throws Exception {
        System.out.println("Fetched" + employeeId);
        return new EmployeeDo();
    }

}
