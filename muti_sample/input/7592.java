public class FormatData_ms_MY extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###",
                    "\u00a4#,##0.00;(\u00a4#,##0.00)",
                    "#,##0%",
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "h:mm:ss a z",
                    "h:mm:ss a z",
                    "h:mm:ss a",
                    "h:mm",
                    "EEEE dd MMM yyyy",
                    "dd MMMM yyyy",
                    "dd MMMM yyyy",
                    "dd/MM/yyyy",
                    "{1} {0}",
                }
            },
        };
    }
}
