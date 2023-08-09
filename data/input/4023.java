public class JavahTask implements NativeHeaderTool.NativeHeaderTask {
    public class BadArgs extends Exception {
        private static final long serialVersionUID = 1479361270874789045L;
        BadArgs(String key, Object... args) {
            super(JavahTask.this.getMessage(key, args));
            this.key = key;
            this.args = args;
        }
        BadArgs showUsage(boolean b) {
            showUsage = b;
            return this;
        }
        final String key;
        final Object[] args;
        boolean showUsage;
    }
    static abstract class Option {
        Option(boolean hasArg, String... aliases) {
            this.hasArg = hasArg;
            this.aliases = aliases;
        }
        boolean isHidden() {
            return false;
        }
        boolean matches(String opt) {
            for (String a: aliases) {
                if (a.equals(opt))
                    return true;
            }
            return false;
        }
        boolean ignoreRest() {
            return false;
        }
        abstract void process(JavahTask task, String opt, String arg) throws BadArgs;
        final boolean hasArg;
        final String[] aliases;
    }
    static abstract class HiddenOption extends Option {
        HiddenOption(boolean hasArg, String... aliases) {
            super(hasArg, aliases);
        }
        @Override
        boolean isHidden() {
            return true;
        }
    }
    static Option[] recognizedOptions = {
        new Option(true, "-o") {
            void process(JavahTask task, String opt, String arg) {
                task.ofile = new File(arg);
            }
        },
        new Option(true, "-d") {
            void process(JavahTask task, String opt, String arg) {
                task.odir = new File(arg);
            }
        },
        new HiddenOption(true, "-td") {
            void process(JavahTask task, String opt, String arg) {
            }
        },
        new HiddenOption(false, "-stubs") {
            void process(JavahTask task, String opt, String arg) {
            }
        },
        new Option(false, "-v", "-verbose") {
            void process(JavahTask task, String opt, String arg) {
                task.verbose = true;
            }
        },
        new Option(false, "-h", "-help", "--help", "-?") {
            void process(JavahTask task, String opt, String arg) {
                task.help = true;
            }
        },
        new HiddenOption(false, "-trace") {
            void process(JavahTask task, String opt, String arg) {
                task.trace = true;
            }
        },
        new Option(false, "-version") {
            void process(JavahTask task, String opt, String arg) {
                task.version = true;
            }
        },
        new HiddenOption(false, "-fullversion") {
            void process(JavahTask task, String opt, String arg) {
                task.fullVersion = true;
            }
        },
        new Option(false, "-jni") {
            void process(JavahTask task, String opt, String arg) {
                task.jni = true;
            }
        },
        new Option(false, "-force") {
            void process(JavahTask task, String opt, String arg) {
                task.force = true;
            }
        },
        new HiddenOption(false, "-Xnew") {
            void process(JavahTask task, String opt, String arg) {
            }
        },
        new HiddenOption(false, "-old") {
            void process(JavahTask task, String opt, String arg) {
                task.old = true;
            }
        },
        new HiddenOption(false, "-llni", "-Xllni") {
            void process(JavahTask task, String opt, String arg) {
                task.llni = true;
            }
        },
        new HiddenOption(false, "-llnidouble") {
            void process(JavahTask task, String opt, String arg) {
                task.llni = true;
                task.doubleAlign = true;
            }
        },
        new HiddenOption(false) {
            boolean matches(String opt) {
                return opt.startsWith("-XD");
            }
            void process(JavahTask task, String opt, String arg) {
                task.javac_extras.add(opt);
            }
        },
    };
    JavahTask() {
    }
    JavahTask(Writer out,
            JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Iterable<String> options,
            Iterable<String> classes) {
        this();
        this.log = getPrintWriterForWriter(out);
        this.fileManager = fileManager;
        this.diagnosticListener = diagnosticListener;
        try {
            handleOptions(options, false);
        } catch (BadArgs e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        this.classes = new ArrayList<String>();
        if (classes != null) {
            for (String classname: classes) {
                classname.getClass(); 
                this.classes.add(classname);
            }
        }
    }
    public void setLocale(Locale locale) {
        if (locale == null)
            locale = Locale.getDefault();
        task_locale = locale;
    }
    public void setLog(PrintWriter log) {
        this.log = log;
    }
    public void setLog(OutputStream s) {
        setLog(getPrintWriterForStream(s));
    }
    static PrintWriter getPrintWriterForStream(OutputStream s) {
        return new PrintWriter(s, true);
    }
    static PrintWriter getPrintWriterForWriter(Writer w) {
        if (w == null)
            return getPrintWriterForStream(null);
        else if (w instanceof PrintWriter)
            return (PrintWriter) w;
        else
            return new PrintWriter(w, true);
    }
    public void setDiagnosticListener(DiagnosticListener<? super JavaFileObject> dl) {
        diagnosticListener = dl;
    }
    public void setDiagnosticListener(OutputStream s) {
        setDiagnosticListener(getDiagnosticListenerForStream(s));
    }
    private DiagnosticListener<JavaFileObject> getDiagnosticListenerForStream(OutputStream s) {
        return getDiagnosticListenerForWriter(getPrintWriterForStream(s));
    }
    private DiagnosticListener<JavaFileObject> getDiagnosticListenerForWriter(Writer w) {
        final PrintWriter pw = getPrintWriterForWriter(w);
        return new DiagnosticListener<JavaFileObject> () {
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    pw.print(getMessage("err.prefix"));
                    pw.print(" ");
                }
                pw.println(diagnostic.getMessage(null));
            }
        };
    }
    int run(String[] args) {
        try {
            handleOptions(args);
            boolean ok = run();
            return ok ? 0 : 1;
        } catch (BadArgs e) {
            diagnosticListener.report(createDiagnostic(e.key, e.args));
            return 1;
        } catch (InternalError e) {
            diagnosticListener.report(createDiagnostic("err.internal.error", e.getMessage()));
            return 1;
        } catch (Util.Exit e) {
            return e.exitValue;
        } finally {
            log.flush();
        }
    }
    public void handleOptions(String[] args) throws BadArgs {
        handleOptions(Arrays.asList(args), true);
    }
    private void handleOptions(Iterable<String> args, boolean allowClasses) throws BadArgs {
        if (log == null) {
            log = getPrintWriterForStream(System.out);
            if (diagnosticListener == null)
              diagnosticListener = getDiagnosticListenerForStream(System.err);
        } else {
            if (diagnosticListener == null)
              diagnosticListener = getDiagnosticListenerForWriter(log);
        }
        if (fileManager == null)
            fileManager = getDefaultFileManager(diagnosticListener, log);
        Iterator<String> iter = expandAtArgs(args).iterator();
        noArgs = !iter.hasNext();
        while (iter.hasNext()) {
            String arg = iter.next();
            if (arg.startsWith("-"))
                handleOption(arg, iter);
            else if (allowClasses) {
                if (classes == null)
                    classes = new ArrayList<String>();
                classes.add(arg);
                while (iter.hasNext())
                    classes.add(iter.next());
            } else
                throw new BadArgs("err.unknown.option", arg).showUsage(true);
        }
        if ((classes == null || classes.size() == 0) &&
                !(noArgs || help || version || fullVersion)) {
            throw new BadArgs("err.no.classes.specified");
        }
        if (jni && llni)
            throw new BadArgs("jni.llni.mixed");
        if (odir != null && ofile != null)
            throw new BadArgs("dir.file.mixed");
    }
    private void handleOption(String name, Iterator<String> rest) throws BadArgs {
        for (Option o: recognizedOptions) {
            if (o.matches(name)) {
                if (o.hasArg) {
                    if (rest.hasNext())
                        o.process(this, name, rest.next());
                    else
                        throw new BadArgs("err.missing.arg", name).showUsage(true);
                } else
                    o.process(this, name, null);
                if (o.ignoreRest()) {
                    while (rest.hasNext())
                        rest.next();
                }
                return;
            }
        }
        if (fileManager.handleOption(name, rest))
            return;
        throw new BadArgs("err.unknown.option", name).showUsage(true);
    }
    private Iterable<String> expandAtArgs(Iterable<String> args) throws BadArgs {
        try {
            List<String> l = new ArrayList<String>();
            for (String arg: args) l.add(arg);
            return Arrays.asList(CommandLine.parse(l.toArray(new String[l.size()])));
        } catch (FileNotFoundException e) {
            throw new BadArgs("at.args.file.not.found", e.getLocalizedMessage());
        } catch (IOException e) {
            throw new BadArgs("at.args.io.exception", e.getLocalizedMessage());
        }
    }
    public Boolean call() {
        return run();
    }
    public boolean run() throws Util.Exit {
        Util util = new Util(log, diagnosticListener);
        if (noArgs || help) {
            showHelp();
            return help; 
        }
        if (version || fullVersion) {
            showVersion(fullVersion);
            return true;
        }
        util.verbose = verbose;
        Gen g;
        if (llni)
            g = new LLNI(doubleAlign, util);
        else {
            g = new JNI(util);
        }
        if (ofile != null) {
            if (!(fileManager instanceof StandardJavaFileManager)) {
                diagnosticListener.report(createDiagnostic("err.cant.use.option.for.fm", "-o"));
                return false;
            }
            Iterable<? extends JavaFileObject> iter =
                    ((StandardJavaFileManager) fileManager).getJavaFileObjectsFromFiles(Collections.singleton(ofile));
            JavaFileObject fo = iter.iterator().next();
            g.setOutFile(fo);
        } else {
            if (odir != null) {
                if (!(fileManager instanceof StandardJavaFileManager)) {
                    diagnosticListener.report(createDiagnostic("err.cant.use.option.for.fm", "-d"));
                    return false;
                }
                if (!odir.exists())
                    if (!odir.mkdirs())
                        util.error("cant.create.dir", odir.toString());
                try {
                    ((StandardJavaFileManager) fileManager).setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(odir));
                } catch (IOException e) {
                    Object msg = e.getLocalizedMessage();
                    if (msg == null) {
                        msg = e;
                    }
                    diagnosticListener.report(createDiagnostic("err.ioerror", odir, msg));
                    return false;
                }
            }
            g.setFileManager(fileManager);
        }
        g.setForce(force);
        if (fileManager instanceof JavahFileManager)
            ((JavahFileManager) fileManager).setIgnoreSymbolFile(true);
        JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        List<String> opts = new ArrayList<String>();
        opts.add("-proc:only");
        opts.addAll(javac_extras);
        CompilationTask t = c.getTask(log, fileManager, diagnosticListener, opts, internalize(classes), null);
        JavahProcessor p = new JavahProcessor(g);
        t.setProcessors(Collections.singleton(p));
        boolean ok = t.call();
        if (p.exit != null)
            throw new Util.Exit(p.exit);
        return ok;
    }
    private List<String> internalize(List<String> classes) {
        List<String> l = new ArrayList<String>();
        for (String c: classes) {
            l.add(c.replace('$', '.'));
        }
        return l;
    }
    private List<File> pathToFiles(String path) {
        List<File> files = new ArrayList<File>();
        for (String f: path.split(File.pathSeparator)) {
            if (f.length() > 0)
                files.add(new File(f));
        }
        return files;
    }
    static StandardJavaFileManager getDefaultFileManager(final DiagnosticListener<? super JavaFileObject> dl, PrintWriter log) {
        return JavahFileManager.create(dl, log);
    }
    private void showHelp() {
        log.println(getMessage("main.usage", progname));
        for (Option o: recognizedOptions) {
            if (o.isHidden())
                continue;
            String name = o.aliases[0].substring(1); 
            log.println(getMessage("main.opt." + name));
        }
        String[] fmOptions = { "-classpath", "-bootclasspath" };
        for (String o: fmOptions) {
            if (fileManager.isSupportedOption(o) == -1)
                continue;
            String name = o.substring(1);
            log.println(getMessage("main.opt." + name));
        }
        log.println(getMessage("main.usage.foot"));
    }
    private void showVersion(boolean full) {
        log.println(version(full));
    }
    private static final String versionRBName = "com.sun.tools.javah.resources.version";
    private static ResourceBundle versionRB;
    private String version(boolean full) {
        String msgKey = (full ? "javah.fullVersion" : "javah.version");
        String versionKey = (full ? "full" : "release");
        if (versionRB == null) {
            try {
                versionRB = ResourceBundle.getBundle(versionRBName);
            } catch (MissingResourceException e) {
                return getMessage("version.resource.missing", System.getProperty("java.version"));
            }
        }
        try {
            return getMessage(msgKey, "javah", versionRB.getString(versionKey));
        }
        catch (MissingResourceException e) {
            return getMessage("version.unknown", System.getProperty("java.version"));
        }
    }
    private Diagnostic<JavaFileObject> createDiagnostic(final String key, final Object... args) {
        return new Diagnostic<JavaFileObject>() {
            public Kind getKind() {
                return Diagnostic.Kind.ERROR;
            }
            public JavaFileObject getSource() {
                return null;
            }
            public long getPosition() {
                return Diagnostic.NOPOS;
            }
            public long getStartPosition() {
                return Diagnostic.NOPOS;
            }
            public long getEndPosition() {
                return Diagnostic.NOPOS;
            }
            public long getLineNumber() {
                return Diagnostic.NOPOS;
            }
            public long getColumnNumber() {
                return Diagnostic.NOPOS;
            }
            public String getCode() {
                return key;
            }
            public String getMessage(Locale locale) {
                return JavahTask.this.getMessage(locale, key, args);
            }
        };
    }
    private String getMessage(String key, Object... args) {
        return getMessage(task_locale, key, args);
    }
    private String getMessage(Locale locale, String key, Object... args) {
        if (bundles == null) {
            bundles = new HashMap<Locale, ResourceBundle>();
        }
        if (locale == null)
            locale = Locale.getDefault();
        ResourceBundle b = bundles.get(locale);
        if (b == null) {
            try {
                b = ResourceBundle.getBundle("com.sun.tools.javah.resources.l10n", locale);
                bundles.put(locale, b);
            } catch (MissingResourceException e) {
                throw new InternalError("Cannot find javah resource bundle for locale " + locale, e);
            }
        }
        try {
            return MessageFormat.format(b.getString(key), args);
        } catch (MissingResourceException e) {
            return key;
        }
    }
    File ofile;
    File odir;
    String bootcp;
    String usercp;
    List<String> classes;
    boolean verbose;
    boolean noArgs;
    boolean help;
    boolean trace;
    boolean version;
    boolean fullVersion;
    boolean jni;
    boolean llni;
    boolean doubleAlign;
    boolean force;
    boolean old;
    Set<String> javac_extras = new LinkedHashSet<String>();
    PrintWriter log;
    JavaFileManager fileManager;
    DiagnosticListener<? super JavaFileObject> diagnosticListener;
    Locale task_locale;
    Map<Locale, ResourceBundle> bundles;
    private static final String progname = "javah";
    @SupportedAnnotationTypes("*")
    class JavahProcessor extends AbstractProcessor {
        private Messager messager;
        JavahProcessor(Gen g) {
            this.g = g;
        }
        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }
        @Override
        public void init(ProcessingEnvironment pEnv) {
            super.init(pEnv);
            messager  = processingEnv.getMessager();
        }
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            try {
                Set<TypeElement> classes = getAllClasses(ElementFilter.typesIn(roundEnv.getRootElements()));
                if (classes.size() > 0) {
                    checkMethodParameters(classes);
                    g.setProcessingEnvironment(processingEnv);
                    g.setClasses(classes);
                    g.run();
                }
            } catch (CompletionFailure cf) {
                messager.printMessage(ERROR, getMessage("class.not.found", cf.sym.getQualifiedName().toString()));
            } catch (ClassNotFoundException cnfe) {
                messager.printMessage(ERROR, getMessage("class.not.found", cnfe.getMessage()));
            } catch (IOException ioe) {
                messager.printMessage(ERROR, getMessage("io.exception", ioe.getMessage()));
            } catch (Util.Exit e) {
                exit = e;
            }
            return true;
        }
        private Set<TypeElement> getAllClasses(Set<? extends TypeElement> classes) {
            Set<TypeElement> allClasses = new LinkedHashSet<TypeElement>();
            getAllClasses0(classes, allClasses);
            return allClasses;
        }
        private void getAllClasses0(Iterable<? extends TypeElement> classes, Set<TypeElement> allClasses) {
            for (TypeElement c: classes) {
                allClasses.add(c);
                getAllClasses0(ElementFilter.typesIn(c.getEnclosedElements()), allClasses);
            }
        }
        private void checkMethodParameters(Set<TypeElement> classes) {
            Types types = processingEnv.getTypeUtils();
            for (TypeElement te: classes) {
                for (ExecutableElement ee: ElementFilter.methodsIn(te.getEnclosedElements())) {
                    for (VariableElement ve: ee.getParameters()) {
                        TypeMirror tm = ve.asType();
                        checkMethodParametersVisitor.visit(tm, types);
                    }
                }
            }
        }
        private TypeVisitor<Void,Types> checkMethodParametersVisitor =
                new SimpleTypeVisitor7<Void,Types>() {
            @Override
            public Void visitArray(ArrayType t, Types types) {
                visit(t.getComponentType(), types);
                return null;
            }
            @Override
            public Void visitDeclared(DeclaredType t, Types types) {
                t.asElement().getKind(); 
                for (TypeMirror st: types.directSupertypes(t))
                    visit(st, types);
                return null;
            }
        };
        private Gen g;
        private Util.Exit exit;
    }
}
