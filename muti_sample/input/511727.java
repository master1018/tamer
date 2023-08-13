public abstract class ConvertWildcardTest extends AbstractConvertTest {
    @Test
    public void convertWildcardUpperBound() throws IOException {
        String source = 
            "package a; " +
            "public class A{" +
            "  public java.util.Set<? extends Number> f; "+
            "}";
            IApi api = convert(new CompilationUnit("a.A", source));
            IPackage sigPackage = ModelUtil.getPackage(api, "a");
            IClassDefinition sigClass = ModelUtil.getClass(sigPackage, "A");
            IField field = ModelUtil.getField(sigClass, "f");
            ITypeReference type = field.getType();
            assertTrue(type instanceof IParameterizedType);
            IParameterizedType parametrizedType = (IParameterizedType)type;
            IClassDefinition rawType = parametrizedType.getRawType().getClassDefinition();
            assertEquals("Set", rawType.getName());
            assertEquals(1, parametrizedType.getTypeArguments().size());
            IWildcardType wildcardType = (IWildcardType) parametrizedType.getTypeArguments().get(0);
            assertEquals(1, wildcardType.getUpperBounds().size());
            ITypeReference upperBound = wildcardType.getUpperBounds().get(0);
            assertTrue(upperBound instanceof IClassReference);
            assertEquals("Number", ((IClassReference)upperBound).getClassDefinition().getName());
    }    
    @Test
    public void convertWildcardLowerBound() throws IOException {
        String source = 
        "package a; " +
        "public class A{" +
        "  public java.util.Set<? super Number> f; "+
        "}";
        IApi api = convert(new CompilationUnit("a.A", source));
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        IClassDefinition sigClass = ModelUtil.getClass(sigPackage, "A");
        IField field = ModelUtil.getField(sigClass, "f");
        ITypeReference type = field.getType();
        assertTrue(type instanceof IParameterizedType);
        IParameterizedType parametrizedType = (IParameterizedType)type;
        IClassDefinition rawType = parametrizedType.getRawType().getClassDefinition();
        assertEquals("Set", rawType.getName());
        assertEquals(1, parametrizedType.getTypeArguments().size());
        IWildcardType wildcardType = (IWildcardType) parametrizedType.getTypeArguments().get(0);
        assertEquals(1, wildcardType.getUpperBounds().size());
        ITypeReference upperBound = wildcardType.getUpperBounds().get(0);
        assertTrue(upperBound instanceof IClassReference);
        assertEquals("Object", ((IClassReference)upperBound).getClassDefinition().getName());
        ITypeReference lowerBound = wildcardType.getLowerBound();
        assertTrue(lowerBound instanceof IClassReference);
        assertEquals("Number", ((IClassReference)lowerBound).getClassDefinition().getName());
    }
}
