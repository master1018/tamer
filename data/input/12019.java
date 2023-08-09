public class MBeanOperationInfo extends MBeanFeatureInfo implements Cloneable {
    static final long serialVersionUID = -6178860474881375330L;
    static final MBeanOperationInfo[] NO_OPERATIONS =
        new MBeanOperationInfo[0];
    public static final int INFO = 0;
    public static final int ACTION = 1;
    public static final int ACTION_INFO = 2;
    public static final int UNKNOWN = 3;
    private final String type;
    private final MBeanParameterInfo[] signature;
    private final int impact;
    private final transient boolean arrayGettersSafe;
    public MBeanOperationInfo(String description, Method method) {
        this(method.getName(),
             description,
             methodSignature(method),
             method.getReturnType().getName(),
             UNKNOWN,
             Introspector.descriptorForElement(method));
    }
    public MBeanOperationInfo(String name,
                              String description,
                              MBeanParameterInfo[] signature,
                              String type,
                              int impact) {
        this(name, description, signature, type, impact, (Descriptor) null);
    }
    public MBeanOperationInfo(String name,
                              String description,
                              MBeanParameterInfo[] signature,
                              String type,
                              int impact,
                              Descriptor descriptor) {
        super(name, description, descriptor);
        if (signature == null || signature.length == 0)
            signature = MBeanParameterInfo.NO_PARAMS;
        else
            signature = signature.clone();
        this.signature = signature;
        this.type = type;
        this.impact = impact;
        this.arrayGettersSafe =
            MBeanInfo.arrayGettersSafe(this.getClass(),
                                       MBeanOperationInfo.class);
    }
     @Override
     public Object clone () {
         try {
             return super.clone() ;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
    public String getReturnType() {
        return type;
    }
    public MBeanParameterInfo[] getSignature() {
        if (signature == null)
            return MBeanParameterInfo.NO_PARAMS;
        else if (signature.length == 0)
            return signature;
        else
            return signature.clone();
    }
    private MBeanParameterInfo[] fastGetSignature() {
        if (arrayGettersSafe) {
            if (signature == null)
                return MBeanParameterInfo.NO_PARAMS;
            else return signature;
        } else return getSignature();
    }
    public int getImpact() {
        return impact;
    }
    @Override
    public String toString() {
        String impactString;
        switch (getImpact()) {
        case ACTION: impactString = "action"; break;
        case ACTION_INFO: impactString = "action/info"; break;
        case INFO: impactString = "info"; break;
        case UNKNOWN: impactString = "unknown"; break;
        default: impactString = "(" + getImpact() + ")";
        }
        return getClass().getName() + "[" +
            "description=" + getDescription() + ", " +
            "name=" + getName() + ", " +
            "returnType=" + getReturnType() + ", " +
            "signature=" + Arrays.asList(fastGetSignature()) + ", " +
            "impact=" + impactString + ", " +
            "descriptor=" + getDescriptor() +
            "]";
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MBeanOperationInfo))
            return false;
        MBeanOperationInfo p = (MBeanOperationInfo) o;
        return (p.getName().equals(getName()) &&
                p.getReturnType().equals(getReturnType()) &&
                p.getDescription().equals(getDescription()) &&
                p.getImpact() == getImpact() &&
                Arrays.equals(p.fastGetSignature(), fastGetSignature()) &&
                p.getDescriptor().equals(getDescriptor()));
    }
    @Override
    public int hashCode() {
        return getName().hashCode() ^ getReturnType().hashCode();
    }
    private static MBeanParameterInfo[] methodSignature(Method method) {
        final Class<?>[] classes = method.getParameterTypes();
        final Annotation[][] annots = method.getParameterAnnotations();
        return parameters(classes, annots);
    }
    static MBeanParameterInfo[] parameters(Class<?>[] classes,
                                           Annotation[][] annots) {
        final MBeanParameterInfo[] params =
            new MBeanParameterInfo[classes.length];
        assert(classes.length == annots.length);
        for (int i = 0; i < classes.length; i++) {
            Descriptor d = Introspector.descriptorForAnnotations(annots[i]);
            final String pn = "p" + (i + 1);
            params[i] =
                new MBeanParameterInfo(pn, classes[i].getName(), "", d);
        }
        return params;
    }
}
