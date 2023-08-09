public class TestSGEuseAlternateFontforJALocales {
    public static void main(String args[]) throws Exception {
        System.out.println("Default Charset = "
            + Charset.defaultCharset().name());
        System.out.println("Locale = " + Locale.getDefault());
        String os = System.getProperty("os.name");
        String encoding = System.getProperty("file.encoding");
        boolean jaTest = encoding.equalsIgnoreCase("windows-31j");
        if (!os.startsWith("Win") && jaTest) {
            System.out.println("Skipping Windows only test");
            return;
        }
        String className = "sun.java2d.SunGraphicsEnvironment";
        String methodName = "useAlternateFontforJALocales";
        Class sge = Class.forName(className);
        Method uafMethod = sge.getMethod(methodName, (Class[])null);
        Object ret = uafMethod.invoke(null);
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.preferLocaleFonts();
        ge.preferProportionalFonts();
        if (jaTest) {
            Font msMincho = new Font("MS Mincho", Font.PLAIN, 12);
            if (!msMincho.getFamily(Locale.ENGLISH).equals("MS Mincho")) {
                 System.out.println("MS Mincho not installed. Skipping test");
                 return;
            }
            Font dialogInput = new Font("DialogInput", Font.PLAIN, 12);
            Font courierNew = new Font("Courier New", Font.PLAIN, 12);
            Font msGothic = new Font("MS Gothic", Font.PLAIN, 12);
            BufferedImage bi = new BufferedImage(1,1,1);
            Graphics2D g2d = bi.createGraphics();
            FontMetrics cnMetrics = g2d.getFontMetrics(courierNew);
            FontMetrics diMetrics = g2d.getFontMetrics(dialogInput);
            FontMetrics mmMetrics = g2d.getFontMetrics(msMincho);
            FontMetrics mgMetrics = g2d.getFontMetrics(msGothic);
            if (cnMetrics.charWidth('A') == diMetrics.charWidth('A')) {
                 throw new RuntimeException
                       ("Courier New should not be used for DialogInput");
            }
            if (diMetrics.charWidth('A') != mmMetrics.charWidth('A')) {
                 throw new RuntimeException
                     ("MS Mincho should be used for DialogInput");
            }
       }
   }
}
