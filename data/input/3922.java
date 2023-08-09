public class FormatData_fr_CH extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4 #,##0.00;\u00A4-#,##0.00", 
                    "#,##0 %" 
                }
            },
            { "NumberElements",
                new String[] {
                    ".", 
                    "'", 
                    ";", 
                    "%", 
                    "0", 
                    "#", 
                    "-", 
                    "E", 
                    "\u2030", 
                    "\u221e", 
                    "\ufffd" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH.mm.' h' z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE, d. MMMM yyyy", 
                    "d. MMMM yyyy", 
                    "d MMM yyyy", 
                    "dd.MM.yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" },
        };
    }
}
