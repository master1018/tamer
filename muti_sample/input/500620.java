public class CS {
  public long csptr;
  protected HDF globalHDF;
  protected HDF localHDF;
  static {
    JNI.loadLibrary();
  }
  public CS(HDF ho) {
    this.globalHDF = null;
    this.localHDF = ho;
    csptr = _init(ho.hdfptr);
  }
  public CS(HDF ho, HDF global) {
    this(ho);
    this.globalHDF = global;
    if (global != null) {
      _setGlobalHdf(csptr,global.hdfptr);
    }
  }
  public void setGlobalHDF(HDF global) {
    _setGlobalHdf(csptr,global.hdfptr);
    this.globalHDF = global;
  }
  public HDF getGlobalHDF() {
    return this.globalHDF;
  }
  public void close() {
    if (csptr != 0) {
      _dealloc(csptr);
      csptr = 0;
    }
  }
  public void finalize() {
    close();
  }
  public void parseFile(String filename) {
    if (csptr == 0) {
      throw new NullPointerException("CS is closed.");
    }
    _parseFile(csptr, filename, fileLoader != null);
  }
  public void parseStr(String content) {
    if (csptr == 0) {
      throw new NullPointerException("CS is closed.");
    }
    _parseStr(csptr,content);
  }
  public String render() {
    if (csptr == 0) {
      throw new NullPointerException("CS is closed.");
    }
    return _render(csptr);
  }
  protected String fileLoad(String filename) throws IOException,
            FileNotFoundException {
    if (csptr == 0) {
      throw new NullPointerException("CS is closed.");
    }
    CSFileLoader aFileLoader = fileLoader;
    if (aFileLoader == null) {
      throw new NullPointerException("No fileLoader specified.");
    } else {
      String result = aFileLoader.load(localHDF, filename);
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
  private native long    _init(long ptr);
  private native void   _dealloc(long ptr);
  private native void   _parseFile(long ptr, String filename,
                                   boolean use_cb);
  private native void   _parseStr(long ptr, String content);
  private native String _render(long ptr);
  private native void   _setGlobalHdf(long csptr, long hdfptr);
};
