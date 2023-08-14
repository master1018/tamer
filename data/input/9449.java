public class FormatData_pl_PL extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "#,##0.## \u00A4;-#,##0.## \u00A4", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH:mm:ss z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE, d MMMM yyyy", 
                    "d MMMM yyyy", 
                    "yyyy-MM-dd", 
                    "dd.MM.yy", 
                    "{1} {0}" 
                }
            }
        };
    }
}
