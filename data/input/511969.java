public class TransformerFactoryImpl extends SAXTransformerFactory
{
  public static final String XSLT_PROPERTIES =
    "org/apache/xalan/res/XSLTInfo.properties";
  private boolean m_isSecureProcessing = false;
  public TransformerFactoryImpl()
  {
  }
  public static final String FEATURE_INCREMENTAL =
                             "http:
  public static final String FEATURE_OPTIMIZE =
                             "http:
  public static final String FEATURE_SOURCE_LOCATION =
                             XalanProperties.SOURCE_LOCATION;
  public javax.xml.transform.Templates processFromNode(Node node)
          throws TransformerConfigurationException
  {
    try
    {
      TemplatesHandler builder = newTemplatesHandler();
      TreeWalker walker = new TreeWalker(builder,
                                         new org.apache.xml.utils.DOM2Helper(),
                                         builder.getSystemId());
      walker.traverse(node);
      return builder.getTemplates();
    }
    catch (org.xml.sax.SAXException se)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(se));
        }
        catch (TransformerConfigurationException ex)
        {
          throw ex;
        }
        catch (TransformerException ex)
        {
          throw new TransformerConfigurationException(ex);
        }
        return null;
      }
      else
      {
        throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), se); 
      }
    }
    catch (TransformerConfigurationException tce)
    {
      throw tce;
    }
    catch (Exception e)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(e));
        }
        catch (TransformerConfigurationException ex)
        {
          throw ex;
        }
        catch (TransformerException ex)
        {
          throw new TransformerConfigurationException(ex);
        }
        return null;
      }
      else
      {
        throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), e); 
      }
    }
  }
  private String m_DOMsystemID = null;
  String getDOMsystemID()
  {
    return m_DOMsystemID;
  }
  javax.xml.transform.Templates processFromNode(Node node, String systemID)
          throws TransformerConfigurationException
  {
    m_DOMsystemID = systemID;
    return processFromNode(node);
  }
  public Source getAssociatedStylesheet(
          Source source, String media, String title, String charset)
            throws TransformerConfigurationException
  {
    String baseID;
    InputSource isource = null;
    Node node = null;
    XMLReader reader = null;
    if (source instanceof DOMSource)
    {
      DOMSource dsource = (DOMSource) source;
      node = dsource.getNode();
      baseID = dsource.getSystemId();
    }
    else
    {
      isource = SAXSource.sourceToInputSource(source);
      baseID = isource.getSystemId();
    }
    StylesheetPIHandler handler = new StylesheetPIHandler(baseID, media,
                                    title, charset);
    if (m_uriResolver != null) 
    {
      handler.setURIResolver(m_uriResolver); 
    }
    try
    {
      if (null != node)
      {
        TreeWalker walker = new TreeWalker(handler, new org.apache.xml.utils.DOM2Helper(), baseID);
        walker.traverse(node);
      }
      else
      {
        try
        {
          javax.xml.parsers.SAXParserFactory factory =
            javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware(true);
          if (m_isSecureProcessing)
          {
            try
            {
              factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (org.xml.sax.SAXException e) {}
          }
          javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();
          reader = jaxpParser.getXMLReader();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
          throw new org.xml.sax.SAXException(ex);
        }
        catch (javax.xml.parsers.FactoryConfigurationError ex1)
        {
          throw new org.xml.sax.SAXException(ex1.toString());
        }
        catch (NoSuchMethodError ex2){}
        catch (AbstractMethodError ame){}
        if (null == reader)
        {
          reader = XMLReaderFactory.createXMLReader();
        }
        reader.setContentHandler(handler);
        reader.parse(isource);
      }
    }
    catch (StopParseException spe)
    {
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerConfigurationException(
        "getAssociatedStylesheets failed", se);
    }
    catch (IOException ioe)
    {
      throw new TransformerConfigurationException(
        "getAssociatedStylesheets failed", ioe);
    }
    return handler.getAssociatedStylesheet();
  }
  public TemplatesHandler newTemplatesHandler()
          throws TransformerConfigurationException
  {
    return new StylesheetHandler(this);
  }
  public void setFeature(String name, boolean value)
	  throws TransformerConfigurationException {
  	if (name == null) {
  	    throw new NullPointerException(
                  XSLMessages.createMessage(
                      XSLTErrorResources.ER_SET_FEATURE_NULL_NAME, null));    
  	}
  	if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
  	    m_isSecureProcessing = value;			
  	}
  	else
    {
      throw new TransformerConfigurationException(
          XSLMessages.createMessage(
            XSLTErrorResources.ER_UNSUPPORTED_FEATURE, 
            new Object[] {name}));
    }
  }
  public boolean getFeature(String name) {
    if (name == null) 
    {
    	throw new NullPointerException(
            XSLMessages.createMessage(
            XSLTErrorResources.ER_GET_FEATURE_NULL_NAME, null));    
    }
    if ((DOMResult.FEATURE == name) || (DOMSource.FEATURE == name)
            || (SAXResult.FEATURE == name) || (SAXSource.FEATURE == name)
            || (StreamResult.FEATURE == name)
            || (StreamSource.FEATURE == name)
            || (SAXTransformerFactory.FEATURE == name)
            || (SAXTransformerFactory.FEATURE_XMLFILTER == name))
      return true;
    else if ((DOMResult.FEATURE.equals(name))
             || (DOMSource.FEATURE.equals(name))
             || (SAXResult.FEATURE.equals(name))
             || (SAXSource.FEATURE.equals(name))
             || (StreamResult.FEATURE.equals(name))
             || (StreamSource.FEATURE.equals(name))
             || (SAXTransformerFactory.FEATURE.equals(name))
             || (SAXTransformerFactory.FEATURE_XMLFILTER.equals(name)))
      return true;	      
    else if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING))
      return m_isSecureProcessing;
    else      
      return false;
  }
  private boolean m_optimize = true;
  private boolean m_source_location = false;
  private boolean m_incremental = false;
  public void setAttribute(String name, Object value)
          throws IllegalArgumentException
  {
    if (name.equals(FEATURE_INCREMENTAL))
    {
      if(value instanceof Boolean)
      {
        m_incremental = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        m_incremental = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); 
      }
	}
    else if (name.equals(FEATURE_OPTIMIZE))
    {
      if(value instanceof Boolean)
      {
        m_optimize = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        m_optimize = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); 
      }
    }
    else if(name.equals(FEATURE_SOURCE_LOCATION))
    {
      if(value instanceof Boolean)
      {
        m_source_location = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        m_source_location = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); 
      }
    }
    else
    {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUPPORTED, new Object[]{name})); 
    }
  }
  public Object getAttribute(String name) throws IllegalArgumentException
  {
    if (name.equals(FEATURE_INCREMENTAL))
    {
      return new Boolean(m_incremental);            
    }
    else if (name.equals(FEATURE_OPTIMIZE))
    {
      return new Boolean(m_optimize);
    }
    else if (name.equals(FEATURE_SOURCE_LOCATION))
    {
      return new Boolean(m_source_location);
    }
    else
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ATTRIB_VALUE_NOT_RECOGNIZED, new Object[]{name})); 
  }
  public XMLFilter newXMLFilter(Source src)
          throws TransformerConfigurationException
  {
    Templates templates = newTemplates(src);
    if( templates==null ) return null;
    return newXMLFilter(templates);
  }
  public XMLFilter newXMLFilter(Templates templates)
          throws TransformerConfigurationException
  {
    try 
    {
      return new TrAXFilter(templates);
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null;
        } 
        catch( TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch( TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      throw ex;
    }
  }
  public TransformerHandler newTransformerHandler(Source src)
          throws TransformerConfigurationException
  {
    Templates templates = newTemplates(src);
    if( templates==null ) return null;
    return newTransformerHandler(templates);
  }
  public TransformerHandler newTransformerHandler(Templates templates)
          throws TransformerConfigurationException
  {
    try {
      TransformerImpl transformer =
        (TransformerImpl) templates.newTransformer();
      transformer.setURIResolver(m_uriResolver);
      TransformerHandler th =
        (TransformerHandler) transformer.getInputContentHandler(true);
      return th;
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null ) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null;
        } 
        catch (TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch (TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      throw ex;
    }
  }
  public TransformerHandler newTransformerHandler()
          throws TransformerConfigurationException
  {
    return new TransformerIdentityImpl(m_isSecureProcessing);
  }
  public Transformer newTransformer(Source source)
          throws TransformerConfigurationException
  {
    try 
    {
      Templates tmpl=newTemplates( source );
      if( tmpl==null ) return null;
      Transformer transformer = tmpl.newTransformer();
      transformer.setURIResolver(m_uriResolver);
      return transformer;
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null ) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null; 
        } 
        catch( TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch( TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException( ex1 );
        }
      }
      throw ex;
    }
  }
  public Transformer newTransformer() throws TransformerConfigurationException
  {
      return new TransformerIdentityImpl(m_isSecureProcessing);
  }
  public Templates newTemplates(Source source)
          throws TransformerConfigurationException
  {
    String baseID = source.getSystemId();
    if (null != baseID) {
       baseID = SystemIDResolver.getAbsoluteURI(baseID);
    }
    if (source instanceof DOMSource)
    {
      DOMSource dsource = (DOMSource) source;
      Node node = dsource.getNode();
      if (null != node)
        return processFromNode(node, baseID);
      else
      {
        String messageStr = XSLMessages.createMessage(
          XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null);
        throw new IllegalArgumentException(messageStr);
      }
    }
    TemplatesHandler builder = newTemplatesHandler();
    builder.setSystemId(baseID);
    try
    {
      InputSource isource = SAXSource.sourceToInputSource(source);
      isource.setSystemId(baseID);
      XMLReader reader = null;
      if (source instanceof SAXSource)
        reader = ((SAXSource) source).getXMLReader();
      if (null == reader)
      {
        try
        {
          javax.xml.parsers.SAXParserFactory factory =
            javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware(true);
          if (m_isSecureProcessing)
          {
            try
            {
              factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (org.xml.sax.SAXException se) {}
          }
          javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();
          reader = jaxpParser.getXMLReader();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
          throw new org.xml.sax.SAXException(ex);
        }
        catch (javax.xml.parsers.FactoryConfigurationError ex1)
        {
          throw new org.xml.sax.SAXException(ex1.toString());
        }
        catch (NoSuchMethodError ex2){}
        catch (AbstractMethodError ame){}
      }
      if (null == reader)
        reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(builder);
      reader.parse(isource);
    }
    catch (org.xml.sax.SAXException se)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(se));
        }
        catch (TransformerConfigurationException ex1)
        {
          throw ex1;
        }
        catch (TransformerException ex1)
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      else
      {
        throw new TransformerConfigurationException(se.getMessage(), se);
      }
    }
    catch (Exception e)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(e));
          return null;
        }
        catch (TransformerConfigurationException ex1)
        {
          throw ex1;
        }
        catch (TransformerException ex1)
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      else
      {
        throw new TransformerConfigurationException(e.getMessage(), e);
      }
    }
    return builder.getTemplates();
  }
  URIResolver m_uriResolver;
  public void setURIResolver(URIResolver resolver)
  {
    m_uriResolver = resolver;
  }
  public URIResolver getURIResolver()
  {
    return m_uriResolver;
  }
  private ErrorListener m_errorListener = new org.apache.xml.utils.DefaultErrorHandler(false);
  public ErrorListener getErrorListener()
  {
    return m_errorListener;
  }
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {
    if (null == listener)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ERRORLISTENER, null));
    m_errorListener = listener;
  }
  public boolean isSecureProcessing()
  {
    return m_isSecureProcessing;
  }
}
