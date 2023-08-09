public class TestHelper_Driver5 extends TestHelper_Driver4 {
    static {
        Driver theDriver = new TestHelper_Driver5();
        try {
            DriverManager.registerDriver(theDriver);
        } catch (SQLException e) {
            System.out.println("Failed to register driver!");
        }
    } 
    protected TestHelper_Driver5() {
        super();
        baseURL = "jdbc:mikes5";
    } 
} 
