public class FormatData_it extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "gennaio", 
                    "febbraio", 
                    "marzo", 
                    "aprile", 
                    "maggio", 
                    "giugno", 
                    "luglio", 
                    "agosto", 
                    "settembre", 
                    "ottobre", 
                    "novembre", 
                    "dicembre", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "gen", 
                    "feb", 
                    "mar", 
                    "apr", 
                    "mag", 
                    "giu", 
                    "lug", 
                    "ago", 
                    "set", 
                    "ott", 
                    "nov", 
                    "dic", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "domenica", 
                    "luned\u00ec", 
                    "marted\u00ec", 
                    "mercoled\u00ec", 
                    "gioved\u00ec", 
                    "venerd\u00ec", 
                    "sabato" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "dom", 
                    "lun", 
                    "mar", 
                    "mer", 
                    "gio", 
                    "ven", 
                    "sab" 
                }
            },
            { "Eras",
                new String[] { 
                    "BC",
                    "dopo Cristo"
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
                    "H.mm.ss z", 
                    "H.mm.ss z", 
                    "H.mm.ss", 
                    "H.mm", 
                    "EEEE d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "d-MMM-yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
