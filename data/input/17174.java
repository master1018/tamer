public class FormatData extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "January", 
                    "February", 
                    "March", 
                    "April", 
                    "May", 
                    "June", 
                    "July", 
                    "August", 
                    "September", 
                    "October", 
                    "November", 
                    "December", 
                    "" 
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "Jan", 
                    "Feb", 
                    "Mar", 
                    "Apr", 
                    "May", 
                    "Jun", 
                    "Jul", 
                    "Aug", 
                    "Sep", 
                    "Oct", 
                    "Nov", 
                    "Dec", 
                    "" 
                }
            },
            { "DayNames",
                new String[] {
                    "Sunday", 
                    "Monday", 
                    "Tuesday", 
                    "Wednesday", 
                    "Thursday", 
                    "Friday", 
                    "Saturday" 
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "Sun", 
                    "Mon", 
                    "Tue", 
                    "Wed", 
                    "Thu", 
                    "Fri", 
                    "Sat" 
                }
            },
            { "AmPmMarkers",
                new String[] {
                    "AM", 
                    "PM" 
                }
            },
            { "Eras",
                new String[] { 
                    "BC",
                    "AD"
                }
            },
            { "sun.util.BuddhistCalendar.Eras",
                new String[] { 
                    "BC",     
                    "B.E."    
                }
            },
            { "sun.util.BuddhistCalendar.short.Eras",
                new String[] { 
                    "BC",     
                    "B.E."    
                }
            },
            { "java.util.JapaneseImperialCalendar.Eras",
                new String[] { 
                    "",
                    "Meiji",
                    "Taisho",
                    "Showa",
                    "Heisei",
                }
            },
            { "java.util.JapaneseImperialCalendar.short.Eras",
                new String[] { 
                    "",
                    "M",
                    "T",
                    "S",
                    "H",
                }
            },
            { "java.util.JapaneseImperialCalendar.FirstYear",
                new String[] { 
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00a4 #,##0.00;-\u00a4 #,##0.00", 
                    "#,##0%" 
                }
            },
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
                    "h:mm:ss a z",        
                    "h:mm:ss a z",        
                    "h:mm:ss a",          
                    "h:mm a",             
                    "EEEE, MMMM d, yyyy", 
                    "MMMM d, yyyy",       
                    "MMM d, yyyy",        
                    "M/d/yy",             
                    "{1} {0}"             
                }
            },
            { "sun.util.BuddhistCalendar.DateTimePatterns",
                new String[] {
                    "H:mm:ss z",          
                    "H:mm:ss z",          
                    "H:mm:ss",            
                    "H:mm",               
                    "EEEE d MMMM G yyyy", 
                    "d MMMM yyyy",        
                    "d MMM yyyy",         
                    "d/M/yyyy",           
                    "{1}, {0}"            
                }
            },
            { "java.util.JapaneseImperialCalendar.DateTimePatterns",
                new String[] {
                    "h:mm:ss a z",             
                    "h:mm:ss a z",             
                    "h:mm:ss a",               
                    "h:mm a",                  
                    "GGGG yyyy MMMM d (EEEE)", 
                    "GGGG yyyy MMMM d",        
                    "GGGG yyyy MMM d",         
                    "Gy.MM.dd",                
                    "{1} {0}"                  
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
