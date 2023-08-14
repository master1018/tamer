public class IOExceptionIfEncodedURLTest extends Applet{
    public void init(){
    }
    public void start(){
        System.err.println("the appletviewer started");
    }
    static String url = "file:IOExceptionIfEncodedURLTest.java";
    public static final void main(String args[])
      throws MalformedURLException{
        System.err.println("prior checking...");
        String prefix = "file:";
        String path = ParseUtil.fileToEncodedURL(new File(System.getProperty("user.dir"))).getPath();
        String filename = url.substring(prefix.length());
        System.err.println("url="+url+" -> path="+path+",filename="+filename);
        if (!path.endsWith("/") && !filename.startsWith("/")) {
            throw new RuntimeException("Incorrect '/' processing");
        }
    }
}
