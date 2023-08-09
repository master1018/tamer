public class FileDecodingImageSource extends DecodingImageSource {
  String filename;
  public FileDecodingImageSource(String file) {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkRead(file);
    }
    filename = file;
  }
  @Override
protected boolean checkConnection() {
      SecurityManager security = System.getSecurityManager();
      if (security != null) {
          try {
            security.checkRead(filename);
          } catch (SecurityException e) {
              return false;
          }
      }
      return true;
  }
  @Override
protected InputStream getInputStream() {
    try {
      return new BufferedInputStream(new FileInputStream(filename), 8192);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
