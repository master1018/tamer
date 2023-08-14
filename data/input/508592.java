public class ParserAdapter implements XMLReader, DocumentHandler
{
    public ParserAdapter ()
      throws SAXException
    {
    super();
    String driver = System.getProperty("org.xml.sax.parser");
    try {
        setup(ParserFactory.makeParser());
    } catch (ClassNotFoundException e1) {
        throw new
        SAXException("Cannot find SAX1 driver class " +
                 driver, e1);
    } catch (IllegalAccessException e2) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " found but cannot be loaded", e2);
    } catch (InstantiationException e3) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " loaded but cannot be instantiated", e3);
    } catch (ClassCastException e4) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " does not implement org.xml.sax.Parser");
    } catch (NullPointerException e5) {
        throw new 
        SAXException("System property org.xml.sax.parser not specified");
    }
    }
    public ParserAdapter (Parser parser)
    {
    super();
    setup(parser);
    }
    private void setup (Parser parser)
    {
    if (parser == null) {
        throw new
        NullPointerException("Parser argument must not be null");
    }
    this.parser = parser;
    atts = new AttributesImpl();
    nsSupport = new NamespaceSupport();
    attAdapter = new AttributeListAdapter();
    }
    private final static String FEATURES = "http:
    private final static String NAMESPACES = FEATURES + "namespaces";
    private final static String NAMESPACE_PREFIXES = FEATURES + "namespace-prefixes";
    private final static String XMLNS_URIs = FEATURES + "xmlns-uris";
    public void setFeature (String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    if (name.equals(NAMESPACES)) {
        checkNotParsing("feature", name);
        namespaces = value;
        if (!namespaces && !prefixes) {
        prefixes = true;
        }
    } else if (name.equals(NAMESPACE_PREFIXES)) {
        checkNotParsing("feature", name);
        prefixes = value;
        if (!prefixes && !namespaces) {
        namespaces = true;
        }
    } else if (name.equals(XMLNS_URIs)) {
        checkNotParsing("feature", name);
        uris = value;
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
    }
    public boolean getFeature (String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    if (name.equals(NAMESPACES)) {
        return namespaces;
    } else if (name.equals(NAMESPACE_PREFIXES)) {
        return prefixes;
    } else if (name.equals(XMLNS_URIs)) {
        return uris;
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
    }
    public void setProperty (String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    throw new SAXNotRecognizedException("Property: " + name);
    }
    public Object getProperty (String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    throw new SAXNotRecognizedException("Property: " + name);
    }
    public void setEntityResolver (EntityResolver resolver)
    {
    entityResolver = resolver;
    }
    public EntityResolver getEntityResolver ()
    {
    return entityResolver;
    }
    public void setDTDHandler (DTDHandler handler)
    {
    dtdHandler = handler;
    }
    public DTDHandler getDTDHandler ()
    {
    return dtdHandler;
    }
    public void setContentHandler (ContentHandler handler)
    {
    contentHandler = handler;
    }
    public ContentHandler getContentHandler ()
    {
    return contentHandler;
    }
    public void setErrorHandler (ErrorHandler handler)
    {
    errorHandler = handler;
    }
    public ErrorHandler getErrorHandler ()
    {
    return errorHandler;
    }
    public void parse (String systemId)
    throws IOException, SAXException
    {
    parse(new InputSource(systemId));
    }
    public void parse (InputSource input)
    throws IOException, SAXException
    {
    if (parsing) {
        throw new SAXException("Parser is already in use");
    }
    setupParser();
    parsing = true;
    try {
        parser.parse(input);
    } finally {
        parsing = false;
    }
    parsing = false;
    }
    public void setDocumentLocator (Locator locator)
    {
    this.locator = locator;
    if (contentHandler != null) {
        contentHandler.setDocumentLocator(locator);
    }
    }
    public void startDocument ()
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.startDocument();
    }
    }
    public void endDocument ()
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.endDocument();
    }
    }
    public void startElement (String qName, AttributeList qAtts)
    throws SAXException
    {
    Vector exceptions = null;
    if (!namespaces) {
        if (contentHandler != null) {
        attAdapter.setAttributeList(qAtts);
        contentHandler.startElement("", "", qName.intern(),
                        attAdapter);
        }
        return;
    }
    nsSupport.pushContext();
    int length = qAtts.getLength();
    for (int i = 0; i < length; i++) {
        String attQName = qAtts.getName(i);
        if (!attQName.startsWith("xmlns"))
        continue;
        String prefix;
        int n = attQName.indexOf(':');
        if (n == -1 && attQName.length () == 5) {
        prefix = "";
        } else if (n != 5) {
        continue;
        } else         
        prefix = attQName.substring(n+1);
        String value = qAtts.getValue(i);
        if (!nsSupport.declarePrefix(prefix, value)) {
        reportError("Illegal Namespace prefix: " + prefix);
        continue;
        }
        if (contentHandler != null)
        contentHandler.startPrefixMapping(prefix, value);
    }
    atts.clear();
    for (int i = 0; i < length; i++) {
        String attQName = qAtts.getName(i);
        String type = qAtts.getType(i);
        String value = qAtts.getValue(i);
        if (attQName.startsWith("xmlns")) {
        String prefix;
        int n = attQName.indexOf(':');
        if (n == -1 && attQName.length () == 5) {
            prefix = "";
        } else if (n != 5) {
            prefix = null;
        } else {
            prefix = attQName.substring(6);
        }
        if (prefix != null) {
            if (prefixes) {
            if (uris)
                atts.addAttribute (nsSupport.XMLNS, prefix,
                    attQName.intern(), type, value);
            else
                atts.addAttribute ("", "",
                    attQName.intern(), type, value);
            }
            continue;
        }
        } 
        try {
        String attName[] = processName(attQName, true, true);
        atts.addAttribute(attName[0], attName[1], attName[2],
                  type, value);
        } catch (SAXException e) {
        if (exceptions == null)
            exceptions = new Vector();
        exceptions.addElement(e);
        atts.addAttribute("", attQName, attQName, type, value);
        }
    }
    if (exceptions != null && errorHandler != null) {
        for (int i = 0; i < exceptions.size(); i++)
        errorHandler.error((SAXParseException)
                (exceptions.elementAt(i)));
    }
    if (contentHandler != null) {
        String name[] = processName(qName, false, false);
        contentHandler.startElement(name[0], name[1], name[2], atts);
    }
    }
    public void endElement (String qName)
    throws SAXException
    {
    if (!namespaces) {
        if (contentHandler != null) {
        contentHandler.endElement("", "", qName.intern());
        }
        return;
    }
    String names[] = processName(qName, false, false);
    if (contentHandler != null) {
        contentHandler.endElement(names[0], names[1], names[2]);
        Enumeration prefixes = nsSupport.getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
        String prefix = (String)prefixes.nextElement();
        contentHandler.endPrefixMapping(prefix);
        }
    }
    nsSupport.popContext();
    }
    public void characters (char ch[], int start, int length)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.characters(ch, start, length);
    }
    }
    public void ignorableWhitespace (char ch[], int start, int length)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.ignorableWhitespace(ch, start, length);
    }
    }
    public void processingInstruction (String target, String data)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.processingInstruction(target, data);
    }
    }
    private void setupParser ()
    {
    if (!prefixes && !namespaces)
        throw new IllegalStateException ();
    nsSupport.reset();
    if (uris)
        nsSupport.setNamespaceDeclUris (true);
    if (entityResolver != null) {
        parser.setEntityResolver(entityResolver);
    }
    if (dtdHandler != null) {
        parser.setDTDHandler(dtdHandler);
    }
    if (errorHandler != null) {
        parser.setErrorHandler(errorHandler);
    }
    parser.setDocumentHandler(this);
    locator = null;
    }
    private String [] processName (String qName, boolean isAttribute,
                   boolean useException)
    throws SAXException
    {
    String parts[] = nsSupport.processName(qName, nameParts,
                           isAttribute);
    if (parts == null) {
        if (useException)
        throw makeException("Undeclared prefix: " + qName);
        reportError("Undeclared prefix: " + qName);
        parts = new String[3];
        parts[0] = parts[1] = "";
        parts[2] = qName.intern();
    }
    return parts;
    }
    void reportError (String message)
    throws SAXException
    {
    if (errorHandler != null)
        errorHandler.error(makeException(message));
    }
    private SAXParseException makeException (String message)
    {
    if (locator != null) {
        return new SAXParseException(message, locator);
    } else {
        return new SAXParseException(message, null, null, -1, -1);
    }
    }
    private void checkNotParsing (String type, String name)
    throws SAXNotSupportedException
    {
    if (parsing) {
        throw new SAXNotSupportedException("Cannot change " +
                           type + ' ' +
                           name + " while parsing");
    }
    }
    private NamespaceSupport nsSupport;
    private AttributeListAdapter attAdapter;
    private boolean parsing = false;
    private String nameParts[] = new String[3];
    private Parser parser = null;
    private AttributesImpl atts = null;
    private boolean namespaces = true;
    private boolean prefixes = false;
    private boolean uris = false;
    Locator locator;
    EntityResolver entityResolver = null;
    DTDHandler dtdHandler = null;
    ContentHandler contentHandler = null;
    ErrorHandler errorHandler = null;
    final class AttributeListAdapter implements Attributes
    {
    AttributeListAdapter ()
    {
    }
    void setAttributeList (AttributeList qAtts)
    {
        this.qAtts = qAtts;
    }
    public int getLength ()
    {
        return qAtts.getLength();
    }
    public String getURI (int i)
    {
        return "";
    }
    public String getLocalName (int i)
    {
        return "";
    }
    public String getQName (int i)
    {
        return qAtts.getName(i).intern();
    }
    public String getType (int i)
    {
        return qAtts.getType(i).intern();
    }
    public String getValue (int i)
    {
        return qAtts.getValue(i);
    }
    public int getIndex (String uri, String localName)
    {
        return -1;
    }
    public int getIndex (String qName)
    {
        int max = atts.getLength();
        for (int i = 0; i < max; i++) {
        if (qAtts.getName(i).equals(qName)) {
            return i;
        }
        }
        return -1;
    }
    public String getType (String uri, String localName)
    {
        return null;
    }
    public String getType (String qName)
    {
        return qAtts.getType(qName).intern();
    }
    public String getValue (String uri, String localName)
    {
        return null;
    }
    public String getValue (String qName)
    {
        return qAtts.getValue(qName);
    }
    private AttributeList qAtts;
    }
}
