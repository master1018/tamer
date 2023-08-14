public class AnnoVal extends Tester {
    public static void main(String[] args) {
        (new AnnoVal()).run();
    }
    @Test(result={
        "i Integer 2",
        "l Long 4294967296",
        "d Double 3.14",
        "b Boolean true",
        "c Character @",
        "s String sigh",
        "k ClassTypeImpl java.lang.Boolean",
        "kb PrimitiveTypeImpl boolean",
        "ka ArrayTypeImpl java.lang.Boolean[]",
        "kab ArrayTypeImpl int[][]",
        "w ClassTypeImpl java.lang.Long",
        "e EnumConstantDeclarationImpl TYPE",
        "sa ArrayList [\"up\", \"down\"]",
        "a AnnotationMirrorImpl @AT1"})
    @AT2(i = 1 + 1,
         l = 1024 * 1024 * 1024 * 4L,
         d = 3.14,
         b = true,
         c = '@',
         s = "sigh",
         k = Boolean.class,
         kb = boolean.class,
         ka = Boolean[].class,          
         kab = int[][].class,           
         w = Long.class,
         e = java.lang.annotation.ElementType.TYPE,
         sa = {"up", "down"},
         a = @AT1)
    Collection<String> getValue() {
        Collection<String> res = new ArrayList<String>();
        AnnotationMirror anno = getAnno("getValue", "AT2");
        for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> e :
                 anno.getElementValues().entrySet()) {
            Object val = e.getValue().getValue();
            res.add(String.format("%s %s %s",
                                  e.getKey().getSimpleName(),
                                  simpleClassName(val),
                                  val));
        }
        return res;
    }
    @Test(result={
        "int i 2",
        "long l 4294967296L",
        "double d 3.14",
        "boolean b true",
        "char c '@'",
        "java.lang.String s \"sigh\"",
        "java.lang.Class k java.lang.Boolean.class",
        "java.lang.Class kb boolean.class",
        "java.lang.Class ka java.lang.Boolean[].class",
        "java.lang.Class kab int[][].class",
        "java.lang.Class<? extends java.lang.Number> w java.lang.Long.class",
        "java.lang.annotation.ElementType e java.lang.annotation.ElementType.TYPE",
        "java.lang.String[] sa {\"up\", \"down\"}",
        "AT1 a @AT1"})
    Collection<String> toStringTests() {
        Collection<String> res = new ArrayList<String>();
        AnnotationMirror anno = getAnno("getValue", "AT2");
        for (Map.Entry<AnnotationTypeElementDeclaration,AnnotationValue> e :
                 anno.getElementValues().entrySet()) {
            res.add(String.format("%s %s %s",
                                  e.getKey().getReturnType(),
                                  e.getKey().getSimpleName(),
                                  e.getValue().toString()));
        }
        return res;
    }
    @Test(result={
        "byte b 0x0b",
        "float f 3.0f",
        "double nan 0.0/0.0",
        "double hi 1.0/0.0",
        "float lo -1.0f/0.0f",
        "char newline '\\n'",
        "char ff '\\u00ff'",
        "java.lang.String s \"\\\"high\\tlow\\\"\"",
        "java.lang.String smiley \"\\u263a\""})
    @AT3(b = 11,
         f = 3,
         nan = 0.0/0.0,
         hi = 1.0/0.0,
         lo = -1.0f/0.0f,
         newline = '\n',
         ff = '\u00FF',
         s = "\"high\tlow\"",
         smiley = "\u263A")
    Collection<String> toStringFancy() {
        Collection<String> res = new ArrayList<String>();
        AnnotationMirror anno = getAnno("toStringFancy", "AT3");
        for (Map.Entry<AnnotationTypeElementDeclaration,AnnotationValue> e :
                 anno.getElementValues().entrySet()) {
            res.add(String.format("%s %s %s",
                                  e.getKey().getReturnType(),
                                  e.getKey().getSimpleName(),
                                  e.getValue().toString()));
        }
        return res;
    }
    private String simpleClassName(Object o) {
        return (o == null)
            ? "null"
            : o.getClass().getName().replaceFirst(".*\\.", "");
    }
}
@interface AT1 {
    String value() default "";
}
@interface AT2 {
    int i();
    long l();
    double d();
    boolean b();
    char c();
    String s();
    Class k();
    Class kb();
    Class ka();
    Class kab();
    Class<? extends Number> w();
    java.lang.annotation.ElementType e();
    String[] sa();
    AT1 a();
}
@interface AT3 {
    byte b();
    float f();
    double nan();
    double hi();
    float lo();
    char newline();
    char ff();
    String s();
    String smiley();
}
