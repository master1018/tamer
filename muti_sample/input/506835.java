public class TrAXFilter extends XMLFilterImpl
{
  private Templates m_templates;
  private TransformerImpl m_transformer;
  public TrAXFilter (Templates templates)
    throws TransformerConfigurationException
  {
    m_templates = templates;
    m_transformer = (TransformerImpl)templates.newTransformer();
  }
  public TransformerImpl getTransformer()
  {
    return m_transformer;
  }
  public void setParent (XMLReader parent)
  { 
    super.setParent(parent);
    if(null != parent.getContentHandler())
      this.setContentHandler(parent.getContentHandler());
    setupParse ();
  }
  public void parse (InputSource input)
    throws org.xml.sax.SAXException, IOException
  {
    if(null == getParent())
    {
      XMLReader reader=null;
      try {
          javax.xml.parsers.SAXParserFactory factory=
              javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware( true );
          if (m_transformer.getStylesheet().isSecureProcessing()) {
              try {
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
      } catch( NoSuchMethodError ex2 ) {
      }
      catch (AbstractMethodError ame){}
      XMLReader parent;
      if( reader==null )
          parent= XMLReaderFactory.createXMLReader();
      else
          parent=reader;
      try
      {
        parent.setFeature("http:
                          true);
      }
      catch (org.xml.sax.SAXException se){}
      setParent(parent);
    }
    else
    {
      setupParse ();
    }
    if(null == m_transformer.getContentHandler())
    {
      throw new org.xml.sax.SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_CALL_PARSE, null)); 
    }
    getParent().parse(input);
    Exception e = m_transformer.getExceptionThrown();
    if(null != e)
    {
      if(e instanceof org.xml.sax.SAXException)
        throw (org.xml.sax.SAXException)e;
      else
        throw new org.xml.sax.SAXException(e);
    }
  }
  public void parse (String systemId)
    throws org.xml.sax.SAXException, IOException
  {
    parse(new InputSource(systemId));
  }
  private void setupParse ()
  {
    XMLReader p = getParent();
    if (p == null) {
      throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_PARENT_FOR_FILTER, null)); 
    }
    ContentHandler ch = m_transformer.getInputContentHandler();
    p.setContentHandler(ch);
    p.setEntityResolver(this);
    p.setDTDHandler(this);
    p.setErrorHandler(this);
  }
  public void setContentHandler (ContentHandler handler)
  {
    m_transformer.setContentHandler(handler);
  }
  public void setErrorListener (ErrorListener handler)
  {
    m_transformer.setErrorListener(handler);
  }
}
