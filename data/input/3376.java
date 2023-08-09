public class ResultLogConfig {
    private String logFileName;
    private long logFileMaxRecords;
    private int memoryMaxRecords;
    public ResultLogConfig() {
    }
    @XmlElement(name="LogFileName",namespace=XmlConfigUtils.NAMESPACE)
    public String getLogFileName() {
        return this.logFileName;
    }
    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }
    @XmlElement(name="LogFileMaxRecords",namespace=XmlConfigUtils.NAMESPACE)
    public long getLogFileMaxRecords() {
        return this.logFileMaxRecords;
    }
    public void setLogFileMaxRecords(long logFileMaxRecords) {
        this.logFileMaxRecords = logFileMaxRecords;
    }
    @XmlElement(name="MemoryMaxRecords",namespace=XmlConfigUtils.NAMESPACE)
    public int getMemoryMaxRecords() {
        return this.memoryMaxRecords;
    }
    public void setMemoryMaxRecords(int memoryMaxRecords) {
        this.memoryMaxRecords = memoryMaxRecords;
    }
    private Object[] toArray() {
        final Object[] thisconfig = {
            memoryMaxRecords,logFileMaxRecords,logFileName
        };
        return thisconfig;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ResultLogConfig)) return false;
        final ResultLogConfig other = (ResultLogConfig)o;
        return Arrays.deepEquals(toArray(),other.toArray());
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(toArray());
    }
}
