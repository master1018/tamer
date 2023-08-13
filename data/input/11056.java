public class JavacTaskImpl extends JavacTask {
    private ClientCodeWrapper ccw;
    private Main compilerMain;
    private JavaCompiler compiler;
    private Locale locale;
    private String[] args;
    private Context context;
    private List<JavaFileObject> fileObjects;
    private Map<JavaFileObject, JCCompilationUnit> notYetEntered;
    private ListBuffer<Env<AttrContext>> genList;
    private TaskListener taskListener;
    private AtomicBoolean used = new AtomicBoolean();
    private Iterable<? extends Processor> processors;
    private Integer result = null;
    JavacTaskImpl(Main compilerMain,
                String[] args,
                Context context,
                List<JavaFileObject> fileObjects) {
        this.ccw = ClientCodeWrapper.instance(context);
        this.compilerMain = compilerMain;
        this.args = args;
        this.context = context;
        this.fileObjects = fileObjects;
        setLocale(Locale.getDefault());
        compilerMain.getClass();
        args.getClass();
        fileObjects.getClass();
    }
    JavacTaskImpl(Main compilerMain,
                Iterable<String> flags,
                Context context,
                Iterable<String> classes,
                Iterable<? extends JavaFileObject> fileObjects) {
        this(compilerMain, toArray(flags, classes), context, toList(fileObjects));
    }
    static private String[] toArray(Iterable<String> flags, Iterable<String> classes) {
        ListBuffer<String> result = new ListBuffer<String>();
        if (flags != null)
            for (String flag : flags)
                result.append(flag);
        if (classes != null)
            for (String cls : classes)
                result.append(cls);
        return result.toArray(new String[result.length()]);
    }
    static private List<JavaFileObject> toList(Iterable<? extends JavaFileObject> fileObjects) {
        if (fileObjects == null)
            return List.nil();
        ListBuffer<JavaFileObject> result = new ListBuffer<JavaFileObject>();
        for (JavaFileObject fo : fileObjects)
            result.append(fo);
        return result.toList();
    }
    public Boolean call() {
        if (!used.getAndSet(true)) {
            initContext();
            notYetEntered = new HashMap<JavaFileObject, JCCompilationUnit>();
            compilerMain.setAPIMode(true);
            result = compilerMain.compile(args, context, fileObjects, processors);
            cleanup();
            return result == 0;
        } else {
            throw new IllegalStateException("multiple calls to method 'call'");
        }
    }
    public void setProcessors(Iterable<? extends Processor> processors) {
        processors.getClass(); 
        if (used.get())
            throw new IllegalStateException();
        this.processors = processors;
    }
    public void setLocale(Locale locale) {
        if (used.get())
            throw new IllegalStateException();
        this.locale = locale;
    }
    private void prepareCompiler() throws IOException {
        if (used.getAndSet(true)) {
            if (compiler == null)
                throw new IllegalStateException();
        } else {
            initContext();
            compilerMain.setOptions(Options.instance(context));
            compilerMain.filenames = new ListBuffer<File>();
            List<File> filenames = compilerMain.processArgs(CommandLine.parse(args));
            if (!filenames.isEmpty())
                throw new IllegalArgumentException("Malformed arguments " + filenames.toString(" "));
            compiler = JavaCompiler.instance(context);
            compiler.keepComments = true;
            compiler.genEndPos = true;
            compiler.initProcessAnnotations(processors);
            notYetEntered = new HashMap<JavaFileObject, JCCompilationUnit>();
            for (JavaFileObject file: fileObjects)
                notYetEntered.put(file, null);
            genList = new ListBuffer<Env<AttrContext>>();
            args = null;
        }
    }
    private void initContext() {
        context.put(JavacTaskImpl.class, this);
        if (context.get(TaskListener.class) != null)
            context.put(TaskListener.class, (TaskListener)null);
        if (taskListener != null)
            context.put(TaskListener.class, ccw.wrap(taskListener));
        context.put(Locale.class, locale);
    }
    void cleanup() {
        if (compiler != null)
            compiler.close();
        compiler = null;
        compilerMain = null;
        args = null;
        context = null;
        fileObjects = null;
        notYetEntered = null;
    }
    public JavaFileObject asJavaFileObject(File file) {
        JavacFileManager fm = (JavacFileManager)context.get(JavaFileManager.class);
        return fm.getRegularFile(file);
    }
    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }
    public Iterable<? extends CompilationUnitTree> parse() throws IOException {
        try {
            prepareCompiler();
            List<JCCompilationUnit> units = compiler.parseFiles(fileObjects);
            for (JCCompilationUnit unit: units) {
                JavaFileObject file = unit.getSourceFile();
                if (notYetEntered.containsKey(file))
                    notYetEntered.put(file, unit);
            }
            return units;
        }
        finally {
            parsed = true;
            if (compiler != null && compiler.log != null)
                compiler.log.flush();
        }
    }
    private boolean parsed = false;
    public Iterable<? extends TypeElement> enter() throws IOException {
        return enter(null);
    }
    public Iterable<? extends TypeElement> enter(Iterable<? extends CompilationUnitTree> trees)
        throws IOException
    {
        prepareCompiler();
        ListBuffer<JCCompilationUnit> roots = null;
        if (trees == null) {
            if (notYetEntered.size() > 0) {
                if (!parsed)
                    parse(); 
                for (JavaFileObject file: fileObjects) {
                    JCCompilationUnit unit = notYetEntered.remove(file);
                    if (unit != null) {
                        if (roots == null)
                            roots = new ListBuffer<JCCompilationUnit>();
                        roots.append(unit);
                    }
                }
                notYetEntered.clear();
            }
        }
        else {
            for (CompilationUnitTree cu : trees) {
                if (cu instanceof JCCompilationUnit) {
                    if (roots == null)
                        roots = new ListBuffer<JCCompilationUnit>();
                    roots.append((JCCompilationUnit)cu);
                    notYetEntered.remove(cu.getSourceFile());
                }
                else
                    throw new IllegalArgumentException(cu.toString());
            }
        }
        if (roots == null)
            return List.nil();
        try {
            List<JCCompilationUnit> units = compiler.enterTrees(roots.toList());
            if (notYetEntered.isEmpty())
                compiler = compiler.processAnnotations(units);
            ListBuffer<TypeElement> elements = new ListBuffer<TypeElement>();
            for (JCCompilationUnit unit : units) {
                for (JCTree node : unit.defs) {
                    if (node.getTag() == JCTree.CLASSDEF) {
                        JCClassDecl cdef = (JCClassDecl) node;
                        if (cdef.sym != null) 
                            elements.append(cdef.sym);
                    }
                }
            }
            return elements.toList();
        }
        finally {
            compiler.log.flush();
        }
    }
    @Override
    public Iterable<? extends Element> analyze() throws IOException {
        return analyze(null);
    }
    public Iterable<? extends Element> analyze(Iterable<? extends TypeElement> classes) throws IOException {
        enter(null);  
        final ListBuffer<Element> results = new ListBuffer<Element>();
        try {
            if (classes == null) {
                handleFlowResults(compiler.flow(compiler.attribute(compiler.todo)), results);
            } else {
                Filter f = new Filter() {
                    public void process(Env<AttrContext> env) {
                        handleFlowResults(compiler.flow(compiler.attribute(env)), results);
                    }
                };
                f.run(compiler.todo, classes);
            }
        } finally {
            compiler.log.flush();
        }
        return results;
    }
        private void handleFlowResults(Queue<Env<AttrContext>> queue, ListBuffer<Element> elems) {
            for (Env<AttrContext> env: queue) {
                switch (env.tree.getTag()) {
                    case JCTree.CLASSDEF:
                        JCClassDecl cdef = (JCClassDecl) env.tree;
                        if (cdef.sym != null)
                            elems.append(cdef.sym);
                        break;
                    case JCTree.TOPLEVEL:
                        JCCompilationUnit unit = (JCCompilationUnit) env.tree;
                        if (unit.packge != null)
                            elems.append(unit.packge);
                        break;
                }
            }
            genList.addAll(queue);
        }
    @Override
    public Iterable<? extends JavaFileObject> generate() throws IOException {
        return generate(null);
    }
    public Iterable<? extends JavaFileObject> generate(Iterable<? extends TypeElement> classes) throws IOException {
        final ListBuffer<JavaFileObject> results = new ListBuffer<JavaFileObject>();
        try {
            analyze(null);  
            if (classes == null) {
                compiler.generate(compiler.desugar(genList), results);
                genList.clear();
            }
            else {
                Filter f = new Filter() {
                        public void process(Env<AttrContext> env) {
                            compiler.generate(compiler.desugar(ListBuffer.of(env)), results);
                        }
                    };
                f.run(genList, classes);
            }
            if (genList.isEmpty()) {
                compiler.reportDeferredDiagnostics();
                cleanup();
            }
        }
        finally {
            if (compiler != null)
                compiler.log.flush();
        }
        return results;
    }
    public TypeMirror getTypeMirror(Iterable<? extends Tree> path) {
        Tree last = null;
        for (Tree node : path)
            last = node;
        return ((JCTree)last).type;
    }
    public JavacElements getElements() {
        if (context == null)
            throw new IllegalStateException();
        return JavacElements.instance(context);
    }
    public JavacTypes getTypes() {
        if (context == null)
            throw new IllegalStateException();
        return JavacTypes.instance(context);
    }
    public Iterable<? extends Tree> pathFor(CompilationUnitTree unit, Tree node) {
        return TreeInfo.pathFor((JCTree) node, (JCTree.JCCompilationUnit) unit).reverse();
    }
    abstract class Filter {
        void run(Queue<Env<AttrContext>> list, Iterable<? extends TypeElement> classes) {
            Set<TypeElement> set = new HashSet<TypeElement>();
            for (TypeElement item: classes)
                set.add(item);
            ListBuffer<Env<AttrContext>> defer = ListBuffer.<Env<AttrContext>>lb();
            while (list.peek() != null) {
                Env<AttrContext> env = list.remove();
                ClassSymbol csym = env.enclClass.sym;
                if (csym != null && set.contains(csym.outermostClass()))
                    process(env);
                else
                    defer = defer.append(env);
            }
            list.addAll(defer);
        }
        abstract void process(Env<AttrContext> env);
    }
    public Context getContext() {
        return context;
    }
    public void updateContext(Context newContext) {
        context = newContext;
    }
    public Type parseType(String expr, TypeElement scope) {
        if (expr == null || expr.equals(""))
            throw new IllegalArgumentException();
        compiler = JavaCompiler.instance(context);
        JavaFileObject prev = compiler.log.useSource(null);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Attr attr = Attr.instance(context);
        try {
            CharBuffer buf = CharBuffer.wrap((expr+"\u0000").toCharArray(), 0, expr.length());
            Parser parser = parserFactory.newParser(buf, false, false, false);
            JCTree tree = parser.parseType();
            return attr.attribType(tree, (Symbol.TypeSymbol)scope);
        } finally {
            compiler.log.useSource(prev);
        }
    }
}
