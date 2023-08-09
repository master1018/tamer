public class OpenMBeanInfoSupport
    extends MBeanInfo
    implements OpenMBeanInfo {
    static final long serialVersionUID = 4349395935420511492L;
    private transient Integer myHashCode = null;
    private transient String  myToString = null;
    public OpenMBeanInfoSupport(String className,
                                String description,
                                OpenMBeanAttributeInfo[] openAttributes,
                                OpenMBeanConstructorInfo[] openConstructors,
                                OpenMBeanOperationInfo[] openOperations,
                                MBeanNotificationInfo[] notifications) {
        this(className, description,
             openAttributes, openConstructors, openOperations, notifications,
             (Descriptor) null);
    }
    public OpenMBeanInfoSupport(String className,
                                String description,
                                OpenMBeanAttributeInfo[] openAttributes,
                                OpenMBeanConstructorInfo[] openConstructors,
                                OpenMBeanOperationInfo[] openOperations,
                                MBeanNotificationInfo[] notifications,
                                Descriptor descriptor) {
        super(className,
              description,
              attributeArray(openAttributes),
              constructorArray(openConstructors),
              operationArray(openOperations),
              (notifications == null) ? null : notifications.clone(),
              descriptor);
    }
    private static MBeanAttributeInfo[]
            attributeArray(OpenMBeanAttributeInfo[] src) {
        if (src == null)
            return null;
        MBeanAttributeInfo[] dst = new MBeanAttributeInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    private static MBeanConstructorInfo[]
            constructorArray(OpenMBeanConstructorInfo[] src) {
        if (src == null)
            return null;
        MBeanConstructorInfo[] dst = new MBeanConstructorInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    private static MBeanOperationInfo[]
            operationArray(OpenMBeanOperationInfo[] src) {
        if (src == null)
            return null;
        MBeanOperationInfo[] dst = new MBeanOperationInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        OpenMBeanInfo other;
        try {
            other = (OpenMBeanInfo) obj;
        } catch (ClassCastException e) {
            return false;
        }
        if ( ! this.getClassName().equals(other.getClassName()) )
            return false;
        if (!sameArrayContents(this.getAttributes(), other.getAttributes()))
            return false;
        if (!sameArrayContents(this.getConstructors(), other.getConstructors()))
            return false;
        if (!sameArrayContents(this.getOperations(), other.getOperations()))
            return false;
        if (!sameArrayContents(this.getNotifications(), other.getNotifications()))
            return false;
        return true;
    }
    private static <T> boolean sameArrayContents(T[] a1, T[] a2) {
        return (new HashSet<T>(Arrays.asList(a1))
                .equals(new HashSet<T>(Arrays.asList(a2))));
    }
    public int hashCode() {
        if (myHashCode == null) {
            int value = 0;
            value += this.getClassName().hashCode();
            value += arraySetHash(this.getAttributes());
            value += arraySetHash(this.getConstructors());
            value += arraySetHash(this.getOperations());
            value += arraySetHash(this.getNotifications());
            myHashCode = Integer.valueOf(value);
        }
        return myHashCode.intValue();
    }
    private static <T> int arraySetHash(T[] a) {
        return new HashSet<T>(Arrays.asList(a)).hashCode();
    }
    public String toString() {
        if (myToString == null) {
            myToString = new StringBuilder()
                .append(this.getClass().getName())
                .append("(mbean_class_name=")
                .append(this.getClassName())
                .append(",attributes=")
                .append(Arrays.asList(this.getAttributes()).toString())
                .append(",constructors=")
                .append(Arrays.asList(this.getConstructors()).toString())
                .append(",operations=")
                .append(Arrays.asList(this.getOperations()).toString())
                .append(",notifications=")
                .append(Arrays.asList(this.getNotifications()).toString())
                .append(",descriptor=")
                .append(this.getDescriptor())
                .append(")")
                .toString();
        }
        return myToString;
    }
}
