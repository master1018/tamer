public class FormatData_en_CA extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "DateTimePatterns",
                new String[] {
                    "h:mm:ss 'o''clock' a z", 
                    "h:mm:ss z a", 
                    "h:mm:ss a", 
                    "h:mm a", 
                    "EEEE, MMMM d, yyyy", 
                    "MMMM d, yyyy", 
                    "d-MMM-yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
