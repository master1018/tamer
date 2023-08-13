public class OpenMBeanParameterInfoSupport
    extends MBeanParameterInfo
    implements OpenMBeanParameterInfo {
    static final long serialVersionUID = -7235016873758443122L;
    private OpenType<?>    openType;
    private Object      defaultValue    = null;
    private Set<?> legalValues     = null;  
    private Comparable<?> minValue        = null;
    private Comparable<?> maxValue        = null;
    private transient Integer myHashCode = null;        
    private transient String  myToString = null;        
    public OpenMBeanParameterInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType) {
        this(name, description, openType, (Descriptor) null);
    }
    public OpenMBeanParameterInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         Descriptor descriptor) {
        super(name,
              (openType==null) ? null : openType.getClassName(),
              description,
              ImmutableDescriptor.union(descriptor,(openType==null)?null:
                openType.getDescriptor()));
        this.openType = openType;
        descriptor = getDescriptor();  
        this.defaultValue = valueFrom(descriptor, "defaultValue", openType);
        this.legalValues = valuesFrom(descriptor, "legalValues", openType);
        this.minValue = comparableValueFrom(descriptor, "minValue", openType);
        this.maxValue = comparableValueFrom(descriptor, "maxValue", openType);
        try {
            check(this);
        } catch (OpenDataException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    public <T> OpenMBeanParameterInfoSupport(String   name,
                                             String   description,
                                             OpenType<T> openType,
                                             T        defaultValue)
            throws OpenDataException {
        this(name, description, openType, defaultValue, (T[]) null);
    }
    public <T> OpenMBeanParameterInfoSupport(String   name,
                                             String   description,
                                             OpenType<T> openType,
                                             T        defaultValue,
                                             T[]      legalValues)
            throws OpenDataException {
        this(name, description, openType,
             defaultValue, legalValues, null, null);
    }
    public <T> OpenMBeanParameterInfoSupport(String     name,
                                             String     description,
                                             OpenType<T>   openType,
                                             T          defaultValue,
                                             Comparable<T> minValue,
                                             Comparable<T> maxValue)
            throws OpenDataException {
        this(name, description, openType,
             defaultValue, null, minValue, maxValue);
    }
    private <T> OpenMBeanParameterInfoSupport(String name,
                                              String description,
                                              OpenType<T> openType,
                                              T defaultValue,
                                              T[] legalValues,
                                              Comparable<T> minValue,
                                              Comparable<T> maxValue)
            throws OpenDataException {
        super(name,
              (openType == null) ? null : openType.getClassName(),
              description,
              makeDescriptor(openType,
                             defaultValue, legalValues, minValue, maxValue));
        this.openType = openType;
        Descriptor d = getDescriptor();
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.legalValues = (Set<?>) d.getFieldValue("legalValues");
        check(this);
    }
    private Object readResolve() {
        if (getDescriptor().getFieldNames().length == 0) {
            OpenType<Object> xopenType = cast(openType);
            Set<Object> xlegalValues = cast(legalValues);
            Comparable<Object> xminValue = cast(minValue);
            Comparable<Object> xmaxValue = cast(maxValue);
            return new OpenMBeanParameterInfoSupport(
                    name, description, openType,
                    makeDescriptor(xopenType, defaultValue, xlegalValues,
                                   xminValue, xmaxValue));
        } else
            return this;
    }
    public OpenType<?> getOpenType() {
        return openType;
    }
    public Object getDefaultValue() {
        return defaultValue;
    }
    public Set<?> getLegalValues() {
        return (legalValues);
    }
    public Comparable<?> getMinValue() {
        return minValue;
    }
    public Comparable<?> getMaxValue() {
        return maxValue;
    }
    public boolean hasDefaultValue() {
        return (defaultValue != null);
    }
    public boolean hasLegalValues() {
        return (legalValues != null);
    }
    public boolean hasMinValue() {
        return (minValue != null);
    }
    public boolean hasMaxValue() {
        return (maxValue != null);
    }
    public boolean isValue(Object obj) {
        return OpenMBeanAttributeInfoSupport.isValue(this, obj);
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof OpenMBeanParameterInfo))
            return false;
        OpenMBeanParameterInfo other = (OpenMBeanParameterInfo) obj;
        return equal(this, other);
    }
    public int hashCode() {
        if (myHashCode == null)
            myHashCode = OpenMBeanAttributeInfoSupport.hashCode(this);
        return myHashCode.intValue();
    }
    public String toString() {
        if (myToString == null)
            myToString = OpenMBeanAttributeInfoSupport.toString(this);
        return myToString;
    }
}
