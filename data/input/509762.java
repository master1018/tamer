@TestTargetClass(ParameterizedType.class) 
public class ParameterizedTypeTest extends GenericReflectionTestsBase {
    static class A<T>{}
    static class B extends A<String>{}
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getActualTypeArguments",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getOwnerType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getRawType",
            args = {}
        )
    })
    public void testStringParameterizedSuperClass() {
        Class<? extends B> clazz = B.class;
        Type genericSuperclass = clazz.getGenericSuperclass();
        assertInstanceOf(ParameterizedType.class, genericSuperclass);
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(A.class, parameterizedType.getRawType());
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(String.class, actualTypeArguments[0]);
    }
    static class C<T>{}
    static class D<T> extends C<T>{}
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getActualTypeArguments",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getOwnerType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getRawType",
            args = {}
        )
    })
    public void testTypeParameterizedSuperClass() {
        Class<? extends D> clazz = D.class;
        Type genericSuperclass = clazz.getGenericSuperclass();
        assertInstanceOf(ParameterizedType.class, genericSuperclass);
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(C.class, parameterizedType.getRawType());
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(getTypeParameter(D.class), actualTypeArguments[0]);
    }
    static class E<T>{}
    static class F<T>{
        E<T> e;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getActualTypeArguments",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getOwnerType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
            method = "getRawType",
            args = {}
        )
    })
    public void testParameterizedMemeber() throws Exception{
        Class<? extends F> clazz = F.class;
        Field field = clazz.getDeclaredField("e");
        assertInstanceOf(ParameterizedType.class, field.getGenericType());
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(E.class, parameterizedType.getRawType());
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(getTypeParameter(clazz), actualTypeArguments[0]);
    }
}
