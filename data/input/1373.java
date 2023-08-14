public class FormatData_en_IN extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberElements",
                new String[] {
                    ".", 
                    ",", 
                    ";", 
                    "%", 
                    "\u0030", 
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
                    "h:mm:ss a z", 
                    "h:mm:ss a z", 
                    "h:mm:ss a", 
                    "h:mm a", 
                    "EEEE, d MMMM, yyyy", 
                    "d MMMM, yyyy", 
                    "d MMM, yyyy", 
                    "d/M/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
