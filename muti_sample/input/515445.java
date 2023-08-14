public class TestHelper_Driver2 extends TestHelper_Driver1 {
    static {
        Driver theDriver = new TestHelper_Driver2();
        try {
            DriverManager.registerDriver(theDriver);
        } catch (SQLException e) {
            System.out.println("Failed to register driver!");
        }
    } 
    protected TestHelper_Driver2() {
        super();
        baseURL = "jdbc:mikes2";
    } 
} 
