public class T6412656 extends ToolTester {
    int count = 0;
    void test(String... args) {
        task = tool.getTask(null, fm, null, null,
                            Collections.singleton(T6412656.class.getName()), null);
        task.setProcessors(Collections.singleton(new MyProc(this)));
        task.call();
        if (count == 0)
            throw new AssertionError("Annotation processor not run");
        System.out.println("OK");
    }
    public static void main(String... args) {
        new T6412656().test(args);
    }
    @SupportedAnnotationTypes("*")
    static class MyProc extends AbstractProcessor {
        T6412656 test;
        MyProc(T6412656 test) {
            this.test = test;
        }
        public boolean process(Set<? extends TypeElement> annotations,
                               RoundEnvironment roundEnv) {
            test.count++;
            return false;
        }
    }
}
