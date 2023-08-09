public class DOMBuilder
        implements ContentHandler, LexicalHandler
{
  public Document m_doc;
  protected Node m_currentNode = null;
  protected Node m_root = null;
  protected Node m_nextSibling = null;
  public DocumentFragment m_docFrag = null;
  protected Stack m_elemStack = new Stack();
  protected Vector m_prefixMappings = new Vector();
  public DOMBuilder(Document doc, Node node)
  {
    m_doc = doc;
    m_currentNode = m_root = node;
    if (node instanceof Element)
      m_elemStack.push(node);
  }
  public DOMBuilder(Document doc, DocumentFragment docFrag)
  {
    m_doc = doc;
    m_docFrag = docFrag;
  }
  public DOMBuilder(Document doc)
  {
    m_doc = doc;
  }
  public Node getRootDocument()
  {
    return (null != m_docFrag) ? (Node) m_docFrag : (Node) m_doc;
  }
  public Node getRootNode()
  {
    return m_root;
  }
  public Node getCurrentNode()
  {
    return m_currentNode;
  }
  public void setNextSibling(Node nextSibling)
  {
    m_nextSibling = nextSibling;
  }
  public Node getNextSibling()
  {
    return m_nextSibling;
  }
  public java.io.Writer getWriter()
  {
    return null;
  }
  protected void append(Node newNode) throws org.xml.sax.SAXException
  {
    Node currentNode = m_currentNode;
    if (null != currentNode)
    {
      if (currentNode == m_root && m_nextSibling != null)
        currentNode.insertBefore(newNode, m_nextSibling);
      else
        currentNode.appendChild(newNode);
    }
    else if (null != m_docFrag)
    {
      if (m_nextSibling != null)
        m_docFrag.insertBefore(newNode, m_nextSibling);
      else
        m_docFrag.appendChild(newNode);
    }
    else
    {
      boolean ok = true;
      short type = newNode.getNodeType();
      if (type == Node.TEXT_NODE)
      {
        String data = newNode.getNodeValue();
        if ((null != data) && (data.trim().length() > 0))
        {
          throw new org.xml.sax.SAXException(
            XMLMessages.createXMLMessage(
              XMLErrorResources.ER_CANT_OUTPUT_TEXT_BEFORE_DOC, null));  
        }
        ok = false;
      }
      else if (type == Node.ELEMENT_NODE)
      {
        if (m_doc.getDocumentElement() != null)
        {
          ok = false;
          throw new org.xml.sax.SAXException(
            XMLMessages.createXMLMessage(
              XMLErrorResources.ER_CANT_HAVE_MORE_THAN_ONE_ROOT, null));  
        }
      }
      if (ok)
      {
        if (m_nextSibling != null)
          m_doc.insertBefore(newNode, m_nextSibling);
        else
          m_doc.appendChild(newNode);
      }
    }
  }
  public void setDocumentLocator(Locator locator)
  {
  }
  public void startDocument() throws org.xml.sax.SAXException
  {
  }
  public void endDocument() throws org.xml.sax.SAXException
  {
  }
  public void startElement(
          String ns, String localName, String name, Attributes atts)
            throws org.xml.sax.SAXException
  {
    Element elem;
    if ((null == ns) || (ns.length() == 0))
      elem = m_doc.createElementNS(null,name);
    else
      elem = m_doc.createElementNS(ns, name);
    append(elem);
    try
    {
      int nAtts = atts.getLength();
      if (0 != nAtts)
      {
        for (int i = 0; i < nAtts; i++)
        {
          if (atts.getType(i).equalsIgnoreCase("ID"))
            setIDAttribute(atts.getValue(i), elem);
          String attrNS = atts.getURI(i);
          if("".equals(attrNS))
            attrNS = null; 
          String attrQName = atts.getQName(i);
          if (attrQName.startsWith("xmlns:") || attrQName.equals("xmlns")) {
            attrNS = "http:
          }
          elem.setAttributeNS(attrNS,attrQName, atts.getValue(i));
        }
      }
      int nDecls = m_prefixMappings.size();
      String prefix, declURL;
      for (int i = 0; i < nDecls; i += 2)
      {
        prefix = (String) m_prefixMappings.elementAt(i);
        if (prefix == null)
          continue;
        declURL = (String) m_prefixMappings.elementAt(i + 1);
        elem.setAttributeNS("http:
      }
      m_prefixMappings.clear();
      m_elemStack.push(elem);
      m_currentNode = elem;
    }
    catch(java.lang.Exception de)
    {
      throw new org.xml.sax.SAXException(de);
    }
  }
  public void endElement(String ns, String localName, String name)
          throws org.xml.sax.SAXException
  {
    m_elemStack.pop();
    m_currentNode = m_elemStack.isEmpty() ? null : (Node)m_elemStack.peek();
  }
  public void setIDAttribute(String id, Element elem)
  {
  }
  public void characters(char ch[], int start, int length) throws org.xml.sax.SAXException
  {
    if(isOutsideDocElem()
       && org.apache.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return;  
    if (m_inCData)
    {
      cdata(ch, start, length);
      return;
    }
    String s = new String(ch, start, length);
    Node childNode;
    childNode =  m_currentNode != null ? m_currentNode.getLastChild(): null;
    if( childNode != null && childNode.getNodeType() == Node.TEXT_NODE ){
       ((Text)childNode).appendData(s);
    }
    else{
       Text text = m_doc.createTextNode(s);
       append(text);
    }
  }
  public void charactersRaw(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {
    if(isOutsideDocElem()
       && org.apache.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return;  
    String s = new String(ch, start, length);
    append(m_doc.createProcessingInstruction("xslt-next-is-raw",
                                             "formatter-to-dom"));
    append(m_doc.createTextNode(s));
  }
  public void startEntity(String name) throws org.xml.sax.SAXException
  {
  }
  public void endEntity(String name) throws org.xml.sax.SAXException{}
  public void entityReference(String name) throws org.xml.sax.SAXException
  {
    append(m_doc.createEntityReference(name));
  }
  public void ignorableWhitespace(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {
    if(isOutsideDocElem())
      return;  
    String s = new String(ch, start, length);
    append(m_doc.createTextNode(s));
  }
   private boolean isOutsideDocElem()
   {
      return (null == m_docFrag) && m_elemStack.size() == 0 && (null == m_currentNode || m_currentNode.getNodeType() == Node.DOCUMENT_NODE);
   }
  public void processingInstruction(String target, String data)
          throws org.xml.sax.SAXException
  {
    append(m_doc.createProcessingInstruction(target, data));
  }
  public void comment(char ch[], int start, int length) throws org.xml.sax.SAXException
  {
    append(m_doc.createComment(new String(ch, start, length)));
  }
  protected boolean m_inCData = false;
  public void startCDATA() throws org.xml.sax.SAXException
  {
    m_inCData = true;
    append(m_doc.createCDATASection(""));
  }
  public void endCDATA() throws org.xml.sax.SAXException
  {
    m_inCData = false;
  }
  public void cdata(char ch[], int start, int length) throws org.xml.sax.SAXException
  {
    if(isOutsideDocElem()
       && org.apache.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return;  
    String s = new String(ch, start, length);
    CDATASection section  =(CDATASection) m_currentNode.getLastChild();
    section.appendData(s);
  }
  public void startDTD(String name, String publicId, String systemId)
          throws org.xml.sax.SAXException
  {
  }
  public void endDTD() throws org.xml.sax.SAXException
  {
  }
  public void startPrefixMapping(String prefix, String uri)
          throws org.xml.sax.SAXException
  {
	      if(null == prefix || prefix.equals(""))
	        prefix = "xmlns";
	      else prefix = "xmlns:"+prefix;
	      m_prefixMappings.addElement(prefix);
	      m_prefixMappings.addElement(uri); 
  }
  public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException{}
  public void skippedEntity(String name) throws org.xml.sax.SAXException{}
}
