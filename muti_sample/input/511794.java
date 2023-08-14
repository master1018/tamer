class PackageDiff {
    public String name_;
    public List classesAdded = null;
    public List classesRemoved = null;
    public List classesChanged = null;
    public String documentationChange_ = null;
    public double pdiff = 0.0;
    public PackageDiff(String name) {
        name_ = name;
        classesAdded = new ArrayList(); 
        classesRemoved = new ArrayList(); 
        classesChanged = new ArrayList(); 
    }   
}
