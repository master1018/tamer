public class ProcessorInclude extends XSLTElementProcessor
{
    static final long serialVersionUID = -4570078731972673481L;
  private String m_href = null;
  public String getHref()
  {
    return m_href;
  }
  public void setHref(String baseIdent)
  {
    m_href = baseIdent;
  }
  protected int getStylesheetType()
  {
    return StylesheetHandler.STYPE_INCLUDE;
  }
  protected String getStylesheetInclErr()
  {
    return XSLTErrorResources.ER_STYLESHEET_INCLUDES_ITSELF;
  }
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    setPropertiesFromAttributes(handler, rawName, attributes, this);
    try
    {
      Source sourceFromURIResolver = getSourceFromUriResolver(handler);
      String hrefUrl = getBaseURIOfIncludedStylesheet(handler, sourceFromURIResolver);
      if (handler.importStackContains(hrefUrl))
      {
        throw new org.xml.sax.SAXException(
          XSLMessages.createMessage(
          getStylesheetInclErr(), new Object[]{ hrefUrl }));  
      }
      handler.pushImportURL(hrefUrl);
      handler.pushImportSource(sourceFromURIResolver);
      int savedStylesheetType = handler.getStylesheetType();
      handler.setStylesheetType(this.getStylesheetType());
      handler.pushNewNamespaceSupport();
      try
      {
        parse(handler, uri, localName, rawName, attributes);
      }
      finally
      {
        handler.setStylesheetType(savedStylesheetType);
        handler.popImportURL();
        handler.popImportSource();
        handler.popNamespaceSupport();
      }
    }
    catch(TransformerException te)
    {
      handler.error(te.getMessage(), te);
    }
  }
  protected void parse(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    TransformerFactoryImpl processor = handler.getStylesheetProcessor();
    URIResolver uriresolver = processor.getURIResolver();
    try
    {
      Source source = null;
      if (null != uriresolver)
      {
        source = handler.peekSourceFromURIResolver();
        if (null != source && source instanceof DOMSource)
        {
          Node node = ((DOMSource)source).getNode();
          String systemId = handler.peekImportURL();
          if (systemId != null)
              handler.pushBaseIndentifier(systemId);
          TreeWalker walker = new TreeWalker(handler, new org.apache.xml.utils.DOM2Helper(), systemId);
          try
          {
            walker.traverse(node);
          }
          catch(org.xml.sax.SAXException se)
          {
            throw new TransformerException(se);
          }
          if (systemId != null)
            handler.popBaseIndentifier();
          return;
        }
      }
      if(null == source)
      {
        String absURL = SystemIDResolver.getAbsoluteURI(getHref(),
                          handler.getBaseIdentifier());
        source = new StreamSource(absURL);
      }
      source = processSource(handler, source);
      XMLReader reader = null;
      if(source instanceof SAXSource)
      {
        SAXSource saxSource = (SAXSource)source;
        reader = saxSource.getXMLReader(); 
      }
      InputSource inputSource = SAXSource.sourceToInputSource(source);
      if (null == reader)
      {  
        try {
          javax.xml.parsers.SAXParserFactory factory=
                                                     javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware( true );
          if (handler.getStylesheetProcessor().isSecureProcessing())
          {
            try
            {
              factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (org.xml.sax.SAXException se) {}
          }
          javax.xml.parsers.SAXParser jaxpParser=
                                                 factory.newSAXParser();
          reader=jaxpParser.getXMLReader();
        } catch( javax.xml.parsers.ParserConfigurationException ex ) {
          throw new org.xml.sax.SAXException( ex );
        } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
            throw new org.xml.sax.SAXException( ex1.toString() );
        } 
        catch( NoSuchMethodError ex2 ) 
        {
        }
        catch (AbstractMethodError ame){}
      }
      if (null == reader)
        reader = XMLReaderFactory.createXMLReader();
      if (null != reader)
      {
        reader.setContentHandler(handler);
        handler.pushBaseIndentifier(inputSource.getSystemId());
        try
        {
          reader.parse(inputSource);
        }
        finally
        {
          handler.popBaseIndentifier();
        }
      }
    }
    catch (IOException ioe)
    {
      handler.error(XSLTErrorResources.ER_IOEXCEPTION,
                    new Object[]{ getHref() }, ioe);
    }
    catch(TransformerException te)
    {
      handler.error(te.getMessage(), te);
    }
  }
  protected Source processSource(StylesheetHandler handler, Source source)
  {
      return source;
  }
  private Source getSourceFromUriResolver(StylesheetHandler handler)
            throws TransformerException {
        Source s = null;
            TransformerFactoryImpl processor = handler.getStylesheetProcessor();
            URIResolver uriresolver = processor.getURIResolver();
            if (uriresolver != null) {
                String href = getHref();
                String base = handler.getBaseIdentifier();
                s = uriresolver.resolve(href,base);
            }
        return s;
    }
    private String getBaseURIOfIncludedStylesheet(StylesheetHandler handler, Source s)
            throws TransformerException {
        String baseURI;
        String idFromUriResolverSource;
        if (s != null && (idFromUriResolverSource = s.getSystemId()) != null) {
            baseURI = idFromUriResolverSource;
        } else {
            baseURI = SystemIDResolver.getAbsoluteURI(getHref(), handler
                    .getBaseIdentifier());
        }
        return baseURI;
    }
}
