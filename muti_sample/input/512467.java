public class URLDecodingImageSource extends DecodingImageSource {
    URL url;
    public URLDecodingImageSource(URL url){
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkConnect(url.getHost(), url.getPort());
            try {
                Permission p = url.openConnection().getPermission();
                security.checkPermission(p);
            } catch (IOException e) {
            }
        }
        this.url = url;
    }
    @Override
    protected boolean checkConnection() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkConnect(url.getHost(), url.getPort());
                return true;
            } catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }
    @Override
    protected InputStream getInputStream() {
        try{
            URLConnection uc = url.openConnection();
            return new BufferedInputStream(uc.getInputStream(), 8192);
        }catch(IOException e){
            return null;
        }
    }
}
