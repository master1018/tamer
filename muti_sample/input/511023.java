final class DOM3TreeWalker {
    private SerializationHandler fSerializer = null;
    private LocatorImpl fLocator = new LocatorImpl();
    private DOMErrorHandler fErrorHandler = null;
    private LSSerializerFilter fFilter = null;
    private LexicalHandler fLexicalHandler = null;
    private int fWhatToShowFilter;
    private String fNewLine = null;
    private Properties fDOMConfigProperties = null;
    private boolean fInEntityRef = false;
    private String fXMLVersion = null;
    private boolean fIsXMLVersion11 = false;
    private boolean fIsLevel3DOM = false;
    private int fFeatures = 0;
    boolean fNextIsRaw = false;
    private static final String XMLNS_URI = "http:
    private static final String XMLNS_PREFIX = "xmlns";
    private static final String XML_URI = "http:
    private static final String XML_PREFIX = "xml";
    protected NamespaceSupport fNSBinder;
    protected NamespaceSupport fLocalNSBinder;
    private int fElementDepth = 0;
    private final static int CANONICAL = 0x1 << 0;
    private final static int CDATA = 0x1 << 1;
    private final static int CHARNORMALIZE = 0x1 << 2;
    private final static int COMMENTS = 0x1 << 3;
    private final static int DTNORMALIZE = 0x1 << 4;
    private final static int ELEM_CONTENT_WHITESPACE = 0x1 << 5;
    private final static int ENTITIES = 0x1 << 6;
    private final static int INFOSET = 0x1 << 7;
    private final static int NAMESPACES = 0x1 << 8;
    private final static int NAMESPACEDECLS = 0x1 << 9;
    private final static int NORMALIZECHARS = 0x1 << 10;
    private final static int SPLITCDATA = 0x1 << 11;
    private final static int VALIDATE = 0x1 << 12;
    private final static int SCHEMAVALIDATE = 0x1 << 13;
    private final static int WELLFORMED = 0x1 << 14;
    private final static int DISCARDDEFAULT = 0x1 << 15;
    private final static int PRETTY_PRINT = 0x1 << 16;
    private final static int IGNORE_CHAR_DENORMALIZE = 0x1 << 17;
    private final static int XMLDECL = 0x1 << 18;
    DOM3TreeWalker(
        SerializationHandler serialHandler,
        DOMErrorHandler errHandler,
        LSSerializerFilter filter,
        String newLine) {
        fSerializer = serialHandler;
        fErrorHandler = errHandler;
        fFilter = filter;
        fLexicalHandler = null;
        fNewLine = newLine;
        fNSBinder = new NamespaceSupport();
        fLocalNSBinder = new NamespaceSupport();
        fDOMConfigProperties = fSerializer.getOutputFormat();
        fSerializer.setDocumentLocator(fLocator);
        initProperties(fDOMConfigProperties);
        try {
            fLocator.setSystemId(
                System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException se) { 
        }
    }
    public void traverse(Node pos) throws org.xml.sax.SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != Node.DOCUMENT_NODE) {
            Document ownerDoc = pos.getOwnerDocument();
            if (ownerDoc != null
                && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                fIsLevel3DOM = true;
            }
        } else {
            if (((Document) pos)
                .getImplementation()
                .hasFeature("Core", "3.0")) {
                fIsLevel3DOM = true;
            }
        }
        if (fSerializer instanceof LexicalHandler) {
            fLexicalHandler = ((LexicalHandler) this.fSerializer);
        }
        if (fFilter != null)
            fWhatToShowFilter = fFilter.getWhatToShow();
        Node top = pos;
        while (null != pos) {
            startNode(pos);
            Node nextNode = null;
            nextNode = pos.getFirstChild();
            while (null == nextNode) {
                endNode(pos);
                if (top.equals(pos))
                    break;
                nextNode = pos.getNextSibling();
                if (null == nextNode) {
                    pos = pos.getParentNode();
                    if ((null == pos) || (top.equals(pos))) {
                        if (null != pos)
                            endNode(pos);
                        nextNode = null;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }
    public void traverse(Node pos, Node top) throws org.xml.sax.SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != Node.DOCUMENT_NODE) {
            Document ownerDoc = pos.getOwnerDocument();
            if (ownerDoc != null
                && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                fIsLevel3DOM = true;
            }
        } else {
            if (((Document) pos)
                .getImplementation()
                .hasFeature("Core", "3.0")) {
                fIsLevel3DOM = true;
            }
        }
        if (fSerializer instanceof LexicalHandler) {
            fLexicalHandler = ((LexicalHandler) this.fSerializer);
        }
        if (fFilter != null)
            fWhatToShowFilter = fFilter.getWhatToShow();
        while (null != pos) {
            startNode(pos);
            Node nextNode = null;
            nextNode = pos.getFirstChild();
            while (null == nextNode) {
                endNode(pos);
                if ((null != top) && top.equals(pos))
                    break;
                nextNode = pos.getNextSibling();
                if (null == nextNode) {
                    pos = pos.getParentNode();
                    if ((null == pos) || ((null != top) && top.equals(pos))) {
                        nextNode = null;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }
    private final void dispatachChars(Node node)
        throws org.xml.sax.SAXException {
        if (fSerializer != null) {
            this.fSerializer.characters(node);
        } else {
            String data = ((Text) node).getData();
            this.fSerializer.characters(data.toCharArray(), 0, data.length());
        }
    }
    protected void startNode(Node node) throws org.xml.sax.SAXException {
        if (node instanceof Locator) {
            Locator loc = (Locator) node;
            fLocator.setColumnNumber(loc.getColumnNumber());
            fLocator.setLineNumber(loc.getLineNumber());
            fLocator.setPublicId(loc.getPublicId());
            fLocator.setSystemId(loc.getSystemId());
        } else {
            fLocator.setColumnNumber(0);
            fLocator.setLineNumber(0);
        }
        switch (node.getNodeType()) {
            case Node.DOCUMENT_TYPE_NODE :
                serializeDocType((DocumentType) node, true);
                break;
            case Node.COMMENT_NODE :
                serializeComment((Comment) node);
                break;
            case Node.DOCUMENT_FRAGMENT_NODE :
                break;
            case Node.DOCUMENT_NODE :
                break;
            case Node.ELEMENT_NODE :
                serializeElement((Element) node, true);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE :
                serializePI((ProcessingInstruction) node);
                break;
            case Node.CDATA_SECTION_NODE :
                serializeCDATASection((CDATASection) node);
                break;
            case Node.TEXT_NODE :
                serializeText((Text) node);
                break;
            case Node.ENTITY_REFERENCE_NODE :
                serializeEntityReference((EntityReference) node, true);
                break;
            default :
                }
    }
    protected void endNode(Node node) throws org.xml.sax.SAXException {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE :
                break;
            case Node.DOCUMENT_TYPE_NODE :
                serializeDocType((DocumentType) node, false);
                break;
            case Node.ELEMENT_NODE :
                serializeElement((Element) node, false);
                break;
            case Node.CDATA_SECTION_NODE :
                break;
            case Node.ENTITY_REFERENCE_NODE :
                serializeEntityReference((EntityReference) node, false);
                break;
            default :
                }
    }
    protected boolean applyFilter(Node node, int nodeType) {
        if (fFilter != null && (fWhatToShowFilter & nodeType) != 0) {
            short code = fFilter.acceptNode(node);
            switch (code) {
                case NodeFilter.FILTER_REJECT :
                case NodeFilter.FILTER_SKIP :
                    return false; 
                default : 
            }
        }
        return true;
    }
    protected void serializeDocType(DocumentType node, boolean bStart)
        throws SAXException {
        String docTypeName = node.getNodeName();
        String publicId = node.getPublicId();
        String systemId = node.getSystemId();
        String internalSubset = node.getInternalSubset();
        if (internalSubset != null && !"".equals(internalSubset)) {
            if (bStart) {
                try {
                    Writer writer = fSerializer.getWriter();
                    StringBuffer dtd = new StringBuffer();
                    dtd.append("<!DOCTYPE ");
                    dtd.append(docTypeName);
                    if (null != publicId) {
                        dtd.append(" PUBLIC \"");
                        dtd.append(publicId);
                        dtd.append('\"');
                    }
                    if (null != systemId) {
                        if (null == publicId) {
                            dtd.append(" SYSTEM \"");
                        } else {
                            dtd.append(" \"");
                        }    
                        dtd.append(systemId);
                        dtd.append('\"');
                    }
                    dtd.append(" [ ");
                    dtd.append(fNewLine);
                    dtd.append(internalSubset);
                    dtd.append("]>");
                    dtd.append(new String(fNewLine));
                    writer.write(dtd.toString());
                    writer.flush();
                } catch (IOException e) {
                    throw new SAXException(Utils.messages.createMessage(
                            MsgKey.ER_WRITING_INTERNAL_SUBSET, null), e);
                }
            } 
        } else {
            if (bStart) {
                if (fLexicalHandler != null) {
                    fLexicalHandler.startDTD(docTypeName, publicId, systemId);
                }
            } else {
                if (fLexicalHandler != null) {
                    fLexicalHandler.endDTD();
                }
            }
        }
    }
    protected void serializeComment(Comment node) throws SAXException {
        if ((fFeatures & COMMENTS) != 0) {
            String data = node.getData();
            if ((fFeatures & WELLFORMED) != 0) {
                isCommentWellFormed(data);
            }
            if (fLexicalHandler != null) {
                if (!applyFilter(node, NodeFilter.SHOW_COMMENT)) {
                    return;
                }
                fLexicalHandler.comment(data.toCharArray(), 0, data.length());
            }
        }
    }
    protected void serializeElement(Element node, boolean bStart)
        throws SAXException {
        if (bStart) {
            fElementDepth++;
            if ((fFeatures & WELLFORMED) != 0) {
                isElementWellFormed(node);
            }
            if (!applyFilter(node, NodeFilter.SHOW_ELEMENT)) {
                return;
            }
            if ((fFeatures & NAMESPACES) != 0) {
            	fNSBinder.pushContext();
            	fLocalNSBinder.reset();
                recordLocalNSDecl(node);
                fixupElementNS(node);
            }
            fSerializer.startElement(
            		node.getNamespaceURI(),
                    node.getLocalName(),
                    node.getNodeName());
            serializeAttList(node);
        } else {
        	fElementDepth--;
            if (!applyFilter(node, NodeFilter.SHOW_ELEMENT)) {
                return;
            }
            this.fSerializer.endElement(
            	node.getNamespaceURI(),
                node.getLocalName(),
                node.getNodeName());
            if ((fFeatures & NAMESPACES) != 0 ) {
                    fNSBinder.popContext();
            }
        }
    }
    protected void serializeAttList(Element node) throws SAXException {
        NamedNodeMap atts = node.getAttributes();
        int nAttrs = atts.getLength();
        for (int i = 0; i < nAttrs; i++) {
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrName = attr.getNodeName();
            String attrPrefix = attr.getPrefix() == null ? "" : attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String type = null;
            if (fIsLevel3DOM) {
                type = ((Attr) attr).getSchemaTypeInfo().getTypeName();
            }
            type = type == null ? "CDATA" : type;
            String attrNS = attr.getNamespaceURI();
            if (attrNS !=null && attrNS.length() == 0) {
            	attrNS=null;
            	attrName=attr.getLocalName();
            }
            boolean isSpecified = ((Attr) attr).getSpecified();
            boolean addAttr = true;
            boolean applyFilter = false;
            boolean xmlnsAttr =
                attrName.equals("xmlns") || attrName.startsWith("xmlns:");
            if ((fFeatures & WELLFORMED) != 0) {
                isAttributeWellFormed(attr);
            }
            if ((fFeatures & NAMESPACES) != 0 && !xmlnsAttr) {
        		if (attrNS != null) {
        			attrPrefix = attrPrefix == null ? "" : attrPrefix;
        			String declAttrPrefix = fNSBinder.getPrefix(attrNS);
        			String declAttrNS = fNSBinder.getURI(attrPrefix);
        			if ("".equals(attrPrefix) || "".equals(declAttrPrefix)
        					|| !attrPrefix.equals(declAttrPrefix)) {
        				if (declAttrPrefix != null && !"".equals(declAttrPrefix)) {
        					attrPrefix = declAttrPrefix;
        					if (declAttrPrefix.length() > 0 ) { 
        						attrName = declAttrPrefix + ":" + localName;
        					} else {
        						attrName = localName;
        					}	
        				} else {
        					if (attrPrefix != null && !"".equals(attrPrefix)
        							&& declAttrNS == null) {
        						if ((fFeatures & NAMESPACEDECLS) != 0) {
        							fSerializer.addAttribute(XMLNS_URI, attrPrefix,
        									XMLNS_PREFIX + ":" + attrPrefix, "CDATA",
        									attrNS);
        							fNSBinder.declarePrefix(attrPrefix, attrNS);
        							fLocalNSBinder.declarePrefix(attrPrefix, attrNS);
        						}
        					} else {
        						int counter = 1;
        						attrPrefix = "NS" + counter++;
        						while (fLocalNSBinder.getURI(attrPrefix) != null) {
        							attrPrefix = "NS" + counter++;
        						}
        						attrName = attrPrefix + ":" + localName;
        						if ((fFeatures & NAMESPACEDECLS) != 0) {
        							fSerializer.addAttribute(XMLNS_URI, attrPrefix,
        									XMLNS_PREFIX + ":" + attrPrefix, "CDATA",
        									attrNS);
            						fNSBinder.declarePrefix(attrPrefix, attrNS);
            						fLocalNSBinder.declarePrefix(attrPrefix, attrNS);
        						}
        					}
        				}
        			}
        		} else { 
        			if (localName == null) {
        				String msg = Utils.messages.createMessage(
        						MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
        						new Object[] { attrName });
        				if (fErrorHandler != null) {
        					fErrorHandler
        							.handleError(new DOMErrorImpl(
        									DOMError.SEVERITY_ERROR, msg,
        									MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null,
        									null, null));
        				}
        			} else { 
        			}
        		}
            }
            if ((((fFeatures & DISCARDDEFAULT) != 0) && isSpecified)
                || ((fFeatures & DISCARDDEFAULT) == 0)) {
                applyFilter = true;
            } else {
            	addAttr = false;
            }
            if (applyFilter) {
                if (fFilter != null
                    && (fFilter.getWhatToShow() & NodeFilter.SHOW_ATTRIBUTE)
                        != 0) {
                    if (!xmlnsAttr) {
                        short code = fFilter.acceptNode(attr);
                        switch (code) {
                            case NodeFilter.FILTER_REJECT :
                            case NodeFilter.FILTER_SKIP :
                                addAttr = false;
                                break;
                            default : 
                        }
                    }
                }
            }
            if (addAttr && xmlnsAttr) {
                if ((fFeatures & NAMESPACEDECLS) != 0) {
                	if (localName != null && !"".equals(localName)) {
                		fSerializer.addAttribute(attrNS, localName, attrName, type, attrValue);
                	} 
                }
            } else if (
                addAttr && !xmlnsAttr) { 
                if (((fFeatures & NAMESPACEDECLS) != 0) && (attrNS != null)) {
                    fSerializer.addAttribute(
                        attrNS,
                        localName,
                        attrName,
                        type,
                        attrValue);
                } else {
                    fSerializer.addAttribute(
                        "",
                        localName,
                        attrName,
                        type,
                        attrValue);
                }
            }
            if (xmlnsAttr && ((fFeatures & NAMESPACEDECLS) != 0)) {
                int index;
                String prefix =
                    (index = attrName.indexOf(":")) < 0
                        ? ""
                        : attrName.substring(index + 1);
                if (!"".equals(prefix)) {
                    fSerializer.namespaceAfterStartElement(prefix, attrValue);
                }
            }
        }
    }
    protected void serializePI(ProcessingInstruction node)
        throws SAXException {
        ProcessingInstruction pi = node;
        String name = pi.getNodeName();
        if ((fFeatures & WELLFORMED) != 0) {
            isPIWellFormed(node);
        }
        if (!applyFilter(node, NodeFilter.SHOW_PROCESSING_INSTRUCTION)) {
            return;
        }
        if (name.equals("xslt-next-is-raw")) {
            fNextIsRaw = true;
        } else {
            this.fSerializer.processingInstruction(name, pi.getData());
        }
    }
    protected void serializeCDATASection(CDATASection node)
        throws SAXException {
        if ((fFeatures & WELLFORMED) != 0) {
            isCDATASectionWellFormed(node);
        }
        if ((fFeatures & CDATA) != 0) {
            String nodeValue = node.getNodeValue();
            int endIndex = nodeValue.indexOf("]]>");
            if ((fFeatures & SPLITCDATA) != 0) {
                if (endIndex >= 0) {
                    String relatedData = nodeValue.substring(0, endIndex + 2);
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_CDATA_SECTIONS_SPLIT,
                            null);
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_WARNING,
                                msg,
                                MsgKey.ER_CDATA_SECTIONS_SPLIT,
                                null,
                                relatedData,
                                null));
                    }
                }
            } else {
                if (endIndex >= 0) {
                    String relatedData = nodeValue.substring(0, endIndex + 2);
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_CDATA_SECTIONS_SPLIT,
                            null);
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_ERROR,
                                msg,
                                MsgKey.ER_CDATA_SECTIONS_SPLIT));
                    }
                    return;
                }
            }
            if (!applyFilter(node, NodeFilter.SHOW_CDATA_SECTION)) {
                return;
            }
            if (fLexicalHandler != null) {
                fLexicalHandler.startCDATA();
            }
            dispatachChars(node);
            if (fLexicalHandler != null) {
                fLexicalHandler.endCDATA();
            }
        } else {
            dispatachChars(node);
        }
    }
    protected void serializeText(Text node) throws SAXException {
        if (fNextIsRaw) {
            fNextIsRaw = false;
            fSerializer.processingInstruction(
                javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING,
                "");
            dispatachChars(node);
            fSerializer.processingInstruction(
                javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING,
                "");
        } else {
            boolean bDispatch = false;
            if ((fFeatures & WELLFORMED) != 0) {
                isTextWellFormed(node);
            }
            boolean isElementContentWhitespace = false;
            if (fIsLevel3DOM) {
                isElementContentWhitespace =
                       node.isElementContentWhitespace();
            }
            if (isElementContentWhitespace) {
                if ((fFeatures & ELEM_CONTENT_WHITESPACE) != 0) {
                    bDispatch = true;
                }
            } else {
                bDispatch = true;
            }
            if (!applyFilter(node, NodeFilter.SHOW_TEXT)) {
                return;
            }
            if (bDispatch) {
                dispatachChars(node);
            }
        }
    }
    protected void serializeEntityReference(
        EntityReference node,
        boolean bStart)
        throws SAXException {
        if (bStart) {
            EntityReference eref = node;
            if ((fFeatures & ENTITIES) != 0) {
                if ((fFeatures & WELLFORMED) != 0) {
                    isEntityReferneceWellFormed(node);
                }
                if ((fFeatures & NAMESPACES) != 0) {
                    checkUnboundPrefixInEntRef(node);
                }
            } 
            if (fLexicalHandler != null) {
                fLexicalHandler.startEntity(eref.getNodeName());
            } 
        } else {
            EntityReference eref = node;
            if (fLexicalHandler != null) {
                fLexicalHandler.endEntity(eref.getNodeName());
            }
        }
    }
    protected boolean isXMLName(String s, boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (!xml11Version)
            return XMLChar.isValidName(s);
        else
            return XML11Char.isXML11ValidName(s);
    }
    protected boolean isValidQName(
        String prefix,
        String local,
        boolean xml11Version) {
        if (local == null)
            return false;
        boolean validNCName = false;
        if (!xml11Version) {
            validNCName =
                (prefix == null || XMLChar.isValidNCName(prefix))
                    && XMLChar.isValidNCName(local);
        } else {
            validNCName =
                (prefix == null || XML11Char.isXML11ValidNCName(prefix))
                    && XML11Char.isXML11ValidNCName(local);
        }
        return validNCName;
    }
    protected boolean isWFXMLChar(String chardata, Character refInvalidChar) {
        if (chardata == null || (chardata.length() == 0)) {
            return true;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        if (fIsXMLVersion11) {
            int i = 0;
            while (i < datalength) {
                if (XML11Char.isXML11Invalid(dataarray[i++])) {
                    char ch = dataarray[i - 1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
            }
        } 
        else {
            int i = 0;
            while (i < datalength) {
                if (XMLChar.isInvalid(dataarray[i++])) {
                    char ch = dataarray[i - 1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
            }
        } 
        return true;
    } 
    protected Character isWFXMLChar(String chardata) {
    	Character refInvalidChar;
        if (chardata == null || (chardata.length() == 0)) {
            return null;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        if (fIsXMLVersion11) {
            int i = 0;
            while (i < datalength) {
                if (XML11Char.isXML11Invalid(dataarray[i++])) {
                    char ch = dataarray[i - 1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return refInvalidChar;
                }
            }
        } 
        else {
            int i = 0;
            while (i < datalength) {
                if (XMLChar.isInvalid(dataarray[i++])) {
                    char ch = dataarray[i - 1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return refInvalidChar;
                }
            }
        } 
        return null;
    } 
    protected void isCommentWellFormed(String data) {
        if (data == null || (data.length() == 0)) {
            return;
        }
        char[] dataarray = data.toCharArray();
        int datalength = dataarray.length;
        if (fIsXMLVersion11) {
            int i = 0;
            while (i < datalength) {
                char c = dataarray[i++];
                if (XML11Char.isXML11Invalid(c)) {
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                            new Object[] { new Character(c)});
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_FATAL_ERROR,
                                msg,
                                MsgKey.ER_WF_INVALID_CHARACTER,
                                null,
                                null,
                                null));
                    }
                } else if (c == '-' && i < datalength && dataarray[i] == '-') {
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_WF_DASH_IN_COMMENT,
                            null);
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_FATAL_ERROR,
                                msg,
                                MsgKey.ER_WF_INVALID_CHARACTER,
                                null,
                                null,
                                null));
                    }
                }
            }
        } 
        else {
            int i = 0;
            while (i < datalength) {
                char c = dataarray[i++];
                if (XMLChar.isInvalid(c)) {
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2)
                            && XMLChar.isSupplemental(
                                XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                            new Object[] { new Character(c)});
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_FATAL_ERROR,
                                msg,
                                MsgKey.ER_WF_INVALID_CHARACTER,
                                null,
                                null,
                                null));
                    }
                } else if (c == '-' && i < datalength && dataarray[i] == '-') {
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_WF_DASH_IN_COMMENT,
                            null);
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_FATAL_ERROR,
                                msg,
                                MsgKey.ER_WF_INVALID_CHARACTER,
                                null,
                                null,
                                null));
                    }
                }
            }
        }
        return;
    }
    protected void isElementWellFormed(Node node) {
        boolean isNameWF = false;
        if ((fFeatures & NAMESPACES) != 0) {
            isNameWF =
                isValidQName(
                    node.getPrefix(),
                    node.getLocalName(),
                    fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), fIsXMLVersion11);
        }
        if (!isNameWF) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                    new Object[] { "Element", node.getNodeName()});
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                        null,
                        null,
                        null));
            }
        }
    }
    protected void isAttributeWellFormed(Node node) {
        boolean isNameWF = false;
        if ((fFeatures & NAMESPACES) != 0) {
            isNameWF =
                isValidQName(
                    node.getPrefix(),
                    node.getLocalName(),
                    fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), fIsXMLVersion11);
        }
        if (!isNameWF) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                    new Object[] { "Attr", node.getNodeName()});
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                        null,
                        null,
                        null));
            }
        }
        String value = node.getNodeValue();
        if (value.indexOf('<') >= 0) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_LT_IN_ATTVAL,
                    new Object[] {
                        ((Attr) node).getOwnerElement().getNodeName(),
                        node.getNodeName()});
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_LT_IN_ATTVAL,
                        null,
                        null,
                        null));
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child == null) {
                continue;
            }
            switch (child.getNodeType()) {
                case Node.TEXT_NODE :
                    isTextWellFormed((Text) child);
                    break;
                case Node.ENTITY_REFERENCE_NODE :
                    isEntityReferneceWellFormed((EntityReference) child);
                    break;
                default :
            }
        }
    }
    protected void isPIWellFormed(ProcessingInstruction node) {
        if (!isXMLName(node.getNodeName(), fIsXMLVersion11)) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                    new Object[] { "ProcessingInstruction", node.getTarget()});
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                        null,
                        null,
                        null));
            }
        }
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                    new Object[] { Integer.toHexString(Character.getNumericValue(invalidChar.charValue())) });
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER,
                        null,
                        null,
                        null));
            }
        }
    }
    protected void isCDATASectionWellFormed(CDATASection node) {
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                    new Object[] { Integer.toHexString(Character.getNumericValue(invalidChar.charValue())) });
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER,
                        null,
                        null,
                        null));
            }
        }
    }
    protected void isTextWellFormed(Text node) {
    	Character invalidChar = isWFXMLChar(node.getData());
    	if (invalidChar != null) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                    new Object[] { Integer.toHexString(Character.getNumericValue(invalidChar.charValue())) });
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER,
                        null,
                        null,
                        null));
            }
        }
    }
    protected void isEntityReferneceWellFormed(EntityReference node) {
        if (!isXMLName(node.getNodeName(), fIsXMLVersion11)) {
            String msg =
                Utils.messages.createMessage(
                    MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                    new Object[] { "EntityReference", node.getNodeName()});
            if (fErrorHandler != null) {
                fErrorHandler.handleError(
                    new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR,
                        msg,
                        MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                        null,
                        null,
                        null));
            }
        }
        Node parent = node.getParentNode();
        DocumentType docType = node.getOwnerDocument().getDoctype();
        if (docType != null) {
            NamedNodeMap entities = docType.getEntities();
            for (int i = 0; i < entities.getLength(); i++) {
                Entity ent = (Entity) entities.item(i);
                String nodeName =
                    node.getNodeName() == null ? "" : node.getNodeName();
                String nodeNamespaceURI =
                    node.getNamespaceURI() == null
                        ? ""
                        : node.getNamespaceURI();
                String entName =
                    ent.getNodeName() == null ? "" : ent.getNodeName();
                String entNamespaceURI =
                    ent.getNamespaceURI() == null ? "" : ent.getNamespaceURI();
                if (parent.getNodeType() == Node.ELEMENT_NODE) {
                    if (entNamespaceURI.equals(nodeNamespaceURI)
                        && entName.equals(nodeName)) {
                        if (ent.getNotationName() != null) {
                            String msg =
                                Utils.messages.createMessage(
                                    MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                                    new Object[] { node.getNodeName()});
                            if (fErrorHandler != null) {
                                fErrorHandler.handleError(
                                    new DOMErrorImpl(
                                        DOMError.SEVERITY_FATAL_ERROR,
                                        msg,
                                        MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                                        null,
                                        null,
                                        null));
                            }
                        }
                    }
                } 
                if (parent.getNodeType() == Node.ATTRIBUTE_NODE) {
                    if (entNamespaceURI.equals(nodeNamespaceURI)
                        && entName.equals(nodeName)) {
                        if (ent.getPublicId() != null
                            || ent.getSystemId() != null
                            || ent.getNotationName() != null) {
                            String msg =
                                Utils.messages.createMessage(
                                    MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                                    new Object[] { node.getNodeName()});
                            if (fErrorHandler != null) {
                                fErrorHandler.handleError(
                                    new DOMErrorImpl(
                                        DOMError.SEVERITY_FATAL_ERROR,
                                        msg,
                                        MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                                        null,
                                        null,
                                        null));
                            }
                        }
                    }
                } 
            }
        }
    } 
    protected void checkUnboundPrefixInEntRef(Node node) {
        Node child, next;
        for (child = node.getFirstChild(); child != null; child = next) {
            next = child.getNextSibling();
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String prefix = child.getPrefix();
                if (prefix != null
                		&& fNSBinder.getURI(prefix) == null) {
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                            new Object[] {
                                node.getNodeName(),
                                child.getNodeName(),
                                prefix });
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_FATAL_ERROR,
                                msg,
                                MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                                null,
                                null,
                                null));
                    }
                }
                NamedNodeMap attrs = child.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    String attrPrefix = attrs.item(i).getPrefix();
                    if (attrPrefix != null
                    		&& fNSBinder.getURI(attrPrefix) == null) {
                        String msg =
                            Utils.messages.createMessage(
                                MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                                new Object[] {
                                    node.getNodeName(),
                                    child.getNodeName(),
                                    attrs.item(i)});
                        if (fErrorHandler != null) {
                            fErrorHandler.handleError(
                                new DOMErrorImpl(
                                    DOMError.SEVERITY_FATAL_ERROR,
                                    msg,
                                    MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                                    null,
                                    null,
                                    null));
                        }
                    }
                }
            }
            if (child.hasChildNodes()) {
                checkUnboundPrefixInEntRef(child);
            }
        }
    }
    protected void recordLocalNSDecl(Node node) {
        NamedNodeMap atts = ((Element) node).getAttributes();
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrPrefix = attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String attrNS = attr.getNamespaceURI();
            localName =
                localName == null
                    || XMLNS_PREFIX.equals(localName) ? "" : localName;
            attrPrefix = attrPrefix == null ? "" : attrPrefix;
            attrValue = attrValue == null ? "" : attrValue;
            attrNS = attrNS == null ? "" : attrNS;
            if (XMLNS_URI.equals(attrNS)) {
                if (XMLNS_URI.equals(attrValue)) {
                    String msg =
                        Utils.messages.createMessage(
                            MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                            new Object[] { attrPrefix, XMLNS_URI });
                    if (fErrorHandler != null) {
                        fErrorHandler.handleError(
                            new DOMErrorImpl(
                                DOMError.SEVERITY_ERROR,
                                msg,
                                MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                                null,
                                null,
                                null));
                    }
                } else {
                	if (XMLNS_PREFIX.equals(attrPrefix) ) {
                        if (attrValue.length() != 0) {
                            fNSBinder.declarePrefix(localName, attrValue);
                        } else {
                        }
                    } else { 
                        fNSBinder.declarePrefix("", attrValue);
                    }
                }
            }
        }
    }
    protected void fixupElementNS(Node node) throws SAXException {
        String namespaceURI = ((Element) node).getNamespaceURI();
        String prefix = ((Element) node).getPrefix();
        String localName = ((Element) node).getLocalName();
        if (namespaceURI != null) {
            prefix = prefix == null ? "" : prefix;
            String inScopeNamespaceURI = fNSBinder.getURI(prefix);
            if ((inScopeNamespaceURI != null
                && inScopeNamespaceURI.equals(namespaceURI))) {
            } else {
                if ((fFeatures & NAMESPACEDECLS) != 0) {
                    if ("".equals(prefix) || "".equals(namespaceURI)) {
                    	((Element)node).setAttributeNS(XMLNS_URI, XMLNS_PREFIX, namespaceURI);
                    } else {
                    	((Element)node).setAttributeNS(XMLNS_URI, XMLNS_PREFIX + ":" + prefix, namespaceURI);
                    }
                }
                fLocalNSBinder.declarePrefix(prefix, namespaceURI);
                fNSBinder.declarePrefix(prefix, namespaceURI);
            } 
        } else {
            if (localName == null || "".equals(localName)) {
                String msg =
                    Utils.messages.createMessage(
                        MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                        new Object[] { node.getNodeName()});
                if (fErrorHandler != null) {
                    fErrorHandler.handleError(
                        new DOMErrorImpl(
                            DOMError.SEVERITY_ERROR,
                            msg,
                            MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                            null,
                            null,
                            null));
                }
            } else {
            	namespaceURI = fNSBinder.getURI("");
            	if (namespaceURI !=null && namespaceURI.length() > 0) {
            	    ((Element)node).setAttributeNS(XMLNS_URI, XMLNS_PREFIX, "");
            		fLocalNSBinder.declarePrefix("", "");
                    fNSBinder.declarePrefix("", "");
            	}
            }
        }
    }
    private static final Hashtable s_propKeys = new Hashtable();
    static {
        int i = CDATA;
        Integer val = new Integer(i);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_CDATA_SECTIONS,
            val);
        int i1 = COMMENTS;
        val = new Integer(i1);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_COMMENTS,
            val);
        int i2 = ELEM_CONTENT_WHITESPACE;
        val = new Integer(i2);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE,
            val);
        int i3 = ENTITIES;
        val = new Integer(i3);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_ENTITIES,
            val);
        int i4 = NAMESPACES;
        val = new Integer(i4);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_NAMESPACES,
            val);
        int i5 = NAMESPACEDECLS;
        val = new Integer(i5);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_NAMESPACE_DECLARATIONS,
            val);
        int i6 = SPLITCDATA;
        val = new Integer(i6);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_SPLIT_CDATA,
            val);
        int i7 = WELLFORMED;
        val = new Integer(i7);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS + DOMConstants.DOM_WELLFORMED,
            val);
        int i8 = DISCARDDEFAULT;
        val = new Integer(i8);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_DISCARD_DEFAULT_CONTENT,
            val);
        s_propKeys.put(
            DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_FORMAT_PRETTY_PRINT,
            "");
        s_propKeys.put(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "");
        s_propKeys.put(
            DOMConstants.S_XERCES_PROPERTIES_NS + DOMConstants.S_XML_VERSION,
            "");
        s_propKeys.put(DOMConstants.S_XSL_OUTPUT_ENCODING, "");
        s_propKeys.put(DOMConstants.S_XERCES_PROPERTIES_NS + DOMConstants.DOM_ENTITIES, "");
    }
    protected void initProperties(Properties properties) {
        for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
            final String key = (String) keys.nextElement();
            final Object iobj = s_propKeys.get(key);
            if (iobj != null) {
                if (iobj instanceof Integer) {
                    final int BITFLAG = ((Integer) iobj).intValue();
                    if ((properties.getProperty(key).endsWith("yes"))) {
                        fFeatures = fFeatures | BITFLAG;
                    } else {
                        fFeatures = fFeatures & ~BITFLAG;
                    }
                } else {
                    if ((DOMConstants.S_DOM3_PROPERTIES_NS
                        + DOMConstants.DOM_FORMAT_PRETTY_PRINT)
                        .equals(key)) {
                        if ((properties.getProperty(key).endsWith("yes"))) {
                            fSerializer.setIndent(true);
                            fSerializer.setIndentAmount(3);
                        } else {
                            fSerializer.setIndent(false);
                        }
                    } else if (
                        (DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL).equals(
                            key)) {
                        if ((properties.getProperty(key).endsWith("yes"))) {
                            fSerializer.setOmitXMLDeclaration(true);
                        } else {
                            fSerializer.setOmitXMLDeclaration(false);
                        }
                    } else if (
                        (
                            DOMConstants.S_XERCES_PROPERTIES_NS
                                + DOMConstants.S_XML_VERSION).equals(
                            key)) {
                        String version = properties.getProperty(key);
                        if ("1.1".equals(version)) {
                            fIsXMLVersion11 = true;
                            fSerializer.setVersion(version);
                        } else {
                            fSerializer.setVersion("1.0");
                        }
                    } else if (
                        (DOMConstants.S_XSL_OUTPUT_ENCODING).equals(key)) {
                        String encoding = properties.getProperty(key);
                        if (encoding != null) {
                            fSerializer.setEncoding(encoding);
                        }
                    } else if ((DOMConstants.S_XERCES_PROPERTIES_NS
                            + DOMConstants.DOM_ENTITIES).equals(key)) {
                        if ((properties.getProperty(key).endsWith("yes"))) {
                            fSerializer.setDTDEntityExpansion(false);
                        }
                        else {
                            fSerializer.setDTDEntityExpansion(true);
                        }
                    } else {
                    }
                }
            }
        }
        if (fNewLine != null) {
            fSerializer.setOutputProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR, fNewLine);
        }
    }
} 
