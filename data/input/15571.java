public class FormatData_en_GB extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "DateTimePatterns",
                new String[] {
                    "HH:mm:ss 'o''clock' z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE, d MMMM yyyy", 
                    "dd MMMM yyyy", 
                    "dd-MMM-yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
