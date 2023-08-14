public class Bug4177489Test extends RBTestFmwk {
    public static void main(String[] args) throws Exception {
        new Bug4177489Test().run(args);
    }
    public void testIt() throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle( "Bug4177489_Resource" );
        Locale l = rb.getLocale();
        if (l.toString().length() > 0) {
            errln("ResourceBundle didn't handle resource class name with '_' in it.");
        }
        Locale loc = new Locale("jf", "");
        ResourceBundle rb2 = ResourceBundle.getBundle( "Bug4177489_Resource", loc );
        if (!loc.equals(rb2.getLocale())) {
            errln("ResourceBundle didn't return proper locale name:"+rb2.getLocale());
        }
        loc = new Locale("jf", "JF");
        ResourceBundle rb3 = ResourceBundle.getBundle("Bug4177489_Resource", loc);
        if (!loc.equals(rb3.getLocale())) {
            errln("ResourceBundle didn't return proper locale name for property bundle:"+rb3.getLocale());
        }
    }
}
