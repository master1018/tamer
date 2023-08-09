public class SupplementaryJavaID6 {
    public static void main(String[] s) {
        new SupplementaryJavaID6();
    }
    public SupplementaryJavaID6() {
        \ud801\udc00 instance = new \ud801\udc00();
        instance.\ud801\udc01();
    }
    class \ud801\udc00 {
        void \ud801\udc01() {
            new java.io.File(this.getClass().getName() + ".class")
                .deleteOnExit();
            System.out.println("success");
        }
    }
}
