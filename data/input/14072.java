public class Bug4396385 {
    private static int errorCount = 0;
    private static String[][] data = {
        { "hh:mma", "-1:30AM" },
        { "hh:mma", "00:30AM" },
        { "hh:mma", "13:30AM" },
        { "kk:mm",  "-1:12" },
        { "kk:mm",  "00:12" },
        { "kk:mm",  "25:12" },
    };
    public static void main(String[] args) {
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            for (String[] item : data) {
                test(item[0], item[1]);
            }
        } finally {
            TimeZone.setDefault(tz);
        }
        if (errorCount > 0) {
            throw new RuntimeException("Failed with " + errorCount + " error(s).");
        }
    }
    private static void test(String pattern, String src) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        System.out.printf("parse: \"%s\" with \"%s\"", src, pattern);
        Date date = sdf.parse(src, pos);
        System.out.printf(": date = %s, errorIndex = %d", date, pos.getErrorIndex());
        if (date != null || pos.getErrorIndex() == -1) {
            System.out.println(": failed");
            errorCount++;
        } else {
            System.out.println(": passed");
        }
    }
}
