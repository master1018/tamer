public class AnnotationParser {
    public static Map<Class<? extends Annotation>, Annotation> parseAnnotations(
                byte[] rawAnnotations,
                ConstantPool constPool,
                Class<?> container) {
        if (rawAnnotations == null)
            return Collections.emptyMap();
        try {
            return parseAnnotations2(rawAnnotations, constPool, container);
        } catch(BufferUnderflowException e) {
            throw new AnnotationFormatError("Unexpected end of annotations.");
        } catch(IllegalArgumentException e) {
            throw new AnnotationFormatError(e);
        }
    }
    private static Map<Class<? extends Annotation>, Annotation> parseAnnotations2(
                byte[] rawAnnotations,
                ConstantPool constPool,
                Class<?> container) {
        Map<Class<? extends Annotation>, Annotation> result =
            new LinkedHashMap<Class<? extends Annotation>, Annotation>();
        ByteBuffer buf = ByteBuffer.wrap(rawAnnotations);
        int numAnnotations = buf.getShort() & 0xFFFF;
        for (int i = 0; i < numAnnotations; i++) {
            Annotation a = parseAnnotation(buf, constPool, container, false);
            if (a != null) {
                Class<? extends Annotation> klass = a.annotationType();
                AnnotationType type = AnnotationType.getInstance(klass);
                if (type.retention() == RetentionPolicy.RUNTIME)
                    if (result.put(klass, a) != null)
                        throw new AnnotationFormatError(
                            "Duplicate annotation for class: "+klass+": " + a);
            }
        }
        return result;
    }
    public static Annotation[][] parseParameterAnnotations(
                    byte[] rawAnnotations,
                    ConstantPool constPool,
                    Class<?> container) {
        try {
            return parseParameterAnnotations2(rawAnnotations, constPool, container);
        } catch(BufferUnderflowException e) {
            throw new AnnotationFormatError(
                "Unexpected end of parameter annotations.");
        } catch(IllegalArgumentException e) {
            throw new AnnotationFormatError(e);
        }
    }
    private static Annotation[][] parseParameterAnnotations2(
                    byte[] rawAnnotations,
                    ConstantPool constPool,
                    Class<?> container) {
        ByteBuffer buf = ByteBuffer.wrap(rawAnnotations);
        int numParameters = buf.get() & 0xFF;
        Annotation[][] result = new Annotation[numParameters][];
        for (int i = 0; i < numParameters; i++) {
            int numAnnotations = buf.getShort() & 0xFFFF;
            List<Annotation> annotations =
                new ArrayList<Annotation>(numAnnotations);
            for (int j = 0; j < numAnnotations; j++) {
                Annotation a = parseAnnotation(buf, constPool, container, false);
                if (a != null) {
                    AnnotationType type = AnnotationType.getInstance(
                                              a.annotationType());
                    if (type.retention() == RetentionPolicy.RUNTIME)
                        annotations.add(a);
                }
            }
            result[i] = annotations.toArray(EMPTY_ANNOTATIONS_ARRAY);
        }
        return result;
    }
    private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY =
                    new Annotation[0];
    private static Annotation parseAnnotation(ByteBuffer buf,
                                              ConstantPool constPool,
                                              Class<?> container,
                                              boolean exceptionOnMissingAnnotationClass) {
        int typeIndex = buf.getShort() & 0xFFFF;
        Class<? extends Annotation> annotationClass = null;
        String sig = "[unknown]";
        try {
            try {
                sig = constPool.getUTF8At(typeIndex);
                annotationClass = (Class<? extends Annotation>)parseSig(sig, container);
            } catch (IllegalArgumentException ex) {
                annotationClass = constPool.getClassAt(typeIndex);
            }
        } catch (NoClassDefFoundError e) {
            if (exceptionOnMissingAnnotationClass)
                throw new TypeNotPresentException(sig, e);
            skipAnnotation(buf, false);
            return null;
        }
        catch (TypeNotPresentException e) {
            if (exceptionOnMissingAnnotationClass)
                throw e;
            skipAnnotation(buf, false);
            return null;
        }
        AnnotationType type = null;
        try {
            type = AnnotationType.getInstance(annotationClass);
        } catch (IllegalArgumentException e) {
            skipAnnotation(buf, false);
            return null;
        }
        Map<String, Class<?>> memberTypes = type.memberTypes();
        Map<String, Object> memberValues =
            new LinkedHashMap<String, Object>(type.memberDefaults());
        int numMembers = buf.getShort() & 0xFFFF;
        for (int i = 0; i < numMembers; i++) {
            int memberNameIndex = buf.getShort() & 0xFFFF;
            String memberName = constPool.getUTF8At(memberNameIndex);
            Class<?> memberType = memberTypes.get(memberName);
            if (memberType == null) {
                skipMemberValue(buf);
            } else {
                Object value = parseMemberValue(memberType, buf, constPool, container);
                if (value instanceof AnnotationTypeMismatchExceptionProxy)
                    ((AnnotationTypeMismatchExceptionProxy) value).
                        setMember(type.members().get(memberName));
                memberValues.put(memberName, value);
            }
        }
        return annotationForMap(annotationClass, memberValues);
    }
    public static Annotation annotationForMap(
        Class<? extends Annotation> type, Map<String, Object> memberValues)
    {
        return (Annotation) Proxy.newProxyInstance(
            type.getClassLoader(), new Class[] { type },
            new AnnotationInvocationHandler(type, memberValues));
    }
    public static Object parseMemberValue(Class<?> memberType,
                                          ByteBuffer buf,
                                          ConstantPool constPool,
                                          Class<?> container) {
        Object result = null;
        int tag = buf.get();
        switch(tag) {
          case 'e':
              return parseEnumValue((Class<? extends Enum<?>>)memberType, buf, constPool, container);
          case 'c':
              result = parseClassValue(buf, constPool, container);
              break;
          case '@':
              result = parseAnnotation(buf, constPool, container, true);
              break;
          case '[':
              return parseArray(memberType, buf, constPool, container);
          default:
              result = parseConst(tag, buf, constPool);
        }
        if (!(result instanceof ExceptionProxy) &&
            !memberType.isInstance(result))
            result = new AnnotationTypeMismatchExceptionProxy(
                result.getClass() + "[" + result + "]");
        return result;
    }
    private static Object parseConst(int tag,
                                     ByteBuffer buf, ConstantPool constPool) {
        int constIndex = buf.getShort() & 0xFFFF;
        switch(tag) {
          case 'B':
            return Byte.valueOf((byte) constPool.getIntAt(constIndex));
          case 'C':
            return Character.valueOf((char) constPool.getIntAt(constIndex));
          case 'D':
            return Double.valueOf(constPool.getDoubleAt(constIndex));
          case 'F':
            return Float.valueOf(constPool.getFloatAt(constIndex));
          case 'I':
            return Integer.valueOf(constPool.getIntAt(constIndex));
          case 'J':
            return Long.valueOf(constPool.getLongAt(constIndex));
          case 'S':
            return Short.valueOf((short) constPool.getIntAt(constIndex));
          case 'Z':
            return Boolean.valueOf(constPool.getIntAt(constIndex) != 0);
          case 's':
            return constPool.getUTF8At(constIndex);
          default:
            throw new AnnotationFormatError(
                "Invalid member-value tag in annotation: " + tag);
        }
    }
    private static Object parseClassValue(ByteBuffer buf,
                                          ConstantPool constPool,
                                          Class<?> container) {
        int classIndex = buf.getShort() & 0xFFFF;
        try {
            try {
                String sig = constPool.getUTF8At(classIndex);
                return parseSig(sig, container);
            } catch (IllegalArgumentException ex) {
                return constPool.getClassAt(classIndex);
            }
        } catch (NoClassDefFoundError e) {
            return new TypeNotPresentExceptionProxy("[unknown]", e);
        }
        catch (TypeNotPresentException e) {
            return new TypeNotPresentExceptionProxy(e.typeName(), e.getCause());
        }
    }
    private static Class<?> parseSig(String sig, Class<?> container) {
        if (sig.equals("V")) return void.class;
        SignatureParser parser = SignatureParser.make();
        TypeSignature typeSig = parser.parseTypeSig(sig);
        GenericsFactory factory = CoreReflectionFactory.make(container, ClassScope.make(container));
        Reifier reify = Reifier.make(factory);
        typeSig.accept(reify);
        Type result = reify.getResult();
        return toClass(result);
    }
    static Class<?> toClass(Type o) {
        if (o instanceof GenericArrayType)
            return Array.newInstance(toClass(((GenericArrayType)o).getGenericComponentType()),
                                     0)
                .getClass();
        return (Class)o;
    }
    private static Object parseEnumValue(Class<? extends Enum> enumType, ByteBuffer buf,
                                         ConstantPool constPool,
                                         Class<?> container) {
        int typeNameIndex = buf.getShort() & 0xFFFF;
        String typeName  = constPool.getUTF8At(typeNameIndex);
        int constNameIndex = buf.getShort() & 0xFFFF;
        String constName = constPool.getUTF8At(constNameIndex);
        if (!typeName.endsWith(";")) {
            if (!enumType.getName().equals(typeName))
            return new AnnotationTypeMismatchExceptionProxy(
                typeName + "." + constName);
        } else if (enumType != parseSig(typeName, container)) {
            return new AnnotationTypeMismatchExceptionProxy(
                typeName + "." + constName);
        }
        try {
            return  Enum.valueOf(enumType, constName);
        } catch(IllegalArgumentException e) {
            return new EnumConstantNotPresentExceptionProxy(
                (Class<? extends Enum>)enumType, constName);
        }
    }
    private static Object parseArray(Class<?> arrayType,
                                     ByteBuffer buf,
                                     ConstantPool constPool,
                                     Class<?> container) {
        int length = buf.getShort() & 0xFFFF;  
        Class<?> componentType = arrayType.getComponentType();
        if (componentType == byte.class) {
            return parseByteArray(length, buf, constPool);
        } else if (componentType == char.class) {
            return parseCharArray(length, buf, constPool);
        } else if (componentType == double.class) {
            return parseDoubleArray(length, buf, constPool);
        } else if (componentType == float.class) {
            return parseFloatArray(length, buf, constPool);
        } else if (componentType == int.class) {
            return parseIntArray(length, buf, constPool);
        } else if (componentType == long.class) {
            return parseLongArray(length, buf, constPool);
        } else if (componentType == short.class) {
            return parseShortArray(length, buf, constPool);
        } else if (componentType == boolean.class) {
            return parseBooleanArray(length, buf, constPool);
        } else if (componentType == String.class) {
            return parseStringArray(length, buf, constPool);
        } else if (componentType == Class.class) {
            return parseClassArray(length, buf, constPool, container);
        } else if (componentType.isEnum()) {
            return parseEnumArray(length, (Class<? extends Enum>)componentType, buf,
                                  constPool, container);
        } else {
            assert componentType.isAnnotation();
            return parseAnnotationArray(length, (Class <? extends Annotation>)componentType, buf,
                                        constPool, container);
        }
    }
    private static Object parseByteArray(int length,
                                  ByteBuffer buf, ConstantPool constPool) {
        byte[] result = new byte[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'B') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = (byte) constPool.getIntAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseCharArray(int length,
                                  ByteBuffer buf, ConstantPool constPool) {
        char[] result = new char[length];
        boolean typeMismatch = false;
        byte tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'C') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = (char) constPool.getIntAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseDoubleArray(int length,
                                    ByteBuffer buf, ConstantPool constPool) {
        double[] result = new  double[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'D') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = constPool.getDoubleAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseFloatArray(int length,
                                   ByteBuffer buf, ConstantPool constPool) {
        float[] result = new float[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'F') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = constPool.getFloatAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseIntArray(int length,
                                 ByteBuffer buf, ConstantPool constPool) {
        int[] result = new  int[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'I') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = constPool.getIntAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseLongArray(int length,
                                  ByteBuffer buf, ConstantPool constPool) {
        long[] result = new long[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'J') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = constPool.getLongAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseShortArray(int length,
                                   ByteBuffer buf, ConstantPool constPool) {
        short[] result = new short[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'S') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = (short) constPool.getIntAt(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseBooleanArray(int length,
                                     ByteBuffer buf, ConstantPool constPool) {
        boolean[] result = new boolean[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'Z') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = (constPool.getIntAt(index) != 0);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseStringArray(int length,
                                    ByteBuffer buf,  ConstantPool constPool) {
        String[] result = new String[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 's') {
                int index = buf.getShort() & 0xFFFF;
                result[i] = constPool.getUTF8At(index);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseClassArray(int length,
                                          ByteBuffer buf,
                                          ConstantPool constPool,
                                          Class<?> container) {
        Object[] result = new Class<?>[length];
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'c') {
                result[i] = parseClassValue(buf, constPool, container);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseEnumArray(int length, Class<? extends Enum> enumType,
                                         ByteBuffer buf,
                                         ConstantPool constPool,
                                         Class<?> container) {
        Object[] result = (Object[]) Array.newInstance(enumType, length);
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == 'e') {
                result[i] = parseEnumValue(enumType, buf, constPool, container);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static Object parseAnnotationArray(int length,
                                               Class<? extends Annotation> annotationType,
                                               ByteBuffer buf,
                                               ConstantPool constPool,
                                               Class<?> container) {
        Object[] result = (Object[]) Array.newInstance(annotationType, length);
        boolean typeMismatch = false;
        int tag = 0;
        for (int i = 0; i < length; i++) {
            tag = buf.get();
            if (tag == '@') {
                result[i] = parseAnnotation(buf, constPool, container, true);
            } else {
                skipMemberValue(tag, buf);
                typeMismatch = true;
            }
        }
        return typeMismatch ? exceptionProxy(tag) : result;
    }
    private static ExceptionProxy exceptionProxy(int tag) {
        return new AnnotationTypeMismatchExceptionProxy(
            "Array with component tag: " + tag);
    }
    private static void skipAnnotation(ByteBuffer buf, boolean complete) {
        if (complete)
            buf.getShort();   
        int numMembers = buf.getShort() & 0xFFFF;
        for (int i = 0; i < numMembers; i++) {
            buf.getShort();   
            skipMemberValue(buf);
        }
    }
    private static void skipMemberValue(ByteBuffer buf) {
        int tag = buf.get();
        skipMemberValue(tag, buf);
    }
    private static void skipMemberValue(int tag, ByteBuffer buf) {
        switch(tag) {
          case 'e': 
            buf.getInt();  
            break;
          case '@':
            skipAnnotation(buf, true);
            break;
          case '[':
            skipArray(buf);
            break;
          default:
            buf.getShort();
        }
    }
    private static void skipArray(ByteBuffer buf) {
        int length = buf.getShort() & 0xFFFF;
        for (int i = 0; i < length; i++)
            skipMemberValue(buf);
    }
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    public static Annotation[] toArray(Map<Class<? extends Annotation>, Annotation> annotations) {
        return annotations.values().toArray(EMPTY_ANNOTATION_ARRAY);
    }
}
