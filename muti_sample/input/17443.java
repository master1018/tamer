public abstract class FileLocator extends Object {
    static final Properties pp = System.getProperties ();
    static final String classPath = pp.getProperty ("java.class.path", ".");
    static final String pathSeparator = pp.getProperty ("path.separator", ";");
    public static DataInputStream locateClassFile (String classFileName)
        throws FileNotFoundException, IOException {
        boolean notFound = true;
        StringTokenizer st;
        String path = "";
        String pathNameForm;
        File cf = null;
        NamedDataInputStream result;
        st = new StringTokenizer (classPath, pathSeparator, false);
        pathNameForm = classFileName.replace ('.', File.separatorChar) +
            ".class";
        while (st.hasMoreTokens () && notFound) {
            try {path = st.nextToken ();}
                catch (NoSuchElementException nse) {break;}
            int pLen = path.length ();
            String pathLast4 = pLen > 3 ? path.substring (pLen - 4) : "";
            if (pathLast4.equalsIgnoreCase (".zip") ||
                pathLast4.equalsIgnoreCase (".jar")) {
                try {
                    result = locateInZipFile (path, classFileName, true, true);
                    if (result == null)
                        continue;
                    return (DataInputStream) result;
                } catch (ZipException zfe) {
                    continue;
                } catch (IOException ioe) {
                    continue;
                }
            } else {
                try {cf = new File (path + File.separator + pathNameForm);
                } catch (NullPointerException npe) { continue; }
                if ((cf != null) && cf.exists ())
                    notFound = false;
            }
        }
        if (notFound) {
            int lastdot = classFileName.lastIndexOf ('.');
            String simpleName =
                (lastdot >= 0) ? classFileName.substring (lastdot+1) :
                classFileName;
            result = new NamedDataInputStream (new BufferedInputStream (
               new FileInputStream (simpleName + ".class")),
                   simpleName + ".class", false);
            return (DataInputStream) result;
        }
        result = new NamedDataInputStream (new BufferedInputStream (
            new FileInputStream (cf)), path + File.separator + pathNameForm,
                false);
        return (DataInputStream) result;
    }
    public static DataInputStream locateLocaleSpecificFileInClassPath (
        String fileName) throws FileNotFoundException, IOException {
        String localeSuffix = "_" + Locale.getDefault ().toString ();
        int lastSlash = fileName.lastIndexOf ('/');
        int lastDot   = fileName.lastIndexOf ('.');
        String fnFront, fnEnd;
        DataInputStream result = null;
        boolean lastAttempt = false;
        if ((lastDot > 0) && (lastDot > lastSlash)) {
            fnFront = fileName.substring (0, lastDot);
            fnEnd   = fileName.substring (lastDot);
        } else {
            fnFront = fileName;
            fnEnd   = "";
        }
        while (true) {
            if (lastAttempt)
                result = locateFileInClassPath (fileName);
            else try {
                result = locateFileInClassPath (fnFront + localeSuffix + fnEnd);
            } catch (Exception e) {  }
            if ((result != null) || lastAttempt)
                break;
            int lastUnderbar = localeSuffix.lastIndexOf ('_');
            if (lastUnderbar > 0)
                localeSuffix = localeSuffix.substring (0, lastUnderbar);
            else
                lastAttempt = true;
        }
        return result;
    }
    public static DataInputStream locateFileInClassPath (String fileName)
        throws FileNotFoundException, IOException {
        boolean notFound = true;
        StringTokenizer st;
        String path = "";
        File cf = null;
        NamedDataInputStream result;
        String zipEntryName = File.separatorChar == '/' ? fileName :
            fileName.replace (File.separatorChar, '/');
        String localFileName = File.separatorChar == '/' ? fileName :
            fileName.replace ('/', File.separatorChar);
        st = new StringTokenizer (classPath, pathSeparator, false);
        while (st.hasMoreTokens () && notFound) {
            try {path = st.nextToken ();}
                catch (NoSuchElementException nse) {break;}
            int pLen = path.length ();
            String pathLast4 = pLen > 3 ? path.substring (pLen - 4) : "";
            if (pathLast4.equalsIgnoreCase (".zip") ||
                pathLast4.equalsIgnoreCase (".jar")) {
                try {
                    result = locateInZipFile (path, zipEntryName, false, false);
                    if (result == null)
                        continue;
                    return (DataInputStream) result;
                } catch (ZipException zfe) {
                    continue;
                } catch (IOException ioe) {
                    continue;
                }
            } else {
                try {cf = new File (path + File.separator + localFileName);
                } catch (NullPointerException npe) { continue; }
                if ((cf != null) && cf.exists ())
                    notFound = false;
            }
        }
        if (notFound) {
            int lastpart = localFileName.lastIndexOf (File.separator);
            String simpleName =
                (lastpart >= 0) ? localFileName.substring (lastpart+1) :
                localFileName;
            result = new NamedDataInputStream (new BufferedInputStream (
               new FileInputStream (simpleName)), simpleName, false);
            return (DataInputStream) result;
        }
        result = new NamedDataInputStream (new BufferedInputStream (
            new FileInputStream (cf)), path + File.separator + localFileName,
                false);
        return (DataInputStream) result;
    }
    public static String getFileNameFromStream (DataInputStream ds) {
        if (ds instanceof NamedDataInputStream)
            return ((NamedDataInputStream) ds).fullyQualifiedFileName;
        return "";
    }
    public static boolean isZipFileAssociatedWithStream (DataInputStream ds) {
        if (ds instanceof NamedDataInputStream)
            return ((NamedDataInputStream) ds).inZipFile;
        return false;
    }
    private static NamedDataInputStream locateInZipFile (String zipFileName,
        String fileName, boolean wantClass, boolean buffered)
        throws ZipException, IOException {
        ZipFile zf;
        ZipEntry ze;
        zf = new ZipFile (zipFileName);
        if (zf == null)
            return null;
        String zeName = wantClass ?
            fileName.replace ('.', '/') + ".class" :
            fileName;
            ze = zf.getEntry (zeName);
            if (ze == null) {
                zf.close(); 
                zf = null;
                return null;
            }
            InputStream istream = zf.getInputStream(ze);
            if (buffered)
                istream = new BufferedInputStream(istream);
            return new NamedDataInputStream (istream,
                    zipFileName + '(' + zeName + ')', true);
    }
}
 class NamedDataInputStream extends DataInputStream {
    public String fullyQualifiedFileName;
    public boolean inZipFile;
    protected NamedDataInputStream (InputStream in, String fullyQualifiedName,
        boolean inZipFile) {
        super (in);
        this.fullyQualifiedFileName = fullyQualifiedName;
        this.inZipFile = inZipFile;
    }
}
