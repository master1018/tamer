public class FormatData_es extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "enero", 
                    "febrero", 
                    "marzo", 
                    "abril", 
                    "mayo", 
                    "junio", 
                    "julio", 
                    "agosto", 
                    "septiembre", 
                    "octubre", 
                    "noviembre", 
                    "diciembre", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "ene", 
                    "feb", 
                    "mar", 
                    "abr", 
                    "may", 
                    "jun", 
                    "jul", 
                    "ago", 
                    "sep", 
                    "oct", 
                    "nov", 
                    "dic", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "domingo", 
                    "lunes", 
                    "martes", 
                    "mi\u00e9rcoles", 
                    "jueves", 
                    "viernes", 
                    "s\u00e1bado" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "dom", 
                    "lun", 
                    "mar", 
                    "mi\u00e9", 
                    "jue", 
                    "vie", 
                    "s\u00e1b" 
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4#,##0.00;(\u00A4#,##0.00)", 
                    "#,##0%" 
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
                    "HH'H'mm'' z", 
                    "H:mm:ss z", 
                    "H:mm:ss", 
                    "H:mm", 
                    "EEEE d' de 'MMMM' de 'yyyy", 
                    "d' de 'MMMM' de 'yyyy", 
                    "dd-MMM-yyyy", 
                    "d/MM/yy", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
