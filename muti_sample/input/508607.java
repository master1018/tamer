final public class XMLReaderFactory
{
    private XMLReaderFactory ()
    {
    }
    private static final String property = "org.xml.sax.driver";
    public static XMLReader createXMLReader ()
    throws SAXException
    {
    String        className = null;
    ClassLoader    loader = NewInstance.getClassLoader ();
    try { className = System.getProperty (property); }
    catch (RuntimeException e) {  }
    if (className == null) {
        try {
        String        service = "META-INF/services/" + property;
        InputStream    in;
        BufferedReader    reader;
        if (loader == null)
            in = ClassLoader.getSystemResourceAsStream (service);
        else
            in = loader.getResourceAsStream (service);
        if (in != null) {
            reader = new BufferedReader (
                new InputStreamReader (in, "UTF8"), 8192);
            className = reader.readLine ();
            in.close ();
        }
        } catch (Exception e) {
        }
    }
    if (className == null) {
    }
    if (className != null)
        return loadClass (loader, className);
    try {
        return new ParserAdapter (ParserFactory.makeParser ());
    } catch (Exception e) {
        throw new SAXException ("Can't create default XMLReader; "
            + "is system property org.xml.sax.driver set?");
    }
    }
    public static XMLReader createXMLReader (String className)
    throws SAXException
    {
    return loadClass (NewInstance.getClassLoader (), className);
    }
    private static XMLReader loadClass (ClassLoader loader, String className)
    throws SAXException
    {
    try {
        return (XMLReader) NewInstance.newInstance (loader, className);
    } catch (ClassNotFoundException e1) {
        throw new SAXException("SAX2 driver class " + className +
                   " not found", e1);
    } catch (IllegalAccessException e2) {
        throw new SAXException("SAX2 driver class " + className +
                   " found but cannot be loaded", e2);
    } catch (InstantiationException e3) {
        throw new SAXException("SAX2 driver class " + className +
       " loaded but cannot be instantiated (no empty public constructor?)",
                   e3);
    } catch (ClassCastException e4) {
        throw new SAXException("SAX2 driver class " + className +
                   " does not implement XMLReader", e4);
    }
    }
}
