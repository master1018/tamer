class ConstructorAPI implements Comparable {
    public String type_ = null;
    public String exceptions_ = "no exceptions";
    public Modifiers modifiers_;
    public String doc_ = null;
    public ConstructorAPI(String type, Modifiers modifiers) {
        type_ = type;
        modifiers_ = modifiers;
    }
    public int compareTo(Object o) {
        ConstructorAPI constructorAPI = (ConstructorAPI)o;
        int comp = type_.compareTo(constructorAPI.type_);
        if (comp != 0)
            return comp;
        comp = exceptions_.compareTo(constructorAPI.exceptions_);
        if (comp != 0)
            return comp;
        comp = modifiers_.compareTo(constructorAPI.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, constructorAPI.doc_))
            return -1;
        return 0;
    }
    public boolean equals(Object o) {
        if (type_.compareTo(((ConstructorAPI)o).type_) == 0)
            return true;
        return false;
    }
}  
