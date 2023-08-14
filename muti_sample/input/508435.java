public abstract class SAXParser {
    private static final boolean DEBUG = false;
    protected SAXParser () {
    }
	public void reset() {
		throw new UnsupportedOperationException(
			"This SAXParser, \"" + this.getClass().getName() + "\", does not support the reset functionality."
			+ "  Specification \"" + this.getClass().getPackage().getSpecificationTitle() + "\""
			+ " version \"" + this.getClass().getPackage().getSpecificationVersion() + "\""
			);
	}
    public void parse(InputStream is, HandlerBase hb)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input = new InputSource(is);
        this.parse(input, hb);
    }
    public void parse(
        InputStream is,
        HandlerBase hb,
        String systemId)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input = new InputSource(is);
        input.setSystemId(systemId);
        this.parse(input, hb);
    }
    public void parse(InputStream is, DefaultHandler dh)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input = new InputSource(is);
        this.parse(input, dh);
    }
    public void parse(
        InputStream is,
        DefaultHandler dh,
        String systemId)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input = new InputSource(is);
        input.setSystemId(systemId);
        this.parse(input, dh);
    }
    public void parse(String uri, HandlerBase hb)
        throws SAXException, IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null");
        }
        InputSource input = new InputSource(uri);
        this.parse(input, hb);
    }
    public void parse(String uri, DefaultHandler dh)
        throws SAXException, IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null");
        }
        InputSource input = new InputSource(uri);
        this.parse(input, dh);
    }
    public void parse(File f, HandlerBase hb)
        throws SAXException, IOException {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        String escapedURI = FilePathToURI.filepath2URI(f.getAbsolutePath());
        if (DEBUG) {
            System.out.println("Escaped URI = " + escapedURI);
        }
        InputSource input = new InputSource(escapedURI);
        this.parse(input, hb);
    }
    public void parse(File f, DefaultHandler dh)
        throws SAXException, IOException {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        String escapedURI = FilePathToURI.filepath2URI(f.getAbsolutePath());
        if (DEBUG) {
            System.out.println("Escaped URI = " + escapedURI);
        }
        InputSource input = new InputSource(escapedURI);
        this.parse(input, dh);
    }
    public void parse(InputSource is, HandlerBase hb)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }
        Parser parser = this.getParser();
        if (hb != null) {
            parser.setDocumentHandler(hb);
            parser.setEntityResolver(hb);
            parser.setErrorHandler(hb);
            parser.setDTDHandler(hb);
        }
        parser.parse(is);
    }
    public void parse(InputSource is, DefaultHandler dh)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }
        XMLReader reader = this.getXMLReader();
        if (dh != null) {
            reader.setContentHandler(dh);
            reader.setEntityResolver(dh);
            reader.setErrorHandler(dh);
            reader.setDTDHandler(dh);
        }
        reader.parse(is);
    }
    public abstract org.xml.sax.Parser getParser() throws SAXException;
    public abstract org.xml.sax.XMLReader getXMLReader() throws SAXException;
    public abstract boolean isNamespaceAware();
    public abstract boolean isValidating();
    public abstract void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException;
    public abstract Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException;
    public Schema getSchema() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
}
