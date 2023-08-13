public class FormatData_ga_IE extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###",
                    "\u00a4#,##0.00",
                    "#,##0%",
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH:mm:ss z",
                    "HH:mm:ss z",
                    "HH:mm:ss",
                    "HH:mm",
                    "EEEE d MMMM yyyy",
                    "d MMMM yyyy",
                    "d MMM yyyy",
                    "dd/MM/yyyy",
                    "{1} {0}",
                }
            },
        };
    }
}
