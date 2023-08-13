public class OpenMBeanConstructorInfoSupport
    extends MBeanConstructorInfo
    implements OpenMBeanConstructorInfo {
    static final long serialVersionUID = -4400441579007477003L;
    private transient Integer myHashCode = null;
    private transient String  myToString = null;
    public OpenMBeanConstructorInfoSupport(String name,
                                           String description,
                                           OpenMBeanParameterInfo[] signature) {
        this(name, description, signature, (Descriptor) null);
    }
    public OpenMBeanConstructorInfoSupport(String name,
                                           String description,
                                           OpenMBeanParameterInfo[] signature,
                                           Descriptor descriptor) {
        super(name,
              description,
              arrayCopyCast(signature), 
              descriptor);
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Argument name cannot be " +
                                               "null or empty");
        }
        if (description == null || description.trim().equals("")) {
            throw new IllegalArgumentException("Argument description cannot " +
                                               "be null or empty");
        }
    }
    private static MBeanParameterInfo[]
            arrayCopyCast(OpenMBeanParameterInfo[] src) {
        if (src == null)
            return null;
        MBeanParameterInfo[] dst = new MBeanParameterInfo[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        OpenMBeanConstructorInfo other;
        try {
            other = (OpenMBeanConstructorInfo) obj;
        } catch (ClassCastException e) {
            return false;
        }
        if ( ! this.getName().equals(other.getName()) ) {
            return false;
        }
        if ( ! Arrays.equals(this.getSignature(), other.getSignature()) ) {
            return false;
        }
        return true;
    }
    public int hashCode() {
        if (myHashCode == null) {
            int value = 0;
            value += this.getName().hashCode();
            value += Arrays.asList(this.getSignature()).hashCode();
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
                .append(",descriptor=")
                .append(this.getDescriptor())
                .append(")")
                .toString();
        }
        return myToString;
    }
}
