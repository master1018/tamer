public class test {
    private void setup() {
        try {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql:
        } catch (Exception e) {
            System.out.println(e);
        }
        return;
    }
}
