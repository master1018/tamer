public class FormatData_es_CR extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
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
                    "\ufffd" 
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
