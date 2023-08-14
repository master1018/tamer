public class ExtCheck {
    private static final boolean DEBUG = false;
    private String targetSpecTitle;
    private String targetSpecVersion;
    private String targetSpecVendor;
    private String targetImplTitle;
    private String targetImplVersion;
    private String targetImplVendor;
    private String targetsealed;
    private boolean verboseFlag;
    static ExtCheck create(File targetFile, boolean verbose) {
        return new ExtCheck(targetFile, verbose);
    }
    private ExtCheck(File targetFile, boolean verbose) {
        verboseFlag = verbose;
        investigateTarget(targetFile);
    }
    private void investigateTarget(File targetFile) {
        verboseMessage("Target file:" + targetFile);
        Manifest targetManifest = null;
        try {
            File canon = new File(targetFile.getCanonicalPath());
            URL url = ParseUtil.fileToEncodedURL(canon);
            if (url != null){
                JarLoader loader = new JarLoader(url);
                JarFile jarFile = loader.getJarFile();
                targetManifest = jarFile.getManifest();
            }
        } catch (MalformedURLException e){
            error("Malformed URL ");
        } catch (IOException e) {
            error("IO Exception ");
        }
        if (targetManifest == null)
            error("No manifest available in "+targetFile);
        Attributes attr = targetManifest.getMainAttributes();
        if (attr != null) {
            targetSpecTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
            targetSpecVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            targetSpecVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
            targetImplTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
            targetImplVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            targetImplVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            targetsealed      = attr.getValue(Name.SEALED);
        } else {
            error("No attributes available in the manifest");
        }
        if (targetSpecTitle == null)
            error("The target file does not have a specification title");
        if (targetSpecVersion == null)
            error("The target file does not have a specification version");
        verboseMessage("Specification title:" + targetSpecTitle);
        verboseMessage("Specification version:" + targetSpecVersion);
        if (targetSpecVendor != null)
            verboseMessage("Specification vendor:" + targetSpecVendor);
        if (targetImplVersion != null)
            verboseMessage("Implementation version:" + targetImplVersion);
        if (targetImplVendor != null)
            verboseMessage("Implementation vendor:" + targetImplVendor);
        verboseMessage("");
    }
    boolean checkInstalledAgainstTarget(){
        String s = System.getProperty("java.ext.dirs");
        File [] dirs;
        if (s != null) {
            StringTokenizer st =
                new StringTokenizer(s, File.pathSeparator);
            int count = st.countTokens();
            dirs = new File[count];
            for (int i = 0; i < count; i++) {
                dirs[i] = new File(st.nextToken());
            }
        } else {
            dirs = new File[0];
        }
        boolean result = true;
        for (int i = 0; i < dirs.length; i++) {
            String[] files = dirs[i].list();
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    try {
                        File f = new File(dirs[i],files[j]);
                        File canon = new File(f.getCanonicalPath());
                        URL url = ParseUtil.fileToEncodedURL(canon);
                        if (url != null){
                            result = result && checkURLRecursively(1,url);
                        }
                    } catch (MalformedURLException e){
                        error("Malformed URL");
                    } catch (IOException e) {
                        error("IO Exception");
                    }
                }
            }
        }
        if (result) {
            generalMessage("No conflicting installed jar found.");
        } else {
            generalMessage("Conflicting installed jar found. "
                           + " Use -verbose for more information.");
        }
        return result;
    }
    private boolean checkURLRecursively(int indent, URL url)
        throws IOException
    {
        verboseMessage("Comparing with " + url);
        JarLoader jarloader = new JarLoader(url);
        JarFile j = jarloader.getJarFile();
        Manifest man = j.getManifest();
        if (man != null) {
            Attributes attr = man.getMainAttributes();
            if (attr != null){
                String title   = attr.getValue(Name.SPECIFICATION_TITLE);
                String version = attr.getValue(Name.SPECIFICATION_VERSION);
                String vendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
                String implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
                String implVersion
                    = attr.getValue(Name.IMPLEMENTATION_VERSION);
                String implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
                String sealed      = attr.getValue(Name.SEALED);
                if (title != null){
                    if (title.equals(targetSpecTitle)){
                        if (version != null){
                            if (version.equals(targetSpecVersion) ||
                                isNotOlderThan(version,targetSpecVersion)){
                                verboseMessage("");
                                verboseMessage("CONFLICT DETECTED ");
                                verboseMessage("Conflicting file:"+ url);
                                verboseMessage("Installed Version:" +
                                               version);
                                if (implTitle != null)
                                    verboseMessage("Implementation Title:"+
                                                   implTitle);
                                if (implVersion != null)
                                    verboseMessage("Implementation Version:"+
                                                   implVersion);
                                if (implVendor != null)
                                    verboseMessage("Implementation Vendor:"+
                                                   implVendor);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        boolean result = true;
        URL[] loaderList = jarloader.getClassPath();
        if (loaderList != null) {
            for(int i=0; i < loaderList.length; i++){
                if (url != null){
                    boolean res =  checkURLRecursively(indent+1,loaderList[i]);
                    result = res && result;
                }
            }
        }
        return result;
    }
    private boolean isNotOlderThan(String already,String target)
        throws NumberFormatException
    {
            if (already == null || already.length() < 1) {
            throw new NumberFormatException("Empty version string");
        }
            StringTokenizer dtok = new StringTokenizer(target, ".", true);
            StringTokenizer stok = new StringTokenizer(already, ".", true);
        while (dtok.hasMoreTokens() || stok.hasMoreTokens()) {
            int dver;
            int sver;
            if (dtok.hasMoreTokens()) {
                dver = Integer.parseInt(dtok.nextToken());
            } else
                dver = 0;
            if (stok.hasMoreTokens()) {
                sver = Integer.parseInt(stok.nextToken());
            } else
                sver = 0;
                if (sver < dver)
                        return false;                
                if (sver > dver)
                        return true;                
                if (dtok.hasMoreTokens())
                        dtok.nextToken();
                if (stok.hasMoreTokens())
                        stok.nextToken();
            }
        return true;
    }
    void verboseMessage(String message){
        if (verboseFlag) {
            System.err.println(message);
        }
    }
    void generalMessage(String message){
        System.err.println(message);
    }
    static void error(String message) throws RuntimeException {
        throw new RuntimeException(message);
    }
    private static class JarLoader {
        private final URL base;
        private JarFile jar;
        private URL csu;
        JarLoader(URL url) {
            String urlName = url + "!/";
            URL tmpBaseURL = null;
            try {
                tmpBaseURL = new URL("jar","",urlName);
                jar = findJarFile(url);
                csu = url;
            } catch (MalformedURLException e) {
                ExtCheck.error("Malformed url "+urlName);
            } catch (IOException e) {
                ExtCheck.error("IO Exception occurred");
            }
            base = tmpBaseURL;
        }
        URL getBaseURL() {
            return base;
        }
        JarFile getJarFile() {
            return jar;
        }
        private JarFile findJarFile(URL url) throws IOException {
             if ("file".equals(url.getProtocol())) {
                 String path = url.getFile().replace('/', File.separatorChar);
                 File file = new File(path);
                 if (!file.exists()) {
                     throw new FileNotFoundException(path);
                 }
                 return new JarFile(path);
             }
             URLConnection uc = getBaseURL().openConnection();
             return ((JarURLConnection)uc).getJarFile();
         }
        URL[] getClassPath() throws IOException {
            Manifest man = jar.getManifest();
            if (man != null) {
                Attributes attr = man.getMainAttributes();
                if (attr != null) {
                    String value = attr.getValue(Name.CLASS_PATH);
                    if (value != null) {
                        return parseClassPath(csu, value);
                    }
                }
            }
            return null;
        }
        private URL[] parseClassPath(URL base, String value)
            throws MalformedURLException
        {
            StringTokenizer st = new StringTokenizer(value);
            URL[] urls = new URL[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                String path = st.nextToken();
                urls[i] = new URL(base, path);
                i++;
            }
            return urls;
        }
    }
}
