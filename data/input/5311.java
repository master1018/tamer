class Bar {
    static { System.out.println("Bar.<clinit> called"); }
    static Object obj = new Object();
}
public class StaticInitializerTest {
    public static void main(String[] args) throws Exception {
        Class cl = Class.forName("Bar", false, StaticInitializerTest.class.getClassLoader());
        if (cl.getDeclaredField("obj").get(null) == null) {
            throw new Error();
        }
    }
}
