public class DefaultMXBeanMappingFactory extends MXBeanMappingFactory {
    static abstract class NonNullMXBeanMapping extends MXBeanMapping {
        NonNullMXBeanMapping(Type javaType, OpenType<?> openType) {
            super(javaType, openType);
        }
        @Override
        public final Object fromOpenValue(Object openValue)
        throws InvalidObjectException {
            if (openValue == null)
                return null;
            else
                return fromNonNullOpenValue(openValue);
        }
        @Override
        public final Object toOpenValue(Object javaValue) throws OpenDataException {
            if (javaValue == null)
                return null;
            else
                return toNonNullOpenValue(javaValue);
        }
        abstract Object fromNonNullOpenValue(Object openValue)
        throws InvalidObjectException;
        abstract Object toNonNullOpenValue(Object javaValue)
        throws OpenDataException;
        boolean isIdentity() {
            return false;
        }
    }
    static boolean isIdentity(MXBeanMapping mapping) {
        return (mapping instanceof NonNullMXBeanMapping &&
                ((NonNullMXBeanMapping) mapping).isIdentity());
    }
    private static final class Mappings
        extends WeakHashMap<Type, WeakReference<MXBeanMapping>> {}
    private static final Mappings mappings = new Mappings();
    private static final List<MXBeanMapping> permanentMappings = newList();
    private static synchronized MXBeanMapping getMapping(Type type) {
        WeakReference<MXBeanMapping> wr = mappings.get(type);
        return (wr == null) ? null : wr.get();
    }
    private static synchronized void putMapping(Type type, MXBeanMapping mapping) {
        WeakReference<MXBeanMapping> wr =
            new WeakReference<MXBeanMapping>(mapping);
        mappings.put(type, wr);
    }
    private static synchronized void putPermanentMapping(
            Type type, MXBeanMapping mapping) {
        putMapping(type, mapping);
        permanentMappings.add(mapping);
    }
    static {
        final OpenType<?>[] simpleTypes = {
            BIGDECIMAL, BIGINTEGER, BOOLEAN, BYTE, CHARACTER, DATE,
            DOUBLE, FLOAT, INTEGER, LONG, OBJECTNAME, SHORT, STRING,
            VOID,
        };
        for (int i = 0; i < simpleTypes.length; i++) {
            final OpenType<?> t = simpleTypes[i];
            Class<?> c;
            try {
                c = Class.forName(t.getClassName(), false,
                                  ObjectName.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            }
            final MXBeanMapping mapping = new IdentityMapping(c, t);
            putPermanentMapping(c, mapping);
            if (c.getName().startsWith("java.lang.")) {
                try {
                    final Field typeField = c.getField("TYPE");
                    final Class<?> primitiveType = (Class<?>) typeField.get(null);
                    final MXBeanMapping primitiveMapping =
                        new IdentityMapping(primitiveType, t);
                    putPermanentMapping(primitiveType, primitiveMapping);
                    if (primitiveType != void.class) {
                        final Class<?> primitiveArrayType =
                            Array.newInstance(primitiveType, 0).getClass();
                        final OpenType<?> primitiveArrayOpenType =
                            ArrayType.getPrimitiveArrayType(primitiveArrayType);
                        final MXBeanMapping primitiveArrayMapping =
                            new IdentityMapping(primitiveArrayType,
                                                primitiveArrayOpenType);
                        putPermanentMapping(primitiveArrayType,
                                            primitiveArrayMapping);
                    }
                } catch (NoSuchFieldException e) {
                } catch (IllegalAccessException e) {
                    assert(false);
                }
            }
        }
    }
    @Override
    public synchronized MXBeanMapping mappingForType(Type objType,
                                                     MXBeanMappingFactory factory)
            throws OpenDataException {
        if (inProgress.containsKey(objType)) {
            throw new OpenDataException(
                    "Recursive data structure, including " + typeName(objType));
        }
        MXBeanMapping mapping;
        mapping = getMapping(objType);
        if (mapping != null)
            return mapping;
        inProgress.put(objType, objType);
        try {
            mapping = makeMapping(objType, factory);
        } catch (OpenDataException e) {
            throw openDataException("Cannot convert type: " + typeName(objType), e);
        } finally {
            inProgress.remove(objType);
        }
        putMapping(objType, mapping);
        return mapping;
    }
    private MXBeanMapping makeMapping(Type objType, MXBeanMappingFactory factory)
    throws OpenDataException {
        if (objType instanceof GenericArrayType) {
            Type componentType =
                ((GenericArrayType) objType).getGenericComponentType();
            return makeArrayOrCollectionMapping(objType, componentType, factory);
        } else if (objType instanceof Class<?>) {
            Class<?> objClass = (Class<?>) objType;
            if (objClass.isEnum()) {
                return makeEnumMapping((Class<?>) objClass, ElementType.class);
            } else if (objClass.isArray()) {
                Type componentType = objClass.getComponentType();
                return makeArrayOrCollectionMapping(objClass, componentType,
                        factory);
            } else if (JMX.isMXBeanInterface(objClass)) {
                return makeMXBeanRefMapping(objClass);
            } else {
                return makeCompositeMapping(objClass, factory);
            }
        } else if (objType instanceof ParameterizedType) {
            return makeParameterizedTypeMapping((ParameterizedType) objType,
                                                factory);
        } else
            throw new OpenDataException("Cannot map type: " + objType);
    }
    private static <T extends Enum<T>> MXBeanMapping
            makeEnumMapping(Class<?> enumClass, Class<T> fake) {
        return new EnumMapping<T>(Util.<Class<T>>cast(enumClass));
    }
    private MXBeanMapping
        makeArrayOrCollectionMapping(Type collectionType, Type elementType,
                                     MXBeanMappingFactory factory)
            throws OpenDataException {
        final MXBeanMapping elementMapping = factory.mappingForType(elementType, factory);
        final OpenType<?> elementOpenType = elementMapping.getOpenType();
        final ArrayType<?> openType = ArrayType.getArrayType(elementOpenType);
        final Class<?> elementOpenClass = elementMapping.getOpenClass();
        final Class<?> openArrayClass;
        final String openArrayClassName;
        if (elementOpenClass.isArray())
            openArrayClassName = "[" + elementOpenClass.getName();
        else
            openArrayClassName = "[L" + elementOpenClass.getName() + ";";
        try {
            openArrayClass = Class.forName(openArrayClassName);
        } catch (ClassNotFoundException e) {
            throw openDataException("Cannot obtain array class", e);
        }
        if (collectionType instanceof ParameterizedType) {
            return new CollectionMapping(collectionType,
                                         openType, openArrayClass,
                                         elementMapping);
        } else {
            if (isIdentity(elementMapping)) {
                return new IdentityMapping(collectionType,
                                           openType);
            } else {
                return new ArrayMapping(collectionType,
                                          openType,
                                          openArrayClass,
                                          elementMapping);
            }
        }
    }
    private static final String[] keyArray = {"key"};
    private static final String[] keyValueArray = {"key", "value"};
    private MXBeanMapping
        makeTabularMapping(Type objType, boolean sortedMap,
                           Type keyType, Type valueType,
                           MXBeanMappingFactory factory)
            throws OpenDataException {
        final String objTypeName = typeName(objType);
        final MXBeanMapping keyMapping = factory.mappingForType(keyType, factory);
        final MXBeanMapping valueMapping = factory.mappingForType(valueType, factory);
        final OpenType<?> keyOpenType = keyMapping.getOpenType();
        final OpenType<?> valueOpenType = valueMapping.getOpenType();
        final CompositeType rowType =
            new CompositeType(objTypeName,
                              objTypeName,
                              keyValueArray,
                              keyValueArray,
                              new OpenType<?>[] {keyOpenType, valueOpenType});
        final TabularType tabularType =
            new TabularType(objTypeName, objTypeName, rowType, keyArray);
        return new TabularMapping(objType, sortedMap, tabularType,
                                    keyMapping, valueMapping);
    }
    private MXBeanMapping
            makeParameterizedTypeMapping(ParameterizedType objType,
                                         MXBeanMappingFactory factory)
            throws OpenDataException {
        final Type rawType = objType.getRawType();
        if (rawType instanceof Class<?>) {
            Class<?> c = (Class<?>) rawType;
            if (c == List.class || c == Set.class || c == SortedSet.class) {
                Type[] actuals = objType.getActualTypeArguments();
                assert(actuals.length == 1);
                if (c == SortedSet.class)
                    mustBeComparable(c, actuals[0]);
                return makeArrayOrCollectionMapping(objType, actuals[0], factory);
            } else {
                boolean sortedMap = (c == SortedMap.class);
                if (c == Map.class || sortedMap) {
                    Type[] actuals = objType.getActualTypeArguments();
                    assert(actuals.length == 2);
                    if (sortedMap)
                        mustBeComparable(c, actuals[0]);
                    return makeTabularMapping(objType, sortedMap,
                            actuals[0], actuals[1], factory);
                }
            }
        }
        throw new OpenDataException("Cannot convert type: " + objType);
    }
    private static MXBeanMapping makeMXBeanRefMapping(Type t)
            throws OpenDataException {
        return new MXBeanRefMapping(t);
    }
    private MXBeanMapping makeCompositeMapping(Class<?> c,
                                               MXBeanMappingFactory factory)
            throws OpenDataException {
        final boolean gcInfoHack =
            (c.getName().equals("com.sun.management.GcInfo") &&
                c.getClassLoader() == null);
        final List<Method> methods =
                MBeanAnalyzer.eliminateCovariantMethods(Arrays.asList(c.getMethods()));
        final SortedMap<String,Method> getterMap = newSortedMap();
        for (Method method : methods) {
            final String propertyName = propertyName(method);
            if (propertyName == null)
                continue;
            if (gcInfoHack && propertyName.equals("CompositeType"))
                continue;
            Method old =
                getterMap.put(decapitalize(propertyName),
                            method);
            if (old != null) {
                final String msg =
                    "Class " + c.getName() + " has method name clash: " +
                    old.getName() + ", " + method.getName();
                throw new OpenDataException(msg);
            }
        }
        final int nitems = getterMap.size();
        if (nitems == 0) {
            throw new OpenDataException("Can't map " + c.getName() +
                                        " to an open data type");
        }
        final Method[] getters = new Method[nitems];
        final String[] itemNames = new String[nitems];
        final OpenType<?>[] openTypes = new OpenType<?>[nitems];
        int i = 0;
        for (Map.Entry<String,Method> entry : getterMap.entrySet()) {
            itemNames[i] = entry.getKey();
            final Method getter = entry.getValue();
            getters[i] = getter;
            final Type retType = getter.getGenericReturnType();
            openTypes[i] = factory.mappingForType(retType, factory).getOpenType();
            i++;
        }
        CompositeType compositeType =
            new CompositeType(c.getName(),
                              c.getName(),
                              itemNames, 
                              itemNames, 
                              openTypes);
        return new CompositeMapping(c,
                                    compositeType,
                                    itemNames,
                                    getters,
                                    factory);
    }
    private static final class IdentityMapping extends NonNullMXBeanMapping {
        IdentityMapping(Type targetType, OpenType<?> openType) {
            super(targetType, openType);
        }
        boolean isIdentity() {
            return true;
        }
        @Override
        Object fromNonNullOpenValue(Object openValue)
        throws InvalidObjectException {
            return openValue;
        }
        @Override
        Object toNonNullOpenValue(Object javaValue) throws OpenDataException {
            return javaValue;
        }
    }
    private static final class EnumMapping<T extends Enum<T>>
            extends NonNullMXBeanMapping {
        EnumMapping(Class<T> enumClass) {
            super(enumClass, SimpleType.STRING);
            this.enumClass = enumClass;
        }
        @Override
        final Object toNonNullOpenValue(Object value) {
            return ((Enum<?>) value).name();
        }
        @Override
        final T fromNonNullOpenValue(Object value)
                throws InvalidObjectException {
            try {
                return Enum.valueOf(enumClass, (String) value);
            } catch (Exception e) {
                throw invalidObjectException("Cannot convert to enum: " +
                                             value, e);
            }
        }
        private final Class<T> enumClass;
    }
    private static final class ArrayMapping extends NonNullMXBeanMapping {
        ArrayMapping(Type targetType,
                     ArrayType<?> openArrayType, Class<?> openArrayClass,
                     MXBeanMapping elementMapping) {
            super(targetType, openArrayType);
            this.elementMapping = elementMapping;
        }
        @Override
        final Object toNonNullOpenValue(Object value)
                throws OpenDataException {
            Object[] valueArray = (Object[]) value;
            final int len = valueArray.length;
            final Object[] openArray = (Object[])
                Array.newInstance(getOpenClass().getComponentType(), len);
            for (int i = 0; i < len; i++)
                openArray[i] = elementMapping.toOpenValue(valueArray[i]);
            return openArray;
        }
        @Override
        final Object fromNonNullOpenValue(Object openValue)
                throws InvalidObjectException {
            final Object[] openArray = (Object[]) openValue;
            final Type javaType = getJavaType();
            final Object[] valueArray;
            final Type componentType;
            if (javaType instanceof GenericArrayType) {
                componentType =
                    ((GenericArrayType) javaType).getGenericComponentType();
            } else if (javaType instanceof Class<?> &&
                       ((Class<?>) javaType).isArray()) {
                componentType = ((Class<?>) javaType).getComponentType();
            } else {
                throw new IllegalArgumentException("Not an array: " +
                                                   javaType);
            }
            valueArray = (Object[]) Array.newInstance((Class<?>) componentType,
                                                      openArray.length);
            for (int i = 0; i < openArray.length; i++)
                valueArray[i] = elementMapping.fromOpenValue(openArray[i]);
            return valueArray;
        }
        public void checkReconstructible() throws InvalidObjectException {
            elementMapping.checkReconstructible();
        }
        private final MXBeanMapping elementMapping;
    }
    private static final class CollectionMapping extends NonNullMXBeanMapping {
        CollectionMapping(Type targetType,
                          ArrayType<?> openArrayType,
                          Class<?> openArrayClass,
                          MXBeanMapping elementMapping) {
            super(targetType, openArrayType);
            this.elementMapping = elementMapping;
            Type raw = ((ParameterizedType) targetType).getRawType();
            Class<?> c = (Class<?>) raw;
            final Class<?> collC;
            if (c == List.class)
                collC = ArrayList.class;
            else if (c == Set.class)
                collC = HashSet.class;
            else if (c == SortedSet.class)
                collC = TreeSet.class;
            else { 
                assert(false);
                collC = null;
            }
            collectionClass = Util.cast(collC);
        }
        @Override
        final Object toNonNullOpenValue(Object value)
                throws OpenDataException {
            final Collection<?> valueCollection = (Collection<?>) value;
            if (valueCollection instanceof SortedSet<?>) {
                Comparator<?> comparator =
                    ((SortedSet<?>) valueCollection).comparator();
                if (comparator != null) {
                    final String msg =
                        "Cannot convert SortedSet with non-null comparator: " +
                        comparator;
                    throw openDataException(msg, new IllegalArgumentException(msg));
                }
            }
            final Object[] openArray = (Object[])
                Array.newInstance(getOpenClass().getComponentType(),
                                  valueCollection.size());
            int i = 0;
            for (Object o : valueCollection)
                openArray[i++] = elementMapping.toOpenValue(o);
            return openArray;
        }
        @Override
        final Object fromNonNullOpenValue(Object openValue)
                throws InvalidObjectException {
            final Object[] openArray = (Object[]) openValue;
            final Collection<Object> valueCollection;
            try {
                valueCollection = cast(collectionClass.newInstance());
            } catch (Exception e) {
                throw invalidObjectException("Cannot create collection", e);
            }
            for (Object o : openArray) {
                Object value = elementMapping.fromOpenValue(o);
                if (!valueCollection.add(value)) {
                    final String msg =
                        "Could not add " + o + " to " +
                        collectionClass.getName() +
                        " (duplicate set element?)";
                    throw new InvalidObjectException(msg);
                }
            }
            return valueCollection;
        }
        public void checkReconstructible() throws InvalidObjectException {
            elementMapping.checkReconstructible();
        }
        private final Class<? extends Collection<?>> collectionClass;
        private final MXBeanMapping elementMapping;
    }
    private static final class MXBeanRefMapping extends NonNullMXBeanMapping {
        MXBeanRefMapping(Type intf) {
            super(intf, SimpleType.OBJECTNAME);
        }
        @Override
        final Object toNonNullOpenValue(Object javaValue)
                throws OpenDataException {
            MXBeanLookup lookup = lookupNotNull(OpenDataException.class);
            ObjectName name = lookup.mxbeanToObjectName(javaValue);
            if (name == null)
                throw new OpenDataException("No name for object: " + javaValue);
            return name;
        }
        @Override
        final Object fromNonNullOpenValue(Object openValue)
                throws InvalidObjectException {
            MXBeanLookup lookup = lookupNotNull(InvalidObjectException.class);
            ObjectName name = (ObjectName) openValue;
            Object mxbean =
                lookup.objectNameToMXBean(name, (Class<?>) getJavaType());
            if (mxbean == null) {
                final String msg =
                    "No MXBean for name: " + name;
                throw new InvalidObjectException(msg);
            }
            return mxbean;
        }
        private <T extends Exception> MXBeanLookup
            lookupNotNull(Class<T> excClass)
                throws T {
            MXBeanLookup lookup = MXBeanLookup.getLookup();
            if (lookup == null) {
                final String msg =
                    "Cannot convert MXBean interface in this context";
                T exc;
                try {
                    Constructor<T> con = excClass.getConstructor(String.class);
                    exc = con.newInstance(msg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                throw exc;
            }
            return lookup;
        }
    }
    private static final class TabularMapping extends NonNullMXBeanMapping {
        TabularMapping(Type targetType,
                       boolean sortedMap,
                       TabularType tabularType,
                       MXBeanMapping keyConverter,
                       MXBeanMapping valueConverter) {
            super(targetType, tabularType);
            this.sortedMap = sortedMap;
            this.keyMapping = keyConverter;
            this.valueMapping = valueConverter;
        }
        @Override
        final Object toNonNullOpenValue(Object value) throws OpenDataException {
            final Map<Object, Object> valueMap = cast(value);
            if (valueMap instanceof SortedMap<?,?>) {
                Comparator<?> comparator = ((SortedMap<?,?>) valueMap).comparator();
                if (comparator != null) {
                    final String msg =
                        "Cannot convert SortedMap with non-null comparator: " +
                        comparator;
                    throw openDataException(msg, new IllegalArgumentException(msg));
                }
            }
            final TabularType tabularType = (TabularType) getOpenType();
            final TabularData table = new TabularDataSupport(tabularType);
            final CompositeType rowType = tabularType.getRowType();
            for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
                final Object openKey = keyMapping.toOpenValue(entry.getKey());
                final Object openValue = valueMapping.toOpenValue(entry.getValue());
                final CompositeData row;
                row =
                    new CompositeDataSupport(rowType, keyValueArray,
                                             new Object[] {openKey,
                                                           openValue});
                table.put(row);
            }
            return table;
        }
        @Override
        final Object fromNonNullOpenValue(Object openValue)
                throws InvalidObjectException {
            final TabularData table = (TabularData) openValue;
            final Collection<CompositeData> rows = cast(table.values());
            final Map<Object, Object> valueMap =
                sortedMap ? newSortedMap() : newInsertionOrderMap();
            for (CompositeData row : rows) {
                final Object key =
                    keyMapping.fromOpenValue(row.get("key"));
                final Object value =
                    valueMapping.fromOpenValue(row.get("value"));
                if (valueMap.put(key, value) != null) {
                    final String msg =
                        "Duplicate entry in TabularData: key=" + key;
                    throw new InvalidObjectException(msg);
                }
            }
            return valueMap;
        }
        @Override
        public void checkReconstructible() throws InvalidObjectException {
            keyMapping.checkReconstructible();
            valueMapping.checkReconstructible();
        }
        private final boolean sortedMap;
        private final MXBeanMapping keyMapping;
        private final MXBeanMapping valueMapping;
    }
    private final class CompositeMapping extends NonNullMXBeanMapping {
        CompositeMapping(Class<?> targetClass,
                         CompositeType compositeType,
                         String[] itemNames,
                         Method[] getters,
                         MXBeanMappingFactory factory) throws OpenDataException {
            super(targetClass, compositeType);
            assert(itemNames.length == getters.length);
            this.itemNames = itemNames;
            this.getters = getters;
            this.getterMappings = new MXBeanMapping[getters.length];
            for (int i = 0; i < getters.length; i++) {
                Type retType = getters[i].getGenericReturnType();
                getterMappings[i] = factory.mappingForType(retType, factory);
            }
        }
        @Override
        final Object toNonNullOpenValue(Object value)
                throws OpenDataException {
            CompositeType ct = (CompositeType) getOpenType();
            if (value instanceof CompositeDataView)
                return ((CompositeDataView) value).toCompositeData(ct);
            if (value == null)
                return null;
            Object[] values = new Object[getters.length];
            for (int i = 0; i < getters.length; i++) {
                try {
                    Object got = getters[i].invoke(value, (Object[]) null);
                    values[i] = getterMappings[i].toOpenValue(got);
                } catch (Exception e) {
                    throw openDataException("Error calling getter for " +
                                            itemNames[i] + ": " + e, e);
                }
            }
            return new CompositeDataSupport(ct, itemNames, values);
        }
        private synchronized void makeCompositeBuilder()
                throws InvalidObjectException {
            if (compositeBuilder != null)
                return;
            Class<?> targetClass = (Class<?>) getJavaType();
            CompositeBuilder[][] builders = {
                {
                    new CompositeBuilderViaFrom(targetClass, itemNames),
                },
                {
                    new CompositeBuilderViaConstructor(targetClass, itemNames),
                },
                {
                    new CompositeBuilderCheckGetters(targetClass, itemNames,
                                                     getterMappings),
                    new CompositeBuilderViaSetters(targetClass, itemNames),
                    new CompositeBuilderViaProxy(targetClass, itemNames),
                },
            };
            CompositeBuilder foundBuilder = null;
            final StringBuilder whyNots = new StringBuilder();
            Throwable possibleCause = null;
        find:
            for (CompositeBuilder[] relatedBuilders : builders) {
                for (int i = 0; i < relatedBuilders.length; i++) {
                    CompositeBuilder builder = relatedBuilders[i];
                    String whyNot = builder.applicable(getters);
                    if (whyNot == null) {
                        foundBuilder = builder;
                        break find;
                    }
                    Throwable cause = builder.possibleCause();
                    if (cause != null)
                        possibleCause = cause;
                    if (whyNot.length() > 0) {
                        if (whyNots.length() > 0)
                            whyNots.append("; ");
                        whyNots.append(whyNot);
                        if (i == 0)
                           break; 
                    }
                }
            }
            if (foundBuilder == null) {
                String msg =
                    "Do not know how to make a " + targetClass.getName() +
                    " from a CompositeData: " + whyNots;
                if (possibleCause != null)
                    msg += ". Remaining exceptions show a POSSIBLE cause.";
                throw invalidObjectException(msg, possibleCause);
            }
            compositeBuilder = foundBuilder;
        }
        @Override
        public void checkReconstructible() throws InvalidObjectException {
            makeCompositeBuilder();
        }
        @Override
        final Object fromNonNullOpenValue(Object value)
                throws InvalidObjectException {
            makeCompositeBuilder();
            return compositeBuilder.fromCompositeData((CompositeData) value,
                                                      itemNames,
                                                      getterMappings);
        }
        private final String[] itemNames;
        private final Method[] getters;
        private final MXBeanMapping[] getterMappings;
        private CompositeBuilder compositeBuilder;
    }
    private static abstract class CompositeBuilder {
        CompositeBuilder(Class<?> targetClass, String[] itemNames) {
            this.targetClass = targetClass;
            this.itemNames = itemNames;
        }
        Class<?> getTargetClass() {
            return targetClass;
        }
        String[] getItemNames() {
            return itemNames;
        }
        abstract String applicable(Method[] getters)
                throws InvalidObjectException;
        Throwable possibleCause() {
            return null;
        }
        abstract Object fromCompositeData(CompositeData cd,
                                          String[] itemNames,
                                          MXBeanMapping[] converters)
                throws InvalidObjectException;
        private final Class<?> targetClass;
        private final String[] itemNames;
    }
    private static final class CompositeBuilderViaFrom
            extends CompositeBuilder {
        CompositeBuilderViaFrom(Class<?> targetClass, String[] itemNames) {
            super(targetClass, itemNames);
        }
        String applicable(Method[] getters) throws InvalidObjectException {
            Class<?> targetClass = getTargetClass();
            try {
                Method fromMethod =
                    targetClass.getMethod("from", CompositeData.class);
                if (!Modifier.isStatic(fromMethod.getModifiers())) {
                    final String msg =
                        "Method from(CompositeData) is not static";
                    throw new InvalidObjectException(msg);
                }
                if (fromMethod.getReturnType() != getTargetClass()) {
                    final String msg =
                        "Method from(CompositeData) returns " +
                        typeName(fromMethod.getReturnType()) +
                        " not " + typeName(targetClass);
                    throw new InvalidObjectException(msg);
                }
                this.fromMethod = fromMethod;
                return null; 
            } catch (InvalidObjectException e) {
                throw e;
            } catch (Exception e) {
                return "no method from(CompositeData)";
            }
        }
        final Object fromCompositeData(CompositeData cd,
                                       String[] itemNames,
                                       MXBeanMapping[] converters)
                throws InvalidObjectException {
            try {
                return fromMethod.invoke(null, cd);
            } catch (Exception e) {
                final String msg = "Failed to invoke from(CompositeData)";
                throw invalidObjectException(msg, e);
            }
        }
        private Method fromMethod;
    }
    private static class CompositeBuilderCheckGetters extends CompositeBuilder {
        CompositeBuilderCheckGetters(Class<?> targetClass, String[] itemNames,
                                     MXBeanMapping[] getterConverters) {
            super(targetClass, itemNames);
            this.getterConverters = getterConverters;
        }
        String applicable(Method[] getters) {
            for (int i = 0; i < getters.length; i++) {
                try {
                    getterConverters[i].checkReconstructible();
                } catch (InvalidObjectException e) {
                    possibleCause = e;
                    return "method " + getters[i].getName() + " returns type " +
                        "that cannot be mapped back from OpenData";
                }
            }
            return "";
        }
        @Override
        Throwable possibleCause() {
            return possibleCause;
        }
        final Object fromCompositeData(CompositeData cd,
                                       String[] itemNames,
                                       MXBeanMapping[] converters) {
            throw new Error();
        }
        private final MXBeanMapping[] getterConverters;
        private Throwable possibleCause;
    }
    private static class CompositeBuilderViaSetters extends CompositeBuilder {
        CompositeBuilderViaSetters(Class<?> targetClass, String[] itemNames) {
            super(targetClass, itemNames);
        }
        String applicable(Method[] getters) {
            try {
                Constructor<?> c = getTargetClass().getConstructor();
            } catch (Exception e) {
                return "does not have a public no-arg constructor";
            }
            Method[] setters = new Method[getters.length];
            for (int i = 0; i < getters.length; i++) {
                Method getter = getters[i];
                Class<?> returnType = getter.getReturnType();
                String name = propertyName(getter);
                String setterName = "set" + name;
                Method setter;
                try {
                    setter = getTargetClass().getMethod(setterName, returnType);
                    if (setter.getReturnType() != void.class)
                        throw new Exception();
                } catch (Exception e) {
                    return "not all getters have corresponding setters " +
                           "(" + getter + ")";
                }
                setters[i] = setter;
            }
            this.setters = setters;
            return null;
        }
        Object fromCompositeData(CompositeData cd,
                                 String[] itemNames,
                                 MXBeanMapping[] converters)
                throws InvalidObjectException {
            Object o;
            try {
                o = getTargetClass().newInstance();
                for (int i = 0; i < itemNames.length; i++) {
                    if (cd.containsKey(itemNames[i])) {
                        Object openItem = cd.get(itemNames[i]);
                        Object javaItem =
                            converters[i].fromOpenValue(openItem);
                        setters[i].invoke(o, javaItem);
                    }
                }
            } catch (Exception e) {
                throw invalidObjectException(e);
            }
            return o;
        }
        private Method[] setters;
    }
    private static final class CompositeBuilderViaConstructor
            extends CompositeBuilder {
        CompositeBuilderViaConstructor(Class<?> targetClass, String[] itemNames) {
            super(targetClass, itemNames);
        }
        String applicable(Method[] getters) throws InvalidObjectException {
            final Class<ConstructorProperties> propertyNamesClass = ConstructorProperties.class;
            Class<?> targetClass = getTargetClass();
            Constructor<?>[] constrs = targetClass.getConstructors();
            List<Constructor<?>> annotatedConstrList = newList();
            for (Constructor<?> constr : constrs) {
                if (Modifier.isPublic(constr.getModifiers())
                        && constr.getAnnotation(propertyNamesClass) != null)
                    annotatedConstrList.add(constr);
            }
            if (annotatedConstrList.isEmpty())
                return "no constructor has @ConstructorProperties annotation";
            annotatedConstructors = newList();
            Map<String, Integer> getterMap = newMap();
            String[] itemNames = getItemNames();
            for (int i = 0; i < itemNames.length; i++)
                getterMap.put(itemNames[i], i);
            Set<BitSet> getterIndexSets = newSet();
            for (Constructor<?> constr : annotatedConstrList) {
                String[] propertyNames =
                    constr.getAnnotation(propertyNamesClass).value();
                Type[] paramTypes = constr.getGenericParameterTypes();
                if (paramTypes.length != propertyNames.length) {
                    final String msg =
                        "Number of constructor params does not match " +
                        "@ConstructorProperties annotation: " + constr;
                    throw new InvalidObjectException(msg);
                }
                int[] paramIndexes = new int[getters.length];
                for (int i = 0; i < getters.length; i++)
                    paramIndexes[i] = -1;
                BitSet present = new BitSet();
                for (int i = 0; i < propertyNames.length; i++) {
                    String propertyName = propertyNames[i];
                    if (!getterMap.containsKey(propertyName)) {
                        String msg =
                            "@ConstructorProperties includes name " + propertyName +
                            " which does not correspond to a property";
                        for (String getterName : getterMap.keySet()) {
                            if (getterName.equalsIgnoreCase(propertyName)) {
                                msg += " (differs only in case from property " +
                                        getterName + ")";
                            }
                        }
                        msg += ": " + constr;
                        throw new InvalidObjectException(msg);
                    }
                    int getterIndex = getterMap.get(propertyName);
                    paramIndexes[getterIndex] = i;
                    if (present.get(getterIndex)) {
                        final String msg =
                            "@ConstructorProperties contains property " +
                            propertyName + " more than once: " + constr;
                        throw new InvalidObjectException(msg);
                    }
                    present.set(getterIndex);
                    Method getter = getters[getterIndex];
                    Type propertyType = getter.getGenericReturnType();
                    if (!propertyType.equals(paramTypes[i])) {
                        final String msg =
                            "@ConstructorProperties gives property " + propertyName +
                            " of type " + propertyType + " for parameter " +
                            " of type " + paramTypes[i] + ": " + constr;
                        throw new InvalidObjectException(msg);
                    }
                }
                if (!getterIndexSets.add(present)) {
                    final String msg =
                        "More than one constructor has a @ConstructorProperties " +
                        "annotation with this set of names: " +
                        Arrays.toString(propertyNames);
                    throw new InvalidObjectException(msg);
                }
                Constr c = new Constr(constr, paramIndexes, present);
                annotatedConstructors.add(c);
            }
            for (BitSet a : getterIndexSets) {
                boolean seen = false;
                for (BitSet b : getterIndexSets) {
                    if (a == b)
                        seen = true;
                    else if (seen) {
                        BitSet u = new BitSet();
                        u.or(a); u.or(b);
                        if (!getterIndexSets.contains(u)) {
                            Set<String> names = new TreeSet<String>();
                            for (int i = u.nextSetBit(0); i >= 0;
                                 i = u.nextSetBit(i+1))
                                names.add(itemNames[i]);
                            final String msg =
                                "Constructors with @ConstructorProperties annotation " +
                                " would be ambiguous for these items: " +
                                names;
                            throw new InvalidObjectException(msg);
                        }
                    }
                }
            }
            return null; 
        }
        final Object fromCompositeData(CompositeData cd,
                                       String[] itemNames,
                                       MXBeanMapping[] mappings)
                throws InvalidObjectException {
            CompositeType ct = cd.getCompositeType();
            BitSet present = new BitSet();
            for (int i = 0; i < itemNames.length; i++) {
                if (ct.getType(itemNames[i]) != null)
                    present.set(i);
            }
            Constr max = null;
            for (Constr constr : annotatedConstructors) {
                if (subset(constr.presentParams, present) &&
                        (max == null ||
                         subset(max.presentParams, constr.presentParams)))
                    max = constr;
            }
            if (max == null) {
                final String msg =
                    "No constructor has a @ConstructorProperties for this set of " +
                    "items: " + ct.keySet();
                throw new InvalidObjectException(msg);
            }
            Object[] params = new Object[max.presentParams.cardinality()];
            for (int i = 0; i < itemNames.length; i++) {
                if (!max.presentParams.get(i))
                    continue;
                Object openItem = cd.get(itemNames[i]);
                Object javaItem = mappings[i].fromOpenValue(openItem);
                int index = max.paramIndexes[i];
                if (index >= 0)
                    params[index] = javaItem;
            }
            try {
                return max.constructor.newInstance(params);
            } catch (Exception e) {
                final String msg =
                    "Exception constructing " + getTargetClass().getName();
                throw invalidObjectException(msg, e);
            }
        }
        private static boolean subset(BitSet sub, BitSet sup) {
            BitSet subcopy = (BitSet) sub.clone();
            subcopy.andNot(sup);
            return subcopy.isEmpty();
        }
        private static class Constr {
            final Constructor<?> constructor;
            final int[] paramIndexes;
            final BitSet presentParams;
            Constr(Constructor<?> constructor, int[] paramIndexes,
                   BitSet presentParams) {
                this.constructor = constructor;
                this.paramIndexes = paramIndexes;
                this.presentParams = presentParams;
            }
        }
        private List<Constr> annotatedConstructors;
    }
    private static final class CompositeBuilderViaProxy
            extends CompositeBuilder {
        CompositeBuilderViaProxy(Class<?> targetClass, String[] itemNames) {
            super(targetClass, itemNames);
        }
        String applicable(Method[] getters) {
            Class<?> targetClass = getTargetClass();
            if (!targetClass.isInterface())
                return "not an interface";
            Set<Method> methods =
                newSet(Arrays.asList(targetClass.getMethods()));
            methods.removeAll(Arrays.asList(getters));
            String bad = null;
            for (Method m : methods) {
                String mname = m.getName();
                Class<?>[] mparams = m.getParameterTypes();
                try {
                    Method om = Object.class.getMethod(mname, mparams);
                    if (!Modifier.isPublic(om.getModifiers()))
                        bad = mname;
                } catch (NoSuchMethodException e) {
                    bad = mname;
                }
            }
            if (bad != null)
                return "contains methods other than getters (" + bad + ")";
            return null; 
        }
        final Object fromCompositeData(CompositeData cd,
                                       String[] itemNames,
                                       MXBeanMapping[] converters) {
            final Class<?> targetClass = getTargetClass();
            return
                Proxy.newProxyInstance(targetClass.getClassLoader(),
                                       new Class<?>[] {targetClass},
                                       new CompositeDataInvocationHandler(cd));
        }
    }
    static InvalidObjectException invalidObjectException(String msg,
                                                         Throwable cause) {
        return EnvHelp.initCause(new InvalidObjectException(msg), cause);
    }
    static InvalidObjectException invalidObjectException(Throwable cause) {
        return invalidObjectException(cause.getMessage(), cause);
    }
    static OpenDataException openDataException(String msg, Throwable cause) {
        return EnvHelp.initCause(new OpenDataException(msg), cause);
    }
    static OpenDataException openDataException(Throwable cause) {
        return openDataException(cause.getMessage(), cause);
    }
    static void mustBeComparable(Class<?> collection, Type element)
            throws OpenDataException {
        if (!(element instanceof Class<?>)
            || !Comparable.class.isAssignableFrom((Class<?>) element)) {
            final String msg =
                "Parameter class " + element + " of " +
                collection.getName() + " does not implement " +
                Comparable.class.getName();
            throw new OpenDataException(msg);
        }
    }
    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        int offset1 = Character.offsetByCodePoints(name, 0, 1);
        if (offset1 < name.length() &&
                Character.isUpperCase(name.codePointAt(offset1)))
            return name;
        return name.substring(0, offset1).toLowerCase() +
               name.substring(offset1);
    }
    static String capitalize(String name) {
        if (name == null || name.length() == 0)
            return name;
        int offset1 = name.offsetByCodePoints(0, 1);
        return name.substring(0, offset1).toUpperCase() +
               name.substring(offset1);
    }
    public static String propertyName(Method m) {
        String rest = null;
        String name = m.getName();
        if (name.startsWith("get"))
            rest = name.substring(3);
        else if (name.startsWith("is") && m.getReturnType() == boolean.class)
            rest = name.substring(2);
        if (rest == null || rest.length() == 0
            || m.getParameterTypes().length > 0
            || m.getReturnType() == void.class
            || name.equals("getClass"))
            return null;
        return rest;
    }
    private final static Map<Type, Type> inProgress = newIdentityHashMap();
}
