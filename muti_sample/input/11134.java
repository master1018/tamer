public class CollationData_hu extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                "& C < cs , cS , Cs , CS " 
                + "& D < \u0111, \u0110 "    
                + "& G < gy, Gy, gY, GY "    
                + "& L < ly, Ly, lY, LY "    
                + "& O < o\u0308 , O\u0308 " 
                + "< o\u030b , O\u030b "     
                + "& S < sz , sZ , Sz , SZ " 
                + "& U < u\u0308 , U\u0308 " 
                + "< u\u030b , U\u030b "     
                + "& Z < zs , zS , Zs , ZS " 
            }
        };
    }
}
