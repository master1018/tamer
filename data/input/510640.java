public class JDiffClassDescription {
    private static final int CLASS_MODIFIER_ANNOTATION = 0x00002000;
    private static final int CLASS_MODIFIER_ENUM       = 0x00004000;
    private static final int METHOD_MODIFIER_BRIDGE    = 0x00000040;
    private static final int METHOD_MODIFIER_VAR_ARGS  = 0x00000080;
    private static final int METHOD_MODIFIER_SYNTHETIC = 0x00001000;
    public enum JDiffType {
        INTERFACE, CLASS
    }
    @SuppressWarnings("unchecked")
    private Class<?> mClass;
    private String mPackageName;
    private String mShortClassName;
    private String mAbsoluteClassName;
    private int mModifier;
    private String mExtendedClass;
    private List<String> implInterfaces = new ArrayList<String>();
    private List<JDiffField> jDiffFields = new ArrayList<JDiffField>();
    private List<JDiffMethod> jDiffMethods = new ArrayList<JDiffMethod>();
    private List<JDiffConstructor> jDiffConstructors = new ArrayList<JDiffConstructor>();
    private ResultObserver mResultObserver;
    private JDiffType mClassType;
    public JDiffClassDescription(String pkg, String className) {
        this(pkg, className, new ResultObserver() {
            public void notifyFailure(SignatureTestActivity.FAILURE_TYPE type,
                    String name,
                    String errorMessage) {
            }
        });
    }
    public JDiffClassDescription(String pkg,
            String className,
            ResultObserver resultObserver) {
        mPackageName = pkg;
        mShortClassName = className;
        mResultObserver = resultObserver;
    }
    public void addImplInterface(String iname) {
        implInterfaces.add(iname);
    }
    public void addField(JDiffField field) {
        jDiffFields.add(field);
    }
    public void addMethod(JDiffMethod method) {
        jDiffMethods.add(method);
    }
    public void addConstructor(JDiffConstructor tc) {
        jDiffConstructors.add(tc);
    }
    static String convertModifiersToAccessLevel(int modifiers) {
        String accessLevel = "";
        if ((modifiers & Modifier.PUBLIC) != 0) {
            return "public";
        } else if ((modifiers & Modifier.PRIVATE) != 0) {
            return "private";
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            return "protected";
        } else {
            return "";
        }
    }
    static String convertModifersToModifierString(int modifiers) {
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        if ((modifiers & Modifier.ABSTRACT) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("abstract");
        }
        if ((modifiers & Modifier.STATIC) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("static");
        }
        if ((modifiers & Modifier.FINAL) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("final");
        }
        if ((modifiers & Modifier.TRANSIENT) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("transient");
        }
        if ((modifiers & Modifier.VOLATILE) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("volatile");
        }
        if ((modifiers & Modifier.SYNCHRONIZED) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("synchronized");
        }
        if ((modifiers & Modifier.NATIVE) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("native");
        }
        if ((modifiers & Modifier.STRICT) != 0) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" ");
            }
            sb.append("strictfp");
        }
        return sb.toString();
    }
    public abstract static class JDiffElement {
        final String mName;
        int mModifier;
        public JDiffElement(String name, int modifier) {
            mName = name;
            mModifier = modifier;
        }
    }
    public static final class JDiffField extends JDiffElement {
        private String mFieldType;
        public JDiffField(String name, String fieldType, int modifier) {
            super(name, modifier);
            mFieldType = fieldType;
        }
        public String toReadableString(String className) {
            return className + "#" + mName + "(" + mFieldType + ")";
        }
        public String toSignatureString() {
            StringBuffer sb = new StringBuffer();
            String accesLevel = convertModifiersToAccessLevel(mModifier);
            if (!"".equals(accesLevel)) {
                sb.append(accesLevel).append(" ");
            }
            String modifierString = convertModifersToModifierString(mModifier);
            if (!"".equals(modifierString)) {
                sb.append(modifierString).append(" ");
            }
            sb.append(mFieldType).append(" ");
            sb.append(mName);
            return sb.toString();
        }
    }
    public static class JDiffMethod extends JDiffElement {
        protected String mReturnType;
        protected ArrayList<String> mParamList;
        protected ArrayList<String> mExceptionList;
        public JDiffMethod(String name, int modifier, String returnType) {
            super(name, modifier);
            if (returnType == null) {
                mReturnType = "void";
            } else {
                mReturnType = scrubJdiffParamType(returnType);
            }
            mParamList = new ArrayList<String>();
            mExceptionList = new ArrayList<String>();
        }
        public void addParam(String param) {
            mParamList.add(scrubJdiffParamType(param));
        }
        public void addException(String exceptionName) {
            mExceptionList.add(exceptionName);
        }
        public String toReadableString(String className) {
            return className + "#" + mName + "(" + convertParamList(mParamList) + ")";
        }
        private static String convertParamList(final ArrayList<String> params) {
            StringBuffer paramList = new StringBuffer();
            if (params != null) {
                for (String str : params) {
                    paramList.append(str + ", ");
                }
                if (params.size() > 0) {
                    paramList.delete(paramList.length() - 2, paramList.length());
                }
            }
            return paramList.toString();
        }
        public String toSignatureString() {
            StringBuffer sb = new StringBuffer();
            String accesLevel = convertModifiersToAccessLevel(mModifier);
            if (!"".equals(accesLevel)) {
                sb.append(accesLevel).append(" ");
            }
            String modifierString = convertModifersToModifierString(mModifier);
            if (!"".equals(modifierString)) {
                sb.append(modifierString).append(" ");
            }
            String returnType = getReturnType();
            if (!"".equals(returnType)) {
                sb.append(returnType).append(" ");
            }
            sb.append(mName);
            sb.append("(");
            for (int x = 0; x < mParamList.size(); x++) {
                sb.append(mParamList.get(x));
                if (x + 1 != mParamList.size()) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            if (mExceptionList.size() > 0) {
                sb.append(" throws ");
                for (int x = 0; x < mExceptionList.size(); x++) {
                    sb.append(mExceptionList.get(x));
                    if (x + 1 != mExceptionList.size()) {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
        protected String getReturnType() {
            return mReturnType;
        }
    }
    public static final class JDiffConstructor extends JDiffMethod {
        public JDiffConstructor(String name, int modifier) {
            super(name, modifier, null);
        }
        public JDiffConstructor(String name, String[] param, int modifier) {
            super(name, modifier, null);
            for (int i = 0; i < param.length; i++) {
                addParam(param[i]);
            }
        }
        @Override
        protected String getReturnType() {
            return "";
        }
    }
    public void checkSignatureCompliance() {
        checkClassCompliance();
        if (mClass != null) {
            checkFieldsCompliance();
            checkConstructorCompliance();
            checkMethodCompliance();
        }
    }
    private boolean areMethodModifiedCompatibile(JDiffMethod apiMethod ,
            Method reflectedMethod) {
        if (((apiMethod.mModifier & Modifier.SYNCHRONIZED) == 0) &&
                ((reflectedMethod.getModifiers() & Modifier.SYNCHRONIZED) != 0)) {
            return false;
        }
        int mod1 = reflectedMethod.getModifiers() & ~(Modifier.NATIVE | Modifier.SYNCHRONIZED);
        int mod2 = apiMethod.mModifier & ~(Modifier.NATIVE | Modifier.SYNCHRONIZED);
        if ((mModifier & Modifier.FINAL) != 0) {
            mod1 &= ~Modifier.FINAL;
            mod2 &= ~Modifier.FINAL;
        }
        return mod1 == mod2;
    }
    private void checkMethodCompliance() {
        for (JDiffMethod method : jDiffMethods) {
            try {
                if (JDiffType.INTERFACE.equals(mClassType)) {
                    method.mModifier |= Modifier.ABSTRACT;
                }
                Method m = findMatchingMethod(method);
                if (m == null) {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISSING_METHOD,
                            method.toReadableString(mAbsoluteClassName),
                            "No method with correct signature found:" +
                            method.toSignatureString());
                } else {
                    if (m.isVarArgs()) {
                        method.mModifier |= METHOD_MODIFIER_VAR_ARGS;
                    }
                    if (m.isBridge()) {
                        method.mModifier |= METHOD_MODIFIER_BRIDGE;
                    }
                    if (m.isSynthetic()) {
                        method.mModifier |= METHOD_MODIFIER_SYNTHETIC;
                    }
                    if (mClass.isEnum() && method.mName.equals("values")) {
                        return;
                    }
                    if (!areMethodModifiedCompatibile(method, m)) {
                        mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISMATCH_METHOD,
                                method.toReadableString(mAbsoluteClassName),
                                "Non-compatible method found when looking for " +
                                method.toSignatureString());
                    }
                }
            } catch (Exception e) {
                SignatureTestLog.e("Got exception when checking method compliance", e);
                mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.CAUGHT_EXCEPTION,
                        method.toReadableString(mAbsoluteClassName),
                "Exception!");
            }
        }
    }
    private boolean matches(JDiffMethod jDiffMethod, Method method) {
        if (jDiffMethod.mName.equals(method.getName())) {
            String jdiffReturnType = jDiffMethod.mReturnType;
            String reflectionReturnType = typeToString(method.getGenericReturnType());
            List<String> jdiffParamList = jDiffMethod.mParamList;
            if (jdiffReturnType.equals(reflectionReturnType)) {
                Type[] params = method.getGenericParameterTypes();
                if (jdiffParamList.size() == params.length) {
                    for (int i = 0; i < jdiffParamList.size(); i++) {
                        if (!compareParam(jdiffParamList.get(i), params[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private Method findMatchingMethod(JDiffMethod method) {
        Method[] methods = mClass.getDeclaredMethods();
        boolean found = false;
        for (Method m : methods) {
            if (matches(method, m)) {
                return m;
            }
        }
        return null;
    }
    private static boolean compareParam(String jdiffParam, Type reflectionParamType) {
        if (jdiffParam == null) {
            return false;
        }
        String reflectionParam = typeToString(reflectionParamType);
        if (jdiffParam.equals(reflectionParam)) {
            return true;
        }
        int jdiffParamEndOffset = jdiffParam.indexOf("...");
        int reflectionParamEndOffset = reflectionParam.indexOf("[]");
        if (jdiffParamEndOffset != -1 && reflectionParamEndOffset != -1) {
            jdiffParam = jdiffParam.substring(0, jdiffParamEndOffset);
            reflectionParam = reflectionParam.substring(0, reflectionParamEndOffset);
            return jdiffParam.equals(reflectionParam);
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private void checkConstructorCompliance() {
        for (JDiffConstructor con : jDiffConstructors) {
            try {
                Constructor<?> c = findMatchingConstructor(con);
                if (c == null) {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISSING_METHOD,
                            con.toReadableString(mAbsoluteClassName),
                            "No method with correct signature found:" +
                            con.toSignatureString());
                } else {
                    if (c.isVarArgs()) {
                        con.mModifier |= METHOD_MODIFIER_VAR_ARGS;
                    }
                    if (c.getModifiers() != con.mModifier) {
                        mResultObserver.notifyFailure(
                                SignatureTestActivity.FAILURE_TYPE.MISMATCH_METHOD,
                                con.toReadableString(mAbsoluteClassName),
                                "Non-compatible method found when looking for " +
                                con.toSignatureString());
                    }
                }
            } catch (Exception e) {
                SignatureTestLog.e("Got exception when checking constructor compliance", e);
                mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.CAUGHT_EXCEPTION,
                        con.toReadableString(mAbsoluteClassName),
                "Exception!");
            }
        }
    }
    @SuppressWarnings("unchecked")
    private Constructor<?> findMatchingConstructor(JDiffConstructor jdiffDes) {
        for (Constructor<?> c : mClass.getDeclaredConstructors()) {
            Type[] params = c.getGenericParameterTypes();
            boolean isStaticClass = ((mClass.getModifiers() & Modifier.STATIC) != 0);
            int startParamOffset = 0;
            int numberOfParams = params.length;
            if (mClass.isMemberClass() && !isStaticClass && params.length >= 1) {
                startParamOffset = 1;
                --numberOfParams;
            }
            ArrayList<String> jdiffParamList = jdiffDes.mParamList;
            if (jdiffParamList.size() == numberOfParams) {
                boolean isFound = true;
                int i = 0;
                int j = startParamOffset;
                while (i < jdiffParamList.size()) {
                    if (!compareParam(jdiffParamList.get(i), params[j])) {
                        isFound = false;
                        break;
                    }
                    ++i;
                    ++j;
                }
                if (isFound) {
                    return c;
                }
            }
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private void checkFieldsCompliance() {
        for (JDiffField field : jDiffFields) {
            try {
                Field f = findMatchingField(field);
                if (f == null) {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISSING_FIELD,
                            field.toReadableString(mAbsoluteClassName),
                            "No field with correct signature found:" +
                            field.toSignatureString());
                } else if (f.getModifiers() != field.mModifier) {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISMATCH_FIELD,
                            field.toReadableString(mAbsoluteClassName),
                            "Non-compatible field modifiers found when looking for " +
                            field.toSignatureString());
                } else if (!f.getType().getCanonicalName().equals(field.mFieldType)) {
                    String genericTypeName = null;
                    Type type = f.getGenericType();
                    if (type != null) {
                        genericTypeName = type instanceof Class ? ((Class) type).getName() :
                            type.toString();
                    }
                    if (genericTypeName == null || !genericTypeName.equals(field.mFieldType)) {
                        mResultObserver.notifyFailure(
                                SignatureTestActivity.FAILURE_TYPE.MISMATCH_FIELD,
                                field.toReadableString(mAbsoluteClassName),
                                "Non-compatible field type found when looking for " +
                                field.toSignatureString());
                    }
                }
            } catch (Exception e) {
                SignatureTestLog.e("Got exception when checking field compliance", e);
                mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.CAUGHT_EXCEPTION,
                        field.toReadableString(mAbsoluteClassName),
                "Exception!");
            }
        }
    }
    private Field findMatchingField(JDiffField field){
        Field[] fields = mClass.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals(field.mName)) {
                return f;
            }
        }
        return null;
    }
    private boolean checkClassModifiersCompliance() {
        int reflectionModifier = mClass.getModifiers();
        int apiModifier = mModifier;
        if (((apiModifier & Modifier.ABSTRACT) == 0) &&
                ((reflectionModifier & Modifier.ABSTRACT) != 0) &&
                !isEnumType()) {
            return false;
        }
        reflectionModifier &= ~Modifier.ABSTRACT;
        apiModifier &= ~Modifier.ABSTRACT;
        if (isAnnotation()) {
            reflectionModifier &= ~CLASS_MODIFIER_ANNOTATION;
        }
        if (mClass.isInterface()) {
            reflectionModifier &= ~(Modifier.INTERFACE);
        }
        if (isEnumType() && mClass.isEnum()) {
            reflectionModifier &= ~CLASS_MODIFIER_ENUM;
        }
        return ((reflectionModifier == apiModifier) &&
                (isEnumType() == mClass.isEnum()));
    }
    private boolean checkClassAnnotationCompliace() {
        if (mClass.isAnnotation()) {
            for (String inter : implInterfaces) {
                if ("java.lang.annotation.Annotation".equals(inter)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    private boolean checkClassExtendsCompliance() {
        if (mExtendedClass != null) {
            Class<?> superClass = mClass.getSuperclass();
            if (superClass == null) {
                return false;
            }
            if (superClass.getCanonicalName().equals(mExtendedClass)) {
                return true;
            }
            if (mAbsoluteClassName.equals("android.hardware.SensorManager")) {
                return true;
            }
            return false;
        }
        return true;
    }
    private boolean checkClassImplementsCompliance() {
        Class<?>[] interfaces = mClass.getInterfaces();
        Set<String> interFaceSet = new HashSet<String>();
        for (Class<?> c : interfaces) {
            interFaceSet.add(c.getCanonicalName());
        }
        for (String inter : implInterfaces) {
            if (!interFaceSet.contains(inter)) {
                return false;
            }
        }
        return true;
    }
    @SuppressWarnings("unchecked")
    private void checkClassCompliance() {
        try {
            mAbsoluteClassName = mPackageName + "." + mShortClassName;
            mClass = findMatchingClass();
            if (mClass == null) {
                if (JDiffType.INTERFACE.equals(mClassType)) {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISSING_INTERFACE,
                            mAbsoluteClassName,
                            "Classloader is unable to find " + mAbsoluteClassName);
                } else {
                    mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISSING_CLASS,
                            mAbsoluteClassName,
                            "Classloader is unable to find " + mAbsoluteClassName);
                }
                return;
            }
            if (!checkClassModifiersCompliance()) {
                logMismatchInterfaceSignature(mAbsoluteClassName,
                        "Non-compatible class found when looking for " +
                        toSignatureString());
                return;
            }
            if (!checkClassAnnotationCompliace()) {
                logMismatchInterfaceSignature(mAbsoluteClassName,
                "Annotation mismatch");
                return;
            }
            if (!mClass.isAnnotation()) {
                if (!checkClassExtendsCompliance()) {
                    logMismatchInterfaceSignature(mAbsoluteClassName,
                    "Extends mismatch");
                    return;
                }
                if (!checkClassImplementsCompliance()) {
                    logMismatchInterfaceSignature(mAbsoluteClassName,
                    "Implements mismatch");
                    return;
                }
            }
        } catch (Exception e) {
            SignatureTestLog.e("Got exception when checking field compliance", e);
            mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.CAUGHT_EXCEPTION,
                    mAbsoluteClassName,
            "Exception!");
        }
    }
    public String toSignatureString() {
        StringBuffer sb = new StringBuffer();
        String accessLevel = convertModifiersToAccessLevel(mModifier);
        if (!"".equals(accessLevel)) {
            sb.append(accessLevel).append(" ");
        }
        if (!JDiffType.INTERFACE.equals(mClassType)) {
            String modifierString = convertModifersToModifierString(mModifier);
            if (!"".equals(modifierString)) {
                sb.append(modifierString).append(" ");
            }
            sb.append("class ");
        } else {
            sb.append("interface ");
        }
        sb.append(mShortClassName);
        if (mExtendedClass != null) {
            sb.append(" extends ").append(mExtendedClass).append(" ");
        }
        if (implInterfaces.size() > 0) {
            sb.append(" implements ");
            for (int x = 0; x < implInterfaces.size(); x++) {
                String interf = implInterfaces.get(x);
                sb.append(interf);
                if (x + 1 != implInterfaces.size()) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }
    private void logMismatchInterfaceSignature(String classFullName, String errorMessage) {
        if (JDiffType.INTERFACE.equals(mClassType)) {
            mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISMATCH_INTERFACE,
                    classFullName,
                    errorMessage);
        } else {
            mResultObserver.notifyFailure(SignatureTestActivity.FAILURE_TYPE.MISMATCH_CLASS,
                    classFullName,
                    errorMessage);
        }
    }
    private boolean isEnumType() {
        return "java.lang.Enum".equals(mExtendedClass);
    }
    @SuppressWarnings("unchecked")
    private Class<?> findMatchingClass() {
        String[] classNameParts = mShortClassName.split("\\.");
        String currentName = mPackageName + "." + classNameParts[0];
        try {
            Class<?> clz = Class.forName(currentName,
                    false,
                    this.getClass().getClassLoader());
            if (clz.getCanonicalName().equals(mAbsoluteClassName)) {
                return clz;
            }
            for (int x = 1; x < classNameParts.length; x++) {
                clz = findInnerClassByName(clz, classNameParts[x]);
                if (clz == null) {
                    return null;
                }
                if (clz.getCanonicalName().equals(mAbsoluteClassName)) {
                    return clz;
                }
            }
        } catch (ClassNotFoundException e) {
            SignatureTestLog.e("ClassNotFoundException for " + mPackageName + "." + mShortClassName, e);
            return null;
        }
        return null;
    }
    private Class<?> findInnerClassByName(Class<?> clz, String simpleName) {
        for (Class<?> c : clz.getClasses()) {
            if (c.getSimpleName().equals(simpleName)) {
                return c;
            }
        }
        return null;
    }
    private boolean isAnnotation() {
        if (implInterfaces.contains("java.lang.annotation.Annotation")) {
            return true;
        }
        return false;
    }
    public String getClassName() {
        return mShortClassName;
    }
    public void setModifier(int modifier) {
        mModifier = modifier;
    }
    public void setType(JDiffType type) {
        mClassType = type;
    }
    public void setExtendsClass(String extendsClass) {
        mExtendedClass = extendsClass;
    }
    public void registerResultObserver(ResultObserver resultObserver) {
        mResultObserver = resultObserver;
    }
    private static String concatWildcardTypes(Type[] types) {
        StringBuffer sb = new StringBuffer();
        int elementNum = 0;
        for (Type t : types) {
            sb.append(typeToString(t));
            if (++elementNum < types.length) {
                sb.append(" & ");
            }
        }
        return sb.toString();
    }
    private static String typeToString(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            StringBuffer sb = new StringBuffer();
            sb.append(typeToString(pt.getRawType()));
            sb.append("<");
            int elementNum = 0;
            Type[] types = pt.getActualTypeArguments();
            for (Type t : types) {
                sb.append(typeToString(t));
                if (++elementNum < types.length) {
                    sb.append(", ");
                }
            }
            sb.append(">");
            return sb.toString();
        } else if (type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).getName();
        } else if (type instanceof Class) {
            return ((Class<?>) type).getCanonicalName();
        } else if (type instanceof GenericArrayType) {
            String typeName = typeToString(((GenericArrayType) type).getGenericComponentType());
            return typeName + "[]";
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length == 0) {
                String name = "? extends " + concatWildcardTypes(wt.getUpperBounds());
                if (name.equals("? extends java.lang.Object")) {
                    return "?";
                } else {
                    return name;
                }
            } else {
                String name = concatWildcardTypes(wt.getUpperBounds()) +
                " super " +
                concatWildcardTypes(wt.getLowerBounds());
                name = name.replace("java.lang.Object", "?");
                return name;
            }
        } else {
            throw new RuntimeException("Got an unknown java.lang.Type");
        }
    }
    private static String scrubJdiffParamType(String paramType) {
        return paramType.replace("<? extends java.lang.Object>", "<?>");
    }
}
