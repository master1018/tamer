class Imports implements Constants {
    Identifier currentPackage = idNull;
    long currentPackageWhere = 0;
    Hashtable classes = new Hashtable();
    Vector packages = new Vector();
    Vector singles = new Vector();
    protected int checked;
    public Imports(Environment env) {
        addPackage(idJavaLang);
    }
    public synchronized void resolve(Environment env) {
        if (checked != 0) {
            return;
        }
        checked = -1;
        Vector resolvedPackages = new Vector();
        for (Enumeration e = packages.elements() ; e.hasMoreElements() ;) {
            IdentifierToken t = (IdentifierToken)e.nextElement();
            Identifier nm = t.getName();
            long where = t.getWhere();
            if (env.isExemptPackage(nm)) {
                resolvedPackages.addElement(t);
                continue;
            }
            try {
                Identifier rnm = env.resolvePackageQualifiedName(nm);
                if (importable(rnm, env)) {
                    if (env.getPackage(rnm.getTopName()).exists()) {
                        env.error(where, "class.and.package",
                                  rnm.getTopName());
                    }
                    if (!rnm.isInner())
                        rnm = Identifier.lookupInner(rnm, idNull);
                    nm = rnm;
                } else if (!env.getPackage(nm).exists()) {
                    env.error(where, "package.not.found", nm, "import");
                } else if (rnm.isInner()) {
                    env.error(where, "class.and.package", rnm.getTopName());
                }
                resolvedPackages.addElement(new IdentifierToken(where, nm));
            } catch (IOException ee) {
                env.error(where, "io.exception", "import");
            }
        }
        packages = resolvedPackages;
        for (Enumeration e = singles.elements() ; e.hasMoreElements() ;) {
            IdentifierToken t = (IdentifierToken)e.nextElement();
            Identifier nm = t.getName();
            long where = t.getWhere();
            Identifier pkg = nm.getQualifier();
            nm = env.resolvePackageQualifiedName(nm);
            if (!env.classExists(nm.getTopName())) {
                env.error(where, "class.not.found", nm, "import");
            }
            Identifier snm = nm.getFlatName().getName();
            Identifier className = (Identifier)classes.get(snm);
            if (className != null) {
                Identifier f1 = Identifier.lookup(className.getQualifier(),
                                                  className.getFlatName());
                Identifier f2 = Identifier.lookup(nm.getQualifier(),
                                                  nm.getFlatName());
                if (!f1.equals(f2)) {
                    env.error(where, "ambig.class", nm, className);
                }
            }
            classes.put(snm, nm);
            try {
                ClassDeclaration decl = env.getClassDeclaration(nm);
                ClassDefinition def = decl.getClassDefinitionNoCheck(env);
                Identifier importedPackage = def.getName().getQualifier();
                for (; def != null; def = def.getOuterClass()) {
                    if (def.isPrivate()
                        || !(def.isPublic()
                             || importedPackage.equals(currentPackage))) {
                        env.error(where, "cant.access.class", def);
                        break;
                    }
                }
            } catch (AmbiguousClass ee) {
                env.error(where, "ambig.class", ee.name1, ee.name2);
            } catch (ClassNotFound ee) {
                env.error(where, "class.not.found", ee.name, "import");
            }
        }
        checked = 1;
    }
    public synchronized Identifier resolve(Environment env, Identifier nm) throws ClassNotFound {
        if (tracing) env.dtEnter("Imports.resolve: " + nm);
        if (nm.hasAmbigPrefix()) {
            nm = nm.removeAmbigPrefix();
        }
        if (nm.isQualified()) {
            if (tracing) env.dtExit("Imports.resolve: QUALIFIED " + nm);
            return nm;
        }
        if (checked <= 0) {
            checked = 0;
            resolve(env);
        }
        Identifier className = (Identifier)classes.get(nm);
        if (className != null) {
            if (tracing) env.dtExit("Imports.resolve: PREVIOUSLY IMPORTED " + nm);
            return className;
        }
        Identifier id = Identifier.lookup(currentPackage, nm);
        if (importable(id, env)) {
            className = id;
        } else {
            Enumeration e = packages.elements();
            while (e.hasMoreElements()) {
                IdentifierToken t = (IdentifierToken)e.nextElement();
                id = Identifier.lookup(t.getName(), nm);
                if (importable(id, env)) {
                    if (className == null) {
                        className = id;
                    } else {
                        if (tracing)
                            env.dtExit("Imports.resolve: AMBIGUOUS " + nm);
                        throw new AmbiguousClass(className, id);
                    }
                }
            }
        }
        if (className == null) {
            if (tracing) env.dtExit("Imports.resolve: NOT FOUND " + nm);
            throw new ClassNotFound(nm);
        }
        classes.put(nm, className);
        if (tracing) env.dtExit("Imports.resolve: FIRST IMPORT " + nm);
        return className;
    }
    static public boolean importable(Identifier id, Environment env) {
        if (!id.isInner()) {
            return env.classExists(id);
        } else if (!env.classExists(id.getTopName())) {
            return false;
        } else {
            try {
                ClassDeclaration decl =
                    env.getClassDeclaration(id.getTopName());
                ClassDefinition c =
                    decl.getClassDefinitionNoCheck(env);
                return c.innerClassExists(id.getFlatName().getTail());
            } catch (ClassNotFound ee) {
                return false;
            }
        }
    }
    public synchronized Identifier forceResolve(Environment env, Identifier nm) {
        if (nm.isQualified())
            return nm;
        Identifier className = (Identifier)classes.get(nm);
        if (className != null) {
            return className;
        }
        className = Identifier.lookup(currentPackage, nm);
        classes.put(nm, className);
        return className;
    }
    public synchronized void addClass(IdentifierToken t) {
        singles.addElement(t);
    }
    public void addClass(Identifier nm) throws AmbiguousClass {
        addClass(new IdentifierToken(nm));
    }
    public synchronized void addPackage(IdentifierToken t) {
        final Identifier name = t.getName();
        if (name == currentPackage) {
            return;
        }
        final int size = packages.size();
        for (int i = 0; i < size; i++) {
            if (name == ((IdentifierToken)packages.elementAt(i)).getName()) {
                return;
            }
        }
        packages.addElement(t);
    }
    public void addPackage(Identifier id) {
        addPackage(new IdentifierToken(id));
    }
    public synchronized void setCurrentPackage(IdentifierToken t) {
        currentPackage = t.getName();
        currentPackageWhere = t.getWhere();
    }
    public synchronized void setCurrentPackage(Identifier id) {
        currentPackage = id;
    }
    public Identifier getCurrentPackage() {
        return currentPackage;
    }
    public List getImportedPackages() {
        return Collections.unmodifiableList(packages);
    }
    public List getImportedClasses() {
        return Collections.unmodifiableList(singles);
    }
    public Environment newEnvironment(Environment env) {
        return new ImportEnvironment(env, this);
    }
}
final
class ImportEnvironment extends Environment {
    Imports imports;
    ImportEnvironment(Environment env, Imports imports) {
        super(env, env.getSource());
        this.imports = imports;
    }
    public Identifier resolve(Identifier nm) throws ClassNotFound {
        return imports.resolve(this, nm);
    }
    public Imports getImports() {
        return imports;
    }
}
