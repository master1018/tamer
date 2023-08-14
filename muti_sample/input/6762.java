public class bug6938813 {
    public static final String DTD_KEY = "dtd_key";
    private static volatile StyleSheet styleSheet;
    public static void main(String[] args) throws Exception {
        validate();
        Thread thread = new ThreadInAnotherAppContext();
        thread.start();
        thread.join();
    }
    private static void validate() throws Exception {
        AppContext appContext = AppContext.getAppContext();
        assertTrue(DTD.getDTD(DTD_KEY).getName().equals(DTD_KEY), "DTD.getDTD() mixed AppContexts");
        DTD invalidDtd = DTD.getDTD("invalid DTD");
        DTD.putDTDHash(DTD_KEY, invalidDtd);
        assertTrue(DTD.getDTD(DTD_KEY) == invalidDtd, "Something wrong with DTD.getDTD()");
        Object dtdKey = getParserDelegator_DTD_KEY();
        assertTrue(appContext.get(dtdKey) == null, "ParserDelegator mixed AppContexts");
        new ParserDelegator();
        Object dtdValue = appContext.get(dtdKey);
        assertTrue(dtdValue != null, "ParserDelegator.defaultDTD isn't initialized");
        new ParserDelegator();
        assertTrue(dtdValue == appContext.get(dtdKey), "ParserDelegator.defaultDTD created a duplicate");
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        if (styleSheet == null) {
            styleSheet = htmlEditorKit.getStyleSheet();
            assertTrue(styleSheet != null, "htmlEditorKit.getStyleSheet() returns null");
            assertTrue(htmlEditorKit.getStyleSheet() == styleSheet, "Something wrong with htmlEditorKit.getStyleSheet()");
        } else {
            assertTrue(htmlEditorKit.getStyleSheet() != styleSheet, "HtmlEditorKit.getStyleSheet() mixed AppContexts");
        }
    }
    private static void assertTrue(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException("Test failed: " + msg);
        }
    }
    private static Object getParserDelegator_DTD_KEY() throws Exception {
        Field field = ParserDelegator.class.getDeclaredField("DTD_KEY");
        field.setAccessible(true);
        return field.get(null);
    }
    private static class ThreadInAnotherAppContext extends Thread {
        public ThreadInAnotherAppContext() {
            super(new ThreadGroup("6938813"), "ThreadInAnotherAppContext");
        }
        public void run() {
            SunToolkit.createNewAppContext();
            try {
                validate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
