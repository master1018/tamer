public class Binding extends NameClassPair {
    private Object boundObj;
    public Binding(String name, Object obj) {
        super(name, null);
        this.boundObj = obj;
    }
    public Binding(String name, Object obj, boolean isRelative) {
        super(name, null, isRelative);
        this.boundObj = obj;
    }
    public Binding(String name, String className, Object obj) {
        super(name, className);
        this.boundObj = obj;
    }
    public Binding(String name, String className, Object obj, boolean isRelative) {
        super(name, className, isRelative);
        this.boundObj = obj;
    }
    public String getClassName() {
        String cname = super.getClassName();
        if (cname != null) {
            return cname;
        }
        if (boundObj != null)
            return boundObj.getClass().getName();
        else
            return null;
    }
    public Object getObject() {
        return boundObj;
    }
    public void setObject(Object obj) {
        boundObj = obj;
    }
    public String toString() {
        return super.toString() + ":" + getObject();
    }
    private static final long serialVersionUID = 8839217842691845890L;
};
