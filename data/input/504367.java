public class APIDiff {
    public List packagesAdded = null; 
    public List packagesRemoved = null; 
    public List packagesChanged = null; 
    public static String oldAPIName_;
    public static String newAPIName_;
    public double pdiff = 0.0;
    public APIDiff() {
        oldAPIName_ = null;
        newAPIName_ = null;
        packagesAdded = new ArrayList(); 
        packagesRemoved = new ArrayList(); 
        packagesChanged = new ArrayList(); 
    }   
}
