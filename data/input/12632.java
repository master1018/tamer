public class SystemPropTest_3 {
    public static void main(String []s){
        boolean propValue = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons"));
        System.out.println("Test System.getProperty = " + System.getProperty("sun.awt.enableExtraMouseButtons"));
        System.out.println("System.getProperty = " + propValue);
        if (propValue){
            throw new RuntimeException("TEST FAILED : System property sun.awt.enableExtraMouseButtons = " + propValue);
        }
        if (Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()){
            throw new RuntimeException("TEST FAILED : Toolkit.areExtraMouseButtonsEnabled() returns true");
        }
        System.out.println("Test passed.");
    }
}
