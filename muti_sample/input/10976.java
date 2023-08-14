public class ScanManagerConfig {
    private final Map<String, DirectoryScannerConfig> directoryScanners;
    private ResultLogConfig initialResultLogConfig;
    private String name;
    public ScanManagerConfig() {
        this(null,true);
    }
    public ScanManagerConfig(String name) {
        this(name,false);
    }
    private ScanManagerConfig(String name, boolean allowsNull) {
        if (name == null && allowsNull==false)
            throw new IllegalArgumentException("name=null");
        this.name = name;
        directoryScanners = new LinkedHashMap<String,DirectoryScannerConfig>();
        this.initialResultLogConfig = new ResultLogConfig();
        this.initialResultLogConfig.setMemoryMaxRecords(1024);
    }
    private Object[] toArray() {
        final Object[] thisconfig = {
            name,directoryScanners,initialResultLogConfig
        };
        return thisconfig;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ScanManagerConfig)) return false;
        final ScanManagerConfig other = (ScanManagerConfig)o;
        if (this.directoryScanners.size() != other.directoryScanners.size())
            return false;
        return Arrays.deepEquals(toArray(),other.toArray());
    }
    @Override
    public int hashCode() {
        final String key = name;
        if (key == null) return 0;
        else return key.hashCode();
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
    @XmlElementWrapper(name="DirectoryScannerList",
            namespace=XmlConfigUtils.NAMESPACE)
    @XmlElementRef
    public DirectoryScannerConfig[] getScanList() {
        return directoryScanners.values().toArray(new DirectoryScannerConfig[0]);
    }
    public void setScanList(DirectoryScannerConfig[] scans) {
        directoryScanners.clear();
        for (DirectoryScannerConfig scan : scans)
            directoryScanners.put(scan.getName(),scan);
    }
    public DirectoryScannerConfig getScan(String name) {
        return directoryScanners.get(name);
    }
    public DirectoryScannerConfig putScan(DirectoryScannerConfig scan) {
        return this.directoryScanners.put(scan.getName(),scan);
    }
    public String toString() {
        return XmlConfigUtils.toString(this);
    }
    public DirectoryScannerConfig removeScan(String name) {
       return this.directoryScanners.remove(name);
    }
    @XmlElement(name="InitialResultLogConfig",namespace=XmlConfigUtils.NAMESPACE)
    public ResultLogConfig getInitialResultLogConfig() {
        return this.initialResultLogConfig;
    }
    public void setInitialResultLogConfig(ResultLogConfig initialLogConfig) {
        this.initialResultLogConfig = initialLogConfig;
    }
    public ScanManagerConfig copy(String newname) {
        return copy(newname,this);
    }
    private static ScanManagerConfig
            copy(String newname, ScanManagerConfig other) {
        ScanManagerConfig newbean = XmlConfigUtils.xmlClone(other);
        newbean.name = newname;
        return newbean;
    }
}
