class MethodAPI implements Comparable {
    public String name_ = null;
    public String returnType_ = null;
    public String inheritedFrom_ = null;
    public String exceptions_ = "no exceptions";
    public boolean isAbstract_ = false;
    public boolean isNative_ = false;
    public boolean isSynchronized_ = false;
    public Modifiers modifiers_;
    public List params_; 
    public String doc_ = null;
    public MethodAPI(String name, String returnType, boolean isAbstract, 
                     boolean isNative, boolean isSynchronized,
                     Modifiers modifiers) {
        name_ = name;
        returnType_ = returnType;
        isAbstract_ = isAbstract;
        isNative_ = isNative;
        isSynchronized_ = isSynchronized;
        modifiers_ = modifiers;
        params_ = new ArrayList(); 
    }
    public MethodAPI(MethodAPI m) {
        name_ = m.name_;
        returnType_ = m.returnType_;
        inheritedFrom_ = m.inheritedFrom_;
        exceptions_ = m.exceptions_;
        isAbstract_ = m.isAbstract_;
        isNative_ = m.isNative_;
        isSynchronized_ = m.isSynchronized_;
        modifiers_ = m.modifiers_; 
        params_ = m.params_; 
        doc_ = m.doc_;
        signature_ = m.signature_; 
    }
    public int compareTo(Object o) {
        MethodAPI oMethod = (MethodAPI)o;
        int comp = name_.compareTo(oMethod.name_);
        if (comp != 0)
            return comp;
        comp = returnType_.compareTo(oMethod.returnType_);
        if (comp != 0)
            return comp;
        if (APIComparator.changedInheritance(inheritedFrom_, oMethod.inheritedFrom_) != 0)
            return -1;
        if (isAbstract_ != oMethod.isAbstract_) {
            return -1;
        }
        if (Diff.showAllChanges && 
	    isNative_ != oMethod.isNative_) {
            return -1;
        }
        if (Diff.showAllChanges && 
	    isSynchronized_ != oMethod.isSynchronized_) {
            return -1;
        }
        comp = exceptions_.compareTo(oMethod.exceptions_);
        if (comp != 0)
            return comp;
        comp = modifiers_.compareTo(oMethod.modifiers_);
        if (comp != 0)
            return comp;
        comp = getSignature().compareTo(oMethod.getSignature());
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, oMethod.doc_))
            return -1;
        return 0;
    }
    public boolean equals(Object o) {
        if (name_.compareTo(((MethodAPI)o).name_) == 0)
            return true;
        return false;
    }
    public boolean equalSignatures(Object o) {
        if (getSignature().compareTo(((MethodAPI)o).getSignature()) == 0)
            return true;
        return false;
    }
    public String signature_ = null;
    public String getSignature() {
        if (signature_ != null)
            return signature_;
        String res = "";
        boolean first = true;
        Iterator iter = params_.iterator();
        while (iter.hasNext()) {
            if (!first)
                res += ", ";
            ParamAPI param = (ParamAPI)(iter.next());
            res += param.toString();
            first = false;
        }
        signature_ = res;
        return res; 
    }
}
