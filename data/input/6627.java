public class Bug4184873Test extends LocaleTestFmwk {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("prepTest")) {
            prepTest();
        } else {
            new Bug4184873Test().run(args);
        }
    }
    public void testIt() throws Exception {
        verify("he");
        verify("yi");
        verify("id");
    }
    private void verify(String lang) {
        try {
            ObjectInputStream in = getStream(lang);
            if (in != null) {
                final Locale loc = (Locale)in.readObject();
                final Locale expected = new Locale(lang, "XX");
                if (!(expected.equals(loc))) {
                    errln("Locale didn't maintain invariants for: "+lang);
                    errln("         got: "+loc);
                    errln("    excpeted: "+expected);
                } else {
                    logln("Locale "+lang+" worked");
                }
                in.close();
            }
        } catch (Exception e) {
            errln(e.toString());
        }
    }
    private ObjectInputStream getStream(String lang) {
        try {
            final File f = new File(System.getProperty("test.src", "."), "Bug4184873_"+lang);
            return new ObjectInputStream(new FileInputStream(f));
        } catch (Exception e) {
            errln(e.toString());
            return null;
        }
    }
    private static void prepTest() {
        outputLocale("he");
        outputLocale("yi");
        outputLocale("id");
    }
    private static void outputLocale(String lang) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream("Bug4184873_"+lang));
            out.writeObject(new Locale(lang, "XX"));
            out.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
