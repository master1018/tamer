public final class ToUnknownStream extends SerializerBase
{
    private SerializationHandler m_handler;
    private static final String EMPTYSTRING = "";
    private boolean m_wrapped_handler_not_initialized = false;
    private String m_firstElementPrefix;
    private String m_firstElementName;
    private String m_firstElementURI;
    private String m_firstElementLocalName = null;
    private boolean m_firstTagNotEmitted = true;
    private Vector m_namespaceURI = null;
    private Vector m_namespacePrefix = null;
    private boolean m_needToCallStartDocument = false;
    private boolean m_setVersion_called = false;
    private boolean m_setDoctypeSystem_called = false;
    private boolean m_setDoctypePublic_called = false;
    private boolean m_setMediaType_called = false;
    public ToUnknownStream()
    {
        m_handler = new ToXMLStream();
    }
    public ContentHandler asContentHandler() throws IOException
    {
        return this;
    }
    public void close()
    {
        m_handler.close();
    }
    public Properties getOutputFormat()
    {
        return m_handler.getOutputFormat();
    }
    public OutputStream getOutputStream()
    {
        return m_handler.getOutputStream();
    }
    public Writer getWriter()
    {
        return m_handler.getWriter();
    }
    public boolean reset()
    {
        return m_handler.reset();
    }
    public void serialize(Node node) throws IOException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.serialize(node);
    }
    public boolean setEscaping(boolean escape) throws SAXException
    {
        return m_handler.setEscaping(escape);
    }
    public void setOutputFormat(Properties format)
    {
        m_handler.setOutputFormat(format);
    }
    public void setOutputStream(OutputStream output)
    {
        m_handler.setOutputStream(output);
    }
    public void setWriter(Writer writer)
    {
        m_handler.setWriter(writer);
    }
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean XSLAttribute)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.addAttribute(uri, localName, rawName, type, value, XSLAttribute);
    }
    public void addAttribute(String rawName, String value)
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.addAttribute(rawName, value);
    }
    public void addUniqueAttribute(String rawName, String value, int flags)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.addUniqueAttribute(rawName, value, flags);
    }
    public void characters(String chars) throws SAXException
    {
        final int length = chars.length();
        if (length > m_charsBuff.length)
        {
            m_charsBuff = new char[length*2 + 1];
        }
        chars.getChars(0, length, m_charsBuff, 0);
        this.characters(m_charsBuff, 0, length);  
    }    
    public void endElement(String elementName) throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.endElement(elementName);
    }
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        this.startPrefixMapping(prefix,uri, true);
    }
    public void namespaceAfterStartElement(String prefix, String uri)
        throws SAXException 
    {  
        if (m_firstTagNotEmitted && m_firstElementURI == null && m_firstElementName != null)
        {
            String prefix1 = getPrefixPart(m_firstElementName);
            if (prefix1 == null && EMPTYSTRING.equals(prefix))
            {
                m_firstElementURI = uri;
            }
        }         
        startPrefixMapping(prefix,uri, false);          
    }
    public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush)
        throws SAXException
    {
        boolean pushed = false;
        if (m_firstTagNotEmitted)
        {
            if (m_firstElementName != null && shouldFlush)
            {
                flush();
                pushed = m_handler.startPrefixMapping(prefix, uri, shouldFlush);
            } 
            else 
            {           
                if (m_namespacePrefix == null)
                {
                    m_namespacePrefix = new Vector();
                    m_namespaceURI = new Vector();
                }
                m_namespacePrefix.addElement(prefix);
                m_namespaceURI.addElement(uri);
                if (m_firstElementURI == null)
                {
                    if (prefix.equals(m_firstElementPrefix))
                        m_firstElementURI = uri;
                }
            }
        }
        else
        {
           pushed = m_handler.startPrefixMapping(prefix, uri, shouldFlush);
        }
        return pushed;
    }
    public void setVersion(String version)
    {
        m_handler.setVersion(version);
        m_setVersion_called = true;
    }
    public void startDocument() throws SAXException
    {
        m_needToCallStartDocument = true;
    }
    public void startElement(String qName) throws SAXException
    {
        this.startElement(null, null, qName, null);
    }
    public void startElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        this.startElement(namespaceURI, localName, qName, null);
    }
    public void startElement(
        String namespaceURI,
        String localName,
        String elementName,
        Attributes atts) throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            if (m_firstElementName != null) 
            {
                flush();
                m_handler.startElement(namespaceURI, localName, elementName,  atts);                
            }
            else
            {
                m_wrapped_handler_not_initialized = true;
                m_firstElementName = elementName;
                m_firstElementPrefix = getPrefixPartUnknown(elementName);
                m_firstElementURI = namespaceURI;
                m_firstElementLocalName = localName;
                if (m_tracer != null)
                    firePseudoElement(elementName);
                if (atts != null)   
                    super.addAttributes(atts);
                if (atts != null)   
                    flush();
            }
        }
        else
        {
            m_handler.startElement(namespaceURI, localName, elementName,  atts);
        }
    }
    public void comment(String comment) throws SAXException
    {
        if (m_firstTagNotEmitted && m_firstElementName != null)
        {
            emitFirstTag();
        }
        else if (m_needToCallStartDocument)
        {
            m_handler.startDocument();
            m_needToCallStartDocument = false;
        }
        m_handler.comment(comment);
    }
    public String getDoctypePublic()
    {
        return m_handler.getDoctypePublic();
    }
    public String getDoctypeSystem()
    {
        return m_handler.getDoctypeSystem();
    }
    public String getEncoding()
    {
        return m_handler.getEncoding();
    }
    public boolean getIndent()
    {
        return m_handler.getIndent();
    }
    public int getIndentAmount()
    {
        return m_handler.getIndentAmount();
    }
    public String getMediaType()
    {
        return m_handler.getMediaType();
    }
    public boolean getOmitXMLDeclaration()
    {
        return m_handler.getOmitXMLDeclaration();
    }
    public String getStandalone()
    {
        return m_handler.getStandalone();
    }
    public String getVersion()
    {
        return m_handler.getVersion();
    }
    public void setDoctype(String system, String pub)
    {
        m_handler.setDoctypePublic(pub);
        m_handler.setDoctypeSystem(system);
    }
    public void setDoctypePublic(String doctype)
    {
        m_handler.setDoctypePublic(doctype);
        m_setDoctypePublic_called = true;
    }
    public void setDoctypeSystem(String doctype)
    {
        m_handler.setDoctypeSystem(doctype);
        m_setDoctypeSystem_called = true;
    }
    public void setEncoding(String encoding)
    {
        m_handler.setEncoding(encoding);
    }
    public void setIndent(boolean indent)
    {
        m_handler.setIndent(indent);
    }
    public void setIndentAmount(int value)
    {
        m_handler.setIndentAmount(value);
    }
    public void setMediaType(String mediaType)
    {
        m_handler.setMediaType(mediaType);
        m_setMediaType_called = true;
    }
    public void setOmitXMLDeclaration(boolean b)
    {
        m_handler.setOmitXMLDeclaration(b);
    }
    public void setStandalone(String standalone)
    {
        m_handler.setStandalone(standalone);
    }
    public void attributeDecl(
        String arg0,
        String arg1,
        String arg2,
        String arg3,
        String arg4)
        throws SAXException
    {
        m_handler.attributeDecl(arg0, arg1, arg2, arg3, arg4);
    }
    public void elementDecl(String arg0, String arg1) throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            emitFirstTag();
        }
        m_handler.elementDecl(arg0, arg1);
    }
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.externalEntityDecl(name, publicId, systemId);
    }
    public void internalEntityDecl(String arg0, String arg1)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.internalEntityDecl(arg0, arg1);
    }
    public void characters(char[] characters, int offset, int length)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.characters(characters, offset, length);
    }
    public void endDocument() throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.endDocument();
    }
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
            if (namespaceURI == null && m_firstElementURI != null)
                namespaceURI = m_firstElementURI;
            if (localName == null && m_firstElementLocalName != null)
                localName = m_firstElementLocalName;
        }
        m_handler.endElement(namespaceURI, localName, qName);
    }
    public void endPrefixMapping(String prefix) throws SAXException
    {
        m_handler.endPrefixMapping(prefix);
    }
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.ignorableWhitespace(ch, start, length);
    }
    public void processingInstruction(String target, String data)
        throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.processingInstruction(target, data);
    }
    public void setDocumentLocator(Locator locator)
    {
        m_handler.setDocumentLocator(locator);
    }
    public void skippedEntity(String name) throws SAXException
    {
        m_handler.skippedEntity(name);
    }
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            flush();
        }
        m_handler.comment(ch, start, length);
    }
    public void endCDATA() throws SAXException
    {
        m_handler.endCDATA();
    }
    public void endDTD() throws SAXException
    {
        m_handler.endDTD();
    }
    public void endEntity(String name) throws SAXException
    {
        if (m_firstTagNotEmitted)
        {
            emitFirstTag();
        }
        m_handler.endEntity(name);
    }
    public void startCDATA() throws SAXException
    {
        m_handler.startCDATA();
    }
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException
    {
        m_handler.startDTD(name, publicId, systemId);
    }
    public void startEntity(String name) throws SAXException
    {
        m_handler.startEntity(name);
    }
    private void initStreamOutput() throws SAXException
    {
        boolean firstElementIsHTML = isFirstElemHTML();
        if (firstElementIsHTML)
        {
            SerializationHandler oldHandler = m_handler;
            Properties htmlProperties =
                OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
            Serializer serializer =
                SerializerFactory.getSerializer(htmlProperties);
            m_handler = (SerializationHandler) serializer;
            Writer writer = oldHandler.getWriter();
            if (null != writer)
                m_handler.setWriter(writer);
            else
            {
                OutputStream os = oldHandler.getOutputStream();
                if (null != os)
                    m_handler.setOutputStream(os);
            }
            m_handler.setVersion(oldHandler.getVersion());
            m_handler.setDoctypeSystem(oldHandler.getDoctypeSystem());
            m_handler.setDoctypePublic(oldHandler.getDoctypePublic());
            m_handler.setMediaType(oldHandler.getMediaType());
            m_handler.setTransformer(oldHandler.getTransformer());
        }
        if (m_needToCallStartDocument)
        {
            m_handler.startDocument();
            m_needToCallStartDocument = false;
        }
        m_wrapped_handler_not_initialized = false;
    }
    private void emitFirstTag() throws SAXException
    {   
        if (m_firstElementName != null)
        {
            if (m_wrapped_handler_not_initialized)
            {
                initStreamOutput();
                m_wrapped_handler_not_initialized = false;
            }
            m_handler.startElement(m_firstElementURI, null, m_firstElementName, m_attributes);
            m_attributes = null;
            if (m_namespacePrefix != null)
            {
                final int n = m_namespacePrefix.size();
                for (int i = 0; i < n; i++)
                {
                    final String prefix =
                        (String) m_namespacePrefix.elementAt(i);
                    final String uri = (String) m_namespaceURI.elementAt(i);
                    m_handler.startPrefixMapping(prefix, uri, false);
                }
                m_namespacePrefix = null;
                m_namespaceURI = null;
            }
            m_firstTagNotEmitted = false;
        }
    }
    private String getLocalNameUnknown(String value)
    {
        int idx = value.lastIndexOf(':');
        if (idx >= 0)
            value = value.substring(idx + 1);
        idx = value.lastIndexOf('@');
        if (idx >= 0)
            value = value.substring(idx + 1);
        return (value);
    }
    private String getPrefixPartUnknown(String qname)
    {
        final int index = qname.indexOf(':');
        return (index > 0) ? qname.substring(0, index) : EMPTYSTRING;
    }
    private boolean isFirstElemHTML()
    {
        boolean isHTML;
        isHTML =
            getLocalNameUnknown(m_firstElementName).equalsIgnoreCase("html");
        if (isHTML
            && m_firstElementURI != null
            && !EMPTYSTRING.equals(m_firstElementURI))
        {
            isHTML = false;
        }
        if (isHTML && m_namespacePrefix != null)
        {
            final int max = m_namespacePrefix.size();
            for (int i = 0; i < max; i++)
            {
                final String prefix = (String) m_namespacePrefix.elementAt(i);
                final String uri = (String) m_namespaceURI.elementAt(i);
                if (m_firstElementPrefix != null
                    && m_firstElementPrefix.equals(prefix)
                    && !EMPTYSTRING.equals(uri))
                {
                    isHTML = false;
                    break;
                }
            }
        }
        return isHTML;
    }
    public DOMSerializer asDOMSerializer() throws IOException
    {
        return m_handler.asDOMSerializer();
    }
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
        m_handler.setCdataSectionElements(URI_and_localNames);
    }
    public void addAttributes(Attributes atts) throws SAXException
    {
        m_handler.addAttributes(atts);
    }
    public NamespaceMappings getNamespaceMappings()
    {
        NamespaceMappings mappings = null;
        if (m_handler != null)
        {
            mappings = m_handler.getNamespaceMappings();
        }
        return mappings;
    }
    public void flushPending() throws SAXException
    {
        flush();
        m_handler.flushPending();
    }
    private void flush()
    {
        try
        {
        if (m_firstTagNotEmitted)
        {
            emitFirstTag();
        }
        if (m_needToCallStartDocument)
        {
            m_handler.startDocument();
            m_needToCallStartDocument = false;
        }
        }
        catch(SAXException e)
        {
            throw new RuntimeException(e.toString());
        }
    }
    public String getPrefix(String namespaceURI)
    {
        return m_handler.getPrefix(namespaceURI);
    }
    public void entityReference(String entityName) throws SAXException
    {
        m_handler.entityReference(entityName);
    }
    public String getNamespaceURI(String qname, boolean isElement)
    {
        return m_handler.getNamespaceURI(qname, isElement);
    }
    public String getNamespaceURIFromPrefix(String prefix)
    {
        return m_handler.getNamespaceURIFromPrefix(prefix);
    }
    public void setTransformer(Transformer t)
    {       
        m_handler.setTransformer(t);
        if ((t instanceof SerializerTrace) &&
            (((SerializerTrace) t).hasTraceListeners())) {
           m_tracer = (SerializerTrace) t;
        } else {
           m_tracer = null;
        }        
    }
    public Transformer getTransformer()
    {
        return m_handler.getTransformer();
    }
    public void setContentHandler(ContentHandler ch)
    {
        m_handler.setContentHandler(ch);
    }
    public void setSourceLocator(SourceLocator locator)
    {
        m_handler.setSourceLocator(locator);
    }
    protected void firePseudoElement(String elementName)
    {
        if (m_tracer != null) {
            StringBuffer sb = new StringBuffer();
            sb.append('<');
            sb.append(elementName);
            char ch[] = sb.toString().toCharArray();
            m_tracer.fireGenerateEvent(
                SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                ch,
                0,
                ch.length);
        }
    }
    public Object asDOM3Serializer() throws IOException
    {
        return m_handler.asDOM3Serializer();
    }
}
