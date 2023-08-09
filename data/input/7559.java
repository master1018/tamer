public class ClassTyp<T1,T2> extends Tester {
    public static void main(String[] args) {
        (new ClassTyp()).run();
    }
    static class C1<S> extends AbstractSet<S> implements Set<S> {
        class C2<R> {
        }
        static class C3<R> {
            class C4<Q> {
            }
        }
        public Iterator<S> iterator() {
            return null;
        }
        public int size() {
            return 0;
        }
    }
    private C1<T1> f0;
    private C1<String> f1;
    private C1 f2;
    private C1.C3<T2> f3;
    private C1<T1>.C2<T2> f4;
    private C1.C2 f5;
    private C1<T1> f6;
    private C1.C3<T2>.C4<T1> f7;
    private static final int NUMTYPES = 8;
    private ClassType[] t = new ClassType[NUMTYPES];
    private ClassTyp<T1,T2> me = this;
    protected void init() {
        for (int i = 0; i < t.length; i++) {
            t[i] = (ClassType) getField("f"+i).getType();
        }
    }
    @Test(result="class")
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
        return t[0].equals(t[6]);
    }
    @Test(result={
              "ClassTyp.C1<T1>",
              "ClassTyp.C1<java.lang.String>",
              "ClassTyp.C1",
              "ClassTyp.C1.C3<T2>",
              "ClassTyp.C1<T1>.C2<T2>",
              "ClassTyp.C1.C2",
              "ClassTyp.C1<T1>",
              "ClassTyp.C1.C3<T2>.C4<T1>"
          },
          ordered=true)
    Collection<String> toStringTests() {
        Collection<String> res = new ArrayList<String>();
        for (ClassType c : t) {
            res.add(c.toString());
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
    @Test(result={"T2"})
    Collection<TypeMirror> getActualTypeArguments3() {
        return t[3].getActualTypeArguments();
    }
    @Test(result="null")
    DeclaredType getContainingType1() {
        ClassType thisType = (ClassType) getField("me").getType();
        return thisType.getContainingType();
    }
    @Test(result="ClassTyp")
    DeclaredType getContainingType2() {
        return t[0].getContainingType();
    }
    @Test(result="ClassTyp.C1")
    DeclaredType getContainingType3() {
        return t[3].getContainingType();
    }
    @Test(result="ClassTyp.C1<T1>")
    DeclaredType getContainingType4() {
        return t[4].getContainingType();
    }
    @Test(result={"java.util.Set<T1>"})
    Collection<InterfaceType> getSuperinterfaces() {
        return t[0].getSuperinterfaces();
    }
    @Test(result="ClassTyp.C1<S>")
    ClassDeclaration getDeclaration1() {
        return t[0].getDeclaration();
    }
    @Test(result="ClassTyp.C1.C3<R>")
    ClassDeclaration getDeclaration2() {
        return t[3].getDeclaration();
    }
    @Test(result="ClassTyp.C1<S>.C2<R>")
    ClassDeclaration getDeclaration3a() {
        return t[4].getDeclaration();
    }
    @Test(result="ClassTyp.C1<S>.C2<R>")
    ClassDeclaration getDeclaration3b() {
        return t[5].getDeclaration();
    }
    @Test(result="true")
    boolean getDeclarationEq() {
        return t[0].getDeclaration() == t[6].getDeclaration();
    }
    @Test(result="java.util.AbstractSet<T1>")
    ClassType getSuperclass1() {
        return t[0].getSuperclass();
    }
    @Test(result="java.lang.Object")
    ClassType getSuperclass2() {
        return t[4].getSuperclass();
    }
    @Test(result="null")
    ClassType getSuperclassOfObject() {
        return t[4].getSuperclass().getSuperclass();
    }
}
