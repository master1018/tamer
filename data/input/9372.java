public class FormatData_fr_CA extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "#,##0.00 \u00A4;(#,##0.00\u00A4)", 
                    "#,##0 %" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "H' h 'mm z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "yyyy-MM-dd", 
                    "yy-MM-dd", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" },
        };
    }
}
