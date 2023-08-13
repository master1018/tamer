public class FormatData_fr_BE extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberElements",
                new String[] {
                    ",", 
                    ".", 
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
                    "H' h 'mm' min 'ss' s 'z", 
                    "H:mm:ss z", 
                    "H:mm:ss", 
                    "H:mm", 
                    "EEEE d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "dd-MMM-yyyy", 
                    "d/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" },
        };
    }
}
