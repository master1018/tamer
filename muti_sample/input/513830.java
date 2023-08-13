public class ElemLiteralResult extends ElemUse
{
    static final long serialVersionUID = -8703409074421657260L;
    private static final String EMPTYSTRING = "";
  private boolean isLiteralResultAsStylesheet = false;
  public void setIsLiteralResultAsStylesheet(boolean b)
  {
    isLiteralResultAsStylesheet = b;
  }
  public boolean getIsLiteralResultAsStylesheet()
  {
    return isLiteralResultAsStylesheet;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if (null != m_avts)
    {
      int nAttrs = m_avts.size();
      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);
        avt.fixupVariables(vnames, cstate.getGlobalsSize());
      } 
    }   
  }
  private List m_avts = null;
  private List m_xslAttr = null;
  public void addLiteralResultAttribute(AVT avt)
  {
    if (null == m_avts)
      m_avts = new ArrayList();
    m_avts.add(avt);
  }
  public void addLiteralResultAttribute(String att)
  {
    if (null == m_xslAttr)
      m_xslAttr = new ArrayList();
    m_xslAttr.add(att);
  }
  public void setXmlSpace(AVT avt)
  {
    addLiteralResultAttribute(avt);
    String val = avt.getSimpleString();
    if(val.equals("default"))
    {
      super.setXmlSpace(Constants.ATTRVAL_STRIP);
    }
    else if(val.equals("preserve"))
    {
      super.setXmlSpace(Constants.ATTRVAL_PRESERVE);
    }
  }
  public AVT getLiteralResultAttributeNS(String namespaceURI, String localName)
  {
    if (null != m_avts)
    {
      int nAttrs = m_avts.size();
      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);
        if (avt.getName().equals(localName) && 
                avt.getURI().equals(namespaceURI))
        {
          return avt;
        }
      }  
    }
    return null;
  }
  public String getAttributeNS(String namespaceURI, String localName)
  {
    AVT avt = getLiteralResultAttributeNS(namespaceURI, localName);
    if ((null != avt))
    {
      return avt.getSimpleString();
    }
    return EMPTYSTRING;
  }
  public AVT getLiteralResultAttribute(String name)
  {
    if (null != m_avts)
    {
      int nAttrs = m_avts.size();
      String namespace = null;
      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);
        namespace = avt.getURI();
        if ((namespace != null && (!namespace.equals("")) && (namespace 
                +":"+avt.getName()).equals(name))|| ((namespace == null || 
                namespace.equals(""))&& avt.getRawName().equals(name)))
        {
          return avt;
        }
      }  
    }
    return null;
  }
  public String getAttribute(String rawName)
  {
    AVT avt = getLiteralResultAttribute(rawName);
    if ((null != avt))
    {
      return avt.getSimpleString();
    }
    return EMPTYSTRING;
  }
  public boolean containsExcludeResultPrefix(String prefix, String uri)
  {
    if (uri == null ||
                (null == m_excludeResultPrefixes &&
                 null == m_ExtensionElementURIs)
                )
      return super.containsExcludeResultPrefix(prefix, uri);
    if (prefix.length() == 0)
      prefix = Constants.ATTRVAL_DEFAULT_PREFIX;
        if(m_excludeResultPrefixes!=null)
            for (int i =0; i< m_excludeResultPrefixes.size(); i++)
            {
                if (uri.equals(getNamespaceForPrefix(m_excludeResultPrefixes.elementAt(i))))
                    return true;
            }    
    if(m_ExtensionElementURIs!=null && m_ExtensionElementURIs.contains(uri))
       return true;
        return super.containsExcludeResultPrefix(prefix, uri);
  }
  public void resolvePrefixTables() throws TransformerException
  {
    super.resolvePrefixTables();
    StylesheetRoot stylesheet = getStylesheetRoot();
    if ((null != m_namespace) && (m_namespace.length() > 0))
    {
      NamespaceAlias nsa = stylesheet.getNamespaceAliasComposed(m_namespace);
      if (null != nsa)
      {
        m_namespace = nsa.getResultNamespace();
        String resultPrefix = nsa.getStylesheetPrefix();  
        if ((null != resultPrefix) && (resultPrefix.length() > 0))
          m_rawName = resultPrefix + ":" + m_localName;
        else
          m_rawName = m_localName;
      }
    }
    if (null != m_avts)
    {
      int n = m_avts.size();
      for (int i = 0; i < n; i++)
      {
        AVT avt = (AVT) m_avts.get(i);
        String ns = avt.getURI();
        if ((null != ns) && (ns.length() > 0))
        {
          NamespaceAlias nsa =
            stylesheet.getNamespaceAliasComposed(m_namespace); 
          if (null != nsa)
          {
            String namespace = nsa.getResultNamespace();
            String resultPrefix = nsa.getStylesheetPrefix();  
            String rawName = avt.getName();
            if ((null != resultPrefix) && (resultPrefix.length() > 0))
              rawName = resultPrefix + ":" + rawName;
            avt.setURI(namespace);
            avt.setRawName(rawName);
          }
        }
      }
    }
  }
  boolean needToCheckExclude()
  {
    if (null == m_excludeResultPrefixes && null == getPrefixTable()
                && m_ExtensionElementURIs==null     
                )
      return false;
    else
    {
      if (null == getPrefixTable())
        setPrefixTable(new java.util.ArrayList());
      return true;
    }
  }
  private String m_namespace;
  public void setNamespace(String ns)
  {
    if(null == ns) 
      ns = "";
    m_namespace = ns;
  }
  public String getNamespace()
  {
    return m_namespace;
  }
  private String m_localName;
  public void setLocalName(String localName)
  {
    m_localName = localName;
  }
  public String getLocalName()
  {
    return m_localName;
  }
  private String m_rawName;
  public void setRawName(String rawName)
  {
    m_rawName = rawName;
  }
  public String getRawName()
  {
    return m_rawName;
  }
  public String getPrefix()
  {
        int len=m_rawName.length()-m_localName.length()-1;
    return (len>0)
            ? m_rawName.substring(0,len)
            : "";
  }
  private StringVector m_ExtensionElementURIs;
  public void setExtensionElementPrefixes(StringVector v)
  {
    m_ExtensionElementURIs = v;
  }
  public NamedNodeMap getAttributes()
  {
        return new LiteralElementAttributes();
  }
  public class LiteralElementAttributes implements NamedNodeMap{
          private int m_count = -1;
          public LiteralElementAttributes(){         
          }
          public int getLength()
          {
            if (m_count == -1)
            {
               if (null != m_avts) m_count = m_avts.size();
               else m_count = 0;
            }
            return m_count;
          }
          public Node getNamedItem(String name)
          {
                if (getLength() == 0) return null;
                String uri = null;
                String localName = name; 
                int index = name.indexOf(":"); 
                if (-1 != index){
                         uri = name.substring(0, index);
                         localName = name.substring(index+1);
                }
                Node retNode = null;
                Iterator eum = m_avts.iterator();
                while (eum.hasNext()){
                        AVT avt = (AVT) eum.next();
                        if (localName.equals(avt.getName()))
                        {
                          String nsURI = avt.getURI(); 
                          if ((uri == null && nsURI == null)
                            || (uri != null && uri.equals(nsURI)))
                          {
                            retNode = new Attribute(avt, ElemLiteralResult.this);
                            break;
                          }
                        }
                }
                return retNode;
          }
          public Node getNamedItemNS(String namespaceURI, String localName)
          {
                  if (getLength() == 0) return null;
                  Node retNode = null;
                  Iterator eum = m_avts.iterator();
                  while (eum.hasNext())
                  {
                    AVT avt = (AVT) eum.next();      
                    if (localName.equals(avt.getName()))
                    {
                      String nsURI = avt.getURI(); 
                      if ((namespaceURI == null && nsURI == null)
                        || (namespaceURI != null && namespaceURI.equals(nsURI)))
                      {
                        retNode = new Attribute(avt, ElemLiteralResult.this);
                        break;
                      }
                    }
                  }
                  return retNode;
          }
          public Node item(int i)
          {
                if (getLength() == 0 || i >= m_avts.size()) return null;
                else return 
                    new Attribute(((AVT)m_avts.get(i)), 
                        ElemLiteralResult.this);
          }
          public Node removeNamedItem(String name) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          public Node removeNamedItemNS(String namespaceURI, String localName) 
                throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          } 
          public Node setNamedItem(Node arg) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          public Node setNamedItemNS(Node arg) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }                                                                         
  }
  public class Attribute implements Attr{
          private AVT m_attribute;
          private Element m_owner = null;
          public Attribute(AVT avt, Element elem){
                m_attribute = avt;
                m_owner = elem;
          }
          public Node appendChild(Node newChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          public Node cloneNode(boolean deep)
          {
                  return new Attribute(m_attribute, m_owner);
          }
          public NamedNodeMap getAttributes()
          {
            return null;
          }
          public NodeList getChildNodes()
          {
                  return new NodeList(){
                          public int getLength(){
                                  return 0;
                          }
                          public Node item(int index){
                                  return null;
                          }
                  };
          }
          public Node getFirstChild()
          {
                  return null;
          }
          public Node getLastChild()
          {
                  return null;
          }
          public String getLocalName()
          {
                  return m_attribute.getName();
          }
          public String getNamespaceURI()
          {
                  String uri = m_attribute.getURI();
                  return (uri.equals(""))?null:uri;
          }
          public Node getNextSibling()
          {
                return null;
          }
          public String getNodeName()
          {
                  String uri = m_attribute.getURI();
                  String localName = getLocalName();
                  return (uri.equals(""))?localName:uri+":"+localName;
          }
          public short getNodeType()
          {
                  return ATTRIBUTE_NODE;
          }
          public String getNodeValue() throws DOMException
          {
                  return m_attribute.getSimpleString();
          }
          public Document getOwnerDocument()
          {
            return m_owner.getOwnerDocument();
          }
          public Node getParentNode()
          {
                  return m_owner;
          }
          public String getPrefix()
          {
                  String uri = m_attribute.getURI();
                  String rawName = m_attribute.getRawName();
                  return (uri.equals(""))? 
                        null:rawName.substring(0, rawName.indexOf(":"));
          }
          public Node getPreviousSibling()
          {
                  return null;
          }
          public boolean hasAttributes()
          {
                  return false;
          }
          public boolean hasChildNodes()
          {
                  return false;
          }                    
          public Node insertBefore(Node newChild, Node refChild) 
                throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
                  return null;
          }
          public boolean isSupported(String feature, String version)
          {
            return false;
          }
          public void normalize(){}
          public Node removeChild(Node oldChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }         
          public Node replaceChild(Node newChild, Node oldChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          public void setNodeValue(String nodeValue) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
          }
          public void setPrefix(String prefix) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
          }
          public String getName(){
                  return m_attribute.getName();                            
          }
          public String getValue(){
                  return m_attribute.getSimpleString();                            
          }
          public Element getOwnerElement(){
                  return m_owner;
          }
          public boolean getSpecified(){
                  return true;
          }
          public void setValue(String value) throws DOMException
          {
            throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
          }
 	  public TypeInfo getSchemaTypeInfo() { return null; }
  	  public boolean isId( ) { return false; }
  	  public Object setUserData(String key,
                                    Object data,
                                    UserDataHandler handler) {
        	return getOwnerDocument().setUserData( key, data, handler);
  	  }
  	  public Object getUserData(String key) {
        	return getOwnerDocument().getUserData( key);
  	  } 
  	  public Object getFeature(String feature, String version) {
        	return isSupported(feature, version) ? this : null;
   	  }
          public boolean isEqualNode(Node arg) {
          	return arg == this;
          }
          public String lookupNamespaceURI(String specifiedPrefix) {
             	return null;
          }
          public boolean isDefaultNamespace(String namespaceURI) {
            	return false;
          }
	  public String lookupPrefix(String namespaceURI) {
	    	return null;
	  }
  	  public boolean isSameNode(Node other) {
        	return this == other;
  	  }
  	  public void setTextContent(String textContent)
        	throws DOMException {
        	setNodeValue(textContent);
  	  }
  	  public String getTextContent() throws DOMException {
            	return getNodeValue();  
   	  }
    	  public short compareDocumentPosition(Node other) throws DOMException {
            	return 0;
    	  }
          public String getBaseURI() {
            	return null;
    	  }
  }        
  public String getExtensionElementPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_ExtensionElementURIs)
      throw new ArrayIndexOutOfBoundsException();
    return m_ExtensionElementURIs.elementAt(i);
  }
  public int getExtensionElementPrefixCount()
  {
    return (null != m_ExtensionElementURIs)
           ? m_ExtensionElementURIs.size() : 0;
  }
  public boolean containsExtensionElementURI(String uri)
  {
    if (null == m_ExtensionElementURIs)
      return false;
    return m_ExtensionElementURIs.contains(uri);
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_LITERALRESULT;
  }
  public String getNodeName()
  {
    return m_rawName;
  }
  private String m_version;
  public void setVersion(String v)
  {
    m_version = v;
  }
  public String getVersion()
  {
    return m_version;
  }
  private StringVector m_excludeResultPrefixes;
  public void setExcludeResultPrefixes(StringVector v)
  {
    m_excludeResultPrefixes = v;
  }
  private boolean excludeResultNSDecl(String prefix, String uri)
          throws TransformerException
  {
    if (null != m_excludeResultPrefixes)
    {
      return containsExcludeResultPrefix(prefix, uri);
    }
    return false;
  }
    public void execute(TransformerImpl transformer)
        throws TransformerException
    {
        SerializationHandler rhandler = transformer.getSerializationHandler();
        try
        {
            rhandler.startPrefixMapping(getPrefix(), getNamespace());
            executeNSDecls(transformer);
            rhandler.startElement(getNamespace(), getLocalName(), getRawName());
        }
        catch (SAXException se)
        {
            throw new TransformerException(se);
        }
        TransformerException tException = null;
        try
        {
            super.execute(transformer);
            if (null != m_avts)
            {
                int nAttrs = m_avts.size();
                for (int i = (nAttrs - 1); i >= 0; i--)
                {
                    AVT avt = (AVT) m_avts.get(i);
                    XPathContext xctxt = transformer.getXPathContext();
                    int sourceNode = xctxt.getCurrentNode();
                    String stringedValue =
                        avt.evaluate(xctxt, sourceNode, this);
                    if (null != stringedValue)
                    {
                        rhandler.addAttribute(
                            avt.getURI(),
                            avt.getName(),
                            avt.getRawName(),
                            "CDATA",
                            stringedValue, false);
                    }
                } 
            }
            transformer.executeChildTemplates(this, true);
        }
        catch (TransformerException te)
        {
            tException = te;
        }
        catch (SAXException se)
        {
            tException = new TransformerException(se);
        }
        try
        {
            rhandler.endElement(getNamespace(), getLocalName(), getRawName());
        }
        catch (SAXException se)
        {
            if (tException != null)
                throw tException;
            else
                throw new TransformerException(se);
        }
        if (tException != null)
            throw tException; 
        unexecuteNSDecls(transformer);
        try
        {
            rhandler.endPrefixMapping(getPrefix());
        }
        catch (SAXException se)
        {
            throw new TransformerException(se);
        }
    }
  public Iterator enumerateLiteralResultAttributes()
  {
    return (null == m_avts) ? null : m_avts.iterator();
  }
    protected boolean accept(XSLTVisitor visitor)
    {
      return visitor.visitLiteralResultElement(this);
    }
    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
    {
      if (callAttrs && null != m_avts)
      {
        int nAttrs = m_avts.size();
        for (int i = (nAttrs - 1); i >= 0; i--)
        {
          AVT avt = (AVT) m_avts.get(i);
          avt.callVisitors(visitor);
        }
      }
      super.callChildVisitors(visitor, callAttrs);
    }
    public void throwDOMException(short code, String msg)
    {
      String themsg = XSLMessages.createMessage(msg, null);
      throw new DOMException(code, themsg);
    }
}
