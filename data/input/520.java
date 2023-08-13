public class CollationData_sr_Latn extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                "& \u200f = \u030c "
                + "& \u0306 = \u030d "
                + "& C < c\u030c , C\u030c "  
                + "< c\u0301 , C\u0301 "      
                + "& D < \u01f3 , \u01f2 , \u01f1 "  
                + "< dz , dZ , Dz , DZ "      
                + "< \u01c6 , \u01c5 , \u01c4 "  
                + "< \u0111 , \u0110 "           
                + "& L < lj , lJ , Lj , LJ " 
                + "& N < nj , nJ , Nj , NJ " 
                + "& S < s\u030c , S\u030c "  
                + "& Z < z\u030c , Z\u030c "  
            }
        };
    }
}
