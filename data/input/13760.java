public class MBeanInfo implements Cloneable, Serializable, DescriptorRead {
    static final long serialVersionUID = -6451021435135161911L;
    private transient Descriptor descriptor;
    private final String description;
    private final String className;
    private final MBeanAttributeInfo[] attributes;
    private final MBeanOperationInfo[] operations;
    private final MBeanConstructorInfo[] constructors;
    private final MBeanNotificationInfo[] notifications;
    private transient int hashCode;
    private final transient boolean arrayGettersSafe;
    public MBeanInfo(String className,
                     String description,
                     MBeanAttributeInfo[] attributes,
                     MBeanConstructorInfo[] constructors,
                     MBeanOperationInfo[] operations,
                     MBeanNotificationInfo[] notifications)
            throws IllegalArgumentException {
        this(className, description, attributes, constructors, operations,
             notifications, null);
    }
    public MBeanInfo(String className,
                     String description,
                     MBeanAttributeInfo[] attributes,
                     MBeanConstructorInfo[] constructors,
                     MBeanOperationInfo[] operations,
                     MBeanNotificationInfo[] notifications,
                     Descriptor descriptor)
            throws IllegalArgumentException {
        this.className = className;
        this.description = description;
        if (attributes == null)
            attributes = MBeanAttributeInfo.NO_ATTRIBUTES;
        this.attributes = attributes;
        if (operations == null)
            operations = MBeanOperationInfo.NO_OPERATIONS;
        this.operations = operations;
        if (constructors == null)
            constructors = MBeanConstructorInfo.NO_CONSTRUCTORS;
        this.constructors = constructors;
        if (notifications == null)
            notifications = MBeanNotificationInfo.NO_NOTIFICATIONS;
        this.notifications = notifications;
        if (descriptor == null)
            descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
        this.descriptor = descriptor;
        this.arrayGettersSafe =
                arrayGettersSafe(this.getClass(), MBeanInfo.class);
    }
     @Override
     public Object clone () {
         try {
             return super.clone() ;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
    public String getClassName()  {
        return className;
    }
    public String getDescription()  {
        return description;
    }
    public MBeanAttributeInfo[] getAttributes()   {
        MBeanAttributeInfo[] as = nonNullAttributes();
        if (as.length == 0)
            return as;
        else
            return as.clone();
    }
    private MBeanAttributeInfo[] fastGetAttributes() {
        if (arrayGettersSafe)
            return nonNullAttributes();
        else
            return getAttributes();
    }
    private MBeanAttributeInfo[] nonNullAttributes() {
        return (attributes == null) ?
            MBeanAttributeInfo.NO_ATTRIBUTES : attributes;
    }
    public MBeanOperationInfo[] getOperations()  {
        MBeanOperationInfo[] os = nonNullOperations();
        if (os.length == 0)
            return os;
        else
            return os.clone();
    }
    private MBeanOperationInfo[] fastGetOperations() {
        if (arrayGettersSafe)
            return nonNullOperations();
        else
            return getOperations();
    }
    private MBeanOperationInfo[] nonNullOperations() {
        return (operations == null) ?
            MBeanOperationInfo.NO_OPERATIONS : operations;
    }
    public MBeanConstructorInfo[] getConstructors()  {
        MBeanConstructorInfo[] cs = nonNullConstructors();
        if (cs.length == 0)
            return cs;
        else
            return cs.clone();
    }
    private MBeanConstructorInfo[] fastGetConstructors() {
        if (arrayGettersSafe)
            return nonNullConstructors();
        else
            return getConstructors();
    }
    private MBeanConstructorInfo[] nonNullConstructors() {
        return (constructors == null) ?
            MBeanConstructorInfo.NO_CONSTRUCTORS : constructors;
    }
    public MBeanNotificationInfo[] getNotifications()  {
        MBeanNotificationInfo[] ns = nonNullNotifications();
        if (ns.length == 0)
            return ns;
        else
            return ns.clone();
    }
    private MBeanNotificationInfo[] fastGetNotifications() {
        if (arrayGettersSafe)
            return nonNullNotifications();
        else
            return getNotifications();
    }
    private MBeanNotificationInfo[] nonNullNotifications() {
        return (notifications == null) ?
            MBeanNotificationInfo.NO_NOTIFICATIONS : notifications;
    }
    public Descriptor getDescriptor() {
        return (Descriptor) nonNullDescriptor(descriptor).clone();
    }
    @Override
    public String toString() {
        return
            getClass().getName() + "[" +
            "description=" + getDescription() + ", " +
            "attributes=" + Arrays.asList(fastGetAttributes()) + ", " +
            "constructors=" + Arrays.asList(fastGetConstructors()) + ", " +
            "operations=" + Arrays.asList(fastGetOperations()) + ", " +
            "notifications=" + Arrays.asList(fastGetNotifications()) + ", " +
            "descriptor=" + getDescriptor() +
            "]";
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MBeanInfo))
            return false;
        MBeanInfo p = (MBeanInfo) o;
        if (!isEqual(getClassName(),  p.getClassName()) ||
                !isEqual(getDescription(), p.getDescription()) ||
                !getDescriptor().equals(p.getDescriptor())) {
            return false;
        }
        return
            (Arrays.equals(p.fastGetAttributes(), fastGetAttributes()) &&
             Arrays.equals(p.fastGetOperations(), fastGetOperations()) &&
             Arrays.equals(p.fastGetConstructors(), fastGetConstructors()) &&
             Arrays.equals(p.fastGetNotifications(), fastGetNotifications()));
    }
    @Override
    public int hashCode() {
        if (hashCode != 0)
            return hashCode;
        hashCode =
            getClassName().hashCode() ^
            getDescriptor().hashCode() ^
            arrayHashCode(fastGetAttributes()) ^
            arrayHashCode(fastGetOperations()) ^
            arrayHashCode(fastGetConstructors()) ^
            arrayHashCode(fastGetNotifications());
        return hashCode;
    }
    private static int arrayHashCode(Object[] array) {
        int hash = 0;
        for (int i = 0; i < array.length; i++)
            hash ^= array[i].hashCode();
        return hash;
    }
    private static final Map<Class<?>, Boolean> arrayGettersSafeMap =
        new WeakHashMap<Class<?>, Boolean>();
    static boolean arrayGettersSafe(Class<?> subclass, Class<?> immutableClass) {
        if (subclass == immutableClass)
            return true;
        synchronized (arrayGettersSafeMap) {
            Boolean safe = arrayGettersSafeMap.get(subclass);
            if (safe == null) {
                try {
                    ArrayGettersSafeAction action =
                        new ArrayGettersSafeAction(subclass, immutableClass);
                    safe = AccessController.doPrivileged(action);
                } catch (Exception e) { 
                    safe = false;
                }
                arrayGettersSafeMap.put(subclass, safe);
            }
            return safe;
        }
    }
    private static class ArrayGettersSafeAction
            implements PrivilegedAction<Boolean> {
        private final Class<?> subclass;
        private final Class<?> immutableClass;
        ArrayGettersSafeAction(Class<?> subclass, Class<?> immutableClass) {
            this.subclass = subclass;
            this.immutableClass = immutableClass;
        }
        public Boolean run() {
            Method[] methods = immutableClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String methodName = method.getName();
                if (methodName.startsWith("get") &&
                        method.getParameterTypes().length == 0 &&
                        method.getReturnType().isArray()) {
                    try {
                        Method submethod =
                            subclass.getMethod(methodName);
                        if (!submethod.equals(method))
                            return false;
                    } catch (NoSuchMethodException e) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
    private static boolean isEqual(String s1, String s2) {
        boolean ret;
        if (s1 == null) {
            ret = (s2 == null);
        } else {
            ret = s1.equals(s2);
        }
        return ret;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (descriptor.getClass() == ImmutableDescriptor.class) {
            out.write(1);
            final String[] names = descriptor.getFieldNames();
            out.writeObject(names);
            out.writeObject(descriptor.getFieldValues(names));
        } else {
            out.write(0);
            out.writeObject(descriptor);
        }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        switch (in.read()) {
        case 1:
            final String[] names = (String[])in.readObject();
            if (names.length == 0) {
                descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
            } else {
                final Object[] values = (Object[])in.readObject();
                descriptor = new ImmutableDescriptor(names, values);
            }
            break;
        case 0:
            descriptor = (Descriptor)in.readObject();
            if (descriptor == null) {
                descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
            }
            break;
        case -1: 
            descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
            break;
        default:
            throw new StreamCorruptedException("Got unexpected byte.");
        }
    }
}
