@TestTargetClass(GenericArrayType.class) 
public class GenericArrayTypeTest extends GenericReflectionTestsBase {
    static class A<T> {
        T[] array;
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Missing tests for TypeNotPresentException, MalformedParameterizedTypeException",
        method = "getGenericComponentType",
        args = {}
    )
    public void testGetGenericComponentType() throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends A> clazz = GenericArrayTypeTest.A.class;
        Field field = clazz.getDeclaredField("array");
        Type genericType = field.getGenericType();
        assertInstanceOf(GenericArrayType.class, genericType);
        Type componentType = ((GenericArrayType) genericType).getGenericComponentType();
        assertEquals(getTypeParameter(clazz), componentType);
        assertInstanceOf(TypeVariable.class, componentType);
        TypeVariable<?> componentTypeVariable = (TypeVariable<?>) componentType;
        assertEquals("T", componentTypeVariable.getName());
        assertEquals(clazz, componentTypeVariable.getGenericDeclaration());
    }
    static class B<T> {
        B<T>[] array;
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Missing tests for TypeNotPresentException, MalformedParameterizedTypeException",
        method = "getGenericComponentType",
        args = {}
    )
    public void testParameterizedComponentType() throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends B> clazz = GenericArrayTypeTest.B.class;
        Field field = clazz.getDeclaredField("array");
        Type genericType = field.getGenericType();
        assertInstanceOf(GenericArrayType.class, genericType);
        GenericArrayType arrayType = (GenericArrayType) genericType;
        Type componentType = arrayType.getGenericComponentType();
        assertInstanceOf(ParameterizedType.class, componentType);
        ParameterizedType parameteriezdType = (ParameterizedType) componentType;
        assertEquals(clazz, parameteriezdType.getRawType());
        assertEquals(clazz.getTypeParameters()[0], parameteriezdType.getActualTypeArguments()[0]);
    }
}
