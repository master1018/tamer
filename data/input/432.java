public class ImmutableResourceTest {
    public static void main(String[] args) throws Exception {
        com.sun.tools.example.debug.tty.TTYResources ttyr =
            new com.sun.tools.example.debug.tty.TTYResources ();
        Object [][] testData = ttyr.getContents();
        for (int ii = 0; ii < testData.length; ii++) {
            testData[ii][0] = "T6287579";
            testData[ii][1] = "yyy";
        }
        String ss = null;
        try {
            ss = ttyr.getString("T6287579");
        } catch (java.util.MissingResourceException mre) {
        }
        if ("yyy".equals(ss)) {
            throw new Exception ("SubClasses of ListResourceBundle should fix getContents()");
        }
        System.out.println("...Finished.");
    }
}
