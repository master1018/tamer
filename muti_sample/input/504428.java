public class DexUtil {
    private static final String PACKAGE_INFO = "package-info";
    private static final String THROWS_ANNOTATION =
            "Ldalvik/annotation/Throws;";
    private static final String SIGNATURE_ANNOTATION =
            "Ldalvik/annotation/Signature;";
    private static final String ANNOTATION_DEFAULT_ANNOTATION =
            "Ldalvik/annotation/AnnotationDefault;";
    private static final String ENCLOSING_CLASS_ANNOTATION =
            "Ldalvik/annotation/EnclosingClass;";
    private static final String ENCLOSING_METHOD_ANNOTATION =
            "Ldalvik/annotation/EnclosingMethod;";
    private static final String INNER_CLASS_ANNOTATION =
            "Ldalvik/annotation/InnerClass;";
    private static final String MEMBER_CLASS_ANNOTATION =
            "Ldalvik/annotation/MemberClasses;";
    private static final String JAVA_LANG_OBJECT = "Ljava/lang/Object;";
    private static final Set<String> INTERNAL_ANNOTATION_NAMES;
    static {
        Set<String> tmp = new HashSet<String>();
        tmp.add(THROWS_ANNOTATION);
        tmp.add(SIGNATURE_ANNOTATION);
        tmp.add(ANNOTATION_DEFAULT_ANNOTATION);
        tmp.add(ENCLOSING_CLASS_ANNOTATION);
        tmp.add(ENCLOSING_METHOD_ANNOTATION);
        tmp.add(INNER_CLASS_ANNOTATION);
        tmp.add(MEMBER_CLASS_ANNOTATION);
        INTERNAL_ANNOTATION_NAMES = Collections.unmodifiableSet(tmp);
    }
    private DexUtil() {
    }
    public static String getPackageName(String classIdentifier) {
        String name = removeTrailingSemicolon(removeHeadingL(classIdentifier));
        return ModelUtil.getPackageName(name.replace("/", "."));
    }
    public static String getClassName(String classIdentifier) {
        String name = removeTrailingSemicolon(removeHeadingL(classIdentifier));
        return ModelUtil.getClassName(name.replace("/", ".")).replace('$', '.');
    }
    public static String getQualifiedName(String classIdentifier) {
        String name = removeTrailingSemicolon(removeHeadingL(classIdentifier));
        return name.replace('/', '.');
    }
    private static String removeHeadingL(String className) {
        assert className.startsWith("L");
        return className.substring(1);
    }
    private static String removeTrailingSemicolon(String className) {
        assert className.endsWith(";");
        return className.substring(0, className.length() - 1);
    }
    public static String getDexName(String packageName, String className) {
        return "L" + packageName.replace('.', '/') + "/"
                + className.replace('.', '$') + ";";
    }
    public static String getDexName(IClassDefinition sigClass) {
        return getDexName(sigClass.getPackageName(), sigClass.getName());
    }
    public static int getClassModifiers(DexClass clazz) {
        int modifiers = 0;
        if (isInnerClass(clazz)) {
            Integer accessFlags = (Integer) getAnnotationAttributeValue(
                    getAnnotation(clazz, INNER_CLASS_ANNOTATION),
                            "accessFlags");
            modifiers = accessFlags.intValue();
        } else {
            modifiers = clazz.getModifiers();
        }
        return modifiers;
    }
    public static Set<Modifier> getModifier(int mod) {
        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (java.lang.reflect.Modifier.isAbstract(mod))
            modifiers.add(Modifier.ABSTRACT);
        if (java.lang.reflect.Modifier.isFinal(mod))
            modifiers.add(Modifier.FINAL);
        if (java.lang.reflect.Modifier.isPrivate(mod))
            modifiers.add(Modifier.PRIVATE);
        if (java.lang.reflect.Modifier.isProtected(mod))
            modifiers.add(Modifier.PROTECTED);
        if (java.lang.reflect.Modifier.isPublic(mod))
            modifiers.add(Modifier.PUBLIC);
        if (java.lang.reflect.Modifier.isStatic(mod))
            modifiers.add(Modifier.STATIC);
        if (java.lang.reflect.Modifier.isVolatile(mod))
            modifiers.add(Modifier.VOLATILE);
        return modifiers;
    }
    public static boolean isEnum(DexClass dexClass) {
        return (getClassModifiers(dexClass) & 0x4000) > 0;
    }
    public static boolean isInterface(DexClass dexClass) {
        int modifiers = getClassModifiers(dexClass);
        return java.lang.reflect.Modifier.isInterface(modifiers);
    }
    public static boolean isAnnotation(DexClass dexClass) {
        return (getClassModifiers(dexClass) & 0x2000) > 0;
    }
    public static boolean isSynthetic(int modifier) {
        return (modifier & 0x1000) > 0;
    }
    public static Kind getKind(DexClass dexClass) {
        if (isEnum(dexClass)) {
            return Kind.ENUM;
        } else if (isAnnotation(dexClass)) {
            return Kind.ANNOTATION;
        } else if (isInterface(dexClass)) {
            return Kind.INTERFACE;
        } else {
            return Kind.CLASS;
        }
    }
    public static boolean declaresExceptions(
            DexAnnotatedElement annotatedElement) {
        return getAnnotation(annotatedElement, THROWS_ANNOTATION) != null;
    }
    @SuppressWarnings("unchecked")
    public static String getExceptionSignature(
            DexAnnotatedElement annotatedElement) {
        DexAnnotation annotation = getAnnotation(annotatedElement,
                THROWS_ANNOTATION);
        if (annotation != null) {
            List<DexEncodedValue> value =
                    (List<DexEncodedValue>) getAnnotationAttributeValue(
                            annotation, "value");
            return concatEncodedValues(value);
        }
        return null;
    }
    public static Set<String> splitTypeList(String typeList) {
        String[] split = typeList.split(";");
        Set<String> separateTypes = new HashSet<String>();
        for (String string : split) {
            separateTypes.add(string + ";");
        }
        return separateTypes;
    }
    public static boolean hasGenericSignature(
            DexAnnotatedElement annotatedElement) {
        return getAnnotation(annotatedElement, SIGNATURE_ANNOTATION) != null;
    }
    @SuppressWarnings("unchecked")
    public static String getGenericSignature(
            DexAnnotatedElement annotatedElement) {
        DexAnnotation annotation = getAnnotation(annotatedElement,
                SIGNATURE_ANNOTATION);
        if (annotation != null) {
            List<DexEncodedValue> value =
                    (List<DexEncodedValue>) getAnnotationAttributeValue(
                            annotation, "value");
            return concatEncodedValues(value);
        }
        return null;
    }
    public static boolean hasAnnotationDefaultSignature(
            DexAnnotatedElement annotatedElement) {
        return getAnnotation(
                annotatedElement, ANNOTATION_DEFAULT_ANNOTATION)!= null;
    }
    public static DexAnnotation getDefaultMappingsAnnotation(
            DexClass dexClass) {
        return getAnnotation(dexClass, ANNOTATION_DEFAULT_ANNOTATION);
    }
    public static DexAnnotation getAnnotation(DexAnnotatedElement element,
            String annotationType) {
        assert element != null;
        assert annotationType != null;
        for (DexAnnotation anno : element.getAnnotations()) {
            if (annotationType.equals(anno.getTypeName())) {
                return anno;
            }
        }
        return null;
    }
    public static Object getAnnotationAttributeValue(DexAnnotation annotation,
            String attributeName) {
        for (DexAnnotationAttribute dexAnnotationAttribute : annotation
                .getAttributes()) {
            if (attributeName.equals(dexAnnotationAttribute.getName())) {
                return dexAnnotationAttribute.getEncodedValue().getValue();
            }
        }
        return null;
    }
    private static String concatEncodedValues(List<DexEncodedValue> values) {
        StringBuilder builder = new StringBuilder();
        for (DexEncodedValue string : values) {
            builder.append(string.getValue());
        }
        return builder.toString();
    }
    public static boolean isConstructor(DexMethod method) {
        return "<init>".equals(method.getName());
    }
    public static boolean isStaticConstructor(DexMethod method) {
        return "<clinit>".equals(method.getName());
    }
    public static boolean isMethod(DexMethod method) {
        return !isConstructor(method) && !isStaticConstructor(method);
    }
    public static IClassDefinition findPackageInfo(SigPackage aPackage) {
        for (IClassDefinition clazz : aPackage.getClasses()) {
            if (PACKAGE_INFO.equals(clazz.getName())) {
                return clazz;
            }
        }
        return null;
    }
    public static boolean isPackageInfo(DexClass clazz) {
        return PACKAGE_INFO.equals(getClassName(clazz.getName()));
    }
    public static boolean isInternalAnnotation(DexAnnotation dexAnnotation) {
        return INTERNAL_ANNOTATION_NAMES.contains(dexAnnotation.getTypeName());
    }
    public static boolean isInnerClass(DexClass clazz) {
        return getAnnotation(clazz, INNER_CLASS_ANNOTATION) != null;
    }
    public static boolean isEnclosingClass(DexClass clazz) {
        return getAnnotation(clazz, ENCLOSING_CLASS_ANNOTATION) != null;
    }
    public static boolean declaresMemberClasses(DexClass dexClass) {
        return getAnnotation(dexClass, MEMBER_CLASS_ANNOTATION) != null;
    }
    @SuppressWarnings("unchecked")
    public static Set<String> getMemberClassNames(DexClass dexClass) {
        DexAnnotation annotation = getAnnotation(dexClass,
                MEMBER_CLASS_ANNOTATION);
        List<DexEncodedValue> enclosedClasses =
                (List<DexEncodedValue>) getAnnotationAttributeValue(
                        annotation, "value");
        Set<String> enclosedClassesNames = new HashSet<String>();
        for (DexEncodedValue string : enclosedClasses) {
            enclosedClassesNames.add((String) string.getValue());
        }
        return enclosedClassesNames;
    }
    public static String getEnclosingClassName(DexClass dexClass) {
        DexAnnotation annotation = getAnnotation(dexClass,
                ENCLOSING_CLASS_ANNOTATION);
        String value = (String) getAnnotationAttributeValue(annotation,
                "value");
        return value;
    }
    public static boolean convertAnyWay(DexClass dexClass) {
        return !isSynthetic(getClassModifiers(dexClass))
                && !isAnonymousClassName(dexClass.getName())
                || isPackageInfo(dexClass);
    }
    public static boolean isVisible(DexClass dexClass, Visibility visibility) {
        if (isPackageInfo(dexClass)) {
            return true;
        }
        if (isDeclaredInMethod(dexClass)) {
            return false;
        }
        if (isAnonymousClassName(dexClass.getName())) {
            return false;
        }
        int modifiers = getClassModifiers(dexClass);
        return isVisible(modifiers, visibility);
    }
    private static boolean isDeclaredInMethod(DexClass dexClass) {
        return getAnnotation(dexClass, ENCLOSING_METHOD_ANNOTATION) != null;
    }
    public static boolean isAnonymousClassName(String dexName) {
        int index = dexName.lastIndexOf('$');
        return (index != 0) ? Character.isDigit(dexName.charAt(index + 1))
                : false;
    }
    public static boolean isVisible(DexField dexField, Visibility visibility) {
        return isVisible(dexField.getModifiers(), visibility);
    }
    public static boolean isVisible(DexMethod dexMethod,
            Visibility visibility) {
        return isVisible(dexMethod.getModifiers(), visibility);
    }
    private static boolean isVisible(int modifiers, Visibility visibility) {
        if (isSynthetic(modifiers)) {
            return false;
        }
        Set<Modifier> elementModifiers = getModifier(modifiers);
        if (elementModifiers.contains(Modifier.PUBLIC)) {
            return true;
        } else if (elementModifiers.contains(Modifier.PROTECTED)) {
            return visibility == Visibility.PROTECTED
                    || visibility == Visibility.PACKAGE
                    || visibility == Visibility.PRIVATE;
        } else if (elementModifiers.contains(Modifier.PRIVATE)) {
            return visibility == Visibility.PRIVATE;
        } else {
            return visibility == Visibility.PACKAGE
                    || visibility == Visibility.PRIVATE;
        }
    }
    public static Set<DexFile> getDexFiles(Set<String> fileNames)
            throws IOException {
        Set<DexFile> parsedFiles = new HashSet<DexFile>();
        for (String dexFile : fileNames) {
            DexFileReader reader = new DexFileReader();
            DexBuffer dexBuffer = new DexBuffer(dexFile);
            parsedFiles.add(reader.read(dexBuffer));
        }
        return parsedFiles;
    }
    public static boolean isJavaLangObject(DexClass dexClass) {
        assert dexClass != null;
        return JAVA_LANG_OBJECT.equals(dexClass.getName());
    }
}
