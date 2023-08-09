public class DirectoryScannerConfig {
    public enum Action {
        NOTIFY,
        DELETE,
        LOGRESULT };
    private String name;
    private String rootDirectory;
    private final List<FileMatch> includeFiles =
            new ArrayList<FileMatch>();
    private final List<FileMatch> excludeFiles =
            new ArrayList<FileMatch>();
    private Action[] actions = { Action.NOTIFY, Action.LOGRESULT };
    public DirectoryScannerConfig() {
        this(null);
    }
    public DirectoryScannerConfig(String name) {
        this.name = name;
        rootDirectory = null;
    }
    @XmlElement(name="RootDirectory",namespace=XmlConfigUtils.NAMESPACE)
    public String getRootDirectory() {
        return rootDirectory;
    }
    public void setRootDirectory(String root) {
        rootDirectory=root;
    }
    @XmlAttribute(name="name",required=true)
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        if (this.name == null)
            this.name = name;
        else if (name == null)
            throw new IllegalArgumentException("name=null");
        else if (!name.equals(this.name))
            throw new IllegalArgumentException("name="+name);
    }
    @XmlElementWrapper(name="IncludeFiles",
            namespace=XmlConfigUtils.NAMESPACE)
    @XmlElementRef
    public FileMatch[] getIncludeFiles() {
        synchronized(includeFiles) {
            return includeFiles.toArray(new FileMatch[0]);
        }
    }
    public void addIncludeFiles(FileMatch include) {
        if (include == null)
            throw new IllegalArgumentException("null");
        synchronized (includeFiles) {
            includeFiles.add(include);
        }
    }
    public void setIncludeFiles(FileMatch[] includeFiles) {
        synchronized (this.includeFiles) {
            this.includeFiles.clear();
            if (includeFiles == null) return;
            this.includeFiles.addAll(Arrays.asList(includeFiles));
        }
    }
    @XmlElementWrapper(name="ExcludeFiles",
            namespace=XmlConfigUtils.NAMESPACE)
    @XmlElementRef
    public FileMatch[] getExcludeFiles() {
        synchronized(excludeFiles) {
            return excludeFiles.toArray(new FileMatch[0]);
        }
    }
    public void setExcludeFiles(FileMatch[] excludeFiles) {
        synchronized (this.excludeFiles) {
            this.excludeFiles.clear();
            if (excludeFiles == null) return;
            this.excludeFiles.addAll(Arrays.asList(excludeFiles));
        }
    }
    public void addExcludeFiles(FileMatch exclude) {
        if (exclude == null)
            throw new IllegalArgumentException("null");
        synchronized (excludeFiles) {
            this.excludeFiles.add(exclude);
        }
    }
    @XmlElement(name="Actions",namespace=XmlConfigUtils.NAMESPACE)
    @XmlList
    public Action[] getActions() {
       return  (actions == null)?null:actions.clone();
    }
    public void setActions(Action[] actions) {
        this.actions = (actions == null)?null:actions.clone();
    }
    public FileFilter buildFileFilter() {
        final FileFilter[] ins = getIncludeFiles();
        final FileFilter[] outs = getExcludeFiles();
        final FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                boolean result = false;
                if (ins != null) {
                    for (FileFilter in: ins) {
                        if (!in.accept(f)) continue;
                        result=true;
                        break;
                    }
                } else result= true;
                if (result == false) return false;
                if (outs != null) {
                    for (FileFilter out: outs) {
                        if (!out.accept(f)) continue;
                        result=false;
                        break;
                    }
                }
                return result;
            }
        };
        return filter;
    }
    private Object[] toArray() {
        final Object[] thisconfig = {
            name,rootDirectory,actions,excludeFiles,includeFiles
        };
        return thisconfig;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DirectoryScannerConfig)) return false;
        final DirectoryScannerConfig other = (DirectoryScannerConfig)o;
        final Object[] thisconfig = toArray();
        final Object[] otherconfig = other.toArray();
        return Arrays.deepEquals(thisconfig,otherconfig);
    }
    @Override
    public int hashCode() {
        final String key = name;
        if (key == null) return 0;
        else return key.hashCode();
    }
}
