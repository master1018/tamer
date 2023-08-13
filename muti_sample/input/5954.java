public class ParserDelegator extends HTMLEditorKit.Parser implements Serializable {
    private static final Object DTD_KEY = new Object();
    protected static void setDefaultDTD() {
        getDefaultDTD();
    }
    private static synchronized DTD getDefaultDTD() {
        AppContext appContext = AppContext.getAppContext();
        DTD dtd = (DTD) appContext.get(DTD_KEY);
        if (dtd == null) {
            DTD _dtd = null;
            String nm = "html32";
            try {
                _dtd = DTD.getDTD(nm);
            } catch (IOException e) {
                System.out.println("Throw an exception: could not get default dtd: " + nm);
            }
            dtd = createDTD(_dtd, nm);
            appContext.put(DTD_KEY, dtd);
        }
        return dtd;
    }
    protected static DTD createDTD(DTD dtd, String name) {
        InputStream in = null;
        boolean debug = true;
        try {
            String path = name + ".bdtd";
            in = getResourceAsStream(path);
            if (in != null) {
                dtd.read(new DataInputStream(new BufferedInputStream(in)));
                dtd.putDTDHash(name, dtd);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return dtd;
    }
    public ParserDelegator() {
        setDefaultDTD();
    }
    public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
        new DocumentParser(getDefaultDTD()).parse(r, cb, ignoreCharSet);
    }
    static InputStream getResourceAsStream(String name) {
        try {
            return ResourceLoader.getResourceAsStream(name);
        } catch (Throwable e) {
            return ParserDelegator.class.getResourceAsStream(name);
        }
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        setDefaultDTD();
    }
}
