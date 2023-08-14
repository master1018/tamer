public class MBeanNotificationInfo extends MBeanFeatureInfo implements Cloneable {
    static final long serialVersionUID = -3888371564530107064L;
    private static final String[] NO_TYPES = new String[0];
    static final MBeanNotificationInfo[] NO_NOTIFICATIONS =
        new MBeanNotificationInfo[0];
    private final String[] types;
    private final transient boolean arrayGettersSafe;
    public MBeanNotificationInfo(String[] notifTypes,
                                 String name,
                                 String description) {
        this(notifTypes, name, description, null);
    }
    public MBeanNotificationInfo(String[] notifTypes,
                                 String name,
                                 String description,
                                 Descriptor descriptor) {
        super(name, description, descriptor);
        if (notifTypes == null)
            notifTypes = NO_TYPES;
        this.types = notifTypes;
        this.arrayGettersSafe =
            MBeanInfo.arrayGettersSafe(this.getClass(),
                                       MBeanNotificationInfo.class);
    }
     public Object clone () {
         try {
             return super.clone() ;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
    public String[] getNotifTypes() {
        if (types.length == 0)
            return NO_TYPES;
        else
            return types.clone();
    }
    private String[] fastGetNotifTypes() {
        if (arrayGettersSafe)
            return types;
        else
            return getNotifTypes();
    }
    public String toString() {
        return
            getClass().getName() + "[" +
            "description=" + getDescription() + ", " +
            "name=" + getName() + ", " +
            "notifTypes=" + Arrays.asList(fastGetNotifTypes()) + ", " +
            "descriptor=" + getDescriptor() +
            "]";
    }
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MBeanNotificationInfo))
            return false;
        MBeanNotificationInfo p = (MBeanNotificationInfo) o;
        return (p.getName().equals(getName()) &&
                p.getDescription().equals(getDescription()) &&
                p.getDescriptor().equals(getDescriptor()) &&
                Arrays.equals(p.fastGetNotifTypes(), fastGetNotifTypes()));
    }
    public int hashCode() {
        int hash = getName().hashCode();
        for (int i = 0; i < types.length; i++)
            hash ^= types[i].hashCode();
        return hash;
    }
}
