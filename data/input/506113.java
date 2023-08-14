public class CSUtil {
  private CSUtil() { }
  public static final String HDF_LOADPATHS = "hdf.loadpaths";
  public static List<String> getLoadPaths(HDF hdf) {
    List<String> list = new LinkedList<String>();
    HDF loadpathsHdf = hdf.getObj(HDF_LOADPATHS);
    if (loadpathsHdf == null) {
      throw new NullPointerException("No HDF loadpaths located in the specified"
          + " HDF structure");
    }
    for (HDF lpHdf = loadpathsHdf.objChild(); lpHdf != null;
        lpHdf = lpHdf.objNext()) {
      list.add(lpHdf.objValue());
    }
    return list;
  }
  public static File locateFile(List<String> loadpaths, String filename) {
    if (filename == null) {
      throw new NullPointerException("No filename provided");
    }
    if (loadpaths == null) {
      throw new NullPointerException("No loadpaths provided.");
    }
    for (String path : loadpaths) {
      File file = new File(path, filename);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }
}
