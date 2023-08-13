public class FormatData_pt_BR extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4 #,##0.00;-\u00A4 #,##0.00", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH'h'mm'min'ss's' z", 
                    "H'h'm'min's's' z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "EEEE, d' de 'MMMM' de 'yyyy", 
                    "d' de 'MMMM' de 'yyyy", 
                    "dd/MM/yyyy", 
                    "dd/MM/yy", 
                    "{1} {0}" 
                }
            }
        };
    }
}
