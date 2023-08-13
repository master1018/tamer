public class BatchEnvironment extends sun.rmi.rmic.BatchEnvironment implements Constants {
    private boolean parseNonConforming = false;
    private boolean standardPackage;
    HashSet alreadyChecked = new HashSet();
    Hashtable allTypes = new Hashtable(3001, 0.5f);
    Hashtable invalidTypes = new Hashtable(256, 0.5f);
    DirectoryLoader loader = null;
    ClassPathLoader classPathLoader = null;
    Hashtable nameContexts = null;
    Hashtable namesCache = new Hashtable();
    NameContext modulesContext = new NameContext(false);
    ClassDefinition defRemote = null;
    ClassDefinition defError = null;
    ClassDefinition defException = null;
    ClassDefinition defRemoteException = null;
    ClassDefinition defCorbaObject = null;
    ClassDefinition defSerializable = null;
    ClassDefinition defExternalizable = null;
    ClassDefinition defThrowable = null;
    ClassDefinition defRuntimeException = null;
    ClassDefinition defIDLEntity = null;
    ClassDefinition defValueBase = null;
    sun.tools.java.Type typeRemoteException = null;
    sun.tools.java.Type typeIOException = null;
    sun.tools.java.Type typeException = null;
    sun.tools.java.Type typeThrowable = null;
    ContextStack contextStack = null;
    public BatchEnvironment(OutputStream out, ClassPath path, Main main) {
        super(out,path,main);
        try {
            defRemote =
                getClassDeclaration(idRemote).getClassDefinition(this);
            defError =
                getClassDeclaration(idJavaLangError).getClassDefinition(this);
            defException =
                getClassDeclaration(idJavaLangException).getClassDefinition(this);
            defRemoteException =
                getClassDeclaration(idRemoteException).getClassDefinition(this);
            defCorbaObject =
                getClassDeclaration(idCorbaObject).getClassDefinition(this);
            defSerializable =
                getClassDeclaration(idJavaIoSerializable).getClassDefinition(this);
            defRuntimeException =
                getClassDeclaration(idJavaLangRuntimeException).getClassDefinition(this);
            defExternalizable =
                getClassDeclaration(idJavaIoExternalizable).getClassDefinition(this);
            defThrowable=
                getClassDeclaration(idJavaLangThrowable).getClassDefinition(this);
            defIDLEntity=
                getClassDeclaration(idIDLEntity).getClassDefinition(this);
            defValueBase=
                getClassDeclaration(idValueBase).getClassDefinition(this);
            typeRemoteException = defRemoteException.getClassDeclaration().getType();
            typeException = defException.getClassDeclaration().getType();
            typeIOException = getClassDeclaration(idJavaIoIOException).getType();
            typeThrowable = getClassDeclaration(idJavaLangThrowable).getType();
            classPathLoader = new ClassPathLoader(path);
        } catch (ClassNotFound e) {
            error(0, "rmic.class.not.found", e.name);
            throw new Error();
        }
    }
    public boolean getParseNonConforming () {
        return parseNonConforming;
    }
    public void setParseNonConforming (boolean parseEm) {
        if (parseEm && !parseNonConforming) {
            reset();
        }
        parseNonConforming = parseEm;
    }
    void setStandardPackage(boolean standardPackage) {
        this.standardPackage = standardPackage;
    }
    boolean getStandardPackage() {
        return standardPackage;
    }
    public void reset () {
        for (Enumeration e = allTypes.elements() ; e.hasMoreElements() ;) {
            Type type = (Type) e.nextElement();
            type.destroy();
        }
        for (Enumeration e = invalidTypes.keys() ; e.hasMoreElements() ;) {
            Type type = (Type) e.nextElement();
            type.destroy();
        }
        for (Iterator e = alreadyChecked.iterator() ; e.hasNext() ;) {
            Type type = (Type) e.next();
            type.destroy();
        }
        if (contextStack != null) contextStack.clear();
        if (nameContexts != null) {
            for (Enumeration e = nameContexts.elements() ; e.hasMoreElements() ;) {
                NameContext context = (NameContext) e.nextElement();
                context.clear();
            }
            nameContexts.clear();
        }
        allTypes.clear();
        invalidTypes.clear();
        alreadyChecked.clear();
        namesCache.clear();
        modulesContext.clear();
        loader = null;
        parseNonConforming = false;
    }
    public void shutdown() {
        if (alreadyChecked != null) {
            reset();
            alreadyChecked = null;
            allTypes = null;
            invalidTypes = null;
            nameContexts = null;
            namesCache = null;
            modulesContext = null;
            defRemote = null;
            defError = null;
            defException = null;
            defRemoteException = null;
            defCorbaObject = null;
            defSerializable = null;
            defExternalizable = null;
            defThrowable = null;
            defRuntimeException = null;
            defIDLEntity = null;
            defValueBase = null;
            typeRemoteException = null;
            typeIOException = null;
            typeException = null;
            typeThrowable = null;
            super.shutdown();
        }
    }
}
