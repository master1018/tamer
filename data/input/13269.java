public class GetAnno extends Tester {
    public static void main(String[] args) {
        (new GetAnno()).run();
    }
    @Retention(RUNTIME)
    @interface AT1 {
        long l();
        String s();
        RetentionPolicy e();
        String[] sa();
        AT2 a();
    }
    @Inherited
    @interface AT2 {
    }
    @interface AT3 {
        Class value() default String.class;
    }
    @interface AT4 {
        boolean[] bs();
        long[] ls();
        String[] ss();
        RetentionPolicy[] es();
        AT2[] as();
    }
    @Test(result="@GetAnno$AT1(l=7, s=sigh, e=CLASS, sa=[in, out], " +
                              "a=@GetAnno$AT2())")
    @AT1(l=7, s="sigh", e=CLASS, sa={"in", "out"}, a=@AT2)
    public Annotation getAnnotation() {
        MethodDeclaration m = getMethod("getAnnotation");
        AT1 a = m.getAnnotation(AT1.class);
        if (a.l() != 7 || !a.s().equals("sigh") || a.e() != CLASS)
            throw new AssertionError();
        return a;
    }
    @Test(result="null")
    public Annotation getAnnotationNotThere() {
        return thisClassDecl.getAnnotation(Deprecated.class);
    }
    @Test(result="@GetAnno$AT4(bs=[true, false], " +
                              "ls=[9, 8], " +
                              "ss=[black, white], " +
                              "es=[CLASS, SOURCE], " +
                              "as=[@GetAnno$AT2(), @GetAnno$AT2()])")
    @AT4(bs={true, false},
         ls={9, 8},
         ss={"black", "white"},
         es={CLASS, SOURCE},
         as={@AT2, @AT2})
    public AT4 getAnnotationArrayValues() {
        MethodDeclaration m = getMethod("getAnnotationArrayValues");
        return m.getAnnotation(AT4.class);
    }
    @Test(result="@GetAnno$AT3(value=java.lang.String)")
    @AT3(String.class)
    public AT3 getAnnotationWithClass1() {
        MethodDeclaration m = getMethod("getAnnotationWithClass1");
        return m.getAnnotation(AT3.class);
    }
    @Test(result="java.lang.String")
    public TypeMirror getAnnotationWithClass2() {
        AT3 a = getAnnotationWithClass1();
        try {
            Class c = a.value();
            throw new AssertionError();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
    @Test(result="boolean")
    @AT3(boolean.class)
    public TypeMirror getAnnotationWithPrim() {
        MethodDeclaration m = getMethod("getAnnotationWithPrim");
        AT3 a = m.getAnnotation(AT3.class);
        try {
            Class c = a.value();
            throw new AssertionError();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
    @Test(result="null")
    public AT2 getInheritedAnnotation() {
        return thisClassDecl.getAnnotation(AT2.class);
    }
    @Test(result="true")
    @AT1(l=7, s="sigh", e=CLASS, sa={"in", "out"}, a=@AT2)
    public boolean getAnnotationHashCode() {
        MethodDeclaration m1 = getMethod("getAnnotationHashCode");
        AT1 a1 = m1.getAnnotation(AT1.class);
        java.lang.reflect.Method m2 = null;
        try {
            m2 = this.getClass().getMethod("getAnnotationHashCode");
        } catch (NoSuchMethodException e) {
            assert false;
        }
        AT1 a2 = m2.getAnnotation(AT1.class);
        return a1.hashCode() == a2.hashCode();
    }
}
