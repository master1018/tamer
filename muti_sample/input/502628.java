public abstract class ConvertEnumTest extends AbstractConvertTest {
    @Test
    public void testEnum1() throws IOException {
        IApi api = convert(new CompilationUnit("test.A", 
                "package test; " +
                "public enum A {" +
                "  ONE, TWO, THREE" +
                "}"));
        IPackage sigPackage = ModelUtil.getPackage(api, "test");
        IClassDefinition c = ModelUtil.getClass(sigPackage, "A");
        assertNotNull(c);
        assertTrue(c.getKind() == Kind.ENUM);
        Set<IEnumConstant> constants = c.getEnumConstants();
        assertEquals(3, constants.size());
        Set<String> constantNames = new HashSet<String>();
        for (IEnumConstant constant : constants) {
            constantNames.add(constant.getName());
        }
        assertTrue(constantNames.contains("ONE"));
        assertTrue(constantNames.contains("TWO"));
        assertTrue(constantNames.contains("THREE"));
    }
    @Test
    public void testEnum2() throws IOException {
        IApi api = convert(new CompilationUnit("test.A", 
                "package test; " +
                "public enum A {" +
                "  ONE, TWO, THREE;" +
                "  public static A FOUR = ONE;" +
                "}"));
        IPackage sigPackage = ModelUtil.getPackage(api, "test");
        IClassDefinition c = ModelUtil.getClass(sigPackage, "A");
        assertNotNull(c);
        assertTrue(c.getKind() == Kind.ENUM);
        Set<IEnumConstant> constants = c.getEnumConstants();
        assertEquals(3, constants.size());
        Set<IField> fields = c.getFields();
        assertEquals(1, fields.size());
        IField field = c.getFields().iterator().next();
        assertEquals("FOUR", field.getName());
        assertSame(c, ((IClassReference)field.getType()).getClassDefinition());
    }
   @Test
    public void testEnum3() throws IOException {
        IApi api = convert(new CompilationUnit("test.A", 
                "package test; " +
                "public enum A {" +
                "  ONE(1), TWO(2), THREE(3);" +
                "  A(int value){}" +
                "}"));
        IPackage sigPackage = ModelUtil.getPackage(api, "test");
        IClassDefinition c = ModelUtil.getClass(sigPackage, "A");
        assertNotNull(c);
        assertTrue(c.getKind() == Kind.ENUM);
        Set<IEnumConstant> constants = c.getEnumConstants();
        assertEquals(3, constants.size());
        Set<IConstructor> ctors = c.getConstructors();
        assertEquals(0, ctors.size());
        Set<IMethod> methods = c.getMethods();
        assertEquals(2, methods.size());
        Map<String, IMethod> map = new HashMap<String, IMethod>();
        for(IMethod m : methods){
            map.put(m.getName(), m);
        }
        IMethod values = map.get("values");
        assertNotNull(values);
        assertEquals(0, values.getParameters().size());
        assertTrue(values.getReturnType() instanceof IArrayType);
        assertSame(c, ((IClassReference)((IArrayType)values.getReturnType()).getComponentType()).getClassDefinition());
        assertTrue(c.getModifiers().contains(Modifier.PUBLIC));
        assertFalse(c.getModifiers().contains(Modifier.STATIC));
        assertTrue(c.getModifiers().contains(Modifier.FINAL));
        IMethod valueOf = map.get("valueOf");
        assertNotNull(valueOf);
        assertEquals(1, valueOf.getParameters().size());
        IParameter param = valueOf.getParameters().iterator().next();
        assertEquals("java.lang.String", ((IClassReference)param.getType()).getClassDefinition().getQualifiedName());
        assertSame(c, ((IClassReference)valueOf.getReturnType()).getClassDefinition());
    }
   @Test
   public void testEnum4() throws IOException {
       IApi api = convert(new CompilationUnit("test.A", 
               "package test; " +
               "public enum A {" +
               "  ONE { void m(){} }, TWO{ void m(){} };" +
               "  abstract void m();" +
               "}"));
       IPackage sigPackage = ModelUtil.getPackage(api, "test");
       IClassDefinition c = ModelUtil.getClass(sigPackage, "A");
       assertNotNull(c);
       assertTrue(c.getKind() == Kind.ENUM);
       Set<IEnumConstant> constants = c.getEnumConstants();
       assertEquals(2, constants.size());
       Set<IConstructor> ctors = c.getConstructors();
       assertEquals(0, ctors.size());
       Set<IMethod> methods = c.getMethods();
       assertEquals(2, methods.size());
       Map<String, IMethod> map = new HashMap<String, IMethod>();
       for(IMethod m : methods){
           map.put(m.getName(), m);
       }
       IMethod values = map.get("values");
       assertNotNull(values);
       assertEquals(0, values.getParameters().size());
       assertTrue(values.getReturnType() instanceof IArrayType);
       assertSame(c, ((IClassReference)((IArrayType)values.getReturnType()).getComponentType()).getClassDefinition());
       assertTrue(c.getModifiers().contains(Modifier.PUBLIC));
       assertFalse(c.getModifiers().contains(Modifier.STATIC));
       assertFalse(c.getModifiers().contains(Modifier.FINAL));
   }
}
