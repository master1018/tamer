import com.sun.tools.classfile.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
public class JavapTask implements DisassemblerTool.DisassemblerTask, Messages {
    public class BadArgs extends Exception {
        static final long serialVersionUID = 8765093759964640721L;
        BadArgs(String key, Object... args) {
            super(JavapTask.this.getMessage(key, args));
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
        abstract void process(JavapTask task, String opt, String arg) throws BadArgs;
        final boolean hasArg;
        final String[] aliases;
    }
    static Option[] recognizedOptions = {
        new Option(false, "-help", "--help", "-?") {
            void process(JavapTask task, String opt, String arg) {
                task.options.help = true;
            }
        },
        new Option(false, "-version") {
            void process(JavapTask task, String opt, String arg) {
                task.options.version = true;
            }
        },
        new Option(false, "-fullversion") {
            void process(JavapTask task, String opt, String arg) {
                task.options.fullVersion = true;
            }
        },
        new Option(false, "-v", "-verbose", "-all") {
            void process(JavapTask task, String opt, String arg) {
                task.options.verbose = true;
                task.options.showFlags = true;
                task.options.showAllAttrs = true;
            }
        },
        new Option(false, "-l") {
            void process(JavapTask task, String opt, String arg) {
                task.options.showLineAndLocalVariableTables = true;
            }
        },
        new Option(false, "-public") {
            void process(JavapTask task, String opt, String arg) {
                task.options.accessOptions.add(opt);
                task.options.showAccess = AccessFlags.ACC_PUBLIC;
            }
        },
        new Option(false, "-protected") {
            void process(JavapTask task, String opt, String arg) {
                task.options.accessOptions.add(opt);
                task.options.showAccess = AccessFlags.ACC_PROTECTED;
            }
        },
        new Option(false, "-package") {
            void process(JavapTask task, String opt, String arg) {
                task.options.accessOptions.add(opt);
                task.options.showAccess = 0;
            }
        },
        new Option(false, "-p", "-private") {
            void process(JavapTask task, String opt, String arg) {
                if (!task.options.accessOptions.contains("-p") &&
                        !task.options.accessOptions.contains("-private")) {
                    task.options.accessOptions.add(opt);
                }
                task.options.showAccess = AccessFlags.ACC_PRIVATE;
            }
        },
        new Option(false, "-c") {
            void process(JavapTask task, String opt, String arg) {
                task.options.showDisassembled = true;
            }
        },
        new Option(false, "-s") {
            void process(JavapTask task, String opt, String arg) {
                task.options.showInternalSignatures = true;
            }
        },
        new Option(false, "-h") {
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                throw task.new BadArgs("err.h.not.supported");
            }
        },
        new Option(false, "-verify", "-verify-verbose") {
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                throw task.new BadArgs("err.verify.not.supported");
            }
        },
        new Option(false, "-sysinfo") {
            void process(JavapTask task, String opt, String arg) {
                task.options.sysInfo = true;
            }
        },
        new Option(false, "-Xold") {
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                task.log.println(task.getMessage("warn.Xold.not.supported"));
            }
        },
        new Option(false, "-Xnew") {
            void process(JavapTask task, String opt, String arg) throws BadArgs {
            }
        },
        new Option(false, "-XDcompat") {
            void process(JavapTask task, String opt, String arg) {
                task.options.compat = true;
            }
        },
        new Option(false, "-XDdetails") {
            void process(JavapTask task, String opt, String arg) {
                task.options.details = EnumSet.allOf(InstructionDetailWriter.Kind.class);
            }
        },
        new Option(false, "-XDdetails:") {
            @Override
            boolean matches(String opt) {
                int sep = opt.indexOf(":");
                return sep != -1 && super.matches(opt.substring(0, sep + 1));
            }
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                int sep = opt.indexOf(":");
                for (String v: opt.substring(sep + 1).split("[,: ]+")) {
                    if (!handleArg(task, v))
                        throw task.new BadArgs("err.invalid.arg.for.option", v);
                }
            }
            boolean handleArg(JavapTask task, String arg) {
                if (arg.length() == 0)
                    return true;
                if (arg.equals("all")) {
                    task.options.details = EnumSet.allOf(InstructionDetailWriter.Kind.class);
                    return true;
                }
                boolean on = true;
                if (arg.startsWith("-")) {
                    on = false;
                    arg = arg.substring(1);
                }
                for (InstructionDetailWriter.Kind k: InstructionDetailWriter.Kind.values()) {
                    if (arg.equalsIgnoreCase(k.option)) {
                        if (on)
                            task.options.details.add(k);
                        else
                            task.options.details.remove(k);
                        return true;
                    }
                }
                return false;
            }
        },
        new Option(false, "-constants") {
            void process(JavapTask task, String opt, String arg) {
                task.options.showConstants = true;
            }
        },
        new Option(false, "-XDinner") {
            void process(JavapTask task, String opt, String arg) {
                task.options.showInnerClasses = true;
            }
        },
        new Option(false, "-XDindent:") {
            @Override
            boolean matches(String opt) {
                int sep = opt.indexOf(":");
                return sep != -1 && super.matches(opt.substring(0, sep + 1));
            }
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                int sep = opt.indexOf(":");
                try {
                    task.options.indentWidth = Integer.valueOf(opt.substring(sep + 1));
                } catch (NumberFormatException e) {
                }
            }
        },
        new Option(false, "-XDtab:") {
            @Override
            boolean matches(String opt) {
                int sep = opt.indexOf(":");
                return sep != -1 && super.matches(opt.substring(0, sep + 1));
            }
            void process(JavapTask task, String opt, String arg) throws BadArgs {
                int sep = opt.indexOf(":");
                try {
                    task.options.tabColumn = Integer.valueOf(opt.substring(sep + 1));
                } catch (NumberFormatException e) {
                }
            }
        }
    };
    public JavapTask() {
        context = new Context();
        context.put(Messages.class, this);
        options = Options.instance(context);
        attributeFactory = new Attribute.Factory();
    }
    public JavapTask(Writer out,
            JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener) {
        this();
        this.log = getPrintWriterForWriter(out);
        this.fileManager = fileManager;
        this.diagnosticListener = diagnosticListener;
    }
    public JavapTask(Writer out,
            JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Iterable<String> options,
            Iterable<String> classes) {
        this(out, fileManager, diagnosticListener);
        this.classes = new ArrayList<String>();
        for (String classname: classes) {
            classname.getClass(); 
            this.classes.add(classname);
        }
        try {
            handleOptions(options, false);
        } catch (BadArgs e) {
            throw new IllegalArgumentException(e.getMessage());
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
    private static PrintWriter getPrintWriterForStream(OutputStream s) {
        return new PrintWriter(s, true);
    }
    private static PrintWriter getPrintWriterForWriter(Writer w) {
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
                switch (diagnostic.getKind()) {
                    case ERROR:
                        pw.print(getMessage("err.prefix"));
                        break;
                    case WARNING:
                        pw.print(getMessage("warn.prefix"));
                        break;
                    case NOTE:
                        pw.print(getMessage("note.prefix"));
                        break;
                }
                pw.print(" ");
                pw.println(diagnostic.getMessage(null));
            }
        };
    }
    static final int
        EXIT_OK = 0,        
        EXIT_ERROR = 1,     
        EXIT_CMDERR = 2,    
        EXIT_SYSERR = 3,    
        EXIT_ABNORMAL = 4;  
    int run(String[] args) {
        try {
            handleOptions(args);
            if (classes == null || classes.size() == 0) {
                if (options.help || options.version || options.fullVersion)
                    return EXIT_OK;
                else
                    return EXIT_CMDERR;
            }
            try {
                boolean ok = run();
                return ok ? EXIT_OK : EXIT_ERROR;
            } finally {
                if (defaultFileManager != null) {
                    try {
                        defaultFileManager.close();
                        defaultFileManager = null;
                    } catch (IOException e) {
                        throw new InternalError(e);
                    }
                }
            }
        } catch (BadArgs e) {
            reportError(e.key, e.args);
            if (e.showUsage) {
                log.println(getMessage("main.usage.summary", progname));
            }
            return EXIT_CMDERR;
        } catch (InternalError e) {
            Object[] e_args;
            if (e.getCause() == null)
                e_args = e.args;
            else {
                e_args = new Object[e.args.length + 1];
                e_args[0] = e.getCause();
                System.arraycopy(e.args, 0, e_args, 1, e.args.length);
            }
            reportError("err.internal.error", e_args);
            return EXIT_ABNORMAL;
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
        Iterator<String> iter = args.iterator();
        boolean noArgs = !iter.hasNext();
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
        if (!options.compat && options.accessOptions.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (String opt: options.accessOptions) {
                if (sb.length() > 0)
                    sb.append(" ");
                sb.append(opt);
            }
            throw new BadArgs("err.incompatible.options", sb);
        }
        if ((classes == null || classes.size() == 0) &&
                !(noArgs || options.help || options.version || options.fullVersion)) {
            throw new BadArgs("err.no.classes.specified");
        }
        if (noArgs || options.help)
            showHelp();
        if (options.version || options.fullVersion)
            showVersion(options.fullVersion);
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
    public Boolean call() {
        return run();
    }
    public boolean run() {
        if (classes == null || classes.size() == 0)
            return false;
        context.put(PrintWriter.class, log);
        ClassWriter classWriter = ClassWriter.instance(context);
        SourceWriter sourceWriter = SourceWriter.instance(context);
        sourceWriter.setFileManager(fileManager);
        attributeFactory.setCompat(options.compat);
        boolean ok = true;
        for (String className: classes) {
            JavaFileObject fo;
            try {
                writeClass(classWriter, className);
            } catch (ConstantPoolException e) {
                reportError("err.bad.constant.pool", className, e.getLocalizedMessage());
                ok = false;
            } catch (EOFException e) {
                reportError("err.end.of.file", className);
                ok = false;
            } catch (FileNotFoundException e) {
                reportError("err.file.not.found", e.getLocalizedMessage());
                ok = false;
            } catch (IOException e) {
                Object msg = e.getLocalizedMessage();
                if (msg == null)
                    msg = e;
                reportError("err.ioerror", className, msg);
                ok = false;
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.close();
                reportError("err.crash", t.toString(), sw.toString());
                ok = false;
            }
        }
        return ok;
    }
    protected boolean writeClass(ClassWriter classWriter, String className)
            throws IOException, ConstantPoolException {
        JavaFileObject fo = open(className);
        if (fo == null) {
            reportError("err.class.not.found", className);
            return false;
        }
        ClassFileInfo cfInfo = read(fo);
        if (!className.endsWith(".class")) {
            String cfName = cfInfo.cf.getName();
            if (!cfName.replaceAll("[/$]", ".").equals(className.replaceAll("[/$]", ".")))
                reportWarning("warn.unexpected.class", className, cfName.replace('/', '.'));
        }
        write(cfInfo);
        if (options.showInnerClasses) {
            ClassFile cf = cfInfo.cf;
            Attribute a = cf.getAttribute(Attribute.InnerClasses);
            if (a instanceof InnerClasses_attribute) {
                InnerClasses_attribute inners = (InnerClasses_attribute) a;
                try {
                    boolean ok = true;
                    for (int i = 0; i < inners.classes.length; i++) {
                        int outerIndex = inners.classes[i].outer_class_info_index;
                        ConstantPool.CONSTANT_Class_info outerClassInfo = cf.constant_pool.getClassInfo(outerIndex);
                        String outerClassName = outerClassInfo.getName();
                        if (outerClassName.equals(cf.getName())) {
                            int innerIndex = inners.classes[i].inner_class_info_index;
                            ConstantPool.CONSTANT_Class_info innerClassInfo = cf.constant_pool.getClassInfo(innerIndex);
                            String innerClassName = innerClassInfo.getName();
                            classWriter.println("
                            classWriter.println();
                            ok = ok & writeClass(classWriter, innerClassName);
                        }
                    }
                    return ok;
                } catch (ConstantPoolException e) {
                    reportError("err.bad.innerclasses.attribute", className);
                    return false;
                }
            } else if (a != null) {
                reportError("err.bad.innerclasses.attribute", className);
                return false;
            }
        }
        return true;
    }
    protected JavaFileObject open(String className) throws IOException {
        JavaFileObject fo = getClassFileObject(className);
        if (fo != null)
            return fo;
        String cn = className;
        int lastDot;
        while ((lastDot = cn.lastIndexOf(".")) != -1) {
            cn = cn.substring(0, lastDot) + "$" + cn.substring(lastDot + 1);
            fo = getClassFileObject(cn);
            if (fo != null)
                return fo;
        }
        if (!className.endsWith(".class"))
            return null;
        if (fileManager instanceof StandardJavaFileManager) {
            StandardJavaFileManager sfm = (StandardJavaFileManager) fileManager;
            fo = sfm.getJavaFileObjects(className).iterator().next();
            if (fo != null && fo.getLastModified() != 0) {
                return fo;
            }
        }
        if (className.matches("^[A-Za-z]+:.*")) {
            try {
                final URI uri = new URI(className);
                final URL url = uri.toURL();
                final URLConnection conn = url.openConnection();
                return new JavaFileObject() {
                    public Kind getKind() {
                        return JavaFileObject.Kind.CLASS;
                    }
                    public boolean isNameCompatible(String simpleName, Kind kind) {
                        throw new UnsupportedOperationException();
                    }
                    public NestingKind getNestingKind() {
                        throw new UnsupportedOperationException();
                    }
                    public Modifier getAccessLevel() {
                        throw new UnsupportedOperationException();
                    }
                    public URI toUri() {
                        return uri;
                    }
                    public String getName() {
                        return url.toString();
                    }
                    public InputStream openInputStream() throws IOException {
                        return conn.getInputStream();
                    }
                    public OutputStream openOutputStream() throws IOException {
                        throw new UnsupportedOperationException();
                    }
                    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                        throw new UnsupportedOperationException();
                    }
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        throw new UnsupportedOperationException();
                    }
                    public Writer openWriter() throws IOException {
                        throw new UnsupportedOperationException();
                    }
                    public long getLastModified() {
                        return conn.getLastModified();
                    }
                    public boolean delete() {
                        throw new UnsupportedOperationException();
                    }
                };
            } catch (URISyntaxException ignore) {
            } catch (IOException ignore) {
            }
        }
        return null;
    }
    public static class ClassFileInfo {
        ClassFileInfo(JavaFileObject fo, ClassFile cf, byte[] digest, int size) {
            this.fo = fo;
            this.cf = cf;
            this.digest = digest;
            this.size = size;
        }
        public final JavaFileObject fo;
        public final ClassFile cf;
        public final byte[] digest;
        public final int size;
    }
    public ClassFileInfo read(JavaFileObject fo) throws IOException, ConstantPoolException {
        InputStream in = fo.openInputStream();
        try {
            SizeInputStream sizeIn = null;
            MessageDigest md  = null;
            if (options.sysInfo || options.verbose) {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException ignore) {
                }
                in = new DigestInputStream(in, md);
                in = sizeIn = new SizeInputStream(in);
            }
            ClassFile cf = ClassFile.read(in, attributeFactory);
            byte[] digest = (md == null) ? null : md.digest();
            int size = (sizeIn == null) ? -1 : sizeIn.size();
            return new ClassFileInfo(fo, cf, digest, size);
        } finally {
            in.close();
        }
    }
    public void write(ClassFileInfo info) {
        ClassWriter classWriter = ClassWriter.instance(context);
        if (options.sysInfo || options.verbose) {
            classWriter.setFile(info.fo.toUri());
            classWriter.setLastModified(info.fo.getLastModified());
            classWriter.setDigest("MD5", info.digest);
            classWriter.setFileSize(info.size);
        }
        classWriter.write(info.cf);
    }
    protected void setClassFile(ClassFile classFile) {
        ClassWriter classWriter = ClassWriter.instance(context);
        classWriter.setClassFile(classFile);
    }
    protected void setMethod(Method enclosingMethod) {
        ClassWriter classWriter = ClassWriter.instance(context);
        classWriter.setMethod(enclosingMethod);
    }
    protected void write(Attribute value) {
        AttributeWriter attrWriter = AttributeWriter.instance(context);
        ClassWriter classWriter = ClassWriter.instance(context);
        ClassFile cf = classWriter.getClassFile();
        attrWriter.write(cf, value, cf.constant_pool);
    }
    protected void write(Attributes attrs) {
        AttributeWriter attrWriter = AttributeWriter.instance(context);
        ClassWriter classWriter = ClassWriter.instance(context);
        ClassFile cf = classWriter.getClassFile();
        attrWriter.write(cf, attrs, cf.constant_pool);
    }
    protected void write(ConstantPool constant_pool) {
        ConstantWriter constantWriter = ConstantWriter.instance(context);
        constantWriter.writeConstantPool(constant_pool);
    }
    protected void write(ConstantPool constant_pool, int value) {
        ConstantWriter constantWriter = ConstantWriter.instance(context);
        constantWriter.write(value);
    }
    protected void write(ConstantPool.CPInfo value) {
        ConstantWriter constantWriter = ConstantWriter.instance(context);
        constantWriter.println(value);
    }
    protected void write(Field value) {
        ClassWriter classWriter = ClassWriter.instance(context);
        classWriter.writeField(value);
    }
    protected void write(Method value) {
        ClassWriter classWriter = ClassWriter.instance(context);
        classWriter.writeMethod(value);
    }
    private JavaFileManager getDefaultFileManager(final DiagnosticListener<? super JavaFileObject> dl, PrintWriter log) {
        if (defaultFileManager == null)
            defaultFileManager = JavapFileManager.create(dl, log);
        return defaultFileManager;
    }
    private JavaFileObject getClassFileObject(String className) throws IOException {
        JavaFileObject fo;
        fo = fileManager.getJavaFileForInput(StandardLocation.PLATFORM_CLASS_PATH, className, JavaFileObject.Kind.CLASS);
        if (fo == null)
            fo = fileManager.getJavaFileForInput(StandardLocation.CLASS_PATH, className, JavaFileObject.Kind.CLASS);
        return fo;
    }
    private void showHelp() {
        log.println(getMessage("main.usage", progname));
        for (Option o: recognizedOptions) {
            String name = o.aliases[0].substring(1); 
            if (name.startsWith("X") || name.equals("fullversion") || name.equals("h") || name.equals("verify"))
                continue;
            log.println(getMessage("main.opt." + name));
        }
        String[] fmOptions = { "-classpath", "-bootclasspath" };
        for (String o: fmOptions) {
            if (fileManager.isSupportedOption(o) == -1)
                continue;
            String name = o.substring(1);
            log.println(getMessage("main.opt." + name));
        }
    }
    private void showVersion(boolean full) {
        log.println(version(full ? "full" : "release"));
    }
    private static final String versionRBName = "com.sun.tools.javap.resources.version";
    private static ResourceBundle versionRB;
    private String version(String key) {
        if (versionRB == null) {
            try {
                versionRB = ResourceBundle.getBundle(versionRBName);
            } catch (MissingResourceException e) {
                return getMessage("version.resource.missing", System.getProperty("java.version"));
            }
        }
        try {
            return versionRB.getString(key);
        }
        catch (MissingResourceException e) {
            return getMessage("version.unknown", System.getProperty("java.version"));
        }
    }
    private void reportError(String key, Object... args) {
        diagnosticListener.report(createDiagnostic(Diagnostic.Kind.ERROR, key, args));
    }
    private void reportNote(String key, Object... args) {
        diagnosticListener.report(createDiagnostic(Diagnostic.Kind.NOTE, key, args));
    }
    private void reportWarning(String key, Object... args) {
        diagnosticListener.report(createDiagnostic(Diagnostic.Kind.WARNING, key, args));
    }
    private Diagnostic<JavaFileObject> createDiagnostic(
            final Diagnostic.Kind kind, final String key, final Object... args) {
        return new Diagnostic<JavaFileObject>() {
            public Kind getKind() {
                return kind;
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
                return JavapTask.this.getMessage(locale, key, args);
            }
            @Override
            public String toString() {
                return getClass().getName() + "[key=" + key + ",args=" + Arrays.asList(args) + "]";
            }
        };
    }
    public String getMessage(String key, Object... args) {
        return getMessage(task_locale, key, args);
    }
    public String getMessage(Locale locale, String key, Object... args) {
        if (bundles == null) {
            bundles = new HashMap<Locale, ResourceBundle>();
        }
        if (locale == null)
            locale = Locale.getDefault();
        ResourceBundle b = bundles.get(locale);
        if (b == null) {
            try {
                b = ResourceBundle.getBundle("com.sun.tools.javap.resources.javap", locale);
                bundles.put(locale, b);
            } catch (MissingResourceException e) {
                throw new InternalError("Cannot find javap resource bundle for locale " + locale);
            }
        }
        try {
            return MessageFormat.format(b.getString(key), args);
        } catch (MissingResourceException e) {
            throw new InternalError(e, key);
        }
    }
    protected Context context;
    JavaFileManager fileManager;
    JavaFileManager defaultFileManager;
    PrintWriter log;
    DiagnosticListener<? super JavaFileObject> diagnosticListener;
    List<String> classes;
    Options options;
    Locale task_locale;
    Map<Locale, ResourceBundle> bundles;
    protected Attribute.Factory attributeFactory;
    private static final String progname = "javap";
    private static class SizeInputStream extends FilterInputStream {
        SizeInputStream(InputStream in) {
            super(in);
        }
        int size() {
            return size;
        }
        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            int n = super.read(buf, offset, length);
            if (n > 0)
                size += n;
            return n;
        }
        @Override
        public int read() throws IOException {
            int b = super.read();
            size += 1;
            return b;
        }
        private int size;
    }
}
