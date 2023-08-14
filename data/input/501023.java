class ParamAPI implements Comparable {
    public String name_;
    public String type_;
    public ParamAPI(String name, String type) {
        name_ = name;
        type_ = type;
    }
    public int compareTo(Object o) {
        ParamAPI oParamAPI = (ParamAPI)o;
        int comp = name_.compareTo(oParamAPI.name_);
        if (comp != 0)
            return comp;
        comp = type_.compareTo(oParamAPI.type_);
        if (comp != 0)
            return comp;
        return 0;
    }
    public boolean equals(Object o) {
        if (name_.compareTo(((ParamAPI)o).name_) == 0)
            return true;
        return false;
    }
    public String toString() {
        if (type_.compareTo("void") == 0)
            return "";
        return type_;
    }
}  
