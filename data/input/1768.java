public class Bug6609750 {
    public static void main(String[] args) {
        boolean error = false;
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Date[] dates = {
            new Date(9-1900,     Calendar.JUNE, 12),
            new Date(99-1900,    Calendar.JUNE, 12),
            new Date(999-1900,   Calendar.JUNE, 12),
            new Date(2009-1900,  Calendar.JUNE, 12),
            new Date(30009-1900, Calendar.JUNE, 12),
        };
        String[] patterns = {
           "y", "yy", "yyy", "yyyy", "yyyyy", "yyyyyy"
        };
        String[][] expectedResults = {
           {"9",     "09", "009",   "0009",  "00009", "000009"},
           {"99",    "99", "099",   "0099",  "00099", "000099"},
           {"999",   "99", "999",   "0999",  "00999", "000999"},
           {"2009",  "09", "2009",  "2009",  "02009", "002009"},
           {"30009", "09", "30009", "30009", "30009", "030009"},
        };
        SimpleDateFormat sdf = new SimpleDateFormat();
        for (int dateNo = 0; dateNo < dates.length; dateNo++) {
            Date date = dates[dateNo];
            for (int patternNo = 0; patternNo < patterns.length; patternNo++) {
                sdf.applyPattern(patterns[patternNo]);
                String got = sdf.format(date);
                if (!expectedResults[dateNo][patternNo].equals(got)) {
                    error = true;
                    System.err.println("Failed: Unexpected format result: " +
                        "Expected: \"" + expectedResults[dateNo][patternNo] +
                        "\", Got: \"" + got + "\" for date " + date +
                        " with pattern \"" + patterns[patternNo] + "\"");
                }
            }
        }
        Locale.setDefault(defaultLocale);
        if (error) {
            throw new RuntimeException("SimpleDateFormat.format() error.");
        };
    }
}
