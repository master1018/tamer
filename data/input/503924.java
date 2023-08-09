public class ExpatReader implements XMLReader {
     ContentHandler contentHandler;
     DTDHandler dtdHandler;
     EntityResolver entityResolver;
     ErrorHandler errorHandler;
     LexicalHandler lexicalHandler;
    private boolean processNamespaces = true;
    private boolean processNamespacePrefixes = false;
    private static final String LEXICAL_HANDLER_PROPERTY
            = "http:
    private static class Feature {
        private static final String BASE_URI = "http:
        private static final String VALIDATION = BASE_URI + "validation";
        private static final String NAMESPACES = BASE_URI + "namespaces";
        private static final String NAMESPACE_PREFIXES
                = BASE_URI + "namespace-prefixes";
        private static final String STRING_INTERNING
                = BASE_URI + "string-interning";
    }
    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.equals(Feature.VALIDATION)) {
            return false;
        }
        if (name.equals(Feature.NAMESPACES)) {
            return processNamespaces;
        }
        if (name.equals(Feature.NAMESPACE_PREFIXES)) {
            return processNamespacePrefixes;
        }
        if (name.equals(Feature.STRING_INTERNING)) {
            return true;
        }
        throw new SAXNotRecognizedException(name);
    }
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.equals(Feature.VALIDATION)) {
            if (value) {
                throw new SAXNotSupportedException("Cannot enable " + name);
            } else {
                return;
            }
        }
        if (name.equals(Feature.NAMESPACES)) {
            processNamespaces = value;
            return;
        }
        if (name.equals(Feature.NAMESPACE_PREFIXES)) {
            processNamespacePrefixes = value;
            return;
        }
        if (name.equals(Feature.STRING_INTERNING)) {
            if (value) {
                return;
            } else {
                throw new SAXNotSupportedException("Cannot disable " + name);
            }
        }
        throw new SAXNotRecognizedException(name);
    }
    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            return lexicalHandler;
        }
        throw new SAXNotRecognizedException(name);
    }
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            if (value instanceof LexicalHandler) {
                this.lexicalHandler = (LexicalHandler) value;
                return;
            }
            throw new SAXNotSupportedException("value doesn't implement " +
                    "org.xml.sax.ext.LexicalHandler");
        }
        throw new SAXNotRecognizedException(name);
    }
    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }
    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }
    public boolean isNamespaceProcessingEnabled() {
        return processNamespaces;
    }
    public void setNamespaceProcessingEnabled(boolean processNamespaces) {
        this.processNamespaces = processNamespaces;
    }
    public void parse(InputSource input) throws IOException, SAXException {
        if (processNamespacePrefixes && processNamespaces) {
            throw new SAXNotSupportedException("The 'namespace-prefix' " +
                    "feature is not supported while the 'namespaces' " +
                    "feature is enabled.");
        }
        Reader reader = input.getCharacterStream();
        if (reader != null) {
            try {
                parse(reader, input.getPublicId(), input.getSystemId());
            } finally {
                reader.close();
            }
            return;
        }
        InputStream in = input.getByteStream();
        String encoding = input.getEncoding();
        if (in != null) {
            try {
                parse(in, encoding, input.getPublicId(), input.getSystemId());
            } finally {
                in.close();
            }
            return;
        }
        String systemId = input.getSystemId();
        if (systemId == null) {
            throw new SAXException("No input specified.");
        }
        in = ExpatParser.openUrl(systemId);
        try {
            parse(in, encoding, input.getPublicId(), systemId);
        } finally {
            in.close();
        }
    }
    private void parse(Reader in, String publicId, String systemId)
            throws IOException, SAXException {
        ExpatParser parser = new ExpatParser(
                ExpatParser.CHARACTER_ENCODING,
                this,
                processNamespaces,
                publicId,
                systemId
        );
        parser.parseDocument(in);
    }
    private void parse(InputStream in, String encoding, String publicId,
            String systemId) throws IOException, SAXException {
        ExpatParser parser = new ExpatParser(
                encoding,
                this,
                processNamespaces,
                publicId,
                systemId
        );
        parser.parseDocument(in);
    }
    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }
}
