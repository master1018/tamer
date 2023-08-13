public class SystemPropTest_1 {
    public static void main(String []s){
        boolean propValue = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons"));
        System.out.println("1. System.getProperty = " + propValue);
        if (propValue){
            throw new RuntimeException("TEST FAILED(1) : System property sun.awt.enableExtraMouseButtons = " + propValue);
        }
        if (!Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()){
            throw new RuntimeException("TEST FAILED : Toolkit.areExtraMouseButtonsEnabled() returns false");
        }
        System.getProperties().list(System.out);
        System.out.println("XXXX. System.getProperty = " + System.getProperty("sun.awt.enableExtraMouseButtons"));
        propValue = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons"));
        System.out.println("2. System.getProperty = " + propValue);
        if (!propValue){
            throw new RuntimeException("TEST FAILED(2) : System property sun.awt.enableExtraMouseButtons = " + propValue);
        }
        System.out.println("Test passed.");
    }
}
