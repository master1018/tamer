class UnixMountEntry {
    private byte[] name;        
    private byte[] dir;         
    private byte[] fstype;      
    private byte[] opts;        
    private long dev;           
    private volatile String fstypeAsString;
    private volatile String optionsAsString;
    UnixMountEntry() {
    }
    String name() {
        return new String(name);
    }
    String fstype() {
        if (fstypeAsString == null)
            fstypeAsString = new String(fstype);
        return fstypeAsString;
    }
    byte[] dir() {
        return dir;
    }
    long dev() {
        return dev;
    }
    boolean hasOption(String requested) {
        if (optionsAsString == null)
            optionsAsString = new String(opts);
        for (String opt: Util.split(optionsAsString, ',')) {
            if (opt.equals(requested))
                return true;
        }
        return false;
    }
    boolean isIgnored() {
        return hasOption("ignore");
    }
    boolean isReadOnly() {
        return hasOption("ro");
    }
}
