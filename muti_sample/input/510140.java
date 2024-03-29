public class TransformerImpl extends Transformer
        implements Runnable, DTMWSFilter, ExtensionsProvider, org.apache.xml.serializer.SerializerTrace
{
  private Boolean m_reentryGuard = new Boolean(true);
  private java.io.FileOutputStream m_outputStream = null;
  private Thread m_transformThread;
  private String m_urlOfSource = null;
  private Result m_outputTarget = null;
  private OutputProperties m_outputFormat;
  ContentHandler m_inputContentHandler;
  private ContentHandler m_outputContentHandler = null;
  private ObjectPool m_textResultHandlerObjectPool =
    new ObjectPool(ToTextStream.class);
  private ObjectPool m_stringWriterObjectPool =
    new ObjectPool(StringWriter.class);
  private OutputProperties m_textformat = new OutputProperties(Method.TEXT);
  ObjectStack m_currentTemplateElements 
      = new ObjectStack(XPathContext.RECURSIONLIMIT);
  Stack m_currentMatchTemplates = new Stack();
  NodeVector m_currentMatchedNodes = new NodeVector();
  private StylesheetRoot m_stylesheetRoot = null;
  private boolean m_quietConflictWarnings = true;
  private XPathContext m_xcontext;
  private SerializationHandler m_serializationHandler;  
  private KeyManager m_keyManager = new KeyManager();
  Stack m_attrSetStack = null;
  CountersTable m_countersTable = null;
  BoolStack m_currentTemplateRuleIsNull = new BoolStack();
  ObjectStack m_currentFuncResult = new ObjectStack();
  private MsgMgr m_msgMgr;
  private boolean m_optimizer = true;
  private boolean m_incremental = false;
  private boolean m_source_location = false;
  private ErrorListener m_errorHandler =
    new org.apache.xml.utils.DefaultErrorHandler(false);
  private Exception m_exceptionThrown = null;
  private int m_doc;
  private boolean m_hasBeenReset = false;
  private boolean m_shouldReset = true;
  private Stack m_modes = new Stack();
  public TransformerImpl(StylesheetRoot stylesheet)
  {
    m_optimizer = stylesheet.getOptimizer();
    m_incremental = stylesheet.getIncremental();
    m_source_location = stylesheet.getSource_location();  	
    setStylesheet(stylesheet);
    XPathContext xPath = new XPathContext(this);
    xPath.setIncremental(m_incremental);
    xPath.getDTMManager().setIncremental(m_incremental);
    xPath.setSource_location(m_source_location);
    xPath.getDTMManager().setSource_location(m_source_location);
    if (stylesheet.isSecureProcessing())
      xPath.setSecureProcessing(true);
    setXPathContext(xPath);
    getXPathContext().setNamespaceContext(stylesheet);
  }
  private ExtensionsTable m_extensionsTable = null;
  public ExtensionsTable getExtensionsTable()
  {
    return m_extensionsTable;
  }
  void setExtensionsTable(StylesheetRoot sroot)
       throws javax.xml.transform.TransformerException
  {
    try
    {
      if (sroot.getExtensions() != null)
        m_extensionsTable = new ExtensionsTable(sroot);
    }
    catch (javax.xml.transform.TransformerException te)
    {te.printStackTrace();}
  }
  public boolean functionAvailable(String ns, String funcName)
          throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().functionAvailable(ns, funcName);
  }
  public boolean elementAvailable(String ns, String elemName)
          throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().elementAvailable(ns, elemName);   
  }
  public Object extFunction(String ns, String funcName, 
                            Vector argVec, Object methodKey)
            throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().extFunction(ns, funcName, 
                                        argVec, methodKey,
                                        getXPathContext().getExpressionContext());   
  }
  public Object extFunction(FuncExtFunction extFunction, Vector argVec)
            throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().extFunction(extFunction, argVec,
                                            getXPathContext().getExpressionContext());   
  }
  public void reset()
  {
    if (!m_hasBeenReset && m_shouldReset)
    {
      m_hasBeenReset = true;
      if (this.m_outputStream != null)
      {
        try
        {
          m_outputStream.close();
        }
        catch (java.io.IOException ioe){}
      }
      m_outputStream = null;
      m_countersTable = null;
      m_xcontext.reset();
      m_xcontext.getVarStack().reset();
      resetUserParameters();
      m_currentTemplateElements.removeAllElements();     
      m_currentMatchTemplates.removeAllElements();
      m_currentMatchedNodes.removeAllElements();
      m_serializationHandler = null;      
      m_outputTarget = null;
      m_keyManager = new KeyManager();
      m_attrSetStack = null;
      m_countersTable = null;
      m_currentTemplateRuleIsNull = new BoolStack();
      m_doc = DTM.NULL;
      m_transformThread = null;
      m_xcontext.getSourceTreeManager().reset();
    }
  }
  public Thread getTransformThread()
  {
    return m_transformThread;
  }
  public void setTransformThread(Thread t)
  {
    m_transformThread = t;
  }
  private boolean m_hasTransformThreadErrorCatcher = false;
  public boolean hasTransformThreadErrorCatcher()
  {
    return m_hasTransformThreadErrorCatcher;
  }
  public void transform(Source source) throws TransformerException
  {
                transform(source, true); 
        }
  public void transform(Source source, boolean shouldRelease) throws TransformerException
  {
    try
    {
      if(getXPathContext().getNamespaceContext() == null){
         getXPathContext().setNamespaceContext(getStylesheet());
      }
      String base = source.getSystemId();
      if(null == base)
      {
        base = m_stylesheetRoot.getBaseIdentifier();
      }
      if(null == base)
      {
        String currentDir = "";
        try {
          currentDir = System.getProperty("user.dir");
        }
        catch (SecurityException se) {}
        if (currentDir.startsWith(java.io.File.separator))
          base = "file:
        else
          base = "file:
        base = base + java.io.File.separatorChar
               + source.getClass().getName();
      }
      setBaseURLOfSource(base);
      DTMManager mgr = m_xcontext.getDTMManager();
      if ((source instanceof StreamSource && source.getSystemId()==null &&
         ((StreamSource)source).getInputStream()==null &&
         ((StreamSource)source).getReader()==null)||
         (source instanceof SAXSource &&
         ((SAXSource)source).getInputSource()==null &&
         ((SAXSource)source).getXMLReader()==null )||
         (source instanceof DOMSource && ((DOMSource)source).getNode()==null)){
        try {
          DocumentBuilderFactory builderF = 
                   DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = builderF.newDocumentBuilder();
          String systemID = source.getSystemId();
          source = new DOMSource(builder.newDocument());
          if (systemID != null) {
            source.setSystemId(systemID);
          }
        } catch (ParserConfigurationException e) {
          fatalError(e);
        }           
      }
      DTM dtm = mgr.getDTM(source, false, this, true, true);
      dtm.setDocumentBaseURI(base);
      boolean hardDelete = true;  
      try
      {
        this.transformNode(dtm.getDocument());
      }
      finally
      {
        if (shouldRelease)
          mgr.release(dtm, hardDelete);
      }
      Exception e = getExceptionThrown();
      if (null != e)
      {
        if (e instanceof javax.xml.transform.TransformerException)
        {
          throw (javax.xml.transform.TransformerException) e;
        }
        else if (e instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          fatalError(
              ((org.apache.xml.utils.WrappedRuntimeException) e).getException());
        }
        else
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else if (null != m_serializationHandler)
      {
        m_serializationHandler.endDocument();
      }
    }
    catch (org.apache.xml.utils.WrappedRuntimeException wre)
    {
      Throwable throwable = wre.getException();
      while (throwable
             instanceof org.apache.xml.utils.WrappedRuntimeException)
      {
        throwable =
          ((org.apache.xml.utils.WrappedRuntimeException) throwable).getException();
      }
      fatalError(throwable);
    }
    catch (org.xml.sax.SAXParseException spe)
    {
      fatalError(spe);
    }
    catch (org.xml.sax.SAXException se)
    {
      m_errorHandler.fatalError(new TransformerException(se));
    }
    finally
    {
      m_hasTransformThreadErrorCatcher = false;
      reset();
    }
  }
  private void fatalError(Throwable throwable) throws TransformerException
  {
    if (throwable instanceof org.xml.sax.SAXParseException)
      m_errorHandler.fatalError(new TransformerException(throwable.getMessage(),new SAXSourceLocator((org.xml.sax.SAXParseException)throwable)));
    else
      m_errorHandler.fatalError(new TransformerException(throwable));
  }
  public void setBaseURLOfSource(String base)
  {
    m_urlOfSource = base;
  }
  public String getOutputProperty(String qnameString)
          throws IllegalArgumentException
  {
    String value = null;
    OutputProperties props = getOutputFormat();
    value = props.getProperty(qnameString);
    if (null == value)
    {
      if (!OutputProperties.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); 
    }
    return value;
  }
  public String getOutputPropertyNoDefault(String qnameString)
          throws IllegalArgumentException
  {
    String value = null;
    OutputProperties props = getOutputFormat();
    value = (String) props.getProperties().get(qnameString);
    if (null == value)
    {
      if (!OutputProperties.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); 
    }
    return value;
  }
  public void setOutputProperty(String name, String value)
          throws IllegalArgumentException
  {
    synchronized (m_reentryGuard)
    {
      if (null == m_outputFormat)
      {
        m_outputFormat =
          (OutputProperties) getStylesheet().getOutputComposed().clone();
      }
      if (!OutputProperties.isLegalPropertyKey(name))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); 
      m_outputFormat.setProperty(name, value);
    }
  }
  public void setOutputProperties(Properties oformat)
  		throws IllegalArgumentException
  {
    synchronized (m_reentryGuard)
    {
      if (null != oformat)
      {
        String method = (String) oformat.get(OutputKeys.METHOD);
        if (null != method)
          m_outputFormat = new OutputProperties(method);
        else if(m_outputFormat==null)
          m_outputFormat = new OutputProperties();
        m_outputFormat.copyFrom(oformat);
        m_outputFormat.copyFrom(m_stylesheetRoot.getOutputProperties());
      }
      else {
        m_outputFormat = null;
      }
    }
  }
  public Properties getOutputProperties()
  {
    return (Properties) getOutputFormat().getProperties().clone();
  }
    public SerializationHandler createSerializationHandler(Result outputTarget)
            throws TransformerException
    {
       SerializationHandler xoh =
        createSerializationHandler(outputTarget, getOutputFormat());
       return xoh;
    }
    public SerializationHandler createSerializationHandler(
            Result outputTarget, OutputProperties format)
              throws TransformerException
    {
      SerializationHandler xoh;
      org.w3c.dom.Node outputNode = null;
      if (outputTarget instanceof DOMResult)
      {
        outputNode = ((DOMResult) outputTarget).getNode();
        org.w3c.dom.Node nextSibling = ((DOMResult)outputTarget).getNextSibling();
        org.w3c.dom.Document doc;
        short type;
        if (null != outputNode)
        {
          type = outputNode.getNodeType();
          doc = (org.w3c.dom.Node.DOCUMENT_NODE == type)
                ? (org.w3c.dom.Document) outputNode
                : outputNode.getOwnerDocument();
        }
        else
        {
          boolean isSecureProcessing = m_stylesheetRoot.isSecureProcessing();
          doc = org.apache.xml.utils.DOMHelper.createDocument(isSecureProcessing);
          outputNode = doc;
          type = outputNode.getNodeType();
          ((DOMResult) outputTarget).setNode(outputNode);
        }
        DOMBuilder handler =
          (org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE == type)
          ? new DOMBuilder(doc, (org.w3c.dom.DocumentFragment) outputNode)
          : new DOMBuilder(doc, outputNode);
        if (nextSibling != null)
          handler.setNextSibling(nextSibling);
          String encoding = format.getProperty(OutputKeys.ENCODING);          
          xoh = new ToXMLSAXHandler(handler, (LexicalHandler)handler, encoding);
      }
      else if (outputTarget instanceof SAXResult)
      {
        ContentHandler handler = ((SAXResult) outputTarget).getHandler();
        if (null == handler)
           throw new IllegalArgumentException(
             "handler can not be null for a SAXResult"); 
        LexicalHandler lexHandler;
        if (handler instanceof LexicalHandler)     
            lexHandler = (LexicalHandler)  handler;
        else
            lexHandler = null;
        String encoding = format.getProperty(OutputKeys.ENCODING); 
        String method = format.getProperty(OutputKeys.METHOD);
        ToXMLSAXHandler toXMLSAXHandler = new ToXMLSAXHandler(handler, lexHandler, encoding);
        toXMLSAXHandler.setShouldOutputNSAttr(false);
        xoh = toXMLSAXHandler;   
        String publicID = format.getProperty(OutputKeys.DOCTYPE_PUBLIC); 
        String systemID = format.getProperty(OutputKeys.DOCTYPE_SYSTEM); 
        if (systemID != null)
            xoh.setDoctypeSystem(systemID);
        if (publicID != null)
            xoh.setDoctypePublic(publicID);
        if (handler instanceof TransformerClient) {
            XalanTransformState state = new XalanTransformState();
            ((TransformerClient)handler).setTransformState(state);
            ((ToSAXHandler)xoh).setTransformState(state);
        }
      }
      else if (outputTarget instanceof StreamResult)
      {
        StreamResult sresult = (StreamResult) outputTarget;
        try
        {
          SerializationHandler serializer =
            (SerializationHandler) SerializerFactory.getSerializer(format.getProperties());
          if (null != sresult.getWriter())
            serializer.setWriter(sresult.getWriter());
          else if (null != sresult.getOutputStream())
            serializer.setOutputStream(sresult.getOutputStream());
          else if (null != sresult.getSystemId())
          {
            String fileURL = sresult.getSystemId();
            if (fileURL.startsWith("file:
            {
              if (fileURL.substring(8).indexOf(":") >0)
                fileURL = fileURL.substring(8);
              else
                fileURL = fileURL.substring(7);
            }
            else if (fileURL.startsWith("file:/"))
            {
                if (fileURL.substring(6).indexOf(":") >0)
                    fileURL = fileURL.substring(6);
                  else
                    fileURL = fileURL.substring(5);            	
            }
            m_outputStream = new java.io.FileOutputStream(fileURL);
            serializer.setOutputStream(m_outputStream);
            xoh = serializer;
          }
          else
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null)); 
          xoh = serializer;  
        }
        catch (IOException ioe)
        {
          throw new TransformerException(ioe);
        }
      }
      else
      {
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()})); 
      }
      xoh.setTransformer(this);
      SourceLocator srcLocator = getStylesheet();
      xoh.setSourceLocator(srcLocator);
      return xoh;
    }
  public void transform(Source xmlSource, Result outputTarget)
          throws TransformerException
  {
                transform(xmlSource, outputTarget, true);
        }
  public void transform(Source xmlSource, Result outputTarget, boolean shouldRelease)
          throws TransformerException
  {
    synchronized (m_reentryGuard)
    {
      SerializationHandler xoh = createSerializationHandler(outputTarget);
      this.setSerializationHandler(xoh);        
      m_outputTarget = outputTarget;
      transform(xmlSource, shouldRelease);
    }
  }
  public void transformNode(int node, Result outputTarget)
          throws TransformerException
  {
    SerializationHandler xoh = createSerializationHandler(outputTarget);
    this.setSerializationHandler(xoh);
    m_outputTarget = outputTarget;
    transformNode(node);
  }
  public void transformNode(int node) throws TransformerException
  {
    setExtensionsTable(getStylesheet());
    synchronized (m_serializationHandler)
    {
      m_hasBeenReset = false;
      XPathContext xctxt = getXPathContext();
      DTM dtm = xctxt.getDTM(node);
      try
      {
        pushGlobalVars(node);
        StylesheetRoot stylesheet = this.getStylesheet();
        int n = stylesheet.getGlobalImportCount();
        for (int i = 0; i < n; i++)
        {
          StylesheetComposed imported = stylesheet.getGlobalImport(i);
          int includedCount = imported.getIncludeCountComposed();
          for (int j = -1; j < includedCount; j++)
          {
            Stylesheet included = imported.getIncludeComposed(j);
            included.runtimeInit(this);
            for (ElemTemplateElement child = included.getFirstChildElem();
                    child != null; child = child.getNextSiblingElem())
            {
              child.runtimeInit(this);
            }
          }
        }
        DTMIterator dtmIter = new org.apache.xpath.axes.SelfIteratorNoPredicate();
        dtmIter.setRoot(node, xctxt);
        xctxt.pushContextNodeList(dtmIter);
        try
        {
          this.applyTemplateToNode(null, null, node);
        }
        finally
        {
          xctxt.popContextNodeList();
        }
        if (null != m_serializationHandler)
        {
          m_serializationHandler.endDocument();
        }
      }
      catch (Exception se)
      {
        while(se instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          Exception e = ((org.apache.xml.utils.WrappedRuntimeException)se).getException();
          if(null != e)
            se = e;
        }
        if (null != m_serializationHandler)
        {
          try
          {
            if(se instanceof org.xml.sax.SAXParseException)
              m_serializationHandler.fatalError((org.xml.sax.SAXParseException)se);
            else if(se instanceof TransformerException)
            {
              TransformerException te = ((TransformerException)se);
              SAXSourceLocator sl = new SAXSourceLocator( te.getLocator() );
              m_serializationHandler.fatalError(new org.xml.sax.SAXParseException(te.getMessage(), sl, te)); 
            }
            else
            {
              m_serializationHandler.fatalError(new org.xml.sax.SAXParseException(se.getMessage(), new SAXSourceLocator(), se)); 
            }             
          }
          catch (Exception e){}
        }        
        if(se instanceof TransformerException)
        {
          m_errorHandler.fatalError((TransformerException)se);
        }
        else if(se instanceof org.xml.sax.SAXParseException)
        {
          m_errorHandler.fatalError(new TransformerException(se.getMessage(), 
                      new SAXSourceLocator((org.xml.sax.SAXParseException)se), 
                      se));
        }
        else
        {
          m_errorHandler.fatalError(new TransformerException(se));
        }
      }
      finally
      {
        this.reset();
      }
    }
  }
  public ContentHandler getInputContentHandler()
  {
    return getInputContentHandler(false);
  }
  public ContentHandler getInputContentHandler(boolean doDocFrag)
  {
    if (null == m_inputContentHandler)
    {
      m_inputContentHandler = new TransformerHandlerImpl(this, doDocFrag,
              m_urlOfSource);
    }
    return m_inputContentHandler;
  }
  public void setOutputFormat(OutputProperties oformat)
  {
    m_outputFormat = oformat;
  }
  public OutputProperties getOutputFormat()
  {
    OutputProperties format = (null == m_outputFormat)
                              ? getStylesheet().getOutputComposed()
                              : m_outputFormat;
    return format;
  }
  public void setParameter(String name, String namespace, Object value)
  {
    VariableStack varstack = getXPathContext().getVarStack();
    QName qname = new QName(namespace, name);
    XObject xobject = XObject.create(value, getXPathContext());
    StylesheetRoot sroot = m_stylesheetRoot;
    Vector vars = sroot.getVariablesAndParamsComposed();
    int i = vars.size();
    while (--i >= 0)
    {
      ElemVariable variable = (ElemVariable)vars.elementAt(i);
      if(variable.getXSLToken() == Constants.ELEMNAME_PARAMVARIABLE && 
         variable.getName().equals(qname))
      {
          varstack.setGlobalVariable(i, xobject);
      }
    }
  }
  Vector m_userParams;
  public void setParameter(String name, Object value)
  {
    if (value == null) {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_SET_PARAM_VALUE, new Object[]{name}));
    }    
    StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);
    try
    {
      String s1 = tokenizer.nextToken();
      String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
      if (null == m_userParams)
        m_userParams = new Vector();
      if (null == s2)
      {
        replaceOrPushUserParam(new QName(s1), XObject.create(value, getXPathContext()));
        setParameter(s1, null, value);
      }
      else
      {
        replaceOrPushUserParam(new QName(s1, s2), XObject.create(value, getXPathContext()));
        setParameter(s2, s1, value);
      }
    }
    catch (java.util.NoSuchElementException nsee)
    {
    }
  }
  private void replaceOrPushUserParam(QName qname, XObject xval)
  {
    int n = m_userParams.size();
    for (int i = n - 1; i >= 0; i--)
    {
      Arg arg = (Arg) m_userParams.elementAt(i);
      if (arg.getQName().equals(qname))
      {
        m_userParams.setElementAt(new Arg(qname, xval, true), i);
        return;
      }
    }
    m_userParams.addElement(new Arg(qname, xval, true));
  }
  public Object getParameter(String name)
  {
    try
    {
      QName qname = QName.getQNameFromString(name);
      if (null == m_userParams)
        return null;
      int n = m_userParams.size();
      for (int i = n - 1; i >= 0; i--)
      {
        Arg arg = (Arg) m_userParams.elementAt(i);
        if (arg.getQName().equals(qname))
        {
          return arg.getVal().object();
        }
      }
      return null;
    }
    catch (java.util.NoSuchElementException nsee)
    {
      return null;
    }
  }
  private void resetUserParameters()
  {
    try
    {
      if (null == m_userParams)
        return;
      int n = m_userParams.size();
      for (int i = n - 1; i >= 0; i--)
      {
        Arg arg = (Arg) m_userParams.elementAt(i);
        QName name = arg.getQName();
        String s1 = name.getNamespace();
        String s2 = name.getLocalPart();
        setParameter(s2, s1, arg.getVal().object());
      }
    }
    catch (java.util.NoSuchElementException nsee)
    {
    }
  }
  public void setParameters(Properties params)
  {
    clearParameters();
    Enumeration names = params.propertyNames();
    while (names.hasMoreElements())
    {
      String name = params.getProperty((String) names.nextElement());
      StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);
      try
      {
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
        if (null == s2)
          setParameter(s1, null, params.getProperty(name));
        else
          setParameter(s2, s1, params.getProperty(name));
      }
      catch (java.util.NoSuchElementException nsee)
      {
      }
    }
  }
  public void clearParameters()
  {
    synchronized (m_reentryGuard)
    {
      VariableStack varstack = new VariableStack();
      m_xcontext.setVarStack(varstack);
      m_userParams = null;
    }
  }
  protected void pushGlobalVars(int contextNode) throws TransformerException
  {
    XPathContext xctxt = m_xcontext;
    VariableStack vs = xctxt.getVarStack();
    StylesheetRoot sr = getStylesheet();
    Vector vars = sr.getVariablesAndParamsComposed();
    int i = vars.size();
    vs.link(i);
    while (--i >= 0)
    {
      ElemVariable v = (ElemVariable) vars.elementAt(i);
      XObject xobj = new XUnresolvedVariable(v, contextNode, this,
                                     vs.getStackFrame(), 0, true);
      if(null == vs.elementAt(i))                               
        vs.setGlobalVariable(i, xobj);
    }
  }
  public void setURIResolver(URIResolver resolver)
  {
    synchronized (m_reentryGuard)
    {
      m_xcontext.getSourceTreeManager().setURIResolver(resolver);
    }
  }
  public URIResolver getURIResolver()
  {
    return m_xcontext.getSourceTreeManager().getURIResolver();
  }
  public void setContentHandler(ContentHandler handler)
  {
    if (handler == null)
    {
      throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_CONTENT_HANDLER, null)); 
    }
    else
    {
      m_outputContentHandler = handler;
      if (null == m_serializationHandler)
      {
        ToXMLSAXHandler h = new ToXMLSAXHandler();
        h.setContentHandler(handler);
        h.setTransformer(this);
        m_serializationHandler = h;
      }
      else
        m_serializationHandler.setContentHandler(handler);
    }
  }
  public ContentHandler getContentHandler()
  {
    return m_outputContentHandler;
  }
  public int transformToRTF(ElemTemplateElement templateParent)
          throws TransformerException
  {
    DTM dtmFrag = m_xcontext.getRTFDTM();
    return transformToRTF(templateParent,dtmFrag);
  }
  public int transformToGlobalRTF(ElemTemplateElement templateParent)
          throws TransformerException
  {
    DTM dtmFrag = m_xcontext.getGlobalRTFDTM();
    return transformToRTF(templateParent,dtmFrag);
  }
  private int transformToRTF(ElemTemplateElement templateParent,DTM dtmFrag)
          throws TransformerException
  {
    XPathContext xctxt = m_xcontext;
    ContentHandler rtfHandler = dtmFrag.getContentHandler();
    int resultFragment; 
    SerializationHandler savedRTreeHandler = this.m_serializationHandler;
    ToSAXHandler h = new ToXMLSAXHandler();
    h.setContentHandler(rtfHandler);
    h.setTransformer(this);
    m_serializationHandler = h;
    SerializationHandler rth = m_serializationHandler;
    try
    {
      rth.startDocument();
      rth.flushPending(); 
      try
      {
        executeChildTemplates(templateParent, true);
        rth.flushPending();
	resultFragment = dtmFrag.getDocument();      
      }
      finally
      {
        rth.endDocument();
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      this.m_serializationHandler = savedRTreeHandler;
    }
    return resultFragment;
  }
  public String transformToString(ElemTemplateElement elem)
          throws TransformerException
  {
    ElemTemplateElement firstChild = elem.getFirstChildElem();
    if(null == firstChild)
      return "";
    if(elem.hasTextLitOnly() && m_optimizer)
    {
      return ((ElemTextLiteral)firstChild).getNodeValue();
    }
    SerializationHandler savedRTreeHandler = this.m_serializationHandler;
    StringWriter sw = (StringWriter) m_stringWriterObjectPool.getInstance();
    m_serializationHandler =
        (ToTextStream) m_textResultHandlerObjectPool.getInstance();
      if (null == m_serializationHandler)
      {
        Serializer serializer = org.apache.xml.serializer.SerializerFactory.getSerializer(
            m_textformat.getProperties());
        m_serializationHandler = (SerializationHandler) serializer;
      } 
        m_serializationHandler.setTransformer(this);
        m_serializationHandler.setWriter(sw);
    String result;
    try
    {
      executeChildTemplates(elem, true);
        this.m_serializationHandler.endDocument();
      result = sw.toString();
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      sw.getBuffer().setLength(0);
      try
      {
        sw.close();
      }
      catch (Exception ioe){}
      m_stringWriterObjectPool.freeInstance(sw);
      m_serializationHandler.reset();
      m_textResultHandlerObjectPool.freeInstance(m_serializationHandler);
      m_serializationHandler = savedRTreeHandler;
    }
    return result;
  }
  public boolean applyTemplateToNode(ElemTemplateElement xslInstruction,  
                                     ElemTemplate template, int child)
                                             throws TransformerException
  {
    DTM dtm = m_xcontext.getDTM(child);
    short nodeType = dtm.getNodeType(child);
    boolean isDefaultTextRule = false;
    boolean isApplyImports = false;
    isApplyImports = ((xslInstruction == null)
                                ? false
                                : xslInstruction.getXSLToken()
                                  == Constants.ELEMNAME_APPLY_IMPORTS);        
    if (null == template || isApplyImports)
    {
      int maxImportLevel, endImportLevel=0;
      if (isApplyImports)
      {
        maxImportLevel =
          template.getStylesheetComposed().getImportCountComposed() - 1;
        endImportLevel =
          template.getStylesheetComposed().getEndImportCountComposed();
      }
      else
      {
        maxImportLevel = -1;
      }
      if (isApplyImports && (maxImportLevel == -1))
      {
        template = null;
      }
      else
      {
        XPathContext xctxt = m_xcontext;
        try
        {
          xctxt.pushNamespaceContext(xslInstruction);
          QName mode = this.getMode();
          if (isApplyImports)
            template = m_stylesheetRoot.getTemplateComposed(xctxt, child, mode,
                  maxImportLevel, endImportLevel, m_quietConflictWarnings, dtm);
          else
            template = m_stylesheetRoot.getTemplateComposed(xctxt, child, mode,
                  m_quietConflictWarnings, dtm);
        }
        finally
        {
          xctxt.popNamespaceContext();
        }
      }
      if (null == template)
      {
        switch (nodeType)
        {
        case DTM.DOCUMENT_FRAGMENT_NODE :
        case DTM.ELEMENT_NODE :
          template = m_stylesheetRoot.getDefaultRule();
          break;
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
        case DTM.ATTRIBUTE_NODE :
          template = m_stylesheetRoot.getDefaultTextRule();
          isDefaultTextRule = true;
          break;
        case DTM.DOCUMENT_NODE :
          template = m_stylesheetRoot.getDefaultRootRule();
          break;
        default :
          return false;
        }
      }
    }
    try
    {
      pushElemTemplateElement(template);
      m_xcontext.pushCurrentNode(child);
      pushPairCurrentMatched(template, child);
      if (!isApplyImports) {
          DTMIterator cnl = new org.apache.xpath.NodeSetDTM(child, m_xcontext.getDTMManager());
          m_xcontext.pushContextNodeList(cnl);
      }
      if (isDefaultTextRule)
      {
        switch (nodeType)
        {
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
          ClonerToResultTree.cloneToResultTree(child, nodeType, 
                                        dtm, getResultTreeHandler(), false);
          break;
        case DTM.ATTRIBUTE_NODE :
          dtm.dispatchCharactersEvents(child, getResultTreeHandler(), false);
          break;
        }
      }
      else
      {
        m_xcontext.setSAXLocator(template);
        m_xcontext.getVarStack().link(template.m_frameSize);
        executeChildTemplates(template, true);
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      if (!isDefaultTextRule)
        m_xcontext.getVarStack().unlink();
      m_xcontext.popCurrentNode();
      if (!isApplyImports) {
          m_xcontext.popContextNodeList();
      }
      popCurrentMatched();
      popElemTemplateElement();
    }
    return true;
  }
  public void executeChildTemplates(
          ElemTemplateElement elem, org.w3c.dom.Node context, QName mode, ContentHandler handler)
            throws TransformerException
  {
    XPathContext xctxt = m_xcontext;
    try
    {
      if(null != mode)
        pushMode(mode);
      xctxt.pushCurrentNode(xctxt.getDTMHandleFromNode(context));
      executeChildTemplates(elem, handler);
    }
    finally
    {
      xctxt.popCurrentNode();
      if (null != mode)
        popMode();
    }
  }
  public void executeChildTemplates(
          ElemTemplateElement elem, boolean shouldAddAttrs)
            throws TransformerException
  {
    ElemTemplateElement t = elem.getFirstChildElem();
    if (null == t)
      return;      
    if(elem.hasTextLitOnly() && m_optimizer)
    {      
      char[] chars = ((ElemTextLiteral)t).getChars();
      try
      {
        this.pushElemTemplateElement(t);
        m_serializationHandler.characters(chars, 0, chars.length);
      }
      catch(SAXException se)
      {
        throw new TransformerException(se);
      }
      finally
      {
        this.popElemTemplateElement();
      }
      return;
    }
    XPathContext xctxt = m_xcontext;
    xctxt.pushSAXLocatorNull();
    int currentTemplateElementsTop = m_currentTemplateElements.size();
    m_currentTemplateElements.push(null);
    try
    {
      for (; t != null; t = t.getNextSiblingElem())
      {
        if (!shouldAddAttrs
                && t.getXSLToken() == Constants.ELEMNAME_ATTRIBUTE)
          continue;
        xctxt.setSAXLocator(t);
        m_currentTemplateElements.setElementAt(t,currentTemplateElementsTop);
        t.execute(this);
      }
    }
    catch(RuntimeException re)
    {
    	TransformerException te = new TransformerException(re);
    	te.setLocator(t);
    	throw te;
    }
    finally
    {
      m_currentTemplateElements.pop();
      xctxt.popSAXLocator();
    }
  }
     public void executeChildTemplates(
             ElemTemplateElement elem, ContentHandler handler)
               throws TransformerException
     {
       SerializationHandler xoh = this.getSerializationHandler();
       SerializationHandler savedHandler = xoh;
       try
       {
         xoh.flushPending();
         LexicalHandler lex = null;
         if (handler instanceof LexicalHandler) {
            lex = (LexicalHandler) handler;
         }
         m_serializationHandler = new ToXMLSAXHandler(handler, lex, savedHandler.getEncoding());
         m_serializationHandler.setTransformer(this);
         executeChildTemplates(elem, true);
       }
       catch (TransformerException e)
       {
         throw e;
       }
       catch (SAXException se) {
       	 throw new TransformerException(se);
       }
       finally
       {
         m_serializationHandler = savedHandler;
    }
  }
  public Vector processSortKeys(ElemForEach foreach, int sourceNodeContext)
          throws TransformerException
  {
    Vector keys = null;
    XPathContext xctxt = m_xcontext;
    int nElems = foreach.getSortElemCount();
    if (nElems > 0)
      keys = new Vector();
    for (int i = 0; i < nElems; i++)
    {
      ElemSort sort = foreach.getSortElem(i);
      String langString =
        (null != sort.getLang())
        ? sort.getLang().evaluate(xctxt, sourceNodeContext, foreach) : null;
      String dataTypeString = sort.getDataType().evaluate(xctxt,
                                sourceNodeContext, foreach);
      if (dataTypeString.indexOf(":") >= 0)
        System.out.println(
          "TODO: Need to write the hooks for QNAME sort data type");
      else if (!(dataTypeString.equalsIgnoreCase(Constants.ATTRVAL_DATATYPE_TEXT))
               &&!(dataTypeString.equalsIgnoreCase(
                 Constants.ATTRVAL_DATATYPE_NUMBER)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_DATATYPE,
                                    dataTypeString });
      boolean treatAsNumbers =
        ((null != dataTypeString) && dataTypeString.equals(
        Constants.ATTRVAL_DATATYPE_NUMBER)) ? true : false;
      String orderString = sort.getOrder().evaluate(xctxt, sourceNodeContext,
                             foreach);
      if (!(orderString.equalsIgnoreCase(Constants.ATTRVAL_ORDER_ASCENDING))
              &&!(orderString.equalsIgnoreCase(
                Constants.ATTRVAL_ORDER_DESCENDING)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_ORDER,
                                    orderString });
      boolean descending =
        ((null != orderString) && orderString.equals(
        Constants.ATTRVAL_ORDER_DESCENDING)) ? true : false;
      AVT caseOrder = sort.getCaseOrder();
      boolean caseOrderUpper;
      if (null != caseOrder)
      {
        String caseOrderString = caseOrder.evaluate(xctxt, sourceNodeContext,
                                                    foreach);
        if (!(caseOrderString.equalsIgnoreCase(Constants.ATTRVAL_CASEORDER_UPPER))
                &&!(caseOrderString.equalsIgnoreCase(
                  Constants.ATTRVAL_CASEORDER_LOWER)))
          foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                        new Object[]{ Constants.ATTRNAME_CASEORDER,
                                      caseOrderString });
        caseOrderUpper =
          ((null != caseOrderString) && caseOrderString.equals(
          Constants.ATTRVAL_CASEORDER_UPPER)) ? true : false;
      }
      else
      {
        caseOrderUpper = false;
      }
      keys.addElement(new NodeSortKey(this, sort.getSelect(), treatAsNumbers,
                                      descending, langString, caseOrderUpper,
                                      foreach));
     }
    return keys;
  }
  public int getCurrentTemplateElementsCount()
  {
  	return m_currentTemplateElements.size();
  }
  public ObjectStack getCurrentTemplateElements()
  {
  	return m_currentTemplateElements;
  }
  public void pushElemTemplateElement(ElemTemplateElement elem)
  {
    m_currentTemplateElements.push(elem);
  }
  public void popElemTemplateElement()
  {
    m_currentTemplateElements.pop();
  }
  public void setCurrentElement(ElemTemplateElement e)
  {
    m_currentTemplateElements.setTop(e);
  }
  public ElemTemplateElement getCurrentElement()
  {
    return (m_currentTemplateElements.size() > 0) ? 
        (ElemTemplateElement) m_currentTemplateElements.peek() : null;
  }
  public int getCurrentNode()
  {
    return m_xcontext.getCurrentNode();
  }
  public ElemTemplate getCurrentTemplate()
  {
    ElemTemplateElement elem = getCurrentElement();
    while ((null != elem)
           && (elem.getXSLToken() != Constants.ELEMNAME_TEMPLATE))
    {
      elem = elem.getParentElem();
    }
    return (ElemTemplate) elem;
  }
  public void pushPairCurrentMatched(ElemTemplateElement template, int child)
  {
    m_currentMatchTemplates.push(template);
    m_currentMatchedNodes.push(child);
  }
  public void popCurrentMatched()
  {
    m_currentMatchTemplates.pop();
    m_currentMatchedNodes.pop();
  }
  public ElemTemplate getMatchedTemplate()
  {
    return (ElemTemplate) m_currentMatchTemplates.peek();
  }
  public int getMatchedNode()
  {
    return m_currentMatchedNodes.peepTail();
  }
  public DTMIterator getContextNodeList()
  {
    try
    {
      DTMIterator cnl = m_xcontext.getContextNodeList();
      return (cnl == null) ? null : (DTMIterator) cnl.cloneWithReset();
    }
    catch (CloneNotSupportedException cnse)
    {
      return null;
    }
  }
  public Transformer getTransformer()
  {
    return this;
  }
  public void setStylesheet(StylesheetRoot stylesheetRoot)
  {
    m_stylesheetRoot = stylesheetRoot;
  }
  public final StylesheetRoot getStylesheet()
  {
    return m_stylesheetRoot;
  }
  public boolean getQuietConflictWarnings()
  {
    return m_quietConflictWarnings;
  }
  public void setXPathContext(XPathContext xcontext)
  {
    m_xcontext = xcontext;
  }
  public final XPathContext getXPathContext()
  {
    return m_xcontext;
  }
  public SerializationHandler getResultTreeHandler()
  {
    return m_serializationHandler;
  }
  public SerializationHandler getSerializationHandler()
  {
    return m_serializationHandler;
  }
  public KeyManager getKeyManager()
  {
    return m_keyManager;
  }
  public boolean isRecursiveAttrSet(ElemAttributeSet attrSet)
  {
    if (null == m_attrSetStack)
    {
      m_attrSetStack = new Stack();
    }
    if (!m_attrSetStack.empty())
    {
      int loc = m_attrSetStack.search(attrSet);
      if (loc > -1)
      {
        return true;
      }
    }
    return false;
  }
  public void pushElemAttributeSet(ElemAttributeSet attrSet)
  {
    m_attrSetStack.push(attrSet);
  }
  public void popElemAttributeSet()
  {
    m_attrSetStack.pop();
  }
  public CountersTable getCountersTable()
  {
    if (null == m_countersTable)
      m_countersTable = new CountersTable();
    return m_countersTable;
  }
  public boolean currentTemplateRuleIsNull()
  {
    return ((!m_currentTemplateRuleIsNull.isEmpty())
            && (m_currentTemplateRuleIsNull.peek() == true));
  }
  public void pushCurrentTemplateRuleIsNull(boolean b)
  {
    m_currentTemplateRuleIsNull.push(b);
  }
  public void popCurrentTemplateRuleIsNull()
  {
    m_currentTemplateRuleIsNull.pop();
  }
  public void pushCurrentFuncResult(Object val) {
    m_currentFuncResult.push(val);
  }
  public Object popCurrentFuncResult() {
    return m_currentFuncResult.pop();
  }
  public boolean currentFuncResultSeen() {
    return !m_currentFuncResult.empty()
               && m_currentFuncResult.peek() != null;
  }
  public MsgMgr getMsgMgr()
  {
    if (null == m_msgMgr)
      m_msgMgr = new MsgMgr(this);
    return m_msgMgr;
  }
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {
    synchronized (m_reentryGuard)
    {
      if (listener == null)
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_ERROR_HANDLER, null)); 
      m_errorHandler = listener;
    }
  }
  public ErrorListener getErrorListener()
  {
    return m_errorHandler;
  }
  public boolean getFeature(String name)
          throws SAXNotRecognizedException, SAXNotSupportedException
  {
    if ("http:
      return true;
    else if ("http:
      return true;
    throw new SAXNotRecognizedException(name);
  }
  public QName getMode()
  {
    return m_modes.isEmpty() ? null : (QName) m_modes.peek();
  }
  public void pushMode(QName mode)
  {
    m_modes.push(mode);
  }
  public void popMode()
  {
    m_modes.pop();
  }
  public void runTransformThread(int priority)
  {
    Thread t = ThreadControllerWrapper.runThread(this, priority);
    this.setTransformThread(t);
  }
  public void runTransformThread()
  {
    ThreadControllerWrapper.runThread(this, -1);
  }
  public static void runTransformThread(Runnable runnable)
  {
    ThreadControllerWrapper.runThread(runnable, -1);
  }
  public void waitTransformThread() throws SAXException
  {
    Thread transformThread = this.getTransformThread();
    if (null != transformThread)
    {
      try
      {
        ThreadControllerWrapper.waitThread(transformThread, this);
        if (!this.hasTransformThreadErrorCatcher())
        {
          Exception e = this.getExceptionThrown();
          if (null != e)
          {
            e.printStackTrace();
            throw new org.xml.sax.SAXException(e);
          }
        }
        this.setTransformThread(null);
      }
      catch (InterruptedException ie){}
    }
  }
  public Exception getExceptionThrown()
  {
    return m_exceptionThrown;
  }
  public void setExceptionThrown(Exception e)
  {
    m_exceptionThrown = e;
  }
  public void setSourceTreeDocForThread(int doc)
  {
    m_doc = doc;
  }
  void postExceptionFromThread(Exception e)
  {
    m_exceptionThrown = e;
    ;  
    synchronized (this)
    {
      notifyAll();
    }
  }
  public void run()
  {
    m_hasBeenReset = false;
    try
    {
      try
      {
        transformNode(m_doc);
      }
      catch (Exception e)
      {
        if (null != m_transformThread)
          postExceptionFromThread(e);   
        else 
          throw new RuntimeException(e.getMessage());
      }
      finally
      {
        if (m_inputContentHandler instanceof TransformerHandlerImpl)
        {
          ((TransformerHandlerImpl) m_inputContentHandler).clearCoRoutine();
        }
      }
    }
    catch (Exception e)
    {
      if (null != m_transformThread)
        postExceptionFromThread(e);
      else 
        throw new RuntimeException(e.getMessage());         
    }
  }
  public short getShouldStripSpace(int elementHandle, DTM dtm)
  {
    try
    {
      org.apache.xalan.templates.WhiteSpaceInfo info =
        m_stylesheetRoot.getWhiteSpaceInfo(m_xcontext, elementHandle, dtm);
      if (null == info)
      {
        return DTMWSFilter.INHERIT;
      }
      else
      {
        return info.getShouldStripSpace()
               ? DTMWSFilter.STRIP : DTMWSFilter.NOTSTRIP;
      }
    }
    catch (TransformerException se)
    {
      return DTMWSFilter.INHERIT;
    }
  }
   public void init(ToXMLSAXHandler h,Transformer transformer, ContentHandler realHandler)
   {
      h.setTransformer(transformer);
      h.setContentHandler(realHandler);
   }
   public void setSerializationHandler(SerializationHandler xoh)
   {
      m_serializationHandler = xoh;
   }
	public void fireGenerateEvent(
		int eventType,
		char[] ch,
		int start,
		int length) {
	}
	public void fireGenerateEvent(
		int eventType,
		String name,
		Attributes atts) {
	}
	public void fireGenerateEvent(int eventType, String name, String data) {
	}
	public void fireGenerateEvent(int eventType, String data) {
	}
	public void fireGenerateEvent(int eventType) {
	}
    public boolean hasTraceListeners() {
        return false;
    }
    public boolean getIncremental() {
        return m_incremental;
    }
    public boolean getOptimize() {
        return m_optimizer;
    }
    public boolean getSource_location() {
        return m_source_location;
    }
}  
