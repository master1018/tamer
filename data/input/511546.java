public class ToHTMLStream extends ToStream 
{
    protected boolean m_inDTD = false;
    private boolean m_inBlockElem = false;
    private final CharInfo m_htmlcharInfo =
        CharInfo.getCharInfo(CharInfo.HTML_ENTITIES_RESOURCE, Method.HTML);
    static final Trie m_elementFlags = new Trie();
    static {
        initTagReference(m_elementFlags);
    }
    static void initTagReference(Trie m_elementFlags) {
        m_elementFlags.put("BASEFONT", new ElemDesc(0 | ElemDesc.EMPTY));
        m_elementFlags.put(
            "FRAME",
            new ElemDesc(0 | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put("FRAMESET", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("NOFRAMES", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put(
            "ISINDEX",
            new ElemDesc(0 | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put(
            "APPLET",
            new ElemDesc(0 | ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put("CENTER", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("DIR", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("MENU", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("TT", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("I", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("B", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("BIG", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("SMALL", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("EM", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("STRONG", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("DFN", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("CODE", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("SAMP", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("KBD", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("VAR", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("CITE", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("ABBR", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put("ACRONYM", new ElemDesc(0 | ElemDesc.PHRASE));
        m_elementFlags.put(
            "SUP",
            new ElemDesc(0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL));
        m_elementFlags.put(
            "SUB",
            new ElemDesc(0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL));
        m_elementFlags.put(
            "SPAN",
            new ElemDesc(0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL));
        m_elementFlags.put(
            "BDO",
            new ElemDesc(0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL));
        m_elementFlags.put(
            "BR",
            new ElemDesc(
                0
                    | ElemDesc.SPECIAL
                    | ElemDesc.ASPECIAL
                    | ElemDesc.EMPTY
                    | ElemDesc.BLOCK));
        m_elementFlags.put("BODY", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put(
            "ADDRESS",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
            "DIV",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("A", new ElemDesc(0 | ElemDesc.SPECIAL));
        m_elementFlags.put(
            "MAP",
            new ElemDesc(
                0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL | ElemDesc.BLOCK));
        m_elementFlags.put(
            "AREA",
            new ElemDesc(0 | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put(
            "LINK",
            new ElemDesc(
                0 | ElemDesc.HEADMISC | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put(
            "IMG",
            new ElemDesc(
                0
                    | ElemDesc.SPECIAL
                    | ElemDesc.ASPECIAL
                    | ElemDesc.EMPTY
                    | ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put(
            "OBJECT",
            new ElemDesc(
                0
                    | ElemDesc.SPECIAL
                    | ElemDesc.ASPECIAL
                    | ElemDesc.HEADMISC
                    | ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put("PARAM", new ElemDesc(0 | ElemDesc.EMPTY));
        m_elementFlags.put(
            "HR",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET
                    | ElemDesc.EMPTY));
        m_elementFlags.put(
            "P",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
            "H1",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "H2",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "H3",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "H4",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "H5",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "H6",
            new ElemDesc(0 | ElemDesc.HEAD | ElemDesc.BLOCK));
        m_elementFlags.put(
            "PRE",
            new ElemDesc(0 | ElemDesc.PREFORMATTED | ElemDesc.BLOCK));
        m_elementFlags.put(
            "Q",
            new ElemDesc(0 | ElemDesc.SPECIAL | ElemDesc.ASPECIAL));
        m_elementFlags.put(
            "BLOCKQUOTE",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("INS", new ElemDesc(0));
        m_elementFlags.put("DEL", new ElemDesc(0));
        m_elementFlags.put(
            "DL",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("DT", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("DD", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put(
            "OL",
            new ElemDesc(0 | ElemDesc.LIST | ElemDesc.BLOCK));
        m_elementFlags.put(
            "UL",
            new ElemDesc(0 | ElemDesc.LIST | ElemDesc.BLOCK));
        m_elementFlags.put("LI", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("FORM", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("LABEL", new ElemDesc(0 | ElemDesc.FORMCTRL));
        m_elementFlags.put(
            "INPUT",
            new ElemDesc(
                0 | ElemDesc.FORMCTRL | ElemDesc.INLINELABEL | ElemDesc.EMPTY));
        m_elementFlags.put(
            "SELECT",
            new ElemDesc(0 | ElemDesc.FORMCTRL | ElemDesc.INLINELABEL));
        m_elementFlags.put("OPTGROUP", new ElemDesc(0));
        m_elementFlags.put("OPTION", new ElemDesc(0));
        m_elementFlags.put(
            "TEXTAREA",
            new ElemDesc(0 | ElemDesc.FORMCTRL | ElemDesc.INLINELABEL));
        m_elementFlags.put(
            "FIELDSET",
            new ElemDesc(0 | ElemDesc.BLOCK | ElemDesc.BLOCKFORM));
        m_elementFlags.put("LEGEND", new ElemDesc(0));
        m_elementFlags.put(
            "BUTTON",
            new ElemDesc(0 | ElemDesc.FORMCTRL | ElemDesc.INLINELABEL));
        m_elementFlags.put(
            "TABLE",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("CAPTION", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("THEAD", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("TFOOT", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("TBODY", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("COLGROUP", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put(
            "COL",
            new ElemDesc(0 | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put("TR", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put("TH", new ElemDesc(0));
        m_elementFlags.put("TD", new ElemDesc(0));
        m_elementFlags.put(
            "HEAD",
            new ElemDesc(0 | ElemDesc.BLOCK | ElemDesc.HEADELEM));
        m_elementFlags.put("TITLE", new ElemDesc(0 | ElemDesc.BLOCK));
        m_elementFlags.put(
            "BASE",
            new ElemDesc(0 | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put(
            "META",
            new ElemDesc(
                0 | ElemDesc.HEADMISC | ElemDesc.EMPTY | ElemDesc.BLOCK));
        m_elementFlags.put(
            "STYLE",
            new ElemDesc(
                0 | ElemDesc.HEADMISC | ElemDesc.RAW | ElemDesc.BLOCK));
        m_elementFlags.put(
            "SCRIPT",
            new ElemDesc(
                0
                    | ElemDesc.SPECIAL
                    | ElemDesc.ASPECIAL
                    | ElemDesc.HEADMISC
                    | ElemDesc.RAW));
        m_elementFlags.put(
            "NOSCRIPT",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("HTML", new ElemDesc(0 | ElemDesc.BLOCK | ElemDesc.HTMLELEM));
        m_elementFlags.put("FONT", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("S", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("STRIKE", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("U", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put("NOBR", new ElemDesc(0 | ElemDesc.FONTSTYLE));
        m_elementFlags.put(
            "IFRAME",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
            "LAYER",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
            "ILAYER",
            new ElemDesc(
                0
                    | ElemDesc.BLOCK
                    | ElemDesc.BLOCKFORM
                    | ElemDesc.BLOCKFORMFIELDSET));
        ElemDesc elemDesc;
        elemDesc = (ElemDesc) m_elementFlags.get("a");
        elemDesc.setAttr("HREF", ElemDesc.ATTRURL);
        elemDesc.setAttr("NAME", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("area");
        elemDesc.setAttr("HREF", ElemDesc.ATTRURL);
        elemDesc.setAttr("NOHREF", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("base");
        elemDesc.setAttr("HREF", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("button");
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("blockquote");
        elemDesc.setAttr("CITE", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("del");
        elemDesc.setAttr("CITE", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("dir");
        elemDesc.setAttr("COMPACT", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("div");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL); 
        elemDesc.setAttr("NOWRAP", ElemDesc.ATTREMPTY); 
        elemDesc = (ElemDesc) m_elementFlags.get("dl");
        elemDesc.setAttr("COMPACT", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("form");
        elemDesc.setAttr("ACTION", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("frame");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC", ElemDesc.ATTRURL);
        elemDesc.setAttr("NORESIZE",ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("head");
        elemDesc.setAttr("PROFILE", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("hr");
        elemDesc.setAttr("NOSHADE", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("iframe");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("ilayer");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("img");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC", ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP", ElemDesc.ATTRURL);
        elemDesc.setAttr("ISMAP", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("input");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP", ElemDesc.ATTRURL);
        elemDesc.setAttr("CHECKED", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("ISMAP", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("READONLY", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("ins");
        elemDesc.setAttr("CITE", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("layer");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("link");
        elemDesc.setAttr("HREF", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("menu");
        elemDesc.setAttr("COMPACT", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("object");
        elemDesc.setAttr("CLASSID", ElemDesc.ATTRURL);
        elemDesc.setAttr("CODEBASE", ElemDesc.ATTRURL);
        elemDesc.setAttr("DATA", ElemDesc.ATTRURL);
        elemDesc.setAttr("ARCHIVE", ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP", ElemDesc.ATTRURL);
        elemDesc.setAttr("DECLARE", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("ol");
        elemDesc.setAttr("COMPACT", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("optgroup");
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("option");
        elemDesc.setAttr("SELECTED", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("q");
        elemDesc.setAttr("CITE", ElemDesc.ATTRURL);
        elemDesc = (ElemDesc) m_elementFlags.get("script");
        elemDesc.setAttr("SRC", ElemDesc.ATTRURL);
        elemDesc.setAttr("FOR", ElemDesc.ATTRURL);
        elemDesc.setAttr("DEFER", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("select");
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("MULTIPLE", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("table");
        elemDesc.setAttr("NOWRAP", ElemDesc.ATTREMPTY); 
        elemDesc = (ElemDesc) m_elementFlags.get("td");
        elemDesc.setAttr("NOWRAP", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("textarea");
        elemDesc.setAttr("DISABLED", ElemDesc.ATTREMPTY);
        elemDesc.setAttr("READONLY", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("th");
        elemDesc.setAttr("NOWRAP", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("tr");
        elemDesc.setAttr("NOWRAP", ElemDesc.ATTREMPTY);
        elemDesc = (ElemDesc) m_elementFlags.get("ul");
        elemDesc.setAttr("COMPACT", ElemDesc.ATTREMPTY);
    }
    static private final ElemDesc m_dummy = new ElemDesc(0 | ElemDesc.BLOCK);
    private boolean m_specialEscapeURLs = true;
    private boolean m_omitMetaTag = false;
    public void setSpecialEscapeURLs(boolean bool)
    {
        m_specialEscapeURLs = bool;
    }
    public void setOmitMetaTag(boolean bool)
    {
        m_omitMetaTag = bool;
    }
    public void setOutputFormat(Properties format)
    {
        String value; 
        value = format.getProperty(OutputPropertiesFactory.S_USE_URL_ESCAPING);
        if (value != null) {
            m_specialEscapeURLs =
                OutputPropertyUtils.getBooleanProperty(
                    OutputPropertiesFactory.S_USE_URL_ESCAPING,
                    format);
        }
        value = format.getProperty(OutputPropertiesFactory.S_OMIT_META_TAG);
        if (value != null) {
           m_omitMetaTag =
                OutputPropertyUtils.getBooleanProperty(
                    OutputPropertiesFactory.S_OMIT_META_TAG,
                    format);
        }
        super.setOutputFormat(format);
    }
    private final boolean getSpecialEscapeURLs()
    {
        return m_specialEscapeURLs;
    }
    private final boolean getOmitMetaTag()
    {
        return m_omitMetaTag;
    }
    public static final ElemDesc getElemDesc(String name)
    {
        Object obj = m_elementFlags.get(name);
        if (null != obj)
            return (ElemDesc)obj;
        return m_dummy;
    }
    private Trie m_htmlInfo = new Trie(m_elementFlags);
    private ElemDesc getElemDesc2(String name)
    {
        Object obj = m_htmlInfo.get2(name);
        if (null != obj)
            return (ElemDesc)obj;
        return m_dummy;
    }
    public ToHTMLStream()
    {
        super();
        m_doIndent = true; 
        m_charInfo = m_htmlcharInfo;
        m_prefixMap = new NamespaceMappings();
    }
    protected void startDocumentInternal() throws org.xml.sax.SAXException
    {
        super.startDocumentInternal();
        m_needToCallStartDocument = false; 
        m_needToOutputDocTypeDecl = true;
        m_startNewLine = false;
        setOmitXMLDeclaration(true);
    }
    private void outputDocTypeDecl(String name) throws SAXException {
        if (true == m_needToOutputDocTypeDecl)
        {
            String doctypeSystem = getDoctypeSystem();
            String doctypePublic = getDoctypePublic();
            if ((null != doctypeSystem) || (null != doctypePublic))
            {
                final java.io.Writer writer = m_writer;
                try
                {
                writer.write("<!DOCTYPE ");
                writer.write(name);
                if (null != doctypePublic)
                {
                    writer.write(" PUBLIC \"");
                    writer.write(doctypePublic);
                    writer.write('"');
                }
                if (null != doctypeSystem)
                {
                    if (null == doctypePublic)
                        writer.write(" SYSTEM \"");
                    else
                        writer.write(" \"");
                    writer.write(doctypeSystem);
                    writer.write('"');
                }
                writer.write('>');
                outputLineSep();
                }
                catch(IOException e)
                {
                    throw new SAXException(e);
                }
            }
        }
        m_needToOutputDocTypeDecl = false;
    }
    public final void endDocument() throws org.xml.sax.SAXException
    {
        flushPending();
        if (m_doIndent && !m_isprevtext)
        {
            try
            {
            outputLineSep();
            }
            catch(IOException e)
            {
                throw new SAXException(e);
            }
        }
        flushWriter();
        if (m_tracer != null)
            super.fireEndDoc();
    }
    public void startElement(
        String namespaceURI,
        String localName,
        String name,
        Attributes atts)
        throws org.xml.sax.SAXException
    {
        ElemContext elemContext = m_elemContext;
        if (elemContext.m_startTagOpen)
        {
            closeStartTag();
            elemContext.m_startTagOpen = false;
        }
        else if (m_cdataTagOpen)
        {
            closeCDATA();
            m_cdataTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }
        if (m_needToOutputDocTypeDecl) {            
            String n = name;
            if (n == null || n.length() == 0) {
                n = localName;
            }
            outputDocTypeDecl(n);
        }
        if (null != namespaceURI && namespaceURI.length() > 0)
        {
            super.startElement(namespaceURI, localName, name, atts);
            return;
        }
        try
        {
            ElemDesc elemDesc = getElemDesc2(name);
            int elemFlags = elemDesc.getFlags();
            if (m_doIndent)
            {
                boolean isBlockElement = (elemFlags & ElemDesc.BLOCK) != 0;
                if (m_ispreserve)
                    m_ispreserve = false;
                else if (
                    (null != elemContext.m_elementName)
                    && (!m_inBlockElem
                        || isBlockElement) 
                    )
                {
                    m_startNewLine = true;
                    indent();
                }
                m_inBlockElem = !isBlockElement;
            }
            if (atts != null)
                addAttributes(atts);            
            m_isprevtext = false;
            final java.io.Writer writer = m_writer;
            writer.write('<');
            writer.write(name);
            if (m_tracer != null)
                firePseudoAttributes();
            if ((elemFlags & ElemDesc.EMPTY) != 0)  
            {
                m_elemContext = elemContext.push();
                m_elemContext.m_elementName = name;
                m_elemContext.m_elementDesc = elemDesc;
                return;                
            } 
            else
            {
                elemContext = elemContext.push(namespaceURI,localName,name);
                m_elemContext = elemContext;
                elemContext.m_elementDesc = elemDesc;
                elemContext.m_isRaw = (elemFlags & ElemDesc.RAW) != 0;
            }
            if ((elemFlags & ElemDesc.HEADELEM) != 0)
            {
                closeStartTag();
                elemContext.m_startTagOpen = false;
                if (!m_omitMetaTag)
                {
                    if (m_doIndent)
                        indent();
                    writer.write(
                        "<META http-equiv=\"Content-Type\" content=\"text/html; charset=");
                    String encoding = getEncoding();
                    String encode = Encodings.getMimeEncoding(encoding);
                    writer.write(encode);
                    writer.write("\">");
                }
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public final void endElement(
        final String namespaceURI,
        final String localName,
        final String name)
        throws org.xml.sax.SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        if (null != namespaceURI && namespaceURI.length() > 0)
        {
            super.endElement(namespaceURI, localName, name);
            return;
        }
        try
        {
            ElemContext elemContext = m_elemContext;
            final ElemDesc elemDesc = elemContext.m_elementDesc;
            final int elemFlags = elemDesc.getFlags();
            final boolean elemEmpty = (elemFlags & ElemDesc.EMPTY) != 0;
            if (m_doIndent)
            {
                final boolean isBlockElement = (elemFlags&ElemDesc.BLOCK) != 0;
                boolean shouldIndent = false;
                if (m_ispreserve)
                {
                    m_ispreserve = false;
                }
                else if (m_doIndent && (!m_inBlockElem || isBlockElement))
                {
                    m_startNewLine = true;
                    shouldIndent = true;
                }
                if (!elemContext.m_startTagOpen && shouldIndent)
                    indent(elemContext.m_currentElemDepth - 1);
                m_inBlockElem = !isBlockElement;
            }
            final java.io.Writer writer = m_writer;
            if (!elemContext.m_startTagOpen)
            {
                writer.write("</");
                writer.write(name);
                writer.write('>');
            }
            else
            {
                if (m_tracer != null)
                    super.fireStartElem(name);
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    processAttributes(m_writer, nAttrs);
                    m_attributes.clear();
                }
                if (!elemEmpty)
                {
                    writer.write("></");
                    writer.write(name);
                    writer.write('>');
                }
                else
                {
                    writer.write('>');
                }
            }
            if ((elemFlags & ElemDesc.WHITESPACESENSITIVE) != 0)
                m_ispreserve = true;
            m_isprevtext = false;
            if (m_tracer != null)
                super.fireEndElem(name);            
            if (elemEmpty)
            {
                m_elemContext = elemContext.m_prev;
                return;
            }
            if (!elemContext.m_startTagOpen)
            {
                if (m_doIndent && !m_preserves.isEmpty())
                    m_preserves.pop();
            }
            m_elemContext = elemContext.m_prev;
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    protected void processAttribute(
        java.io.Writer writer,
        String name,
        String value,
        ElemDesc elemDesc)
        throws IOException
    {
        writer.write(' ');
        if (   ((value.length() == 0) || value.equalsIgnoreCase(name))
            && elemDesc != null 
            && elemDesc.isAttrFlagSet(name, ElemDesc.ATTREMPTY))
        {
            writer.write(name);
        }
        else
        {
            writer.write(name);
            writer.write("=\"");
            if (   elemDesc != null
                && elemDesc.isAttrFlagSet(name, ElemDesc.ATTRURL))
                writeAttrURI(writer, value, m_specialEscapeURLs);
            else
                writeAttrString(writer, value, this.getEncoding());
            writer.write('"');
        }
    }
    private boolean isASCIIDigit(char c)
    {
        return (c >= '0' && c <= '9');
    }
    private static String makeHHString(int i)
    {
        String s = Integer.toHexString(i).toUpperCase();
        if (s.length() == 1)
        {
            s = "0" + s;
        }
        return s;
    }
    private boolean isHHSign(String str)
    {
        boolean sign = true;
        try
        {
            char r = (char) Integer.parseInt(str, 16);
        }
        catch (NumberFormatException e)
        {
            sign = false;
        }
        return sign;
    }
    public void writeAttrURI(
        final java.io.Writer writer, String string, boolean doURLEscaping)
        throws IOException
    {
        final int end = string.length();
        if (end > m_attrBuff.length)
        {
           m_attrBuff = new char[end*2 + 1];               
        }
        string.getChars(0,end, m_attrBuff, 0); 
        final char[] chars = m_attrBuff;
        int cleanStart = 0;
        int cleanLength = 0;
        char ch = 0;
        for (int i = 0; i < end; i++)
        {
            ch = chars[i];
            if ((ch < 32) || (ch > 126))
            {
                if (cleanLength > 0)
                {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                if (doURLEscaping)
                {
                    if (ch <= 0x7F)
                    {
                        writer.write('%');
                        writer.write(makeHHString(ch));
                    }
                    else if (ch <= 0x7FF)
                    {
                        int high = (ch >> 6) | 0xC0;
                        int low = (ch & 0x3F) | 0x80;
                        writer.write('%');
                        writer.write(makeHHString(high));
                        writer.write('%');
                        writer.write(makeHHString(low));
                    }
                    else if (Encodings.isHighUTF16Surrogate(ch)) 
                    {
                        int highSurrogate = ((int) ch) & 0x03FF;
                        int wwww = ((highSurrogate & 0x03C0) >> 6);
                        int uuuuu = wwww + 1;
                        int zzzz = (highSurrogate & 0x003C) >> 2;
                        int yyyyyy = ((highSurrogate & 0x0003) << 4) & 0x30;
                        ch = chars[++i];
                        int lowSurrogate = ((int) ch) & 0x03FF;
                        yyyyyy = yyyyyy | ((lowSurrogate & 0x03C0) >> 6);
                        int xxxxxx = (lowSurrogate & 0x003F);
                        int byte1 = 0xF0 | (uuuuu >> 2); 
                        int byte2 =
                            0x80 | (((uuuuu & 0x03) << 4) & 0x30) | zzzz;
                        int byte3 = 0x80 | yyyyyy;
                        int byte4 = 0x80 | xxxxxx;
                        writer.write('%');
                        writer.write(makeHHString(byte1));
                        writer.write('%');
                        writer.write(makeHHString(byte2));
                        writer.write('%');
                        writer.write(makeHHString(byte3));
                        writer.write('%');
                        writer.write(makeHHString(byte4));
                    }
                    else
                    {
                        int high = (ch >> 12) | 0xE0; 
                        int middle = ((ch & 0x0FC0) >> 6) | 0x80;
                        int low = (ch & 0x3F) | 0x80;
                        writer.write('%');
                        writer.write(makeHHString(high));
                        writer.write('%');
                        writer.write(makeHHString(middle));
                        writer.write('%');
                        writer.write(makeHHString(low));
                    }
                }
                else if (escapingNotNeeded(ch))
                {
                    writer.write(ch);
                }
                else
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                cleanStart = i + 1;
            }
            else if (ch == '"')
            {
                if (cleanLength > 0)
                {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }   
                if (doURLEscaping)
                    writer.write("%22");
                else
                    writer.write("&quot;"); 
                cleanStart = i + 1;    
            }
            else if (ch == '&')
            {
                if (cleanLength > 0)
                {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                } 
                writer.write("&amp;");
                cleanStart = i + 1; 
            }
            else
            {
                cleanLength++;
            }
        }
        if (cleanLength > 1)
        {
            if (cleanStart == 0)
                writer.write(string);
            else
                writer.write(chars, cleanStart, cleanLength);
        }
        else if (cleanLength == 1)
        {
            writer.write(ch);
        }
    }
    public void writeAttrString(
        final java.io.Writer writer, String string, String encoding)
        throws IOException
    {
        final int end = string.length();
        if (end > m_attrBuff.length)
        {
            m_attrBuff = new char[end * 2 + 1];
        }
        string.getChars(0, end, m_attrBuff, 0);
        final char[] chars = m_attrBuff;
        int cleanStart = 0;
        int cleanLength = 0;
        char ch = 0;
        for (int i = 0; i < end; i++)
        {
            ch = chars[i];
            if (escapingNotNeeded(ch) && (!m_charInfo.shouldMapAttrChar(ch)))
            {
                cleanLength++;
            }
            else if ('<' == ch || '>' == ch)
            {
                cleanLength++; 
            }
            else if (
                ('&' == ch) && ((i + 1) < end) && ('{' == chars[i + 1]))
            {
                cleanLength++; 
            }
            else
            {
                if (cleanLength > 0)
                {
                    writer.write(chars,cleanStart,cleanLength);
                    cleanLength = 0;
                }
                int pos = accumDefaultEntity(writer, ch, i, chars, end, false, true);
                if (i != pos)
                {
                    i = pos - 1;
                }
                else
                {
                    if (Encodings.isHighUTF16Surrogate(ch))
                    {
                            writeUTF16Surrogate(ch, chars, i, end);
                            i++; 
                    }
                    String outputStringForChar = m_charInfo.getOutputStringForChar(ch);
                    if (null != outputStringForChar)
                    {
                        writer.write(outputStringForChar);
                    }
                    else if (escapingNotNeeded(ch))
                    {
                        writer.write(ch); 
                    }
                    else
                    {
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                    }
                }
                cleanStart = i + 1;
            }
        } 
        if (cleanLength > 1)
        {
            if (cleanStart == 0)
                writer.write(string);
            else
                writer.write(chars, cleanStart, cleanLength);
        }
        else if (cleanLength == 1)
        {
            writer.write(ch);
        }
    }
    public final void characters(char chars[], int start, int length)
        throws org.xml.sax.SAXException
    {
        if (m_elemContext.m_isRaw)
        { 
            try
            {
                if (m_elemContext.m_startTagOpen)
                {
                    closeStartTag();
                    m_elemContext.m_startTagOpen = false;
                }
                m_ispreserve = true;
                writeNormalizedChars(chars, start, length, false, m_lineSepUse);
                if (m_tracer != null)
                    super.fireCharEvent(chars, start, length);
                return;
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(
                    Utils.messages.createMessage(MsgKey.ER_OIERROR,null),ioe);
            }
        }
        else
        {
            super.characters(chars, start, length);
        }
    }
    public final void cdata(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {
        if ((null != m_elemContext.m_elementName)
            && (m_elemContext.m_elementName.equalsIgnoreCase("SCRIPT")
                || m_elemContext.m_elementName.equalsIgnoreCase("STYLE")))
        {
            try
            {
                if (m_elemContext.m_startTagOpen)
                {
                    closeStartTag();
                    m_elemContext.m_startTagOpen = false;
                }
                m_ispreserve = true;
                if (shouldIndent())
                    indent();
                writeNormalizedChars(ch, start, length, true, m_lineSepUse);
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(
                    Utils.messages.createMessage(
                        MsgKey.ER_OIERROR,
                        null),
                    ioe);
            }
        }
        else
        {
            super.cdata(ch, start, length);
        }
    }
    public void processingInstruction(String target, String data)
        throws org.xml.sax.SAXException
    {
        flushPending(); 
        if (target.equals(Result.PI_DISABLE_OUTPUT_ESCAPING))
        {
            startNonEscaping();
        }
        else if (target.equals(Result.PI_ENABLE_OUTPUT_ESCAPING))
        {
            endNonEscaping();
        }
        else
        {
            try
            {
                if (m_elemContext.m_startTagOpen)
                {
                    closeStartTag();
                    m_elemContext.m_startTagOpen = false;
                }
                else if (m_cdataTagOpen)
                {
                    closeCDATA();
                }
                else if (m_needToCallStartDocument)
                {
                    startDocumentInternal();
                }
            if (true == m_needToOutputDocTypeDecl)
                outputDocTypeDecl("html"); 
            if (shouldIndent())
                indent();
            final java.io.Writer writer = m_writer;
            writer.write("<?");
            writer.write(target);
            if (data.length() > 0 && !Character.isSpaceChar(data.charAt(0)))
                writer.write(' '); 
            writer.write(data); 
            writer.write('>'); 
            if (m_elemContext.m_currentElemDepth <= 0)
                outputLineSep();
            m_startNewLine = true;
            }
            catch(IOException e)
            {
                throw new SAXException(e);
            }
        }
        if (m_tracer != null)
            super.fireEscapingEvent(target, data);
     }
    public final void entityReference(String name)
        throws org.xml.sax.SAXException
    {
        try
        {
        final java.io.Writer writer = m_writer;
        writer.write('&');
        writer.write(name);
        writer.write(';');
        } catch(IOException e)
        {
            throw new SAXException(e);
        }
    }
    public final void endElement(String elemName) throws SAXException
    {
        endElement(null, null, elemName);
    }
    public void processAttributes(java.io.Writer writer, int nAttrs)
        throws IOException,SAXException
    {
            for (int i = 0; i < nAttrs; i++)
            {
                processAttribute(
                    writer,
                    m_attributes.getQName(i),
                    m_attributes.getValue(i),
                    m_elemContext.m_elementDesc);
            }
    }
    protected void closeStartTag() throws SAXException
    {
            try
            {
            if (m_tracer != null)
                super.fireStartElem(m_elemContext.m_elementName);  
            int nAttrs = m_attributes.getLength();   
            if (nAttrs>0)
            {
                processAttributes(m_writer, nAttrs);
                m_attributes.clear();
            }
            m_writer.write('>');
            if (m_CdataElems != null) 
                m_elemContext.m_isCdataSection = isCdataSection();
            if (m_doIndent)
            {
                m_isprevtext = false;
                m_preserves.push(m_ispreserve);
            }
            }
            catch(IOException e)
            {
                throw new SAXException(e);
            }
    }
        public void namespaceAfterStartElement(String prefix, String uri)
            throws SAXException
        {
            if (m_elemContext.m_elementURI == null)
            {
                String prefix1 = getPrefixPart(m_elemContext.m_elementName);
                if (prefix1 == null && EMPTYSTRING.equals(prefix))
                {
                    m_elemContext.m_elementURI = uri;
                }
            }            
            startPrefixMapping(prefix,uri,false);
        }
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException
    {
        m_inDTD = true;
        super.startDTD(name, publicId, systemId);
    }
    public void endDTD() throws org.xml.sax.SAXException
    {
        m_inDTD = false;
    }
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
    }
    public void elementDecl(String name, String model) throws SAXException
    {
    }
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
    }
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
    }
    public void addUniqueAttribute(String name, String value, int flags)
        throws SAXException
    {
        try
        {
            final java.io.Writer writer = m_writer;
            if ((flags & NO_BAD_CHARS) > 0 && m_htmlcharInfo.onlyQuotAmpLtGt)
            {
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                writer.write(value);
                writer.write('"');
            }
            else if (
                (flags & HTML_ATTREMPTY) > 0
                    && (value.length() == 0 || value.equalsIgnoreCase(name)))
            {
                writer.write(' ');
                writer.write(name);
            }
            else
            {
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                if ((flags & HTML_ATTRURL) > 0)
                {
                    writeAttrURI(writer, value, m_specialEscapeURLs);
                }
                else
                {
                    writeAttrString(writer, value, this.getEncoding());
                }
                writer.write('"');
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    public void comment(char ch[], int start, int length)
            throws SAXException
    {
        if (m_inDTD)
            return;
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_cdataTagOpen)
        {
            closeCDATA();
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }
        if (m_needToOutputDocTypeDecl)
            outputDocTypeDecl("html"); 
        super.comment(ch, start, length);
    }
    public boolean reset()
    {
        boolean ret = super.reset();
        if (!ret)
            return false;
        resetToHTMLStream();
        return true;        
    }
    private void resetToHTMLStream()
    {
        m_inBlockElem = false;
        m_inDTD = false;
        m_omitMetaTag = false;
        m_specialEscapeURLs = true;     
    }
    static class Trie
    {
        public static final int ALPHA_SIZE = 128;
        final Node m_Root;
        private char[] m_charBuffer = new char[0];
        private final boolean m_lowerCaseOnly;
        public Trie()
        {
            m_Root = new Node();
            m_lowerCaseOnly = false;
        }
        public Trie(boolean lowerCaseOnly)
        {
            m_Root = new Node();
            m_lowerCaseOnly = lowerCaseOnly;
        }
        public Object put(String key, Object value)
        {
            final int len = key.length();
            if (len > m_charBuffer.length)
            {
                m_charBuffer = new char[len];
            }
            Node node = m_Root;
            for (int i = 0; i < len; i++)
            {
                Node nextNode =
                    node.m_nextChar[Character.toLowerCase(key.charAt(i))];
                if (nextNode != null)
                {
                    node = nextNode;
                }
                else
                {
                    for (; i < len; i++)
                    {
                        Node newNode = new Node();
                        if (m_lowerCaseOnly)
                        {
                            node.m_nextChar[Character.toLowerCase(
                                key.charAt(i))] =
                                newNode;
                        }
                        else
                        {
                            node.m_nextChar[Character.toUpperCase(
                                key.charAt(i))] =
                                newNode;
                            node.m_nextChar[Character.toLowerCase(
                                key.charAt(i))] =
                                newNode;
                        }
                        node = newNode;
                    }
                    break;
                }
            }
            Object ret = node.m_Value;
            node.m_Value = value;
            return ret;
        }
        public Object get(final String key)
        {
            final int len = key.length();
            if (m_charBuffer.length < len)
                return null;
            Node node = m_Root;
            switch (len) 
            {
                case 0 :
                    {
                        return null;
                    }
                case 1 :
                    {
                        final char ch = key.charAt(0);
                        if (ch < ALPHA_SIZE)
                        {
                            node = node.m_nextChar[ch];
                            if (node != null)
                                return node.m_Value;
                        }
                        return null;
                    }
                default :
                    {
                        for (int i = 0; i < len; i++)
                        {
                            final char ch = key.charAt(i);
                            if (ALPHA_SIZE <= ch)
                            {
                                return null;
                            }
                            node = node.m_nextChar[ch];
                            if (node == null)
                                return null;
                        }
                        return node.m_Value;
                    }
            }
        }
        private class Node
        {
            Node()
            {
                m_nextChar = new Node[ALPHA_SIZE];
                m_Value = null;
            }
            final Node m_nextChar[];
            Object m_Value;
        }
        public Trie(Trie existingTrie)
        {
            m_Root = existingTrie.m_Root;
            m_lowerCaseOnly = existingTrie.m_lowerCaseOnly;
            int max = existingTrie.getLongestKeyLength();
            m_charBuffer = new char[max];
        }
        public Object get2(final String key)
        {
            final int len = key.length();
            if (m_charBuffer.length < len)
                return null;
            Node node = m_Root;
            switch (len) 
            {
                case 0 :
                    {
                        return null;
                    }
                case 1 :
                    {
                        final char ch = key.charAt(0);
                        if (ch < ALPHA_SIZE)
                        {
                            node = node.m_nextChar[ch];
                            if (node != null)
                                return node.m_Value;
                        }
                        return null;
                    }
                default :
                    {
                        key.getChars(0, len, m_charBuffer, 0);
                        for (int i = 0; i < len; i++)
                        {
                            final char ch = m_charBuffer[i];
                            if (ALPHA_SIZE <= ch)
                            {
                                return null;
                            }
                            node = node.m_nextChar[ch];
                            if (node == null)
                                return null;
                        }
                        return node.m_Value;
                    }
            }
        }
        public int getLongestKeyLength()
        {
            return m_charBuffer.length;
        }
    }
}
