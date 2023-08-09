public class MBeanConstructorInfo extends MBeanFeatureInfo implements Cloneable {
    static final long serialVersionUID = 4433990064191844427L;
    static final MBeanConstructorInfo[] NO_CONSTRUCTORS =
        new MBeanConstructorInfo[0];
    private final transient boolean arrayGettersSafe;
    private final MBeanParameterInfo[] signature;
    public MBeanConstructorInfo(String description, Constructor<?> constructor) {
        this(constructor.getName(), description,
             constructorSignature(constructor),
             Introspector.descriptorForElement(constructor));
    }
    public MBeanConstructorInfo(String name,
                                String description,
                                MBeanParameterInfo[] signature) {
        this(name, description, signature, null);
    }
    public MBeanConstructorInfo(String name,
                                String description,
                                MBeanParameterInfo[] signature,
                                Descriptor descriptor) {
        super(name, description, descriptor);
        if (signature == null || signature.length == 0)
            signature = MBeanParameterInfo.NO_PARAMS;
        else
            signature = signature.clone();
        this.signature = signature;
        this.arrayGettersSafe =
            MBeanInfo.arrayGettersSafe(this.getClass(),
                                       MBeanConstructorInfo.class);
    }
     public Object clone () {
         try {
             return super.clone() ;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
    public MBeanParameterInfo[] getSignature() {
        if (signature.length == 0)
            return signature;
        else
            return signature.clone();
    }
    private MBeanParameterInfo[] fastGetSignature() {
        if (arrayGettersSafe)
            return signature;
        else
            return getSignature();
    }
    public String toString() {
        return
            getClass().getName() + "[" +
            "description=" + getDescription() + ", " +
            "name=" + getName() + ", " +
            "signature=" + Arrays.asList(fastGetSignature()) + ", " +
            "descriptor=" + getDescriptor() +
            "]";
    }
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MBeanConstructorInfo))
            return false;
        MBeanConstructorInfo p = (MBeanConstructorInfo) o;
        return (p.getName().equals(getName()) &&
                p.getDescription().equals(getDescription()) &&
                Arrays.equals(p.fastGetSignature(), fastGetSignature()) &&
                p.getDescriptor().equals(getDescriptor()));
    }
    public int hashCode() {
        int hash = getName().hashCode();
        MBeanParameterInfo[] sig = fastGetSignature();
        for (int i = 0; i < sig.length; i++)
            hash ^= sig[i].hashCode();
        return hash;
    }
    private static MBeanParameterInfo[] constructorSignature(Constructor<?> cn) {
        final Class<?>[] classes = cn.getParameterTypes();
        final Annotation[][] annots = cn.getParameterAnnotations();
        return MBeanOperationInfo.parameters(classes, annots);
    }
}
