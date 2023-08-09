public class ResolveResult implements java.io.Serializable {
    protected Object resolvedObj;
    protected Name remainingName;
    protected ResolveResult() {
        resolvedObj = null;
        remainingName = null;
    }
    public ResolveResult(Object robj, String rcomp) {
        resolvedObj = robj;
        try {
        remainingName = new CompositeName(rcomp);
        } catch (InvalidNameException e) {
        }
    }
    public ResolveResult(Object robj, Name rname) {
        resolvedObj = robj;
        setRemainingName(rname);
    }
    public Name getRemainingName() {
        return this.remainingName;
    }
    public Object getResolvedObj() {
        return this.resolvedObj;
    }
    public void setRemainingName(Name name) {
        if (name != null)
            this.remainingName = (Name)(name.clone());
        else {
            this.remainingName = null;
        }
    }
    public void appendRemainingName(Name name) {
        if (name != null) {
            if (this.remainingName != null) {
                try {
                    this.remainingName.addAll(name);
                } catch (InvalidNameException e) {
                }
            } else {
                this.remainingName = (Name)(name.clone());
            }
        }
    }
    public void appendRemainingComponent(String name) {
        if (name != null) {
            CompositeName rname = new CompositeName();
            try {
                rname.add(name);
            } catch (InvalidNameException e) {
            }
            appendRemainingName(rname);
        }
    }
    public void setResolvedObj(Object obj) {
        this.resolvedObj = obj;
    }
    private static final long serialVersionUID = -4552108072002407559L;
}
