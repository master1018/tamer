@TestTargetClass(Method.class) 
public class BoundedGenericMethodsTests extends GenericReflectionTestsBase {
    @SuppressWarnings("unchecked")
    static class BoundedGenericMethods<S> {
        public <T extends BoundedGenericMethods> void noParamNoReturn() {}
        public <T extends BoundedGenericMethods> void paramNoReturn(T param) {}
        public <T extends BoundedGenericMethods> T noParamReturn() {
            return (T) new Object();
        }
        public <T extends BoundedGenericMethods> T paramReturn(T t) {
            return t;
        }
    }
    @SuppressWarnings("unchecked")
    private static Class<? extends BoundedGenericMethods> clazz = BoundedGenericMethodsTests.BoundedGenericMethods.class;
    private void checkBoundedTypeParameter(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertEquals("T", typeParameter.getName());
        assertEquals(method, typeParameter.getGenericDeclaration());
        Type[] bounds = typeParameter.getBounds();
        assertLenghtOne(bounds);
        Type bound = bounds[0];
        assertEquals(BoundedGenericMethods.class, bound);
    }
    private void parameterType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertLenghtOne(method.getGenericParameterTypes());
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertEquals(typeParameter, genericParameterType);
        assertTrue(genericParameterType instanceof TypeVariable);
        TypeVariable<?> typeVariable = (TypeVariable<?>) genericParameterType;
        assertEquals(method, typeVariable.getGenericDeclaration());
        Type[] paramBounds = typeVariable.getBounds();
        assertLenghtOne(paramBounds);
        Type paramBound = paramBounds[0];
        assertEquals(BoundedGenericMethods.class, paramBound);
    }
    @SuppressWarnings("unchecked")
    private void checkReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        assertEquals(getTypeParameter(method), genericReturnType);
        assertTrue(genericReturnType instanceof TypeVariable);
        TypeVariable<Method> returnTypeVariable = (TypeVariable<Method>) genericReturnType;
        assertEquals(method, returnTypeVariable.getGenericDeclaration());
        Type[] bounds = returnTypeVariable.getBounds();
        assertLenghtOne(bounds);
        Type bound = bounds[0];
        assertEquals(BoundedGenericMethods.class, bound);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check GenericSignatureFormatError.",
        method = "getTypeParameters",
        args = {}
    )
    public void testBoundedGenericMethods() {
        assertLenghtOne(clazz.getTypeParameters());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check GenericSignatureFormatError.",
        method = "getTypeParameters",
        args = {}
    )
    public void testNoParamNoReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("noParamNoReturn");
        checkBoundedTypeParameter(method);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check GenericSignatureFormatError.",
        method = "getTypeParameters",
        args = {}
    )
    public void testUnboundedParamNoReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("paramNoReturn", BoundedGenericMethods.class);
        checkBoundedTypeParameter(method);
        parameterType(method);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check GenericSignatureFormatError.",
        method = "getTypeParameters",
        args = {}
    )
    public void testNoParamReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("noParamReturn");
        checkBoundedTypeParameter(method);
        assertLenghtZero(method.getGenericParameterTypes());
        checkReturnType(method);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check GenericSignatureFormatError.",
        method = "getTypeParameters",
        args = {}
    )
    public void testUnboundedParamReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("paramReturn", BoundedGenericMethods.class);
        checkBoundedTypeParameter(method);
        parameterType(method);
        checkReturnType(method);
    }
}
