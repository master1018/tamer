class ArgTypeCompilerFactory implements Example.Compiler.Factory {
    public Example.Compiler getCompiler(List<String> opts, boolean verbose) {
        String first;
        String[] rest;
        if (opts == null || opts.isEmpty()) {
            first = null;
            rest = new String[0];
        } else {
            first = opts.get(0);
            rest = opts.subList(1, opts.size()).toArray(new String[opts.size() - 1]);
        }
        if (first == null || first.equals("jsr199"))
            return new Jsr199Compiler(verbose, rest);
        else if (first.equals("simple"))
            return new SimpleCompiler(verbose);
        else if (first.equals("backdoor"))
            return new BackdoorCompiler(verbose);
        else
            throw new IllegalArgumentException(first);
    }
    static class Jsr199Compiler extends Example.Compiler {
        List<String> fmOpts;
        Jsr199Compiler(boolean verbose, String... args) {
            super(verbose);
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("-filemanager") && (i + 1 < args.length)) {
                    fmOpts = Arrays.asList(args[++i].split(","));
                } else
                    throw new IllegalArgumentException(arg);
            }
        }
        @Override
        boolean run(PrintWriter out, Set<String> keys, boolean raw, List<String> opts, List<File> files) {
            assert out != null && keys == null;
            if (verbose)
                System.err.println("run_jsr199: " + opts + " " + files);
            JavacTool tool = JavacTool.create();
            StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
            if (fmOpts != null)
                fm = new FileManager(fm, fmOpts);
            Iterable<? extends JavaFileObject> fos = fm.getJavaFileObjectsFromFiles(files);
            JavacTaskImpl t = (JavacTaskImpl) tool.getTask(out, fm, null, opts, null, fos);
            Context c = t.getContext();
            ArgTypeMessages.preRegister(c);
            ArgTypeJavaCompiler.preRegister(c);
            Boolean ok = t.call();
            return ok;
        }
    }
    static class SimpleCompiler extends Example.Compiler {
        SimpleCompiler(boolean verbose) {
            super(verbose);
        }
        @Override
        boolean run(PrintWriter out, Set<String> keys, boolean raw, List<String> opts, List<File> files) {
            assert out != null && keys == null;
            if (verbose)
                System.err.println("run_simple: " + opts + " " + files);
            List<String> args = new ArrayList<String>();
            args.addAll(opts);
            for (File f: files)
                args.add(f.getPath());
            Main main = new Main("javac", out);
            Context c = new Context() {
                @Override public void clear() {
                    ((JavacFileManager) get(JavaFileManager.class)).close();
                    super.clear();
                }
            };
            JavacFileManager.preRegister(c); 
            ArgTypeJavaCompiler.preRegister(c);
            ArgTypeMessages.preRegister(c);
            int result = main.compile(args.toArray(new String[args.size()]), c);
            return (result == 0);
        }
    }
    static class BackdoorCompiler extends Example.Compiler {
        BackdoorCompiler(boolean verbose) {
            super(verbose);
        }
        @Override
        boolean run(PrintWriter out, Set<String> keys, boolean raw, List<String> opts, List<File> files) {
            assert out != null && keys == null;
            if (verbose)
                System.err.println("run_simple: " + opts + " " + files);
            List<String> args = new ArrayList<String>(opts);
            for (File f: files)
                args.add(f.getPath());
            Context c = new Context();
            JavacFileManager.preRegister(c); 
            ArgTypeJavaCompiler.preRegister(c);
            ArgTypeMessages.preRegister(c);
            com.sun.tools.javac.main.Main m = new com.sun.tools.javac.main.Main("javac", out);
            int rc = m.compile(args.toArray(new String[args.size()]), c);
            return (rc == 0);
        }
    }
    static class ArgTypeDiagnosticFormatter extends AbstractDiagnosticFormatter {
        ArgTypeDiagnosticFormatter(Options options) {
            super(null, new SimpleConfiguration(options,
                    EnumSet.of(DiagnosticPart.SUMMARY,
                    DiagnosticPart.DETAILS,
                    DiagnosticPart.SUBDIAGNOSTICS)));
        }
        @Override
        protected String formatDiagnostic(JCDiagnostic d, Locale locale) {
            return formatMessage(d, locale);
        }
        @Override
        public String formatMessage(JCDiagnostic d, Locale l) {
            StringBuilder buf = new StringBuilder();
            formatMessage(d, buf);
            return buf.toString();
        }
        private void formatMessage(JCDiagnostic d, StringBuilder buf) {
            String key = d.getCode();
            Object[] args = d.getArgs();
            buf.append(getKeyArgsString(key, args));
            for (Object arg: args) {
                if (arg instanceof JCDiagnostic) {
                    buf.append("\n");
                    formatMessage((JCDiagnostic) arg, buf);
                }
            }
            for (String s: formatSubdiagnostics(d, null)) {
                buf.append("\n");
                buf.append(s);
            }
        }
        @Override
        public boolean isRaw() {
            return true;
        }
    }
    static class ArgTypeJavaCompiler extends JavaCompiler {
        static void preRegister(Context context) {
            context.put(compilerKey, new Context.Factory<JavaCompiler>() {
                public JavaCompiler make(Context c) {
                    Log log = Log.instance(c);
                    Options options = Options.instance(c);
                    log.setDiagnosticFormatter(new ArgTypeDiagnosticFormatter(options));
                    return new JavaCompiler(c);
                }
            });
        }
        private ArgTypeJavaCompiler() {
            super(null);
        }
    }
    static class ArgTypeMessages extends JavacMessages {
        static void preRegister(Context context) {
            context.put(JavacMessages.messagesKey, new Context.Factory<JavacMessages>() {
                public JavacMessages make(Context c) {
                    return new ArgTypeMessages(c) {
                        @Override
                        public String getLocalizedString(Locale l, String key, Object... args) {
                            return getKeyArgsString(key, args);
                        }
                    };
                }
            });
        }
        ArgTypeMessages(Context context) {
            super(context);
        }
    }
    static String getKeyArgsString(String key, Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(key);
        String sep = ": ";
        for (Object o : args) {
            buf.append(sep);
            buf.append(getArgTypeOrStringValue(o));
            sep = ", ";
        }
        return buf.toString();
    }
    static boolean showStringValues = false;
    static String getArgTypeOrStringValue(Object o) {
        if (showStringValues && o instanceof String)
            return "\"" + o + "\"";
        return getArgType(o);
    }
    static String getArgType(Object o) {
        if (o == null)
            return "null";
        if (o instanceof Name)
            return "name";
        if (o instanceof Boolean)
            return "boolean";
        if (o instanceof Integer)
            return "number";
        if (o instanceof String)
            return "string";
        if (o instanceof Flag)
            return "modifier";
        if (o instanceof KindName)
            return "symbol kind";
        if (o instanceof Token)
            return "token";
        if (o instanceof Symbol)
            return "symbol";
        if (o instanceof Type)
            return "type";
        if (o instanceof List) {
            List<?> l = (List<?>) o;
            if (l.isEmpty())
                return "list";
            else
                return "list of " + getArgType(l.get(0));
        }
        if (o instanceof ListBuffer)
            return getArgType(((ListBuffer) o).toList());
        if (o instanceof Set) {
            Set<?> s = (Set<?>) o;
            if (s.isEmpty())
                return "set";
            else
                return "set of " + getArgType(s.iterator().next());
        }
        if (o instanceof SourceVersion)
            return "source version";
        if (o instanceof FileObject || o instanceof File)
            return "file name";
        if (o instanceof JCDiagnostic)
            return "message segment";
        if (o instanceof LocalizedString)
            return "message segment";  
        String s = o.getClass().getSimpleName();
        return (s.isEmpty() ? o.getClass().getName() : s);
    }
}
