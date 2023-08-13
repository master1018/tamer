public class Overrides extends Tester {
    public static void main(String[] args) {
        (new Overrides()).run();
    }
    static class A {
        void m1(int i) {};              
        void m2(int i) {};
        static void m3(int i) {};
    }
        static class B extends A {
            void m1(int j) {};          
            void m1(String i) {};       
            void m4(int i) {};          
        }
            static class C extends B {
                void m1(int i) {};      
                void m2(int i) {};      
            }
        static class D extends A {
            static void m3(int i) {};   
        }
    static class E {
        void m1(int i) {};              
    }
    private Declarations decls;
    private TypeDeclaration A;
    private TypeDeclaration B;
    private TypeDeclaration C;
    private TypeDeclaration D;
    private TypeDeclaration E;
    private MethodDeclaration Am1;
    private MethodDeclaration Am2;
    private MethodDeclaration Am3;
    private MethodDeclaration Bm1;
    private MethodDeclaration Bm1b;
    private MethodDeclaration Bm4;
    private MethodDeclaration Cm1;
    private MethodDeclaration Cm2;
    private MethodDeclaration Dm3;
    private MethodDeclaration Em1;
    protected void init() {
        decls = env.getDeclarationUtils();
        A = env.getTypeDeclaration("Overrides.A");
        B = env.getTypeDeclaration("Overrides.B");
        C = env.getTypeDeclaration("Overrides.C");
        D = env.getTypeDeclaration("Overrides.D");
        E = env.getTypeDeclaration("Overrides.E");
        Am1  = getMethod(A, "m1", "i");
        Am2  = getMethod(A, "m2", "i");
        Am3  = getMethod(A, "m3", "i");
        Bm1  = getMethod(B, "m1", "j");
        Bm1b = getMethod(B, "m1", "i");
        Bm4  = getMethod(B, "m4", "i");
        Cm1  = getMethod(C, "m1", "i");
        Cm2  = getMethod(C, "m2", "i");
        Dm3  = getMethod(D, "m3", "i");
        Em1  = getMethod(E, "m1", "i");
    }
    private MethodDeclaration getMethod(TypeDeclaration t,
                                        String methodName, String paramName) {
        for (MethodDeclaration m : t.getMethods()) {
            if (methodName.equals(m.getSimpleName()) &&
                    paramName.equals(m.getParameters().iterator().next()
                                                        .getSimpleName())) {
                return m;
            }
        }
        throw new AssertionError();
    }
    @Test(result={"false",
                  "true",
                  "false",
                  "false",
                  "true",
                  "true",
                  "true",
                  "false",
                  "false"},
          ordered=true)
    List<Boolean> overrides() {
        return Arrays.asList(
                decls.overrides(Am1, Am1),
                decls.overrides(Bm1, Am1),
                decls.overrides(Bm1b,Am1),
                decls.overrides(Bm4, Am1),
                decls.overrides(Cm1, Am1),
                decls.overrides(Cm1, Bm1),
                decls.overrides(Cm2, Am2),
                decls.overrides(Dm3, Am3),
                decls.overrides(Em1, Am1));
    }
}
