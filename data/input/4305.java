public class Bug6271396 {
    public static void main(String[] args) {
        TimeZone Lord_Howe = TimeZone.getTimeZone("Australia/Lord_Howe");
        Locale tzLocale = new Locale("fr");
        if (!Lord_Howe.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Heure standard de Lord Howe"))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "non-daylight saving name for " +
                                        "Australia/Lord_Howe should be " +
                                        "\"Heure standard de Lord Howe\"");
        if (!Lord_Howe.getDisplayName(true, TimeZone.LONG, tzLocale).equals
           ("Heure d'\u00e9t\u00e9 de Lord Howe"))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "daylight saving name for " +
                                        "Australia/Lord_Howe should be " +
                                        "\"Heure d'\u00e9t\u00e9 de Lord Howe\"");
        tzLocale = new Locale("zh", "TW");
        if (!Lord_Howe.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u8c6a\u52f3\u7235\u5cf6\u6a19\u6e96\u6642\u9593"))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "non-daylight saving name for " +
                                        "Australia/Lord_Howe should be " +
                                        "\"\u8c6a\u52f3\u7235\u5cf6" +
                                        "\u6a19\u6e96\u6642\u9593\"");
        if (!Lord_Howe.getDisplayName(true, TimeZone.LONG, tzLocale).equals
           ("\u8c6a\u52f3\u7235\u5cf6\u590f\u4ee4\u6642\u9593"))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "daylight saving name for " +
                                        "Australia/Lord_Howe should be " +
                                        "\"\u8c6a\u52f3\u7235\u5cf6" +
                                        "\u590f\u4ee4\u6642\u9593\"");
   }
}
