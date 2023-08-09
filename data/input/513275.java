class FieldAPI implements Comparable {
    public String name_;
    public String type_;
    public String inheritedFrom_ = null;
    public boolean isTransient_ = false;
    public boolean isVolatile_ = false;
    public String value_ = null;
    public Modifiers modifiers_;
    public String doc_ = null;
    public FieldAPI(String name, String type, 
                    boolean isTransient, boolean isVolatile, 
                    String value, Modifiers modifiers) {
        name_ = name;
        type_ = type;
        isTransient_ = isTransient;
        isVolatile_ = isVolatile;
        value_ = value;
        modifiers_ = modifiers;
    }
    public FieldAPI(FieldAPI f) {
        name_ = f.name_;
        type_ = f.type_;
        inheritedFrom_ = f.inheritedFrom_;
        isTransient_ = f.isTransient_;
        isVolatile_ = f.isVolatile_;
        value_ = f.value_;
        modifiers_ = f.modifiers_; 
        doc_ = f.doc_;
    }
    public int compareTo(Object o) {
        FieldAPI oFieldAPI = (FieldAPI)o;
        int comp = name_.compareTo(oFieldAPI.name_);
        if (comp != 0)
            return comp;
        comp = type_.compareTo(oFieldAPI.type_);
        if (comp != 0)
            return comp;
        if (APIComparator.changedInheritance(inheritedFrom_, oFieldAPI.inheritedFrom_) != 0)
            return -1;
        if (isTransient_ != oFieldAPI.isTransient_) {
            return -1;
        }
        if (isVolatile_ != oFieldAPI.isVolatile_) {
            return -1;
        }
        if (value_ != null && oFieldAPI.value_ != null) {
            comp = value_.compareTo(oFieldAPI.value_);
            if (comp != 0)
                return comp;
        }
        comp = modifiers_.compareTo(oFieldAPI.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, oFieldAPI.doc_))
            return -1;
        return 0;
    }
    public boolean equals(Object o) {
        if (name_.compareTo(((FieldAPI)o).name_) == 0)
            return true;
        return false;
    }
}  
