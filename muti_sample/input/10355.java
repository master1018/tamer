public class T6358024 extends AbstractProcessor {
    static JavacFileManager fm;
    public static void main(String... args) throws Throwable {
        String self = T6358024.class.getName();
        String testSrc = System.getProperty("test.src");
        fm = new JavacFileManager(new Context(), false, null);
        JavaFileObject f = fm.getFileForInput(testSrc + File.separatorChar + self + ".java");
        test(fm, f,
             new Option[] { new Option("-d", ".")},
             7);
        test(fm, f,
             new Option[] { new XOption("-XprintRounds"),
                            new Option("-processorpath", "."),
                            new Option("-processor", self) },
             12);
    }
    static void test(JavacFileManager fm, JavaFileObject f, Option[] opts, int expect) throws Throwable {
        PrintWriter out = new PrintWriter(System.err, true);
        JavacTool tool = JavacTool.create();
        List<String> flags = new ArrayList<String>();
        for (Option opt: opts) {
            flags.add(opt.name);
            for (Object arg : opt.args)
                flags.add(arg.toString());
        }
        JavacTaskImpl task = (JavacTaskImpl) tool.getTask(out,
                                                          fm,
                                                          null,
                                                          flags,
                                                          null,
                                                          Arrays.asList(f));
        MyTaskListener tl = new MyTaskListener();
        task.setTaskListener(tl);
        task.call();
        if (tl.started != expect)
            throw new AssertionError("Unexpected number of TaskListener events; "
                                     + "expected " + expect + ", found " + tl.started);
    }
    public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
        return true;
    }
    static class MyTaskListener implements TaskListener {
        public void started(TaskEvent e) {
            System.err.println("Started: " + e);
            started++;
        }
        public void finished(TaskEvent e) {
        }
        int started = 0;
    }
    static class Option {
        Option(String name, String... args) {
            this.name = name;
            this.args = args;
        }
        public final String name;
        public final String[] args;
    }
    static class XOption extends Option {
        XOption(String name, String... args) {
            super(name, args);
        }
    }
}
