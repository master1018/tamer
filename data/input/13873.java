public class FormatData_no extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "januar", 
                    "februar", 
                    "mars", 
                    "april", 
                    "mai", 
                    "juni", 
                    "juli", 
                    "august", 
                    "september", 
                    "oktober", 
                    "november", 
                    "desember", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "jan", 
                    "feb", 
                    "mar", 
                    "apr", 
                    "mai", 
                    "jun", 
                    "jul", 
                    "aug", 
                    "sep", 
                    "okt", 
                    "nov", 
                    "des", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "s\u00f8ndag", 
                    "mandag", 
                    "tirsdag", 
                    "onsdag", 
                    "torsdag", 
                    "fredag", 
                    "l\u00f8rdag" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "s\u00f8", 
                    "ma", 
                    "ti", 
                    "on", 
                    "to", 
                    "fr", 
                    "l\u00f8" 
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
                    "'kl 'HH.mm z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "d. MMMM yyyy", 
                    "d. MMMM yyyy", 
                    "dd.MMM.yyyy", 
                    "dd.MM.yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
