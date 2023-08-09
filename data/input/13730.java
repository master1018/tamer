public class FileMatch implements FileFilter {
    private String directoryPattern;
    private String filePattern;
    private long sizeExceedsMaxBytes;
    private Date lastModifiedAfter;
    private Date lastModifiedBefore;
    public FileMatch() {
    }
    @XmlElement(name="DirectoryPattern",namespace=XmlConfigUtils.NAMESPACE)
    public String getDirectoryPattern() {
        return this.directoryPattern;
    }
    public void setDirectoryPattern(String directoryPattern) {
        this.directoryPattern = directoryPattern;
    }
    @XmlElement(name="FilePattern",namespace=XmlConfigUtils.NAMESPACE)
    public String getFilePattern() {
        return this.filePattern;
    }
    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }
    @XmlElement(name="SizeExceedsMaxBytes",namespace=XmlConfigUtils.NAMESPACE)
    public long getSizeExceedsMaxBytes() {
        return this.sizeExceedsMaxBytes;
    }
    public void setSizeExceedsMaxBytes(long sizeLimitInBytes) {
        this.sizeExceedsMaxBytes = sizeLimitInBytes;
    }
    @XmlElement(name="LastModifiedAfter",namespace=XmlConfigUtils.NAMESPACE)
    public Date getLastModifiedAfter() {
        return (lastModifiedAfter==null)?null:(Date)lastModifiedAfter.clone();
    }
    public void setLastModifiedAfter(Date lastModifiedAfter) {
        this.lastModifiedAfter =
                (lastModifiedAfter==null)?null:(Date)lastModifiedAfter.clone();
    }
    @XmlElement(name="LastModifiedBefore",namespace=XmlConfigUtils.NAMESPACE)
    public Date getLastModifiedBefore() {
        return (lastModifiedBefore==null)?null:(Date)lastModifiedBefore.clone();
    }
    public void setLastModifiedBefore(Date lastModifiedBefore) {
        this.lastModifiedBefore =
             (lastModifiedBefore==null)?null:(Date)lastModifiedBefore.clone();
    }
    public boolean accept(File f) {
        if (f.isDirectory()) {
            if (directoryPattern != null
                && !f.getName().matches(directoryPattern))
                return false;
            else return true;
        }
        if (filePattern != null
                && !f.getName().matches(filePattern))
            return false;
        if (sizeExceedsMaxBytes > 0 && f.length() <= sizeExceedsMaxBytes)
            return false;
        if (lastModifiedAfter != null &&
                lastModifiedAfter.after(new Date(f.lastModified())))
            return false;
        if (lastModifiedBefore != null &&
                lastModifiedBefore.before(new Date(f.lastModified())))
            return false;
        return true;
    }
    private Object[] toArray() {
        final Object[] thisconfig = {
            directoryPattern, filePattern, lastModifiedAfter,
            lastModifiedBefore, sizeExceedsMaxBytes
        };
        return thisconfig;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FileMatch)) return false;
        final FileMatch other = (FileMatch)o;
        final Object[] thisconfig = toArray();
        final Object[] otherconfig = other.toArray();
        return Arrays.deepEquals(thisconfig,otherconfig);
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(toArray());
    }
}
