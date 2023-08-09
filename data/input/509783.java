public class DexTestsCommon {
    protected JavaSourceToDexUtil javaToDexUtil;
    @Before
    public void initializeJavaToDexUtil() {
        javaToDexUtil = new JavaSourceToDexUtil();
    }
    @After
    public void shutdownJavaToDexUtil() {
        javaToDexUtil = null;
    }
    public static void assertPublic(WithModifiers withMod) {
        assertTrue(Modifier.isPublic(withMod.getModifiers()));
        assertFalse(Modifier.isPrivate(withMod.getModifiers())
                || Modifier.isProtected(withMod.getModifiers()));
    }
    public static void assertProtected(WithModifiers withMod) {
        assertTrue(Modifier.isProtected(withMod.getModifiers()));
        assertFalse(Modifier.isPrivate(withMod.getModifiers())
                || Modifier.isPublic(withMod.getModifiers()));
    }
    public static void assertPrivate(WithModifiers withMod) {
        assertTrue(Modifier.isPrivate(withMod.getModifiers()));
        assertFalse(Modifier.isProtected(withMod.getModifiers())
                || Modifier.isPublic(withMod.getModifiers()));
    }
    public static void assertDefault(WithModifiers withMod) {
        assertFalse(Modifier.isPrivate(withMod.getModifiers())
                || Modifier.isProtected(withMod.getModifiers())
                || Modifier.isPublic(withMod.getModifiers()));
    }
    protected DexFile prepareDexFile(String fileName) throws IOException{
        DexFileReader dexReader = new DexFileReader();
        return  dexReader.read(new DexBuffer(fileName));
    }
    protected DexClass getClass(DexFile file, String className) {
        assertNotNull(file);
        assertNotNull(className);
        for (DexClass clazz : file.getDefinedClasses()) {
            if(className.equals(clazz.getName())){
                return clazz;
            }
        }
        fail("Class: " + className +" not present in file: " + file.getName());
        throw new IllegalArgumentException("Class: " + className +" not present in file: " + file.getName());
    }
    protected DexField getField(DexClass clazz, String fieldName) {
        assertNotNull(clazz);
        assertNotNull(fieldName);
        for (DexField field : clazz.getFields()) {
            if(fieldName.equals(field.getName())){
                return field;
            }
        }
        fail("Field: " + fieldName +" not present in class: " + clazz.getName());
        throw new IllegalArgumentException("Field: " + fieldName +" not present in class: " + clazz.getName());
    }
    protected DexAnnotation getAnnotation(DexAnnotatedElement element, String annotationType) {
        assertNotNull(element);
        assertNotNull(annotationType);
        for (DexAnnotation anno : element.getAnnotations()) {
            if(annotationType.equals(anno.getTypeName())){
                return anno;
            }
        }
        fail("Annotation: " + annotationType +" not present in Element.");
        throw new IllegalArgumentException("Annotation: " + annotationType +" not present in Element.");
    }
    protected DexMethod getMethod(DexClass clazz, String methodName, String... typeNames) {
        assertNotNull(clazz);
        assertNotNull(methodName);
        List<String> paramTypeNames = Arrays.asList(typeNames);
        for (DexMethod method : clazz.getMethods()) {
            List<String> methodsParamTypeNames = getParamTypeNames(method.getParameters());
            if(methodName.equals(method.getName()) && paramTypeNames.equals(methodsParamTypeNames)){
                return method;
            }
        }
        fail("Method: " + methodName +" not present in class: " + clazz.getName());
        throw new IllegalArgumentException("Method: " + methodName +" not present in class: " + clazz.getName());
    }
    private List<String> getParamTypeNames(List<DexParameter> parameters) {
        List<String> paramTypeNames = new LinkedList<String>();
        for (DexParameter parameter : parameters) {
            paramTypeNames.add(parameter.getTypeName());
        }
        return paramTypeNames;
    }
}
