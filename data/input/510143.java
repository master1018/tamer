public abstract class DocumentBuilder {
    private static final boolean DEBUG = false;
    protected DocumentBuilder () {
    }
	public void reset() {
		throw new UnsupportedOperationException(
			"This DocumentBuilder, \"" + this.getClass().getName() + "\", does not support the reset functionality."
			+ "  Specification \"" + this.getClass().getPackage().getSpecificationTitle() + "\""
			+ " version \"" + this.getClass().getPackage().getSpecificationVersion() + "\""
			);
	}
    public Document parse(InputStream is)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource in = new InputSource(is);
        return parse(in);
    }
    public Document parse(InputStream is, String systemId)
        throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource in = new InputSource(is);
        in.setSystemId(systemId);
        return parse(in);
    }
    public Document parse(String uri)
        throws SAXException, IOException {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        InputSource in = new InputSource(uri);
        return parse(in);
    }
    public Document parse(File f) throws SAXException, IOException {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        String escapedURI = FilePathToURI.filepath2URI(f.getAbsolutePath());
        if (DEBUG) {
            System.out.println("Escaped URI = " + escapedURI);
        }
        InputSource in = new InputSource(escapedURI);
        return parse(in);
    }
    public abstract Document parse(InputSource is)
        throws  SAXException, IOException;
    public abstract boolean isNamespaceAware();
    public abstract boolean isValidating();
    public abstract void setEntityResolver(EntityResolver er);
    public abstract void setErrorHandler(ErrorHandler eh);
    public abstract Document newDocument();
    public abstract DOMImplementation getDOMImplementation();
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
