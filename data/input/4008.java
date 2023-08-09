public class ResultRecord {
    private String filename;
    private Date date;
    private String directoryScanner;
    private Action[] actions;
    public ResultRecord() {
    }
    public ResultRecord(DirectoryScannerConfig scan, Action[] actions,
                     File f) {
        directoryScanner = scan.getName();
        this.actions = actions;
        date = new Date();
        filename = f.getAbsolutePath();
    }
    @XmlElement(name="Filename",namespace=XmlConfigUtils.NAMESPACE)
    public String getFilename() {
        return this.filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    @XmlElement(name="Date",namespace=XmlConfigUtils.NAMESPACE)
    public Date getDate() {
        synchronized(this) {
            return (date==null)?null:(new Date(date.getTime()));
        }
    }
    public void setDate(Date date) {
        synchronized (this) {
            this.date = (date==null)?null:(new Date(date.getTime()));
        }
    }
    @XmlElement(name="DirectoryScanner",namespace=XmlConfigUtils.NAMESPACE)
    public String getDirectoryScanner() {
        return this.directoryScanner;
    }
    public void setDirectoryScanner(String directoryScanner) {
        this.directoryScanner = directoryScanner;
    }
    @XmlElement(name="Actions",namespace=XmlConfigUtils.NAMESPACE)
    @XmlList
    public Action[] getActions() {
        return (actions == null)?null:actions.clone();
    }
    public void setActions(Action[] actions) {
        this.actions = (actions == null)?null:actions.clone();
    }
    private Object[] toArray() {
        final Object[] thisconfig = {
            filename, date, directoryScanner, actions
        };
        return thisconfig;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultRecord)) return false;
        return Arrays.deepEquals(toArray(),((ResultRecord)o).toArray());
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(toArray());
    }
}
