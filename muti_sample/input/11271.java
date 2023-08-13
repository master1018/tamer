public class CollationData_is extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                "@"                                           
                + "& A < a\u0301, A\u0301 "       
                + "& D < \u00f0, \u00d0"          
                + "& E < e\u0301, E\u0301 "       
                + "& I < i\u0301, I\u0301 "       
                + "& O < o\u0301, O\u0301 "       
                + "& U < u\u0301, U\u0301 "       
                + "& Y < y\u0301, Y\u0301 "       
                + "& Z < \u00fe, \u00de < \u00e6, \u00c6" 
                + "< o\u0308, O\u0308 ; \u00f8, \u00d8" 
            }
        };
    }
}
