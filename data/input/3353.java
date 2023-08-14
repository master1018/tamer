class ZipEntry implements ZipConstants, Cloneable {
    String name;        
    long time = -1;     
    long crc = -1;      
    long size = -1;     
    long csize = -1;    
    int method = -1;    
    int flag = 0;       
    byte[] extra;       
    String comment;     
    public static final int STORED = 0;
    public static final int DEFLATED = 8;
    public ZipEntry(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.length() > 0xFFFF) {
            throw new IllegalArgumentException("entry name too long");
        }
        this.name = name;
    }
    public ZipEntry(ZipEntry e) {
        name = e.name;
        time = e.time;
        crc = e.crc;
        size = e.size;
        csize = e.csize;
        method = e.method;
        flag = e.flag;
        extra = e.extra;
        comment = e.comment;
    }
    ZipEntry() {}
    public String getName() {
        return name;
    }
    public void setTime(long time) {
        this.time = javaToDosTime(time);
    }
    public long getTime() {
        return time != -1 ? dosToJavaTime(time) : -1;
    }
    public void setSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("invalid entry size");
        }
        this.size = size;
    }
    public long getSize() {
        return size;
    }
    public long getCompressedSize() {
        return csize;
    }
    public void setCompressedSize(long csize) {
        this.csize = csize;
    }
    public void setCrc(long crc) {
        if (crc < 0 || crc > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("invalid entry crc-32");
        }
        this.crc = crc;
    }
    public long getCrc() {
        return crc;
    }
    public void setMethod(int method) {
        if (method != STORED && method != DEFLATED) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }
    public int getMethod() {
        return method;
    }
    public void setExtra(byte[] extra) {
        if (extra != null && extra.length > 0xFFFF) {
            throw new IllegalArgumentException("invalid extra field length");
        }
        this.extra = extra;
    }
    public byte[] getExtra() {
        return extra;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return comment;
    }
    public boolean isDirectory() {
        return name.endsWith("/");
    }
    public String toString() {
        return getName();
    }
    private static long dosToJavaTime(long dtime) {
        Date d = new Date((int)(((dtime >> 25) & 0x7f) + 80),
                          (int)(((dtime >> 21) & 0x0f) - 1),
                          (int)((dtime >> 16) & 0x1f),
                          (int)((dtime >> 11) & 0x1f),
                          (int)((dtime >> 5) & 0x3f),
                          (int)((dtime << 1) & 0x3e));
        return d.getTime();
    }
    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return (1 << 21) | (1 << 16);
        }
        return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
               d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
               d.getSeconds() >> 1;
    }
    public int hashCode() {
        return name.hashCode();
    }
    public Object clone() {
        try {
            ZipEntry e = (ZipEntry)super.clone();
            e.extra = (extra == null) ? null : extra.clone();
            return e;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
