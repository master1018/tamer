public class GenericReflectionTestsBase extends TestCase{
    public TypeVariable<Method> getTypeParameter(Method method) {
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Method> typeParameter = typeParameters[0];
        return typeParameter;
    }
    @SuppressWarnings("unchecked")
    public TypeVariable<Class> getTypeParameter(Class<?> clazz) {
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Class> typeVariable = typeParameters[0];
        assertEquals(clazz, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName()); 
        return typeVariable;
    }
    public static void assertLenghtOne(Object[] array) {
        TestCase.assertEquals("Array does NOT contain exactly one element.", 1, array.length);
    }
    public static void assertLenghtZero(Object[] array) {
        TestCase.assertEquals("Array has more than zero elements.", 0, array.length);
    }
    public static void assertInstanceOf(Class<?> expectedClass, Object actual) {
        TestCase.assertTrue(actual.getClass().getName() + " is not instance of :" + expectedClass.getName(), expectedClass
                .isInstance(actual));
    }
    public static void assertNotEquals(Object expected, Object actual) {
        TestCase.assertFalse(actual.toString() + " has not to be equal to " + expected.toString(), expected.equals(actual));
    }
}
