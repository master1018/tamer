public class MBeanParameterInfo extends MBeanFeatureInfo implements Cloneable {
    static final long serialVersionUID = 7432616882776782338L;
    static final MBeanParameterInfo[] NO_PARAMS = new MBeanParameterInfo[0];
    private final String type;
    public MBeanParameterInfo(String name,
                              String type,
                              String description) {
        this(name, type, description, (Descriptor) null);
    }
    public MBeanParameterInfo(String name,
                              String type,
                              String description,
                              Descriptor descriptor) {
        super(name, description, descriptor);
        this.type = type;
    }
     public Object clone () {
         try {
             return super.clone() ;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
    public String getType() {
        return type;
    }
    public String toString() {
        return
            getClass().getName() + "[" +
            "description=" + getDescription() + ", " +
            "name=" + getName() + ", " +
            "type=" + getType() + ", " +
            "descriptor=" + getDescriptor() +
            "]";
    }
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MBeanParameterInfo))
            return false;
        MBeanParameterInfo p = (MBeanParameterInfo) o;
        return (p.getName().equals(getName()) &&
                p.getType().equals(getType()) &&
                p.getDescription().equals(getDescription()) &&
                p.getDescriptor().equals(getDescriptor()));
    }
    public int hashCode() {
        return getName().hashCode() ^ getType().hashCode();
    }
}
