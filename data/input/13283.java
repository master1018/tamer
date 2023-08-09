public class FormatData_es_US extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "AmPmMarkers",
                new String[] {
                    "a.m.",
                    "p.m.",
                }
            },
            { "Eras",
                new String[] {
                    "a.C.",
                    "d.C.",
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###",
                    "\u00a4#,##0.00;(\u00a4#,##0.00)",
                    "#,##0%",
                }
            },
            { "NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "h:mm:ss a z",
                    "h:mm:ss a z",
                    "h:mm:ss a",
                    "h:mm a",
                    "EEEE d' de 'MMMM' de 'yyyy",
                    "d' de 'MMMM' de 'yyyy",
                    "MMM d, yyyy",
                    "M/d/yy",
                    "{1} {0}",
                }
            },
            { "DateTimePatternChars", "GuMtkHmsSEDFwWahKzZ" },
        };
    }
}
