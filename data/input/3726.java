public class FormatData_en_ZA extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4 #,##0.00;\u00A4-#,##0.00", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "h:mm:ss a", 
                    "h:mm:ss a", 
                    "h:mm:ss a", 
                    "h:mm a", 
                    "EEEE dd MMMM yyyy", 
                    "dd MMMM yyyy", 
                    "dd MMM yyyy", 
                    "yyyy/MM/dd", 
                    "{1} {0}" 
                }
            }
        };
    }
}
