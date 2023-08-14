class ClassAPI implements Comparable {
    public String name_;
    public boolean isInterface_;
    boolean isAbstract_ = false;
    public Modifiers modifiers_;
    public String extends_; 
    public List implements_; 
    public List ctors_; 
    public List methods_; 
    public List fields_; 
    public String doc_ = null;
    public ClassAPI(String name, String parent, boolean isInterface, 
                    boolean isAbstract, Modifiers modifiers) {
        name_ = name;
        extends_ = parent;
        isInterface_ = isInterface;
        isAbstract_ = isAbstract;
        modifiers_ = modifiers;
        implements_ = new ArrayList(); 
        ctors_ = new ArrayList(); 
        methods_ = new ArrayList(); 
        fields_ = new ArrayList(); 
    }
    public int compareTo(Object o) {
        ClassAPI oClassAPI = (ClassAPI)o;
        int comp = name_.compareTo(oClassAPI.name_);
        if (comp != 0)
            return comp;
        if (isInterface_ != oClassAPI.isInterface_)
            return -1;
        if (isAbstract_ != oClassAPI.isAbstract_)
            return -1;
        comp = modifiers_.compareTo(oClassAPI.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, oClassAPI.doc_))
            return -1;
        return 0;
    }  
    public boolean equals(Object o) {
        if (name_.compareTo(((ClassAPI)o).name_) == 0)
            return true;
        return false;
    }
}
