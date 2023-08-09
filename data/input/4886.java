public class InterfaceTyp<T1,T2> extends Tester {
    public static void main(String[] args) {
        (new InterfaceTyp()).run();
    }
    interface I1<S> extends Set<String> {
        interface I2<R> {
        }
    }
    private I1<T1> f0;
    private I1<String> f1;
    private I1 f2;
    private I1.I2<String> f3;
    private I1.I2 f4;
    private I1<T1> f5;
    private I3<T1> f6;
    private static final int NUMTYPES = 7;
    private InterfaceType[] t = new InterfaceType[NUMTYPES];
    protected void init() {
        for (int i = 0; i < t.length; i++) {
            t[i] = (InterfaceType) getField("f"+i).getType();
        }
    }
    @Test(result="interface")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        t[0].accept(new SimpleTypeVisitor() {
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
            public void visitClassType(ClassType t) {
                res.add("class");
            }
            public void visitInterfaceType(InterfaceType t) {
                res.add("interface");
            }
        });
        return res;
    }
    @Test(result="true")
    boolean equals1() {
        return t[0].equals(t[0]);
    }
    @Test(result="false")
    boolean equals2() {
        return t[0].equals(t[1]);
    }
    @Test(result="false")
    boolean equals3() {
        return t[0].equals(t[2]);
    }
    @Test(result="true")
    boolean equals4() {
        return t[0].equals(t[5]);
    }
    @Test(result={
              "InterfaceTyp.I1<T1>",
              "InterfaceTyp.I1<java.lang.String>",
              "InterfaceTyp.I1",
              "InterfaceTyp.I1.I2<java.lang.String>",
              "InterfaceTyp.I1.I2",
              "InterfaceTyp.I1<T1>",
              "I3<T1>"
          },
          ordered=true)
    Collection<String> toStringTests() {
        Collection<String> res = new ArrayList<String>();
        for (InterfaceType i : t) {
            res.add(i.toString());
        }
        return res;
    }
    @Test(result={"T1"})
    Collection<TypeMirror> getActualTypeArguments1() {
        return t[0].getActualTypeArguments();
    }
    @Test(result={})
    Collection<TypeMirror> getActualTypeArguments2() {
        return t[2].getActualTypeArguments();
    }
    @Test(result={"java.lang.String"})
    Collection<TypeMirror> getActualTypeArguments3() {
        return t[3].getActualTypeArguments();
    }
    @Test(result="InterfaceTyp")
    DeclaredType getContainingType1() {
        return t[0].getContainingType();
    }
    @Test(result="InterfaceTyp.I1")
    DeclaredType getContainingType2() {
        return t[3].getContainingType();
    }
    @Test(result="null")
    DeclaredType getContainingTypeTopLevel() {
        return t[6].getContainingType();
    }
    @Test(result={"java.util.Set<java.lang.String>"})
    Collection<InterfaceType> getSuperinterfaces() {
        return t[0].getSuperinterfaces();
    }
    @Test(result="InterfaceTyp.I1<S>")
    InterfaceDeclaration getDeclaration1() {
        return t[0].getDeclaration();
    }
    @Test(result="InterfaceTyp.I1.I2<R>")
    InterfaceDeclaration getDeclaration2a() {
        return t[3].getDeclaration();
    }
    @Test(result="InterfaceTyp.I1.I2<R>")
    InterfaceDeclaration getDeclaration2b() {
        return t[4].getDeclaration();
    }
    @Test(result="true")
    boolean getDeclarationCaching() {
        return t[0].getDeclaration() == t[5].getDeclaration();
    }
}
interface I3<T> {
}
