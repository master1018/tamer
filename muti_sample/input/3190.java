public abstract class IIOMetadataFormatImpl implements IIOMetadataFormat {
    public static final String standardMetadataFormatName =
        "javax_imageio_1.0";
    private static IIOMetadataFormat standardFormat = null;
    private String resourceBaseName = this.getClass().getName() + "Resources";
    private String rootName;
    private HashMap elementMap = new HashMap();
    class Element {
        String elementName;
        int childPolicy;
        int minChildren = 0;
        int maxChildren = 0;
        List childList = new ArrayList();
        List parentList = new ArrayList();
        List attrList = new ArrayList();
        Map attrMap = new HashMap();
        ObjectValue objectValue;
    }
    class Attribute {
        String attrName;
        int valueType = VALUE_ARBITRARY;
        int dataType;
        boolean required;
        String defaultValue = null;
        List enumeratedValues;
        String minValue;
        String maxValue;
        int listMinLength;
        int listMaxLength;
    }
    class ObjectValue {
        int valueType = VALUE_NONE;
        Class classType = null;
        Object defaultValue = null;
        List enumeratedValues = null;
        Comparable minValue = null;
        Comparable maxValue = null;
        int arrayMinLength = 0;
        int arrayMaxLength = 0;
    }
    public IIOMetadataFormatImpl(String rootName,
                                 int childPolicy) {
        if (rootName == null) {
            throw new IllegalArgumentException("rootName == null!");
        }
        if (childPolicy < CHILD_POLICY_EMPTY ||
            childPolicy > CHILD_POLICY_MAX ||
            childPolicy == CHILD_POLICY_REPEAT) {
            throw new IllegalArgumentException("Invalid value for childPolicy!");
        }
        this.rootName = rootName;
        Element root = new Element();
        root.elementName = rootName;
        root.childPolicy = childPolicy;
        elementMap.put(rootName, root);
    }
    public IIOMetadataFormatImpl(String rootName,
                                 int minChildren,
                                 int maxChildren) {
        if (rootName == null) {
            throw new IllegalArgumentException("rootName == null!");
        }
        if (minChildren < 0) {
            throw new IllegalArgumentException("minChildren < 0!");
        }
        if (minChildren > maxChildren) {
            throw new IllegalArgumentException("minChildren > maxChildren!");
        }
        Element root = new Element();
        root.elementName = rootName;
        root.childPolicy = CHILD_POLICY_REPEAT;
        root.minChildren = minChildren;
        root.maxChildren = maxChildren;
        this.rootName = rootName;
        elementMap.put(rootName, root);
    }
    protected void setResourceBaseName(String resourceBaseName) {
        if (resourceBaseName == null) {
            throw new IllegalArgumentException("resourceBaseName == null!");
        }
        this.resourceBaseName = resourceBaseName;
    }
    protected String getResourceBaseName() {
        return resourceBaseName;
    }
    private Element getElement(String elementName, boolean mustAppear) {
        if (mustAppear && (elementName == null)) {
            throw new IllegalArgumentException("element name is null!");
        }
        Element element = (Element)elementMap.get(elementName);
        if (mustAppear && (element == null)) {
            throw new IllegalArgumentException("No such element: " +
                                               elementName);
        }
        return element;
    }
    private Element getElement(String elementName) {
        return getElement(elementName, true);
    }
    private Attribute getAttribute(String elementName, String attrName) {
        Element element = getElement(elementName);
        Attribute attr = (Attribute)element.attrMap.get(attrName);
        if (attr == null) {
            throw new IllegalArgumentException("No such attribute \"" +
                                               attrName + "\"!");
        }
        return attr;
    }
    protected void addElement(String elementName,
                              String parentName,
                              int childPolicy) {
        Element parent = getElement(parentName);
        if (childPolicy < CHILD_POLICY_EMPTY ||
            childPolicy > CHILD_POLICY_MAX ||
            childPolicy == CHILD_POLICY_REPEAT) {
            throw new IllegalArgumentException
                ("Invalid value for childPolicy!");
        }
        Element element = new Element();
        element.elementName = elementName;
        element.childPolicy = childPolicy;
        parent.childList.add(elementName);
        element.parentList.add(parentName);
        elementMap.put(elementName, element);
    }
    protected void addElement(String elementName,
                              String parentName,
                              int minChildren,
                              int maxChildren) {
        Element parent = getElement(parentName);
        if (minChildren < 0) {
            throw new IllegalArgumentException("minChildren < 0!");
        }
        if (minChildren > maxChildren) {
            throw new IllegalArgumentException("minChildren > maxChildren!");
        }
        Element element = new Element();
        element.elementName = elementName;
        element.childPolicy = CHILD_POLICY_REPEAT;
        element.minChildren = minChildren;
        element.maxChildren = maxChildren;
        parent.childList.add(elementName);
        element.parentList.add(parentName);
        elementMap.put(elementName, element);
    }
    protected void addChildElement(String elementName, String parentName) {
        Element parent = getElement(parentName);
        Element element = getElement(elementName);
        parent.childList.add(elementName);
        element.parentList.add(parentName);
    }
    protected void removeElement(String elementName) {
        Element element = getElement(elementName, false);
        if (element != null) {
            Iterator iter = element.parentList.iterator();
            while (iter.hasNext()) {
                String parentName = (String)iter.next();
                Element parent = getElement(parentName, false);
                if (parent != null) {
                    parent.childList.remove(elementName);
                }
            }
            elementMap.remove(elementName);
        }
    }
    protected void addAttribute(String elementName,
                                String attrName,
                                int dataType,
                                boolean required,
                                String defaultValue) {
        Element element = getElement(elementName);
        if (attrName == null) {
            throw new IllegalArgumentException("attrName == null!");
        }
        if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
            throw new IllegalArgumentException("Invalid value for dataType!");
        }
        Attribute attr = new Attribute();
        attr.attrName = attrName;
        attr.valueType = VALUE_ARBITRARY;
        attr.dataType = dataType;
        attr.required = required;
        attr.defaultValue = defaultValue;
        element.attrList.add(attrName);
        element.attrMap.put(attrName, attr);
    }
    protected void addAttribute(String elementName,
                                String attrName,
                                int dataType,
                                boolean required,
                                String defaultValue,
                                List<String> enumeratedValues) {
        Element element = getElement(elementName);
        if (attrName == null) {
            throw new IllegalArgumentException("attrName == null!");
        }
        if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
            throw new IllegalArgumentException("Invalid value for dataType!");
        }
        if (enumeratedValues == null) {
            throw new IllegalArgumentException("enumeratedValues == null!");
        }
        if (enumeratedValues.size() == 0) {
            throw new IllegalArgumentException("enumeratedValues is empty!");
        }
        Iterator iter = enumeratedValues.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o == null) {
                throw new IllegalArgumentException
                    ("enumeratedValues contains a null!");
            }
            if (!(o instanceof String)) {
                throw new IllegalArgumentException
                    ("enumeratedValues contains a non-String value!");
            }
        }
        Attribute attr = new Attribute();
        attr.attrName = attrName;
        attr.valueType = VALUE_ENUMERATION;
        attr.dataType = dataType;
        attr.required = required;
        attr.defaultValue = defaultValue;
        attr.enumeratedValues = enumeratedValues;
        element.attrList.add(attrName);
        element.attrMap.put(attrName, attr);
    }
    protected void addAttribute(String elementName,
                                String attrName,
                                int dataType,
                                boolean required,
                                String defaultValue,
                                String minValue,
                                String maxValue,
                                boolean minInclusive,
                                boolean maxInclusive) {
        Element element = getElement(elementName);
        if (attrName == null) {
            throw new IllegalArgumentException("attrName == null!");
        }
        if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
            throw new IllegalArgumentException("Invalid value for dataType!");
        }
        Attribute attr = new Attribute();
        attr.attrName = attrName;
        attr.valueType = VALUE_RANGE;
        if (minInclusive) {
            attr.valueType |= VALUE_RANGE_MIN_INCLUSIVE_MASK;
        }
        if (maxInclusive) {
            attr.valueType |= VALUE_RANGE_MAX_INCLUSIVE_MASK;
        }
        attr.dataType = dataType;
        attr.required = required;
        attr.defaultValue = defaultValue;
        attr.minValue = minValue;
        attr.maxValue = maxValue;
        element.attrList.add(attrName);
        element.attrMap.put(attrName, attr);
    }
    protected void addAttribute(String elementName,
                                String attrName,
                                int dataType,
                                boolean required,
                                int listMinLength,
                                int listMaxLength) {
        Element element = getElement(elementName);
        if (attrName == null) {
            throw new IllegalArgumentException("attrName == null!");
        }
        if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
            throw new IllegalArgumentException("Invalid value for dataType!");
        }
        if (listMinLength < 0 || listMinLength > listMaxLength) {
            throw new IllegalArgumentException("Invalid list bounds!");
        }
        Attribute attr = new Attribute();
        attr.attrName = attrName;
        attr.valueType = VALUE_LIST;
        attr.dataType = dataType;
        attr.required = required;
        attr.listMinLength = listMinLength;
        attr.listMaxLength = listMaxLength;
        element.attrList.add(attrName);
        element.attrMap.put(attrName, attr);
    }
    protected void addBooleanAttribute(String elementName,
                                       String attrName,
                                       boolean hasDefaultValue,
                                       boolean defaultValue) {
        List values = new ArrayList();
        values.add("TRUE");
        values.add("FALSE");
        String dval = null;
        if (hasDefaultValue) {
            dval = defaultValue ? "TRUE" : "FALSE";
        }
        addAttribute(elementName,
                     attrName,
                     DATATYPE_BOOLEAN,
                     true,
                     dval,
                     values);
    }
    protected void removeAttribute(String elementName, String attrName) {
        Element element = getElement(elementName);
        element.attrList.remove(attrName);
        element.attrMap.remove(attrName);
    }
    protected <T> void addObjectValue(String elementName,
                                      Class<T> classType,
                                      boolean required,
                                      T defaultValue)
    {
        Element element = getElement(elementName);
        ObjectValue obj = new ObjectValue();
        obj.valueType = VALUE_ARBITRARY;
        obj.classType = classType;
        obj.defaultValue = defaultValue;
        element.objectValue = obj;
    }
    protected <T> void addObjectValue(String elementName,
                                      Class<T> classType,
                                      boolean required,
                                      T defaultValue,
                                      List<? extends T> enumeratedValues)
    {
        Element element = getElement(elementName);
        if (enumeratedValues == null) {
            throw new IllegalArgumentException("enumeratedValues == null!");
        }
        if (enumeratedValues.size() == 0) {
            throw new IllegalArgumentException("enumeratedValues is empty!");
        }
        Iterator iter = enumeratedValues.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o == null) {
                throw new IllegalArgumentException("enumeratedValues contains a null!");
            }
            if (!classType.isInstance(o)) {
                throw new IllegalArgumentException("enumeratedValues contains a value not of class classType!");
            }
        }
        ObjectValue obj = new ObjectValue();
        obj.valueType = VALUE_ENUMERATION;
        obj.classType = classType;
        obj.defaultValue = defaultValue;
        obj.enumeratedValues = enumeratedValues;
        element.objectValue = obj;
    }
    protected <T extends Object & Comparable<? super T>> void
        addObjectValue(String elementName,
                       Class<T> classType,
                       T defaultValue,
                       Comparable<? super T> minValue,
                       Comparable<? super T> maxValue,
                       boolean minInclusive,
                       boolean maxInclusive)
    {
        Element element = getElement(elementName);
        ObjectValue obj = new ObjectValue();
        obj.valueType = VALUE_RANGE;
        if (minInclusive) {
            obj.valueType |= VALUE_RANGE_MIN_INCLUSIVE_MASK;
        }
        if (maxInclusive) {
            obj.valueType |= VALUE_RANGE_MAX_INCLUSIVE_MASK;
        }
        obj.classType = classType;
        obj.defaultValue = defaultValue;
        obj.minValue = minValue;
        obj.maxValue = maxValue;
        element.objectValue = obj;
    }
    protected void addObjectValue(String elementName,
                                  Class<?> classType,
                                  int arrayMinLength,
                                  int arrayMaxLength) {
        Element element = getElement(elementName);
        ObjectValue obj = new ObjectValue();
        obj.valueType = VALUE_LIST;
        obj.classType = classType;
        obj.arrayMinLength = arrayMinLength;
        obj.arrayMaxLength = arrayMaxLength;
        element.objectValue = obj;
    }
    protected void removeObjectValue(String elementName) {
        Element element = getElement(elementName);
        element.objectValue = null;
    }
    public String getRootName() {
        return rootName;
    }
    public abstract boolean canNodeAppear(String elementName,
                                          ImageTypeSpecifier imageType);
    public int getElementMinChildren(String elementName) {
        Element element = getElement(elementName);
        if (element.childPolicy != CHILD_POLICY_REPEAT) {
            throw new IllegalArgumentException("Child policy not CHILD_POLICY_REPEAT!");
        }
        return element.minChildren;
    }
    public int getElementMaxChildren(String elementName) {
        Element element = getElement(elementName);
        if (element.childPolicy != CHILD_POLICY_REPEAT) {
            throw new IllegalArgumentException("Child policy not CHILD_POLICY_REPEAT!");
        }
        return element.maxChildren;
    }
    private String getResource(String key, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        ClassLoader loader = (ClassLoader)
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                   public Object run() {
                       return Thread.currentThread().getContextClassLoader();
                   }
            });
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(resourceBaseName,
                                              locale, loader);
        } catch (MissingResourceException mre) {
            try {
                bundle = ResourceBundle.getBundle(resourceBaseName, locale);
            } catch (MissingResourceException mre1) {
                return null;
            }
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }
    public String getElementDescription(String elementName,
                                        Locale locale) {
        Element element = getElement(elementName);
        return getResource(elementName, locale);
    }
    public int getChildPolicy(String elementName) {
        Element element = getElement(elementName);
        return element.childPolicy;
    }
    public String[] getChildNames(String elementName) {
        Element element = getElement(elementName);
        if (element.childPolicy == CHILD_POLICY_EMPTY) {
            return null;
        }
        return (String[])element.childList.toArray(new String[0]);
    }
    public String[] getAttributeNames(String elementName) {
        Element element = getElement(elementName);
        List names = element.attrList;
        String[] result = new String[names.size()];
        return (String[])names.toArray(result);
    }
    public int getAttributeValueType(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        return attr.valueType;
    }
    public int getAttributeDataType(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        return attr.dataType;
    }
    public boolean isAttributeRequired(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        return attr.required;
    }
    public String getAttributeDefaultValue(String elementName,
                                           String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        return attr.defaultValue;
    }
    public String[] getAttributeEnumerations(String elementName,
                                             String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        if (attr.valueType != VALUE_ENUMERATION) {
            throw new IllegalArgumentException
                ("Attribute not an enumeration!");
        }
        List values = attr.enumeratedValues;
        Iterator iter = values.iterator();
        String[] result = new String[values.size()];
        return (String[])values.toArray(result);
    }
    public String getAttributeMinValue(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        if (attr.valueType != VALUE_RANGE &&
            attr.valueType != VALUE_RANGE_MIN_INCLUSIVE &&
            attr.valueType != VALUE_RANGE_MAX_INCLUSIVE &&
            attr.valueType != VALUE_RANGE_MIN_MAX_INCLUSIVE) {
            throw new IllegalArgumentException("Attribute not a range!");
        }
        return attr.minValue;
    }
    public String getAttributeMaxValue(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        if (attr.valueType != VALUE_RANGE &&
            attr.valueType != VALUE_RANGE_MIN_INCLUSIVE &&
            attr.valueType != VALUE_RANGE_MAX_INCLUSIVE &&
            attr.valueType != VALUE_RANGE_MIN_MAX_INCLUSIVE) {
            throw new IllegalArgumentException("Attribute not a range!");
        }
        return attr.maxValue;
    }
    public int getAttributeListMinLength(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        if (attr.valueType != VALUE_LIST) {
            throw new IllegalArgumentException("Attribute not a list!");
        }
        return attr.listMinLength;
    }
    public int getAttributeListMaxLength(String elementName, String attrName) {
        Attribute attr = getAttribute(elementName, attrName);
        if (attr.valueType != VALUE_LIST) {
            throw new IllegalArgumentException("Attribute not a list!");
        }
        return attr.listMaxLength;
    }
    public String getAttributeDescription(String elementName,
                                          String attrName,
                                          Locale locale) {
        Element element = getElement(elementName);
        if (attrName == null) {
            throw new IllegalArgumentException("attrName == null!");
        }
        Attribute attr = (Attribute)element.attrMap.get(attrName);
        if (attr == null) {
            throw new IllegalArgumentException("No such attribute!");
        }
        String key = elementName + "/" + attrName;
        return getResource(key, locale);
    }
    private ObjectValue getObjectValue(String elementName) {
        Element element = getElement(elementName);
        ObjectValue objv = (ObjectValue)element.objectValue;
        if (objv == null) {
            throw new IllegalArgumentException("No object within element " +
                                               elementName + "!");
        }
        return objv;
    }
    public int getObjectValueType(String elementName) {
        Element element = getElement(elementName);
        ObjectValue objv = (ObjectValue)element.objectValue;
        if (objv == null) {
            return VALUE_NONE;
        }
        return objv.valueType;
    }
    public Class<?> getObjectClass(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        return objv.classType;
    }
    public Object getObjectDefaultValue(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        return objv.defaultValue;
    }
    public Object[] getObjectEnumerations(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        if (objv.valueType != VALUE_ENUMERATION) {
            throw new IllegalArgumentException("Not an enumeration!");
        }
        List vlist = objv.enumeratedValues;
        Object[] values = new Object[vlist.size()];
        return vlist.toArray(values);
    }
    public Comparable<?> getObjectMinValue(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        if ((objv.valueType & VALUE_RANGE) != VALUE_RANGE) {
            throw new IllegalArgumentException("Not a range!");
        }
        return objv.minValue;
    }
    public Comparable<?> getObjectMaxValue(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        if ((objv.valueType & VALUE_RANGE) != VALUE_RANGE) {
            throw new IllegalArgumentException("Not a range!");
        }
        return objv.maxValue;
    }
    public int getObjectArrayMinLength(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        if (objv.valueType != VALUE_LIST) {
            throw new IllegalArgumentException("Not a list!");
        }
        return objv.arrayMinLength;
    }
    public int getObjectArrayMaxLength(String elementName) {
        ObjectValue objv = getObjectValue(elementName);
        if (objv.valueType != VALUE_LIST) {
            throw new IllegalArgumentException("Not a list!");
        }
        return objv.arrayMaxLength;
    }
    private synchronized static void createStandardFormat() {
        if (standardFormat == null) {
            standardFormat = new StandardMetadataFormat();
        }
    }
    public static IIOMetadataFormat getStandardFormatInstance() {
        createStandardFormat();
        return standardFormat;
    }
}
