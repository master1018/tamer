public class NonPublicStaticInitializer {
    public static void main(String args[]) throws Exception {
        Method m[] = TestedInterface.class.getMethods();
        for (int i = 0; i < m.length; i++) {
            System.out.println("Found: " +
                               Modifier.toString(m[i].getModifiers()) +
                               " " + m[i].getName());
            if (m[i].getName().equals("<clinit>")) {
                throw new Exception("Shouldn't have found <clinit>");
            }
        }
    }
}
