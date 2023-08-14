public abstract class ConvertParameterizedTypeTest extends AbstractConvertTest {
    @Test
    public void convertParameterizedType() throws IOException {
        String source = 
        "package a; " +
        "public class A{" +
        "  public class B<T> {} " +
        "  public A.B<Integer> f; "+
        "}";
        IApi api = convert(new CompilationUnit("a.A", source));
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        IClassDefinition sigClass = ModelUtil.getClass(sigPackage, "A");
        IField field = ModelUtil.getField(sigClass, "f");
        ITypeReference type = field.getType();
        assertTrue(type instanceof IParameterizedType);
        IParameterizedType parametrizedType = (IParameterizedType)type;
        ITypeReference ownerType = parametrizedType.getOwnerType();
        assertNotNull(ownerType);
    }
    @Test
    public void convertWildcardLowerBound() throws IOException {
        String clazz = 
        "package a; " +
        "public final class A<T> implements I<T>{ " +
        " abstract class Super{} " +
        " final class Sub extends Super implements I<T>{} " +
        "}";
        String interfaze = 
            "package a; " +
            "public interface I <T>{}";
        IApi api = convert(Visibility.PRIVATE, new CompilationUnit("a.A", clazz),new CompilationUnit("a.I", interfaze));
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        IClassDefinition sigClass = ModelUtil.getClass(sigPackage, "A.Sub");
        System.out.println(sigClass);
    }
    public class A{
      public class B<T> {}
      public A.B<Integer> f;
    }
    @Test
    public void reflectionTest0() throws SecurityException, NoSuchFieldException{
        Field field = A.class.getDeclaredField("f");
        ParameterizedType paramType = (ParameterizedType)field.getGenericType();
        assertNotNull(paramType.getOwnerType());
    }
    public static class C<T>{}
    ConvertParameterizedTypeTest.C<String> f;
      @Test
      public void reflectionTest1() throws SecurityException, NoSuchFieldException{
          Field field = ConvertParameterizedTypeTest.class.getDeclaredField("f");
          ParameterizedType paramType = (ParameterizedType)field.getGenericType();
          assertNotNull(paramType.getOwnerType());
      }
      public static class E<T>{
          static class F<Q>{}
          E.F<String> f;
      }
     @Test
    public void reflectionTest2() throws SecurityException, NoSuchFieldException {
        Field field = E.class.getDeclaredField("f");
        ParameterizedType paramType = (ParameterizedType) field.getGenericType();
        assertNotNull(paramType.getOwnerType());
    }
}
