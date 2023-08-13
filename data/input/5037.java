public class FormatData_es_AR extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4#,##0.00;\u00A4-#,##0.00", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH'h'''mm z", 
                    "H:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE d' de 'MMMM' de 'yyyy", 
                    "d' de 'MMMM' de 'yyyy", 
                    "dd/MM/yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            }
        };
    }
}
