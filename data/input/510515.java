class DocumentBuilderImpl extends DocumentBuilder {
    private static DOMImplementationImpl dom = DOMImplementationImpl.getInstance();
    private boolean coalescing;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private boolean ignoreComments;
    private boolean ignoreElementContentWhitespace;
    private boolean namespaceAware;
    DocumentBuilderImpl() {
    }
    @Override
    public DOMImplementation getDOMImplementation() {
        return dom;
    }
    @Override
    public boolean isNamespaceAware() {
        return namespaceAware;
    }
    @Override
    public boolean isValidating() {
        return false;
    }
    @Override
    public Document newDocument() {
        return dom.createDocument(null, null, null);
    }
    @Override
    public Document parse(InputSource source) throws SAXException, IOException {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        String namespaceURI = null;
        String qualifiedName = null;
        DocumentType doctype = null;
        String inputEncoding = source.getEncoding();
        String systemId = source.getSystemId();
        DocumentImpl document = new DocumentImpl(
                dom, namespaceURI, qualifiedName, doctype, inputEncoding);
        document.setDocumentURI(systemId);
        try {
            KXmlParser parser = new KXmlParser();
            parser.keepNamespaceAttributes();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespaceAware);
            if (source.getByteStream() != null) {
                parser.setInput(source.getByteStream(), inputEncoding);
            } else if (source.getCharacterStream() != null) {
                parser.setInput(source.getCharacterStream());
            } else if (systemId != null) {
                URL url = new URL(systemId);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                parser.setInput(urlConnection.getInputStream(), inputEncoding);
            } else {
                throw new SAXParseException(
                        "InputSource needs a stream, reader or URI", null);
            }
            if(parser.nextToken() == XmlPullParser.END_DOCUMENT) {
                throw new SAXParseException(
                        "Unexpected end of document", null);
            }
            parse(parser, document, document, XmlPullParser.END_DOCUMENT);
            parser.require(XmlPullParser.END_DOCUMENT, null, null);
        } catch (XmlPullParserException ex) {
            if(ex.getDetail() instanceof IOException) {
                throw (IOException)ex.getDetail();
            }
            if(ex.getDetail() instanceof RuntimeException) {
                throw (RuntimeException)ex.getDetail();
            }
            LocatorImpl locator = new LocatorImpl();
            locator.setPublicId(source.getPublicId());
            locator.setSystemId(systemId);
            locator.setLineNumber(ex.getLineNumber());
            locator.setColumnNumber(ex.getColumnNumber());
            SAXParseException newEx = new SAXParseException(ex.getMessage(),
                    locator);
            if (errorHandler != null) {
                errorHandler.error(newEx);
            }
            throw newEx;
        }
        return document;
    }
    private void parse(XmlPullParser parser, DocumentImpl document, Node node,
            int endToken) throws XmlPullParserException, IOException {
        int token = parser.getEventType();
        while (token != endToken && token != XmlPullParser.END_DOCUMENT) {
            if (token == XmlPullParser.PROCESSING_INSTRUCTION) {
                String text = parser.getText();
                int dot = text.indexOf(' ');
                String target = (dot != -1 ? text.substring(0, dot) : text);
                String data = (dot != -1 ? text.substring(dot + 1) : "");
                node.appendChild(document.createProcessingInstruction(target,
                        data));
            } else if (token == XmlPullParser.DOCDECL) {
                StringTokenizer tokenizer = new StringTokenizer(parser.getText());
                if (tokenizer.hasMoreTokens()) {
                    String name = tokenizer.nextToken();
                    String pubid = null;
                    String sysid = null;
                    if (tokenizer.hasMoreTokens()) {
                        String text = tokenizer.nextToken();
                        if ("SYSTEM".equals(text)) {
                            if (tokenizer.hasMoreTokens()) {
                                sysid = tokenizer.nextToken();
                            }
                        } else if ("PUBLIC".equals(text)) {
                            if (tokenizer.hasMoreTokens()) {
                                pubid = tokenizer.nextToken();
                            }
                            if (tokenizer.hasMoreTokens()) {
                                sysid = tokenizer.nextToken();
                            }
                        }
                    }
                    if (pubid != null && pubid.length() >= 2 && pubid.startsWith("\"") && pubid.endsWith("\"")) {
                        pubid = pubid.substring(1, pubid.length() - 1);
                    }
                    if (sysid != null && sysid.length() >= 2 && sysid.startsWith("\"") && sysid.endsWith("\"")) {
                        sysid = sysid.substring(1, sysid.length() - 1);
                    }
                    document.appendChild(new DocumentTypeImpl(document, name, pubid, sysid));
                }
            } else if (token == XmlPullParser.COMMENT) {
                if (!ignoreComments) {
                    node.appendChild(document.createComment(parser.getText()));
                }
            } else if (token == XmlPullParser.IGNORABLE_WHITESPACE) {
                if (!ignoreElementContentWhitespace && document != node) {
                    appendText(document, node, token, parser.getText());
                }
            } else if (token == XmlPullParser.TEXT || token == XmlPullParser.CDSECT) {
                appendText(document, node, token, parser.getText());
            } else if (token == XmlPullParser.ENTITY_REF) {
                String entity = parser.getName();
                if (entityResolver != null) {
                }
                String replacement = resolveStandardEntity(entity);
                if (replacement != null) {
                    appendText(document, node, token, replacement);
                } else {
                    node.appendChild(document.createEntityReference(entity));
                }
            } else if (token == XmlPullParser.START_TAG) {
                if (namespaceAware) {
                    String namespace = parser.getNamespace();
                    String name = parser.getName();
                    String prefix = parser.getPrefix();
                    if ("".equals(namespace)) {
                        namespace = null;
                    }
                    Element element = document.createElementNS(namespace, name);
                    element.setPrefix(prefix);
                    node.appendChild(element);
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attrNamespace = parser.getAttributeNamespace(i);
                        String attrPrefix = parser.getAttributePrefix(i);
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);
                        if ("".equals(attrNamespace)) {
                            attrNamespace = null;
                        }
                        Attr attr = document.createAttributeNS(attrNamespace, attrName);
                        attr.setPrefix(attrPrefix);
                        attr.setValue(attrValue);
                        element.setAttributeNodeNS(attr);
                    }
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);
                    parser.require(XmlPullParser.END_TAG, namespace, name);
                } else {
                    String name = parser.getName();
                    Element element = document.createElement(name);
                    node.appendChild(element);
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);
                        Attr attr = document.createAttribute(attrName);
                        attr.setValue(attrValue);
                        element.setAttributeNode(attr);
                    }
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);
                    parser.require(XmlPullParser.END_TAG, "", name);
                }
            }
            token = parser.nextToken();
        }
    }
    private void appendText(DocumentImpl document, Node parent, int token, String text) {
        if (text.length() == 0) {
            return;
        }
        if (coalescing) {
            Node lastChild = parent.getLastChild();
            if (lastChild != null && lastChild.getNodeType() == Node.TEXT_NODE) {
                Text textNode = (Text) lastChild;
                textNode.setData(textNode.getNodeValue() + text);
                return;
            }
        }
        parent.appendChild(token == XmlPullParser.CDSECT
                ? new CDATASectionImpl(document, text)
                : new TextImpl(document, text));
    }
    @Override
    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }
    @Override
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }
    public void setIgnoreComments(boolean value) {
        ignoreComments = value;
    }
    public void setCoalescing(boolean value) {
        coalescing = value;
    }
    public void setIgnoreElementContentWhitespace(boolean value) {
        ignoreElementContentWhitespace = value;
    }
    public void setNamespaceAware(boolean value) {
        namespaceAware = value;
    }
    private String resolveStandardEntity(String entity) {
        if (entity.startsWith("#x")) {
            return resolveCharacterReference(entity.substring(2), 16);
        } else if (entity.startsWith("#")) {
            return resolveCharacterReference(entity.substring(1), 10);
        }
        if ("lt".equals(entity)) {
            return "<";
        } else if ("gt".equals(entity)) {
            return ">";
        } else if ("amp".equals(entity)) {
            return "&";
        } else if ("apos".equals(entity)) {
            return "'";
        } else if ("quot".equals(entity)) {
            return "\"";
        } else {
            return null;
        }
    }
    private String resolveCharacterReference(String value, int base) {
        try {
            int ch = Integer.parseInt(value, base);
            if (ch < 0 || ch > Character.MAX_VALUE) {
                return null;
            }
            return String.valueOf((char) ch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
