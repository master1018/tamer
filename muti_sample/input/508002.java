@TestTargetClass(Method.class)
public class GenericMethodsTests extends GenericReflectionTestsBase{
    static class GenericMethods {
        public <T> void noParamNoReturn() {}
        public <T> void paramNoReturn(T param) {}
        @SuppressWarnings("unchecked")
        public <T> T noParamReturn() { return (T) new Object(); }
        public <T> T paramReturn(T param) {return param;}
    }
    private static Class<? extends GenericMethods> clazz = GenericMethodsTests.GenericMethods.class;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getTypeParameters",
        args = {}
    )
    public void testGenericMethods() {
        assertLenghtZero(clazz.getTypeParameters());
    }
    private void checkTypeParameter(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertEquals("T", typeParameter.getName());
        assertEquals(method, typeParameter.getGenericDeclaration());
    }
    private void checkParameterType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertLenghtOne(method.getGenericParameterTypes());
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertEquals(typeParameter, genericParameterType);
        assertInstanceOf(TypeVariable.class, genericParameterType);
        assertEquals(method, ((TypeVariable<?>) genericParameterType).getGenericDeclaration());
    }
    private void checkReturnType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        Type genericReturnType = method.getGenericReturnType();
        assertEquals(typeParameter, genericReturnType);
        assertInstanceOf(TypeVariable.class, genericReturnType);
        assertEquals(method, ((TypeVariable<?>) genericReturnType).getGenericDeclaration());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getTypeParameters",
        args = {}
    )
    public void testNoParamNoReturn() throws Exception {
        Method method = clazz.getMethod("noParamNoReturn");
        checkTypeParameter(method);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getGenericParameterTypes",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getParameterTypes",
            args = {}
        )
    })
    public void testParamNoReturn() throws Exception {
        Method method = clazz.getMethod("paramNoReturn", Object.class);
        checkTypeParameter(method);
        checkParameterType(method);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getGenericParameterTypes",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getGenericReturnType",
            args = {}
        )
    })
    public void testNoParamReturn() throws Exception {
        Method method = clazz.getMethod("noParamReturn");
        checkTypeParameter(method);
        assertLenghtZero(method.getGenericParameterTypes());
        checkReturnType(method);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getParameterTypes",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getGenericReturnType",
            args = {}
        )
    })
    public void testParamReturn() throws Exception {
        Method method = clazz.getMethod("paramReturn", Object.class);
        checkTypeParameter(method);
        checkParameterType(method);
        checkReturnType(method);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "getTypeParameters",
        args = {}
    )
    public void testIndependencyOfMethodTypeParameters() throws Exception {
        Method method0 = clazz.getMethod("paramNoReturn", Object.class);
        TypeVariable<Method> typeParameter0 = method0.getTypeParameters()[0];
        Method method1 = clazz.getMethod("noParamNoReturn");
        TypeVariable<Method> typeParameter1 = method1.getTypeParameters()[0];
        assertEquals(typeParameter0.getName(), typeParameter1.getName());
        assertNotEquals(typeParameter0, typeParameter1);
    }
}
