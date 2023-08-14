public class OpenMBeanOperationInfoSupport
    extends MBeanOperationInfo
    implements OpenMBeanOperationInfo {
    static final long serialVersionUID = 4996859732565369366L;
    private OpenType<?> returnOpenType;
    private transient Integer myHashCode = null;
    private transient String  myToString = null;
    public OpenMBeanOperationInfoSupport(String name,
                                         String description,
                                         OpenMBeanParameterInfo[] signature,
                                         OpenType<?> returnOpenType,
                                         int impact) {
        this(name, description, signature, returnOpenType, impact,
             (Descriptor) null);
    }
    public OpenMBeanOperationInfoSupport(String name,
                                         String description,
                                         OpenMBeanParameterInfo[] signature,
                                         OpenType<?> returnOpenType,
                                         int impact,
                                         Descriptor descriptor) {
        super(name,
              description,
              arrayCopyCast(signature),
              (returnOpenType == null) ? null : returnOpenType.getClassName(),
              impact,
              ImmutableDescriptor.union(descriptor,
                (returnOpenType==null) ? null :returnOpenType.getDescriptor()));
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Argument name cannot " +
                                               "be null or empty");
        }
        if (description == null || description.trim().equals("")) {
            throw new IllegalArgumentException("Argument description cannot " +
                                               "be null or empty");
        }
        if (returnOpenType == null) {
            throw new IllegalArgumentException("Argument returnOpenType " +
                                               "cannot be null");
        }
        if (impact != ACTION && impact != ACTION_INFO && impact != INFO &&
                impact != UNKNOWN) {
            throw new IllegalArgumentException("Argument impact can only be " +
                                               "one of ACTION, ACTION_INFO, " +
                                               "INFO, or UNKNOWN: " + impact);
        }
        this.returnOpenType = returnOpenType;
    }
    private static MBeanParameterInfo[]
            arrayCopyCast(OpenMBeanParameterInfo[] src) {
        if (src == null)
            return null;
        MBeanParameterInfo[] dst = new MBeanParameterInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    private static OpenMBeanParameterInfo[]
            arrayCopyCast(MBeanParameterInfo[] src) {
        if (src == null)
            return null;
        OpenMBeanParameterInfo[] dst = new OpenMBeanParameterInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    public OpenType<?> getReturnOpenType() {
        return returnOpenType;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        OpenMBeanOperationInfo other;
        try {
            other = (OpenMBeanOperationInfo) obj;
        } catch (ClassCastException e) {
            return false;
        }
        if ( ! this.getName().equals(other.getName()) ) {
            return false;
        }
        if ( ! Arrays.equals(this.getSignature(), other.getSignature()) ) {
            return false;
        }
        if ( ! this.getReturnOpenType().equals(other.getReturnOpenType()) ) {
            return false;
        }
        if ( this.getImpact() != other.getImpact() ) {
            return false;
        }
        return true;
    }
    public int hashCode() {
        if (myHashCode == null) {
            int value = 0;
            value += this.getName().hashCode();
            value += Arrays.asList(this.getSignature()).hashCode();
            value += this.getReturnOpenType().hashCode();
            value += this.getImpact();
            myHashCode = Integer.valueOf(value);
        }
        return myHashCode.intValue();
    }
    public String toString() {
        if (myToString == null) {
            myToString = new StringBuilder()
                .append(this.getClass().getName())
                .append("(name=")
                .append(this.getName())
                .append(",signature=")
                .append(Arrays.asList(this.getSignature()).toString())
                .append(",return=")
                .append(this.getReturnOpenType().toString())
                .append(",impact=")
                .append(this.getImpact())
                .append(",descriptor=")
                .append(this.getDescriptor())
                .append(")")
                .toString();
        }
        return myToString;
    }
    private Object readResolve() {
        if (getDescriptor().getFieldNames().length == 0) {
            return new OpenMBeanOperationInfoSupport(
                    name, description, arrayCopyCast(getSignature()),
                    returnOpenType, getImpact());
        } else
            return this;
    }
}
