public final class CharsetUtils {
    private static final String VENDOR_DOCOMO = "docomo";
    private CharsetUtils() {
    }
    public static String nameForVendor(String charsetName, String vendor) {
        if (vendor.equalsIgnoreCase(VENDOR_DOCOMO)
                && isShiftJis(charsetName)) {
            return "docomo-shift_jis-2007";
        }
        return charsetName;
    }
    public static String nameForDefaultVendor(String charsetName) {
        return nameForVendor(charsetName, getDefaultVendor());
    }
    public static Charset charsetForVendor(String charsetName, String vendor)
            throws UnsupportedCharsetException, IllegalCharsetNameException {
        charsetName = nameForVendor(charsetName, vendor);
        return Charset.forName(charsetName);
    }
    public static Charset charsetForVendor(String charsetName)
            throws UnsupportedCharsetException, IllegalCharsetNameException {
        return charsetForVendor(charsetName, getDefaultVendor());
    }
    private static boolean isShiftJis(String charsetName) {
        if (charsetName == null) {
            return false;
        }
        int length = charsetName.length();
        if (length != 4 && length != 9) {
            return false;
        }
        return charsetName.equalsIgnoreCase("shift_jis")
            || charsetName.equalsIgnoreCase("shift-jis")
            || charsetName.equalsIgnoreCase("sjis");
    }
    private static String getDefaultVendor() {
        return Build.BRAND;
    }
}
