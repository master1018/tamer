class ExpatParser {
    private static final int BUFFER_SIZE = 8096; 
    private int pointer;
    private boolean inStartElement = false;
    private int attributeCount = -1;
    private int attributePointer = 0;
    private final Locator locator = new ExpatLocator();
    private final ExpatReader xmlReader;
    private final String publicId;
    private final String systemId;
    private final String encoding;
    private final ExpatAttributes attributes = new CurrentAttributes();
    private static final String OUTSIDE_START_ELEMENT
            = "Attributes can only be used within the scope of startElement().";
    private static final String DEFAULT_ENCODING = "UTF-8";
     static final String CHARACTER_ENCODING = "UTF-16";
    private static final int TIMEOUT = 20 * 1000;
     ExpatParser(String encoding, ExpatReader xmlReader,
            boolean processNamespaces, String publicId, String systemId) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.xmlReader = xmlReader;
        this.encoding = encoding == null ? DEFAULT_ENCODING : encoding;
        this.pointer = initialize(
            this.encoding,
            processNamespaces
        );
    }
    private ExpatParser(String encoding, ExpatReader xmlReader, int pointer,
            String publicId, String systemId) {
        this.encoding = encoding;
        this.xmlReader = xmlReader;
        this.pointer = pointer;
        this.systemId = systemId;
        this.publicId = publicId;
    }
    private native int initialize(String encoding, boolean namespacesEnabled);
     void startElement(String uri, String localName, String qName,
            int attributePointer, int attributeCount) throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler == null) {
            return;
        }
        try {
            inStartElement = true;
            this.attributePointer = attributePointer;
            this.attributeCount = attributeCount;
            contentHandler.startElement(
                    uri, localName, qName, this.attributes);
        }
        finally {
            inStartElement = false;
            this.attributeCount = -1;
            this.attributePointer = 0;
        }
    }
     void endElement(String uri, String localName, String qName)
            throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }
     void text(char[] text, int length) throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.characters(text, 0, length);
        }
    }
     void comment(char[] text, int length) throws SAXException {
        LexicalHandler lexicalHandler = xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(text, 0, length);
        }
    }
     void startCdata() throws SAXException {
        LexicalHandler lexicalHandler = xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }
     void endCdata() throws SAXException {
        LexicalHandler lexicalHandler = xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }
     void startNamespace(String prefix, String uri)
            throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }
     void endNamespace(String prefix) throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }
     void startDtd(String name, String publicId, String systemId)
            throws SAXException {
        LexicalHandler lexicalHandler = xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId, systemId);
        }
    }
     void endDtd() throws SAXException {
        LexicalHandler lexicalHandler = xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }
     void processingInstruction(String target, String data)
            throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }        
    }
     void notationDecl(String name, String publicId, String systemId) throws SAXException {
        DTDHandler dtdHandler = xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.notationDecl(name, publicId, systemId);
        }
    }
     void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        DTDHandler dtdHandler = xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }
     void handleExternalEntity(String context, String publicId,
            String systemId) throws SAXException, IOException {
        EntityResolver entityResolver = xmlReader.entityResolver;
        if (entityResolver == null) {
            return;
        }
        if (this.systemId != null) {
            try {
                URI systemUri = new URI(systemId);
                if (!systemUri.isAbsolute() && !systemUri.isOpaque()) {
                    URI baseUri = new URI(this.systemId);
                    systemUri = baseUri.resolve(systemUri);
                    systemId = systemUri.toString();
                }
            } catch (Exception e) {
                Logger.getLogger(ExpatParser.class.getName()).log(Level.INFO,
                        "Could not resolve '" + systemId + "' relative to"
                        + " '" + this.systemId + "' at " + locator, e);
            }
        }
        InputSource inputSource = entityResolver.resolveEntity(
                publicId, systemId);
        if (inputSource == null) {
            return;
        }
        String encoding = pickEncoding(inputSource);
        int pointer = createEntityParser(this.pointer, context, encoding);
        try {
            EntityParser entityParser = new EntityParser(encoding, xmlReader,
                    pointer, inputSource.getPublicId(),
                    inputSource.getSystemId());
            parseExternalEntity(entityParser, inputSource);
        } finally {
            releaseParser(pointer);
        }
    }
    private String pickEncoding(InputSource inputSource) {
        Reader reader = inputSource.getCharacterStream();
        if (reader != null) {
            return CHARACTER_ENCODING;
        }
        String encoding = inputSource.getEncoding();
        return encoding == null ? DEFAULT_ENCODING : encoding;
    }
    private void parseExternalEntity(ExpatParser entityParser,
            InputSource inputSource) throws IOException, SAXException {
        Reader reader = inputSource.getCharacterStream();
        if (reader != null) {
            try {
                entityParser.append("<externalEntity>");
                entityParser.parseFragment(reader);
                entityParser.append("</externalEntity>");
            } finally {
                reader.close();
            }
            return;
        }
        InputStream in = inputSource.getByteStream();
        if (in != null) {
            try {
                entityParser.append("<externalEntity>"
                        .getBytes(entityParser.encoding));
                entityParser.parseFragment(in);
                entityParser.append("</externalEntity>"
                        .getBytes(entityParser.encoding));
            } finally {
                in.close();
            }
            return;
        }
        String systemId = inputSource.getSystemId();
        if (systemId == null) {
            throw new ParseException("No input specified.", locator);
        }
        in = openUrl(systemId);
        try {
            entityParser.append("<externalEntity>"
                    .getBytes(entityParser.encoding));
            entityParser.parseFragment(in);
            entityParser.append("</externalEntity>"
                    .getBytes(entityParser.encoding));
        } finally {
            in.close();
        }
    }
    private static native int createEntityParser(int parentPointer,
            String context, String encoding);
     void append(String xml) throws SAXException {
        try {
            append(this.pointer, xml, false);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }
    private native void append(int pointer, String xml, boolean isFinal)
            throws SAXException, ExpatException;
     void append(char[] xml, int offset, int length)
            throws SAXException {
        try {
            append(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }
    private native void append(int pointer, char[] xml, int offset,
            int length) throws SAXException, ExpatException;
     void append(byte[] xml) throws SAXException {
        append(xml, 0, xml.length);
    }
     void append(byte[] xml, int offset, int length)
            throws SAXException {
        try {
            append(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }
    private native void append(int pointer, byte[] xml, int offset,
            int length) throws SAXException, ExpatException;
     void parseDocument(InputStream in) throws IOException,
            SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }
     void parseDocument(Reader in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }
    private void parseFragment(Reader in) throws IOException, SAXException {
        char[] buffer = new char[BUFFER_SIZE / 2];
        int length;
        while ((length = in.read(buffer)) != -1) {
            try {
                append(this.pointer, buffer, 0, length);
            } catch (ExpatException e) {
                throw new ParseException(e.getMessage(), locator);
            }
        }
    }
    private void parseFragment(InputStream in)
            throws IOException, SAXException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1) {
            try {
                append(this.pointer, buffer, 0, length);
            } catch (ExpatException e) {
                throw new ParseException(e.getMessage(), this.locator);
            }
        }
    }
    private void startDocument() throws SAXException {
        ContentHandler contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(this.locator);
            contentHandler.startDocument();
        }
    }
    private void endDocument() throws SAXException {
        ContentHandler contentHandler;
        contentHandler = xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endDocument();
        }
    }
     void finish() throws SAXException {
        try {
            append(this.pointer, "", true);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }
    @Override
    @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
    protected synchronized void finalize() throws Throwable {
        if (this.pointer != 0) {
            release(this.pointer);
            this.pointer = 0;
        }
    }
    private native void release(int pointer);
    private static native void releaseParser(int pointer);
    private static native void staticInitialize(String emptyString);
    static {
        staticInitialize("");
    }
    private int line() {
        return line(this.pointer);
    }
    private static native int line(int pointer);
    private int column() {
        return column(this.pointer);
    }
    private static native int column(int pointer);
     Attributes cloneAttributes() {
        if (!inStartElement) {
            throw new IllegalStateException(OUTSIDE_START_ELEMENT);
        }
        if (attributeCount == 0) {
            return ClonedAttributes.EMPTY;
        }
        int clonePointer
                = cloneAttributes(this.attributePointer, this.attributeCount);
        return new ClonedAttributes(pointer, clonePointer, attributeCount);
    }
    private static native int cloneAttributes(int pointer, int attributeCount);
    private static class ClonedAttributes extends ExpatAttributes {
        private static final Attributes EMPTY = new ClonedAttributes(0, 0, 0);
        private final int parserPointer;
        private int pointer;
        private final int length;
        private ClonedAttributes(int parserPointer, int pointer, int length) {
            this.parserPointer = parserPointer;
            this.pointer = pointer;
            this.length = length;
        }
        @Override
        public int getParserPointer() {
            return this.parserPointer;
        }
        @Override
        public int getPointer() {
            return pointer;
        }
        @Override
        public int getLength() {
            return length;
        }
        @Override
        @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
        protected synchronized void finalize() throws Throwable {
            if (pointer != 0) {
                freeAttributes(pointer);
                pointer = 0;
            }
        }
    }
    private class ExpatLocator implements Locator {
        public String getPublicId() {
            return publicId;
        }
        public String getSystemId() {
            return systemId;
        }
        public int getLineNumber() {
            return line();
        }
        public int getColumnNumber() {
            return column();
        }
        @Override
        public String toString() {
            return "Locator[publicId: " + publicId + ", systemId: " + systemId
                + ", line: " + getLineNumber()
                + ", column: " + getColumnNumber() + "]";
        }
    }
    private class CurrentAttributes extends ExpatAttributes {
        @Override
        public int getParserPointer() {
            return pointer;
        }
        @Override
        public int getPointer() {
            if (!inStartElement) {
                throw new IllegalStateException(OUTSIDE_START_ELEMENT);
            }
            return attributePointer;
        }
        @Override
        public int getLength() {
            if (!inStartElement) {
                throw new IllegalStateException(OUTSIDE_START_ELEMENT);
            }
            return attributeCount;
        }
    }
    private static class ParseException extends SAXParseException {
        private ParseException(String message, Locator locator) {
            super(makeMessage(message, locator), locator);
        }
        private static String makeMessage(String message, Locator locator) {
            return makeMessage(message, locator.getLineNumber(),
                    locator.getColumnNumber());
        }
        private static String makeMessage(
                String message, int line, int column) {
            return "At line " + line + ", column "
                    + column + ": " + message;
        }
    }
     static InputStream openUrl(String url) throws IOException {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            return urlConnection.getInputStream();
        } catch (Exception e) {
            IOException ioe = new IOException("Couldn't open " + url);
            ioe.initCause(e);
            throw ioe;
        }
    }
    private static class EntityParser extends ExpatParser {
        private int depth = 0; 
        private EntityParser(String encoding, ExpatReader xmlReader,
                int pointer, String publicId, String systemId) {
            super(encoding, xmlReader, pointer, publicId, systemId);
        }
        @Override
        void startElement(String uri, String localName, String qName,
                int attributePointer, int attributeCount) throws SAXException {
            if (depth++ > 0) {
                super.startElement(uri, localName, qName, attributePointer,
                        attributeCount);
            }
        }
        @Override
        void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (--depth > 0) {
                super.endElement(uri, localName, qName);
            }
        }
        @Override
        @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
        protected synchronized void finalize() throws Throwable {
        }
    }
}
