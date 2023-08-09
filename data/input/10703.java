public class FormatData_hr_HR extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4 #,##0.##;-\u00A4 #,##0.##", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "HH:mm:ss z", 
                    "HH:mm:ss z", 
                    "HH:mm:ss", 
                    "HH:mm", 
                    "yyyy. MMMM dd", 
                    "yyyy. MMMM dd", 
                    "dd.MM.yyyy.", 
                    "dd.MM.yy.", 
                    "{1} {0}" 
                }
            }
        };
    }
}
