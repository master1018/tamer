public class FormatData_fr extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "janvier", 
                    "f\u00e9vrier", 
                    "mars", 
                    "avril", 
                    "mai", 
                    "juin", 
                    "juillet", 
                    "ao\u00fbt", 
                    "septembre", 
                    "octobre", 
                    "novembre", 
                    "d\u00e9cembre", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "janv.", 
                    "f\u00e9vr.", 
                    "mars", 
                    "avr.", 
                    "mai", 
                    "juin", 
                    "juil.", 
                    "ao\u00fbt", 
                    "sept.", 
                    "oct.", 
                    "nov.", 
                    "d\u00e9c.", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "dimanche", 
                    "lundi", 
                    "mardi", 
                    "mercredi", 
                    "jeudi", 
                    "vendredi", 
                    "samedi" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "dim.", 
                    "lun.", 
                    "mar.", 
                    "mer.", 
                    "jeu.", 
                    "ven.", 
                    "sam." 
                }
            },
            { "Eras",
                new String[] { 
                    "BC",
                    "ap. J.-C."
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "#,##0.00 \u00A4;-#,##0.00 \u00A4", 
                    "#,##0 %" 
                }
            },
            { "NumberElements",
                new String[] {
                    ",", 
                    "\u00a0", 
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
                    "HH' h 'mm z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "d MMM yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" },
        };
    }
}
