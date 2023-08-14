public class SAXSource implements Source {
    public static final String FEATURE =
        "http:
    public SAXSource() { }
    public SAXSource(XMLReader reader, InputSource inputSource) {
        this.reader      = reader;
        this.inputSource = inputSource;
    }
    public SAXSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }
    public void setXMLReader(XMLReader reader) {
        this.reader = reader;
    }
    public XMLReader getXMLReader() {
        return reader;
    }
    public void setInputSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }
    public InputSource getInputSource() {
        return inputSource;
    }
    public void setSystemId(String systemId) {
        if (null == inputSource) {
            inputSource = new InputSource(systemId);
        } else {
            inputSource.setSystemId(systemId);
        }
    }
    public String getSystemId() {
        if (inputSource == null) {
            return null;
        } else {
            return inputSource.getSystemId();
        }
    }
    private XMLReader reader;
    private InputSource inputSource;
    public static InputSource sourceToInputSource(Source source) {
        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        } else if (source instanceof StreamSource) {
            StreamSource ss      = (StreamSource) source;
            InputSource  isource = new InputSource(ss.getSystemId());
            isource.setByteStream(ss.getInputStream());
            isource.setCharacterStream(ss.getReader());
            isource.setPublicId(ss.getPublicId());
            return isource;
        } else {
            return null;
        }
    }
}
