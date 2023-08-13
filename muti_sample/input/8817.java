public class FormatData_es_UY extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4 #,##0.00;(\u00A4#,##0.00)", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "hh:mm:ss a z", 
                    "hh:mm:ss a z", 
                    "hh:mm:ss a", 
                    "hh:mm a", 
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
