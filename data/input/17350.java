public class PackageDocImpl extends DocImpl implements PackageDoc {
    protected PackageSymbol sym;
    private JCCompilationUnit tree = null;    
    public FileObject docPath = null;
    private boolean foundDoc;   
    boolean isIncluded = false;  
    public boolean setDocPath = false;  
    public PackageDocImpl(DocEnv env, PackageSymbol sym) {
        this(env, sym, null, null);
    }
    public PackageDocImpl(DocEnv env, PackageSymbol sym,
                          String documentation, JCTree tree) {
        super(env, documentation);
        this.sym = sym;
        this.tree = (JCCompilationUnit) tree;
        foundDoc = (documentation != null);
    }
    void setTree(JCTree tree) {
        this.tree = (JCCompilationUnit) tree;
    }
    public void setRawCommentText(String rawDocumentation) {
        super.setRawCommentText(rawDocumentation);
        checkDoc();
    }
    String documentation() {
        if (documentation != null)
            return documentation;
        if (docPath != null) {
            try {
                InputStream s = docPath.openInputStream();
                documentation = readHTMLDocumentation(s, docPath);
            } catch (IOException exc) {
                documentation = "";
                env.error(null, "javadoc.File_Read_Error", docPath.getName());
            }
        } else {
            documentation = "";
        }
        return documentation;
    }
    private List<ClassDocImpl> allClassesFiltered = null;
    private List<ClassDocImpl> allClasses = null;
    private List<ClassDocImpl> getClasses(boolean filtered) {
        if (allClasses != null && !filtered) {
            return allClasses;
        }
        if (allClassesFiltered != null && filtered) {
            return allClassesFiltered;
        }
        ListBuffer<ClassDocImpl> classes = new ListBuffer<ClassDocImpl>();
        for (Scope.Entry e = sym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null) {
                ClassSymbol s = (ClassSymbol)e.sym;
                ClassDocImpl c = env.getClassDoc(s);
                if (c != null && !c.isSynthetic())
                    c.addAllClasses(classes, filtered);
            }
        }
        if (filtered)
            return allClassesFiltered = classes.toList();
        else
            return allClasses = classes.toList();
    }
    public void addAllClassesTo(ListBuffer<ClassDocImpl> list) {
        list.appendList(getClasses(true));
    }
    public ClassDoc[] allClasses(boolean filter) {
        List<ClassDocImpl> classes = getClasses(filter);
        return classes.toArray(new ClassDocImpl[classes.length()]);
    }
    public ClassDoc[] allClasses() {
        return allClasses(true);
    }
    public ClassDoc[] ordinaryClasses() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isOrdinaryClass()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    public ClassDoc[] exceptions() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isException()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    public ClassDoc[] errors() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isError()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    public ClassDoc[] enums() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isEnum()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    public ClassDoc[] interfaces() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isInterface()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    public AnnotationTypeDoc[] annotationTypes() {
        ListBuffer<AnnotationTypeDocImpl> ret =
            new ListBuffer<AnnotationTypeDocImpl>();
        for (ClassDocImpl c : getClasses(true)) {
            if (c.isAnnotationType()) {
                ret.append((AnnotationTypeDocImpl)c);
            }
        }
        return ret.toArray(new AnnotationTypeDocImpl[ret.length()]);
    }
    public AnnotationDesc[] annotations() {
        AnnotationDesc res[] = new AnnotationDesc[sym.getAnnotationMirrors().length()];
        int i = 0;
        for (Attribute.Compound a : sym.getAnnotationMirrors()) {
            res[i++] = new AnnotationDescImpl(env, a);
        }
        return res;
    }
    public ClassDoc findClass(String className) {
        final boolean filtered = true;
        for (ClassDocImpl c : getClasses(filtered)) {
            if (c.name().equals(className)) {
                return c;
            }
        }
        return null;
    }
    public boolean isIncluded() {
        return isIncluded;
    }
    public String name() {
        return qualifiedName();
    }
    public String qualifiedName() {
        Name fullname = sym.getQualifiedName();
        return fullname.isEmpty() ? "" : fullname.toString();
    }
    public void setDocPath(FileObject path) {
        setDocPath = true;
        if (path == null)
            return;
        if (!path.equals(docPath)) {
            docPath = path;
            checkDoc();
        }
    }
    private boolean checkDocWarningEmitted = false;
    private void checkDoc() {
        if (foundDoc) {
            if (!checkDocWarningEmitted) {
                env.warning(null, "javadoc.Multiple_package_comments", name());
                checkDocWarningEmitted = true;
            }
        } else {
            foundDoc = true;
        }
    }
    public SourcePosition position() {
        return (tree != null)
                ? SourcePositionImpl.make(tree.sourcefile, tree.pos, tree.lineMap)
                : SourcePositionImpl.make(docPath, Position.NOPOS, null);
    }
}
