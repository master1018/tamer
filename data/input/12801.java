public class Test6341798 {
    private static final Locale TURKISH = new Locale("tr");
    private static final String DATA
            = "<java>\n"
            + " <object class=\"Test6341798$DataBean\">\n"
            + "  <void property=\"illegal\">\n"
            + "   <boolean>true</boolean>\n"
            + "  </void>\n"
            + " </object>\n"
            + "</java> ";
    public static void main(String[] args) {
        test(ENGLISH, DATA.getBytes());
        test(TURKISH, DATA.getBytes());
    }
    private static void test(Locale locale, byte[] data) {
        Locale.setDefault(locale);
        System.out.println("locale = " + locale);
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(data));
        System.out.println("object = " + decoder.readObject());
        decoder.close();
    }
    public static class DataBean {
        private boolean illegal;
        public boolean isIllegal() {
            return this.illegal;
        }
        public void setIllegal(boolean illegal) {
            this.illegal = illegal;
        }
        public String toString() {
            if (this.illegal) {
                return "property is set";
            }
            throw new Error("property is not set");
        }
    }
}
