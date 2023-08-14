public class TypeTag {
    private static class Inner { }
    public static void main(String[] args) throws Exception {
        Class[] v = null;
        v = Integer.TYPE.getDeclaredClasses();
        if (v == null || v.length != 0)
            throw new Exception("Integer.TYPE.getDeclaredClasses is not working");
        System.out.println("Integer.TYPE: "+ v.toString());
        v = TypeTag.class.getDeclaredClasses();
        if (v == null)
            throw new Exception("TypeTag.class.getDeclaredClasses returned null");
        System.out.println("TypeTag.class: " + v.toString());
        int n = 0;
        for (int i = 0; i < v.length; i++) {
            String name = v[i].getName();
            System.out.print(name);
            if (!name.matches("\\D\\w*\\$\\d*")) {
                n++;
                System.out.println(" -- user class");
            } else {
                System.out.println();
            }
        }
        if (n != 1)
            throw new Exception("TypeTag.class.getDeclaredClasses found too many classes");
    }
}
