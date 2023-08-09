public class FormatData_nl extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "januari", 
                    "februari", 
                    "maart", 
                    "april", 
                    "mei", 
                    "juni", 
                    "juli", 
                    "augustus", 
                    "september", 
                    "oktober", 
                    "november", 
                    "december", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "jan", 
                    "feb", 
                    "mrt", 
                    "apr", 
                    "mei", 
                    "jun", 
                    "jul", 
                    "aug", 
                    "sep", 
                    "okt", 
                    "nov", 
                    "dec", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "zondag", 
                    "maandag", 
                    "dinsdag", 
                    "woensdag", 
                    "donderdag", 
                    "vrijdag", 
                    "zaterdag" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "zo", 
                    "ma", 
                    "di", 
                    "wo", 
                    "do", 
                    "vr", 
                    "za" 
                }
            },
            { "Eras",
                new String[] { 
                    "v. Chr.",
                    "n. Chr."
                }
            },
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
                    "H:mm:ss' uur' z", 
                    "H:mm:ss z", 
                    "H:mm:ss", 
                    "H:mm", 
                    "EEEE d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "d-MMM-yyyy", 
                    "d-M-yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
