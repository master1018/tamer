class DiffOutput implements Comparable {
    public String pkgName_ = null;
    public String className_ = null;
    public String id_ = null;
    public String title_ = null;
    public String text_ = null;
    public DiffOutput(String pkgName, String className, String id, 
                      String title, String text) {
        pkgName_ = pkgName;
        className_ = className;
        id_ = id;
        title_ = title;
        text_ = text;
    }
    public int compareTo(Object o) {
        DiffOutput oDiffOutput = (DiffOutput)o;
        int comp = pkgName_.compareTo(oDiffOutput.pkgName_);
        if (comp != 0)
            return comp;
        return id_.compareTo(oDiffOutput.id_);
    }    
}  
