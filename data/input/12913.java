public class FormatData_zh_TW extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "Eras",
                new String[] { 
                    "\u897f\u5143\u524d",
                    "\u897f\u5143"
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4#,##0.00;-\u00A4#,##0.00", 
                    "#,##0%" 
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "ahh'\u6642'mm'\u5206'ss'\u79d2' z", 
                    "ahh'\u6642'mm'\u5206'ss'\u79d2'", 
                    "a hh:mm:ss", 
                    "a h:mm", 
                    "yyyy'\u5e74'M'\u6708'd'\u65e5' EEEE", 
                    "yyyy'\u5e74'M'\u6708'd'\u65e5'", 
                    "yyyy/M/d", 
                    "yyyy/M/d", 
                    "{1} {0}" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
