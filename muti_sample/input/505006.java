public class StylesheetHandler extends DefaultHandler
        implements TemplatesHandler, PrefixResolver, NodeConsumer
{
  private FunctionTable m_funcTable = new FunctionTable();
  private boolean m_optimize = true;
  private boolean m_incremental = false;
  private boolean m_source_location = false;
  public StylesheetHandler(TransformerFactoryImpl processor)
          throws TransformerConfigurationException
  {
    Class func = org.apache.xalan.templates.FuncDocument.class;
    m_funcTable.installFunction("document", func);
    func = org.apache.xalan.templates.FuncFormatNumb.class;
    m_funcTable.installFunction("format-number", func);
    m_optimize =((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_OPTIMIZE)).booleanValue();
    m_incremental = ((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_INCREMENTAL)).booleanValue();
    m_source_location = ((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_SOURCE_LOCATION)).booleanValue();
    init(processor);
  }
  void init(TransformerFactoryImpl processor)
  {
    m_stylesheetProcessor = processor;
    m_processors.push(m_schema.getElementProcessor());
    this.pushNewNamespaceSupport();
  }
  public XPath createXPath(String str, ElemTemplateElement owningTemplate)
          throws javax.xml.transform.TransformerException
  {
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    XPath xpath = new XPath(str, owningTemplate, this, XPath.SELECT, handler, 
            m_funcTable);
    xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
    return xpath;
  }
  XPath createMatchPatternXPath(String str, ElemTemplateElement owningTemplate)
          throws javax.xml.transform.TransformerException
  {
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    XPath xpath = new XPath(str, owningTemplate, this, XPath.MATCH, handler, 
        m_funcTable);
    xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
    return xpath;    
  }
  public String getNamespaceForPrefix(String prefix)
  {
    return this.getNamespaceSupport().getURI(prefix);
  }
  public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context)
  {
    assertion(true, "can't process a context node in StylesheetHandler!");
    return null;
  }
  private boolean stackContains(Stack stack, String url)
  {
    int n = stack.size();
    boolean contains = false;
    for (int i = 0; i < n; i++)
    {
      String url2 = (String) stack.elementAt(i);
      if (url2.equals(url))
      {
        contains = true;
        break;
      }
    }
    return contains;
  }
  public Templates getTemplates()
  {
    return getStylesheetRoot();
  }
  public void setSystemId(String baseID)
  {
    pushBaseIndentifier(baseID);
  }
  public String getSystemId()
  {
    return this.getBaseIdentifier();
  }
  public InputSource resolveEntity(String publicId, String systemId)
          throws org.xml.sax.SAXException
  {
    return getCurrentProcessor().resolveEntity(this, publicId, systemId);
  }
  public void notationDecl(String name, String publicId, String systemId)
  {
    getCurrentProcessor().notationDecl(this, name, publicId, systemId);
  }
  public void unparsedEntityDecl(String name, String publicId,
                                 String systemId, String notationName)
  {
    getCurrentProcessor().unparsedEntityDecl(this, name, publicId, systemId,
                                             notationName);
  }
  XSLTElementProcessor getProcessorFor(
          String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    XSLTElementProcessor currentProcessor = getCurrentProcessor();
    XSLTElementDef def = currentProcessor.getElemDef();
    XSLTElementProcessor elemProcessor = def.getProcessorFor(uri, localName);
    if (null == elemProcessor
            && !(currentProcessor instanceof ProcessorStylesheetDoc)
            && ((null == getStylesheet()
                || Double.valueOf(getStylesheet().getVersion()).doubleValue()
                   > Constants.XSLTVERSUPPORTED) 
                ||(!uri.equals(Constants.S_XSLNAMESPACEURL) &&
                            currentProcessor instanceof ProcessorStylesheetElement)
                || getElemVersion() > Constants.XSLTVERSUPPORTED
        ))
    {
      elemProcessor = def.getProcessorForUnknown(uri, localName);
    }
    if (null == elemProcessor)
      error(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_ALLOWED_IN_POSITION, new Object[]{rawName}),null);
    return elemProcessor;
  }
  public void setDocumentLocator(Locator locator)
  {
    m_stylesheetLocatorStack.push(new SAXSourceLocator(locator));
  }
  private int m_stylesheetLevel = -1;
  public void startDocument() throws org.xml.sax.SAXException
  {
    m_stylesheetLevel++;
    pushSpaceHandling(false);
  }
  private boolean m_parsingComplete = false;
  public boolean isStylesheetParsingComplete()
  {
    return m_parsingComplete;
  }
  public void endDocument() throws org.xml.sax.SAXException
  {
    try
    {
      if (null != getStylesheetRoot())
      {
        if (0 == m_stylesheetLevel)
          getStylesheetRoot().recompose();        
      }
      else
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_STYLESHEETROOT, null)); 
      XSLTElementProcessor elemProcessor = getCurrentProcessor();
      if (null != elemProcessor)
        elemProcessor.startNonText(this);
      m_stylesheetLevel--;			
      popSpaceHandling();
      m_parsingComplete = (m_stylesheetLevel < 0);
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  private java.util.Vector m_prefixMappings = new java.util.Vector();
  public void startPrefixMapping(String prefix, String uri)
          throws org.xml.sax.SAXException
  {
    m_prefixMappings.addElement(prefix); 
    m_prefixMappings.addElement(uri); 
  }
  public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException
  {
  }
  private void flushCharacters() throws org.xml.sax.SAXException
  {
    XSLTElementProcessor elemProcessor = getCurrentProcessor();
    if (null != elemProcessor)
      elemProcessor.startNonText(this);
  }
  public void startElement(
          String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    NamespaceSupport nssupport = this.getNamespaceSupport();
    nssupport.pushContext();
    int n = m_prefixMappings.size();
    for (int i = 0; i < n; i++) 
    {
      String prefix = (String)m_prefixMappings.elementAt(i++);
      String nsURI = (String)m_prefixMappings.elementAt(i);
      nssupport.declarePrefix(prefix, nsURI);
    }
    m_prefixMappings.removeAllElements(); 
    m_elementID++;
    checkForFragmentID(attributes);
    if (!m_shouldProcess)
      return;
    flushCharacters();
    pushSpaceHandling(attributes);
    XSLTElementProcessor elemProcessor = getProcessorFor(uri, localName,
                                           rawName);
    if(null != elemProcessor)  
    {
      this.pushProcessor(elemProcessor);
      elemProcessor.startElement(this, uri, localName, rawName, attributes);
    }
    else
    {
      m_shouldProcess = false;
      popSpaceHandling();
    }
  }
  public void endElement(String uri, String localName, String rawName)
          throws org.xml.sax.SAXException
  {
    m_elementID--;
    if (!m_shouldProcess)
      return;
    if ((m_elementID + 1) == m_fragmentID)
      m_shouldProcess = false;
    flushCharacters();
    popSpaceHandling();
    XSLTElementProcessor p = getCurrentProcessor();
    p.endElement(this, uri, localName, rawName);
    this.popProcessor();
    this.getNamespaceSupport().popContext();
  }
  public void characters(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {
    if (!m_shouldProcess)
      return;
    XSLTElementProcessor elemProcessor = getCurrentProcessor();
    XSLTElementDef def = elemProcessor.getElemDef();
    if (def.getType() != XSLTElementDef.T_PCDATA)
      elemProcessor = def.getProcessorFor(null, "text()");
    if (null == elemProcessor)
    {
      if (!XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
        error(
          XSLMessages.createMessage(XSLTErrorResources.ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION, null),null);
    }
    else
      elemProcessor.characters(this, ch, start, length);
  }
  public void ignorableWhitespace(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {
    if (!m_shouldProcess)
      return;
    getCurrentProcessor().ignorableWhitespace(this, ch, start, length);
  }
  public void processingInstruction(String target, String data)
          throws org.xml.sax.SAXException
  {
    if (!m_shouldProcess)
      return;
    String prefix="",ns="", localName=target;
    int colon=target.indexOf(':');
    if(colon>=0)
    {
      ns=getNamespaceForPrefix(prefix=target.substring(0,colon));
      localName=target.substring(colon+1);
    }
    try
    {
      if(
        "xalan-doc-cache-off".equals(target) ||
        "xalan:doc-cache-off".equals(target) ||
	   ("doc-cache-off".equals(localName) &&
	    ns.equals("org.apache.xalan.xslt.extensions.Redirect") )
	 )
      {
	if(!(m_elems.peek() instanceof ElemForEach))
          throw new TransformerException
	    ("xalan:doc-cache-off not allowed here!", 
	     getLocator());
        ElemForEach elem = (ElemForEach)m_elems.peek();
        elem.m_doc_cache_off = true;
      }
    }
    catch(Exception e)
    {
    }
    flushCharacters();
    getCurrentProcessor().processingInstruction(this, target, data);
  }
  public void skippedEntity(String name) throws org.xml.sax.SAXException
  {
    if (!m_shouldProcess)
      return;
    getCurrentProcessor().skippedEntity(this, name);
  }
  public void warn(String msg, Object args[]) throws org.xml.sax.SAXException
  {
    String formattedMsg = XSLMessages.createWarning(msg, args);
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    try
    {
      if (null != handler)
        handler.warning(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  private void assertion(boolean condition, String msg) throws RuntimeException
  {
    if (!condition)
      throw new RuntimeException(msg);
  }
  protected void error(String msg, Exception e)
          throws org.xml.sax.SAXException
  {
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    TransformerException pe;
    if (!(e instanceof TransformerException))
    {
      pe = (null == e)
           ? new TransformerException(msg, locator)
           : new TransformerException(msg, locator, e);
    }
    else
      pe = (TransformerException) e;
    if (null != handler)
    {
      try
      {
        handler.error(pe);
      }
      catch (TransformerException te)
      {
        throw new org.xml.sax.SAXException(te);
      }
    }
    else
      throw new org.xml.sax.SAXException(pe);
  }
  protected void error(String msg, Object args[], Exception e)
          throws org.xml.sax.SAXException
  {
    String formattedMsg = XSLMessages.createMessage(msg, args);
    error(formattedMsg, e);
  }
  public void warning(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {
    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    try
    {
      handler.warning(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  public void error(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {
    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    try
    {
      handler.error(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  public void fatalError(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {
    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    try
    {
      handler.fatalError(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  private boolean m_shouldProcess = true;
  private String m_fragmentIDString;
  private int m_elementID = 0;
  private int m_fragmentID = 0;
  private void checkForFragmentID(Attributes attributes)
  {
    if (!m_shouldProcess)
    {
      if ((null != attributes) && (null != m_fragmentIDString))
      {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++)
        {
          String name = attributes.getQName(i);
          if (name.equals(Constants.ATTRNAME_ID))
          {
            String val = attributes.getValue(i);
            if (val.equalsIgnoreCase(m_fragmentIDString))
            {
              m_shouldProcess = true;
              m_fragmentID = m_elementID;
            }
          }
        }
      }
    }
  }
  private TransformerFactoryImpl m_stylesheetProcessor;
  public TransformerFactoryImpl getStylesheetProcessor()
  {
    return m_stylesheetProcessor;
  }
  public static final int STYPE_ROOT = 1;
  public static final int STYPE_INCLUDE = 2;
  public static final int STYPE_IMPORT = 3;
  private int m_stylesheetType = STYPE_ROOT;
  int getStylesheetType()
  {
    return m_stylesheetType;
  }
  void setStylesheetType(int type)
  {
    m_stylesheetType = type;
  }
  private Stack m_stylesheets = new Stack();
  Stylesheet getStylesheet()
  {
    return (m_stylesheets.size() == 0)
           ? null : (Stylesheet) m_stylesheets.peek();
  }
  Stylesheet getLastPoppedStylesheet()
  {
    return m_lastPoppedStylesheet;
  }
  public StylesheetRoot getStylesheetRoot()
  {
    if (m_stylesheetRoot != null){
        m_stylesheetRoot.setOptimizer(m_optimize);
        m_stylesheetRoot.setIncremental(m_incremental);
        m_stylesheetRoot.setSource_location(m_source_location);  		
    }
    return m_stylesheetRoot;
  }
  StylesheetRoot m_stylesheetRoot;
  Stylesheet m_lastPoppedStylesheet;
  public void pushStylesheet(Stylesheet s)
  {
    if (m_stylesheets.size() == 0)
      m_stylesheetRoot = (StylesheetRoot) s;
    m_stylesheets.push(s);
  }
  Stylesheet popStylesheet()
  {
    if (!m_stylesheetLocatorStack.isEmpty())
      m_stylesheetLocatorStack.pop();
    if (!m_stylesheets.isEmpty())
      m_lastPoppedStylesheet = (Stylesheet) m_stylesheets.pop();
    return m_lastPoppedStylesheet;
  }
  private Stack m_processors = new Stack();
  XSLTElementProcessor getCurrentProcessor()
  {
    return (XSLTElementProcessor) m_processors.peek();
  }
  void pushProcessor(XSLTElementProcessor processor)
  {
    m_processors.push(processor);
  }
  XSLTElementProcessor popProcessor()
  {
    return (XSLTElementProcessor) m_processors.pop();
  }
  private XSLTSchema m_schema = new XSLTSchema();
  public XSLTSchema getSchema()
  {
    return m_schema;
  }
  private Stack m_elems = new Stack();
  ElemTemplateElement getElemTemplateElement()
  {
    try
    {
      return (ElemTemplateElement) m_elems.peek();
    }
    catch (java.util.EmptyStackException ese)
    {
      return null;
    }
  }  
  private int m_docOrderCount = 0;
  int nextUid()
  {
    return m_docOrderCount++;
  }
  void pushElemTemplateElement(ElemTemplateElement elem)
  {
    if (elem.getUid() == -1)
      elem.setUid(nextUid());
    m_elems.push(elem);
  }
  ElemTemplateElement popElemTemplateElement()
  {
    return (ElemTemplateElement) m_elems.pop();
  }
  Stack m_baseIdentifiers = new Stack();
  void pushBaseIndentifier(String baseID)
  {
    if (null != baseID)
    {
      int posOfHash = baseID.indexOf('#');
      if (posOfHash > -1)
      {
        m_fragmentIDString = baseID.substring(posOfHash + 1);
        m_shouldProcess = false;
      }
      else
        m_shouldProcess = true;
    }
    else
      m_shouldProcess = true;
    m_baseIdentifiers.push(baseID);
  }
  String popBaseIndentifier()
  {
    return (String) m_baseIdentifiers.pop();
  }
  public String getBaseIdentifier()
  {
    String base = (String) (m_baseIdentifiers.isEmpty()
                            ? null : m_baseIdentifiers.peek());
    if (null == base)
    {
      SourceLocator locator = getLocator();
      base = (null == locator) ? "" : locator.getSystemId();
    }
    return base;
  }
  private Stack m_stylesheetLocatorStack = new Stack();
  public SAXSourceLocator getLocator()
  {
    if (m_stylesheetLocatorStack.isEmpty())
    {
      SAXSourceLocator locator = new SAXSourceLocator();
      locator.setSystemId(this.getStylesheetProcessor().getDOMsystemID());
      return locator;
    }
    return ((SAXSourceLocator) m_stylesheetLocatorStack.peek());
  }
  private Stack m_importStack = new Stack();
  private Stack m_importSourceStack = new Stack();
  void pushImportURL(String hrefUrl)
  {
    m_importStack.push(hrefUrl);
  }
  void pushImportSource(Source sourceFromURIResolver)
  {
    m_importSourceStack.push(sourceFromURIResolver);
  }
  boolean importStackContains(String hrefUrl)
  {
    return stackContains(m_importStack, hrefUrl);
  }
  String popImportURL()
  {
    return (String) m_importStack.pop();
  }
  String peekImportURL()
  {
    return (String) m_importStack.peek();
  }
  Source peekSourceFromURIResolver()
  {
    return (Source) m_importSourceStack.peek();
  }
  Source popImportSource()
  {
    return (Source) m_importSourceStack.pop();
  }
  private boolean warnedAboutOldXSLTNamespace = false;
  Stack m_nsSupportStack = new Stack();
  void pushNewNamespaceSupport()
  {
    m_nsSupportStack.push(new NamespaceSupport2());
  }
  void popNamespaceSupport()
  {
    m_nsSupportStack.pop();
  }
  NamespaceSupport getNamespaceSupport()
  {
    return (NamespaceSupport) m_nsSupportStack.peek();
  }
  private Node m_originatingNode;
  public void setOriginatingNode(Node n)
  {
    m_originatingNode = n;
  }
  public Node getOriginatingNode()
  {
    return m_originatingNode;
  }
  private BoolStack m_spacePreserveStack = new BoolStack();
  boolean isSpacePreserve()
  {
    return m_spacePreserveStack.peek();
  }
  void popSpaceHandling()
  {
    m_spacePreserveStack.pop();
  }
  void pushSpaceHandling(boolean b)
    throws org.xml.sax.SAXParseException
  {
    m_spacePreserveStack.push(b);
  }
  void pushSpaceHandling(Attributes attrs)
    throws org.xml.sax.SAXParseException
  {    
    String value = attrs.getValue("xml:space");
    if(null == value)
    {
      m_spacePreserveStack.push(m_spacePreserveStack.peekOrFalse());
    }
    else if(value.equals("preserve"))
    {
      m_spacePreserveStack.push(true);
    }
    else if(value.equals("default"))
    {
      m_spacePreserveStack.push(false);
    }
    else
    {
      SAXSourceLocator locator = getLocator();
      ErrorListener handler = m_stylesheetProcessor.getErrorListener();
      try
      {
        handler.error(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_XMLSPACE_VALUE, null), locator)); 
      }
      catch (TransformerException te)
      {
        throw new org.xml.sax.SAXParseException(te.getMessage(), locator, te);
      }
      m_spacePreserveStack.push(m_spacePreserveStack.peek());
    }
  }
  private double getElemVersion()
  {
    ElemTemplateElement elem = getElemTemplateElement();
    double version = -1; 
    while ((version == -1 || version == Constants.XSLTVERSUPPORTED) && elem != null)
    {
      try{
      version = Double.valueOf(elem.getXmlVersion()).doubleValue();
      }
      catch (Exception ex)
      {
        version = -1;
      }
      elem = elem.getParentElem();
      }
    return (version == -1)? Constants.XSLTVERSUPPORTED : version;
  }
    public boolean handlesNullPrefixes() {
        return false;
    }
    public boolean getOptimize() {
        return m_optimize;
    }
    public boolean getIncremental() {
        return m_incremental;
    }
    public boolean getSource_location() {
        return m_source_location;
    }
}
