public class XmlConfigUtils {
    public static final String NAMESPACE =
            "jmx:com.sun.jmx.examples.scandir.config";
    private static final Logger LOG =
            Logger.getLogger(XmlConfigUtils.class.getName());
    private static JAXBContext context;
    final String file;
    public XmlConfigUtils(String file) {
        this.file = file;
    }
    public synchronized void writeToFile(ScanManagerConfig bean)
        throws IOException {
        final File f = newXmlTmpFile(file);
        try {
            final FileOutputStream out = new FileOutputStream(f);
            boolean failed = true;
            try {
                write(bean,out,false);
                failed = false;
            } finally {
                out.close();
                if (failed == true) f.delete();
            }
            commit(file,f);
        } catch (JAXBException x) {
            final IOException io =
                    new IOException("Failed to write SessionConfigBean to " +
                    file+": "+x,x);
            throw io;
        }
    }
    public static String toString(Object bean) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Marshaller m = createMarshaller();
            m.setProperty(m.JAXB_FRAGMENT,Boolean.TRUE);
            m.marshal(bean, baos);
            return baos.toString();
        } catch (JAXBException x) {
            final IllegalArgumentException iae =
                    new IllegalArgumentException(
                        "Failed to write SessionConfigBean: "+x,x);
            throw iae;
        }
    }
    public static ScanManagerConfig xmlClone(ScanManagerConfig bean) {
        final Object clone = copy(bean);
        return (ScanManagerConfig)clone;
    }
    private static Object copy(Object bean) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Marshaller m = createMarshaller();
            m.marshal(bean, baos);
            final ByteArrayInputStream bais =
                    new ByteArrayInputStream(baos.toByteArray());
            return createUnmarshaller().unmarshal(bais);
        } catch (JAXBException x) {
            final IllegalArgumentException iae =
                    new IllegalArgumentException("Failed to write SessionConfigBean: "+x,x);
            throw iae;
        }
    }
    public static DirectoryScannerConfig xmlClone(DirectoryScannerConfig bean) {
        final Object clone = copy(bean);
        return (DirectoryScannerConfig)clone;
    }
    public synchronized ScanManagerConfig readFromFile() throws IOException {
        final File f = new File(file);
        if (!f.exists())
            throw new IOException("No such file: "+file);
        if (!f.canRead())
            throw new IOException("Can't read file: "+file);
        try {
            return read(f);
        } catch (JAXBException x) {
            final IOException io =
                    new IOException("Failed to read SessionConfigBean from " +
                    file+": "+x,x);
            throw io;
        }
    }
    public static ScanManagerConfig read(File f)
        throws JAXBException {
        final Unmarshaller u = createUnmarshaller();
        return (ScanManagerConfig) u.unmarshal(f);
    }
    public static void write(ScanManagerConfig bean, OutputStream os,
            boolean fragment)
        throws JAXBException {
        writeXml((Object)bean,os,fragment);
    }
    public static void write(ResultRecord bean, OutputStream os, boolean fragment)
        throws JAXBException {
        writeXml((Object)bean,os,fragment);
    }
    private static void writeXml(Object bean, OutputStream os, boolean fragment)
        throws JAXBException {
        final Marshaller m = createMarshaller();
        if (fragment) m.setProperty(m.JAXB_FRAGMENT,Boolean.TRUE);
        m.marshal(bean,os);
    }
    private static Unmarshaller createUnmarshaller() throws JAXBException {
        return getContext().createUnmarshaller();
    }
    private static Marshaller createMarshaller() throws JAXBException {
        final  Marshaller m = getContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        return m;
    }
    private static synchronized JAXBContext getContext() throws JAXBException {
        if (context == null)
            context = JAXBContext.newInstance(ScanManagerConfig.class,
                                              ResultRecord.class);
        return context;
    }
    private static File newXmlTmpFile(String basename) throws IOException {
        final File f = new File(basename+".new");
        if (!f.createNewFile())
            throw new IOException("file "+f.getName()+" already exists");
        try {
            final OutputStream newStream = new FileOutputStream(f);
            try {
                final String decl =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
                newStream.write(decl.getBytes("UTF-8"));
                newStream.flush();
            } finally {
                newStream.close();
            }
        } catch (IOException x) {
            f.delete();
            throw x;
        }
        return f;
    }
    private static File commit(String basename, File tmpFile)
        throws IOException {
        try {
            final String backupName = basename+"~";
            final File desired = new File(basename);
            final File backup = new File(backupName);
            backup.delete();
            if (desired.exists()) {
                if (!desired.renameTo(new File(backupName)))
                    throw new IOException("can't rename to "+backupName);
            }
            if (!tmpFile.renameTo(new File(basename)))
                throw new IOException("can't rename to "+basename);
        } catch (IOException x) {
            tmpFile.delete();
            throw x;
        }
        return new File(basename);
    }
    public static File createNewXmlFile(String basename) throws IOException {
        return commit(basename,newXmlTmpFile(basename));
    }
}
