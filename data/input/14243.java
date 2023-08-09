public class Bug6442006 {
    public static void main(String[] args) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Taipei");
        Locale tzLocale = new Locale("ja");
        String jaStdName = "\u4e2d\u56fd\u6a19\u6e96\u6642";
        String jaDstName = "\u4e2d\u56fd\u590f\u6642\u9593";
        if (!tz.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           (jaStdName))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "non-daylight saving name for " +
                                        tz.getID() +
                                        " should be " +
                                        jaStdName);
        if (!tz.getDisplayName(true, TimeZone.LONG, tzLocale).equals
           (jaDstName))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "daylight saving name for " +
                                        tz.getID() +
                                        " should be " +
                                        jaDstName);
    }
}
