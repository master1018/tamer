public class OpenMBeanAttributeInfoSupport
    extends MBeanAttributeInfo
    implements OpenMBeanAttributeInfo {
    static final long serialVersionUID = -4867215622149721849L;
    private OpenType<?> openType;
    private final Object defaultValue;
    private final Set<?> legalValues;  
    private final Comparable<?> minValue;
    private final Comparable<?> maxValue;
    private transient Integer myHashCode = null;
    private transient String  myToString = null;
    public OpenMBeanAttributeInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         boolean isReadable,
                                         boolean isWritable,
                                         boolean isIs) {
        this(name, description, openType, isReadable, isWritable, isIs,
             (Descriptor) null);
    }
    public OpenMBeanAttributeInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         boolean isReadable,
                                         boolean isWritable,
                                         boolean isIs,
                                         Descriptor descriptor) {
        super(name,
              (openType==null) ? null : openType.getClassName(),
              description,
              isReadable,
              isWritable,
              isIs,
              ImmutableDescriptor.union(descriptor, (openType==null)?null:
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
    public <T> OpenMBeanAttributeInfoSupport(String   name,
                                             String   description,
                                             OpenType<T> openType,
                                             boolean  isReadable,
                                             boolean  isWritable,
                                             boolean  isIs,
                                             T        defaultValue)
            throws OpenDataException {
        this(name, description, openType, isReadable, isWritable, isIs,
             defaultValue, (T[]) null);
    }
    public <T> OpenMBeanAttributeInfoSupport(String   name,
                                             String   description,
                                             OpenType<T> openType,
                                             boolean  isReadable,
                                             boolean  isWritable,
                                             boolean  isIs,
                                             T        defaultValue,
                                             T[]      legalValues)
            throws OpenDataException {
        this(name, description, openType, isReadable, isWritable, isIs,
             defaultValue, legalValues, null, null);
    }
    public <T> OpenMBeanAttributeInfoSupport(String     name,
                                             String     description,
                                             OpenType<T>   openType,
                                             boolean    isReadable,
                                             boolean    isWritable,
                                             boolean    isIs,
                                             T          defaultValue,
                                             Comparable<T> minValue,
                                             Comparable<T> maxValue)
            throws OpenDataException {
        this(name, description, openType, isReadable, isWritable, isIs,
             defaultValue, null, minValue, maxValue);
    }
    private <T> OpenMBeanAttributeInfoSupport(String name,
                                              String description,
                                              OpenType<T> openType,
                                              boolean isReadable,
                                              boolean isWritable,
                                              boolean isIs,
                                              T defaultValue,
                                              T[] legalValues,
                                              Comparable<T> minValue,
                                              Comparable<T> maxValue)
            throws OpenDataException {
        super(name,
              (openType==null) ? null : openType.getClassName(),
              description,
              isReadable,
              isWritable,
              isIs,
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
            return new OpenMBeanAttributeInfoSupport(
                    name, description, openType,
                    isReadable(), isWritable(), isIs(),
                    makeDescriptor(xopenType, defaultValue, xlegalValues,
                                   xminValue, xmaxValue));
        } else
            return this;
    }
    static void check(OpenMBeanParameterInfo info) throws OpenDataException {
        OpenType<?> openType = info.getOpenType();
        if (openType == null)
            throw new IllegalArgumentException("OpenType cannot be null");
        if (info.getName() == null ||
                info.getName().trim().equals(""))
            throw new IllegalArgumentException("Name cannot be null or empty");
        if (info.getDescription() == null ||
                info.getDescription().trim().equals(""))
            throw new IllegalArgumentException("Description cannot be null or empty");
        if (info.hasDefaultValue()) {
            if (openType.isArray() || (Object)openType instanceof TabularType) {
                throw new OpenDataException("Default value not supported " +
                                            "for ArrayType and TabularType");
            }
            if (!openType.isValue(info.getDefaultValue())) {
                final String msg =
                    "Argument defaultValue's class [\"" +
                    info.getDefaultValue().getClass().getName() +
                    "\"] does not match the one defined in openType[\"" +
                    openType.getClassName() +"\"]";
                throw new OpenDataException(msg);
            }
        }
        if (info.hasLegalValues() &&
                (info.hasMinValue() || info.hasMaxValue())) {
            throw new OpenDataException("cannot have both legalValue and " +
                                        "minValue or maxValue");
        }
        if (info.hasMinValue() && !openType.isValue(info.getMinValue())) {
            final String msg =
                "Type of minValue [" + info.getMinValue().getClass().getName() +
                "] does not match OpenType [" + openType.getClassName() + "]";
            throw new OpenDataException(msg);
        }
        if (info.hasMaxValue() && !openType.isValue(info.getMaxValue())) {
            final String msg =
                "Type of maxValue [" + info.getMaxValue().getClass().getName() +
                "] does not match OpenType [" + openType.getClassName() + "]";
            throw new OpenDataException(msg);
        }
        if (info.hasDefaultValue()) {
            Object defaultValue = info.getDefaultValue();
            if (info.hasLegalValues() &&
                    !info.getLegalValues().contains(defaultValue)) {
                throw new OpenDataException("defaultValue is not contained " +
                                            "in legalValues");
            }
            if (info.hasMinValue()) {
                if (compare(info.getMinValue(), defaultValue) > 0) {
                    throw new OpenDataException("minValue cannot be greater " +
                                                "than defaultValue");
                }
            }
            if (info.hasMaxValue()) {
                if (compare(info.getMaxValue(), defaultValue) < 0) {
                    throw new OpenDataException("maxValue cannot be less " +
                                                "than defaultValue");
                }
            }
        }
        if (info.hasLegalValues()) {
            if ((Object)openType instanceof TabularType || openType.isArray()) {
                throw new OpenDataException("Legal values not supported " +
                                            "for TabularType and arrays");
            }
            for (Object v : info.getLegalValues()) {
                if (!openType.isValue(v)) {
                    final String msg =
                        "Element of legalValues [" + v +
                        "] is not a valid value for the specified openType [" +
                        openType.toString() +"]";
                    throw new OpenDataException(msg);
                }
            }
        }
        if (info.hasMinValue() && info.hasMaxValue()) {
            if (compare(info.getMinValue(), info.getMaxValue()) > 0) {
                throw new OpenDataException("minValue cannot be greater " +
                                            "than maxValue");
            }
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compare(Object x, Object y) {
        return ((Comparable) x).compareTo(y);
    }
    static <T> Descriptor makeDescriptor(OpenType<T> openType,
                                         T defaultValue,
                                         T[] legalValues,
                                         Comparable<T> minValue,
                                         Comparable<T> maxValue) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (defaultValue != null)
            map.put("defaultValue", defaultValue);
        if (legalValues != null) {
            Set<T> set = new HashSet<T>();
            for (T v : legalValues)
                set.add(v);
            set = Collections.unmodifiableSet(set);
            map.put("legalValues", set);
        }
        if (minValue != null)
            map.put("minValue", minValue);
        if (maxValue != null)
            map.put("maxValue", maxValue);
        if (map.isEmpty()) {
            return openType.getDescriptor();
        } else {
            map.put("openType", openType);
            return new ImmutableDescriptor(map);
        }
    }
    static <T> Descriptor makeDescriptor(OpenType<T> openType,
                                         T defaultValue,
                                         Set<T> legalValues,
                                         Comparable<T> minValue,
                                         Comparable<T> maxValue) {
        T[] legals;
        if (legalValues == null)
            legals = null;
        else {
            legals = cast(new Object[legalValues.size()]);
            legalValues.toArray(legals);
        }
        return makeDescriptor(openType, defaultValue, legals, minValue, maxValue);
    }
    static <T> T valueFrom(Descriptor d, String name, OpenType<T> openType) {
        Object x = d.getFieldValue(name);
        if (x == null)
            return null;
        try {
            return convertFrom(x, openType);
        } catch (Exception e) {
            final String msg =
                "Cannot convert descriptor field " + name + "  to " +
                openType.getTypeName();
            throw EnvHelp.initCause(new IllegalArgumentException(msg), e);
        }
    }
    static <T> Set<T> valuesFrom(Descriptor d, String name,
                                 OpenType<T> openType) {
        Object x = d.getFieldValue(name);
        if (x == null)
            return null;
        Collection<?> coll;
        if (x instanceof Set<?>) {
            Set<?> set = (Set<?>) x;
            boolean asis = true;
            for (Object element : set) {
                if (!openType.isValue(element)) {
                    asis = false;
                    break;
                }
            }
            if (asis)
                return cast(set);
            coll = set;
        } else if (x instanceof Object[]) {
            coll = Arrays.asList((Object[]) x);
        } else {
            final String msg =
                "Descriptor value for " + name + " must be a Set or " +
                "an array: " + x.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
        Set<T> result = new HashSet<T>();
        for (Object element : coll)
            result.add(convertFrom(element, openType));
        return result;
    }
    static <T> Comparable<?> comparableValueFrom(Descriptor d, String name,
                                                 OpenType<T> openType) {
        T t = valueFrom(d, name, openType);
        if (t == null || t instanceof Comparable<?>)
            return (Comparable<?>) t;
        final String msg =
            "Descriptor field " + name + " with value " + t +
            " is not Comparable";
        throw new IllegalArgumentException(msg);
    }
    private static <T> T convertFrom(Object x, OpenType<T> openType) {
        if (openType.isValue(x)) {
            T t = OpenMBeanAttributeInfoSupport.<T>cast(x);
            return t;
        }
        return convertFromStrings(x, openType);
    }
    private static <T> T convertFromStrings(Object x, OpenType<T> openType) {
        if (openType instanceof ArrayType<?>)
            return convertFromStringArray(x, openType);
        else if (x instanceof String)
            return convertFromString((String) x, openType);
        final String msg =
            "Cannot convert value " + x + " of type " +
            x.getClass().getName() + " to type " + openType.getTypeName();
        throw new IllegalArgumentException(msg);
    }
    private static <T> T convertFromString(String s, OpenType<T> openType) {
        Class<T> c;
        try {
            c = cast(Class.forName(openType.safeGetClassName()));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.toString());  
        }
        Method valueOf;
        try {
            valueOf = c.getMethod("valueOf", String.class);
            if (!Modifier.isStatic(valueOf.getModifiers()) ||
                    valueOf.getReturnType() != c)
                valueOf = null;
        } catch (NoSuchMethodException e) {
            valueOf = null;
        }
        if (valueOf != null) {
            try {
                return c.cast(valueOf.invoke(null, s));
            } catch (Exception e) {
                final String msg =
                    "Could not convert \"" + s + "\" using method: " + valueOf;
                throw new IllegalArgumentException(msg, e);
            }
        }
        Constructor<T> con;
        try {
            con = c.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            con = null;
        }
        if (con != null) {
            try {
                return con.newInstance(s);
            } catch (Exception e) {
                final String msg =
                    "Could not convert \"" + s + "\" using constructor: " + con;
                throw new IllegalArgumentException(msg, e);
            }
        }
        throw new IllegalArgumentException("Don't know how to convert " +
                                           "string to " +
                                           openType.getTypeName());
    }
    private static <T> T convertFromStringArray(Object x,
                                                OpenType<T> openType) {
        ArrayType<?> arrayType = (ArrayType<?>) openType;
        OpenType<?> baseType = arrayType.getElementOpenType();
        int dim = arrayType.getDimension();
        String squareBrackets = "[";
        for (int i = 1; i < dim; i++)
            squareBrackets += "[";
        Class<?> stringArrayClass;
        Class<?> targetArrayClass;
        try {
            stringArrayClass =
                Class.forName(squareBrackets + "Ljava.lang.String;");
            targetArrayClass =
                Class.forName(squareBrackets + "L" + baseType.safeGetClassName() +
                              ";");
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.toString());  
        }
        if (!stringArrayClass.isInstance(x)) {
            final String msg =
                "Value for " + dim + "-dimensional array of " +
                baseType.getTypeName() + " must be same type or a String " +
                "array with same dimensions";
            throw new IllegalArgumentException(msg);
        }
        OpenType<?> componentOpenType;
        if (dim == 1)
            componentOpenType = baseType;
        else {
            try {
                componentOpenType = new ArrayType<T>(dim - 1, baseType);
            } catch (OpenDataException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        int n = Array.getLength(x);
        Object[] targetArray = (Object[])
            Array.newInstance(targetArrayClass.getComponentType(), n);
        for (int i = 0; i < n; i++) {
            Object stringish = Array.get(x, i);  
            Object converted =
                convertFromStrings(stringish, componentOpenType);
            Array.set(targetArray, i, converted);
        }
        return OpenMBeanAttributeInfoSupport.<T>cast(targetArray);
    }
    @SuppressWarnings("unchecked")
    static <T> T cast(Object x) {
        return (T) x;
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
        return isValue(this, obj);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})  
    static boolean isValue(OpenMBeanParameterInfo info, Object obj) {
        if (info.hasDefaultValue() && obj == null)
            return true;
        return
            info.getOpenType().isValue(obj) &&
            (!info.hasLegalValues() || info.getLegalValues().contains(obj)) &&
            (!info.hasMinValue() ||
            ((Comparable) info.getMinValue()).compareTo(obj) <= 0) &&
            (!info.hasMaxValue() ||
            ((Comparable) info.getMaxValue()).compareTo(obj) >= 0);
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof OpenMBeanAttributeInfo))
            return false;
        OpenMBeanAttributeInfo other = (OpenMBeanAttributeInfo) obj;
        return
            this.isReadable() == other.isReadable() &&
            this.isWritable() == other.isWritable() &&
            this.isIs() == other.isIs() &&
            equal(this, other);
    }
    static boolean equal(OpenMBeanParameterInfo x1, OpenMBeanParameterInfo x2) {
        if (x1 instanceof DescriptorRead) {
            if (!(x2 instanceof DescriptorRead))
                return false;
            Descriptor d1 = ((DescriptorRead) x1).getDescriptor();
            Descriptor d2 = ((DescriptorRead) x2).getDescriptor();
            if (!d1.equals(d2))
                return false;
        } else if (x2 instanceof DescriptorRead)
            return false;
        return
            x1.getName().equals(x2.getName()) &&
            x1.getOpenType().equals(x2.getOpenType()) &&
            (x1.hasDefaultValue() ?
                x1.getDefaultValue().equals(x2.getDefaultValue()) :
                !x2.hasDefaultValue()) &&
            (x1.hasMinValue() ?
                x1.getMinValue().equals(x2.getMinValue()) :
                !x2.hasMinValue()) &&
            (x1.hasMaxValue() ?
                x1.getMaxValue().equals(x2.getMaxValue()) :
                !x2.hasMaxValue()) &&
            (x1.hasLegalValues() ?
                x1.getLegalValues().equals(x2.getLegalValues()) :
                !x2.hasLegalValues());
    }
    public int hashCode() {
        if (myHashCode == null)
            myHashCode = hashCode(this);
        return myHashCode.intValue();
    }
    static int hashCode(OpenMBeanParameterInfo info) {
        int value = 0;
        value += info.getName().hashCode();
        value += info.getOpenType().hashCode();
        if (info.hasDefaultValue())
            value += info.getDefaultValue().hashCode();
        if (info.hasMinValue())
            value += info.getMinValue().hashCode();
        if (info.hasMaxValue())
            value += info.getMaxValue().hashCode();
        if (info.hasLegalValues())
            value += info.getLegalValues().hashCode();
        if (info instanceof DescriptorRead)
            value += ((DescriptorRead) info).getDescriptor().hashCode();
        return value;
    }
    public String toString() {
        if (myToString == null)
            myToString = toString(this);
        return myToString;
    }
    static String toString(OpenMBeanParameterInfo info) {
        Descriptor d = (info instanceof DescriptorRead) ?
            ((DescriptorRead) info).getDescriptor() : null;
        return
            info.getClass().getName() +
            "(name=" + info.getName() +
            ",openType=" + info.getOpenType() +
            ",default=" + info.getDefaultValue() +
            ",minValue=" + info.getMinValue() +
            ",maxValue=" + info.getMaxValue() +
            ",legalValues=" + info.getLegalValues() +
            ((d == null) ? "" : ",descriptor=" + d) +
            ")";
    }
}
