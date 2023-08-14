public class HDF {
  long hdfptr;  
  HDF root;    
  static {
    JNI.loadLibrary();
  }
  public HDF() {
    hdfptr = _init();
    root = null;
  }
  private HDF(long hdfptr, HDF parent) {
    this.hdfptr = hdfptr;
    this.root = (parent.root != null) ? parent.root : parent;
  }
  public void close() {
    if ( root == null) {
      if (hdfptr != 0) {
        _dealloc(hdfptr);
        hdfptr = 0;
      }
    }
  }
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }
  public boolean readFile(String filename) throws IOException,
         FileNotFoundException {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _readFile(hdfptr, filename, fileLoader != null);
  }
  protected String fileLoad(String filename) throws IOException,
            FileNotFoundException {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    CSFileLoader aFileLoader = fileLoader;
    if (aFileLoader == null) {
      throw new NullPointerException("No fileLoader specified.");
    } else {
      String result = aFileLoader.load(this, filename);
      if (result == null) {
        throw new NullPointerException("CSFileLoader.load() returned null");
      }
      return result;
    }
  }
  private CSFileLoader fileLoader = null;
  public CSFileLoader getFileLoader() {
    return fileLoader;
  }
  public void setFileLoader(CSFileLoader fileLoader) {
    this.fileLoader = fileLoader;
  }
  public boolean writeFile(String filename) throws IOException {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _writeFile(hdfptr, filename);
  }
  public boolean writeFileAtomic(String filename) throws IOException {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _writeFileAtomic(hdfptr, filename);
  }
  public boolean readString(String data) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _readString(hdfptr, data);
  }
  public String writeString() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _writeString(hdfptr);
  }
  public int getIntValue(String hdfname, int default_value) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _getIntValue(hdfptr,hdfname,default_value);
  }
  public String getValue(String hdfname, String default_value) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _getValue(hdfptr,hdfname,default_value);
  }
  public void setValue(String hdfname, String value) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    _setValue(hdfptr,hdfname,value);
  }
  public void removeTree(String hdfname) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    _removeTree(hdfptr,hdfname);
  }
  public void setSymLink(String hdf_name_src, String hdf_name_dest) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    _setSymLink(hdfptr,hdf_name_src,hdf_name_dest);
  }
  public void exportDate(String hdfname, TimeZone timeZone, Date date) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    Calendar cal = Calendar.getInstance(timeZone);
    cal.setTime(date);
    String sec = Integer.toString(cal.get(Calendar.SECOND));
    setValue(hdfname + ".sec", sec.length() == 1 ? "0" + sec : sec);
    String min = Integer.toString(cal.get(Calendar.MINUTE));
    setValue(hdfname + ".min", min.length() == 1 ? "0" + min : min);
    setValue(hdfname + ".24hour",
             Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
    setValue(hdfname + ".hour",
             Integer.toString(
                 cal.get(Calendar.HOUR) == 0 ? 12 : cal.get(Calendar.HOUR)));
    setValue(hdfname + ".am",
             cal.get(Calendar.AM_PM) == Calendar.AM ? "1" : "0");
    setValue(hdfname + ".mday",
             Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
    setValue(hdfname + ".mon",
             Integer.toString(cal.get(Calendar.MONTH)+1));
    setValue(hdfname + ".year",
             Integer.toString(cal.get(Calendar.YEAR)));
    setValue(hdfname + ".2yr",
             Integer.toString(cal.get(Calendar.YEAR)).substring(2));
    setValue(hdfname + ".wday",
             Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
    boolean tzNegative = timeZone.getRawOffset() < 0;
    int tzAbsolute = java.lang.Math.abs(timeZone.getRawOffset()/1000);
    String tzHour = Integer.toString(tzAbsolute/3600);
    String tzMin = Integer.toString(tzAbsolute/60 - (tzAbsolute/3600)*60);
    String tzString = (tzNegative ? "-" : "+")
                      + (tzHour.length() == 1 ? "0" + tzHour : tzHour)
                      + (tzMin.length() == 1 ? "0" + tzMin : tzMin);
    setValue(hdfname + ".tzoffset", tzString);
  }
  public void exportDate(String hdfname, String tz, int tt) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    TimeZone timeZone = TimeZone.getTimeZone(tz);
    if (timeZone == null) {
      throw new RuntimeException("Unknown timezone: " + tz);
    }
    Date date = new Date((long)tt * 1000);
    exportDate(hdfname, timeZone, date);
  }
  public HDF getObj(String hdfpath) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    long obj_ptr = _getObj(hdfptr, hdfpath);
    if ( obj_ptr == 0 ) {
      return null;
    }
    return new HDF(obj_ptr, this);
  }
  public HDF getChild(String hdfpath) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    long obj_ptr = _getChild(hdfptr, hdfpath);
    if ( obj_ptr == 0 ) {
      return null;
    }
    return new HDF(obj_ptr, this);
  }
  public HDF getRootObj() {
    return root != null ? root : this;
  }
  public HDF getOrCreateObj(String hdfpath) {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    long obj_ptr = _getObj(hdfptr, hdfpath);
    if ( obj_ptr == 0 ) {
      _setValue(hdfptr, hdfpath, "");
      obj_ptr = _getObj( hdfptr, hdfpath );
      if ( obj_ptr == 0 ) {
        return null;
      }
    }
    return new HDF(obj_ptr, this);
  }
  public String objName() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _objName(hdfptr);
  }
  public String objValue() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _objValue(hdfptr);
  }
  public HDF objChild() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    long child_ptr = _objChild(hdfptr);
    if ( child_ptr == 0 ) {
      return null;
    }
    return new HDF(child_ptr, this);
  }
  public HDF objNext() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    long next_ptr = _objNext(hdfptr);
    if ( next_ptr == 0 ) {
      return null;
    }
    return new HDF(next_ptr, this);
  }
  public void copy(String hdfpath, HDF src) {
    if (hdfptr == 0 || src.hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    _copy(hdfptr, hdfpath, src.hdfptr);
  }
  public String dump() {
    if (hdfptr == 0) {
      throw new NullPointerException("HDF is closed.");
    }
    return _dump(hdfptr);
  }
  private static native long _init();
  private static native void _dealloc(long ptr);
  private native boolean _readFile(long ptr, String filename, boolean use_cb);
  private static native boolean _writeFile(long ptr, String filename);
  private static native boolean _writeFileAtomic(long ptr, String filename);
  private static native boolean _readString(long ptr, String data);
  private static native String _writeString(long ptr);
  private static native int _getIntValue(long ptr, String hdfname,
                                         int default_value);
  private static native String _getValue(long ptr, String hdfname,
                                         String default_value);
  private static native void _setValue(long ptr, String hdfname,
                                       String hdf_value);
  private static native void _removeTree(long ptr, String hdfname);
  private static native void _setSymLink(long ptr, String hdf_name_src,
                                       String hdf_name_dest);
  private static native long _getObj(long ptr, String hdfpath);
  private static native long _getChild(long ptr, String hdfpath);
  private static native long _objChild(long ptr);
  private static native long _objNext(long ptr);
  private static native String _objName(long ptr);
  private static native String _objValue(long ptr);
  private static native void _copy(long destptr, String hdfpath, long srcptr);
  private static native String _dump(long ptr);
}
