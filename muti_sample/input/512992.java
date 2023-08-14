public final class FirstElementParser {
    private static SAXParserFactory sSaxfactory;
    public static final class Result {
        private String mElement;
        private String mXmlnsPrefix;
        private String mXmlnsUri;
        public String getElement() {
            return mElement;
        }
        public String getXmlnsPrefix() {
            return mXmlnsPrefix;
        }
        public String getXmlnsUri() {
            return mXmlnsUri;
        }
        void setElement(String element) {
            mElement = element;
        }
        void setXmlnsPrefix(String xmlnsPrefix) {
            mXmlnsPrefix = xmlnsPrefix;
        }
        void setXmlnsUri(String xmlnsUri) {
            mXmlnsUri = xmlnsUri;
        }
    }
    private static class ResultFoundException extends SAXException { }
    public static Result parse(String osFilename, String xmlnsUri) {
        if (sSaxfactory == null) {
            sSaxfactory = SAXParserFactory.newInstance();
            sSaxfactory.setNamespaceAware(true);
        }
        Result result = new Result();
        if (xmlnsUri != null && xmlnsUri.length() > 0) {
            result.setXmlnsUri(xmlnsUri);
        }
        try {
            SAXParser parser = sSaxfactory.newSAXParser();
            XmlHandler handler = new XmlHandler(result);
            parser.parse(new InputSource(new FileReader(osFilename)), handler);
        } catch(ResultFoundException e) {
            return result;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }
    private FirstElementParser() {
    }
    private static class XmlHandler extends DefaultHandler {
        private final Result mResult;
        public XmlHandler(Result result) {
            mResult = result;
        }
        @Override
        public void startPrefixMapping(String prefix, String uri) {
            if (uri.equals(mResult.getXmlnsUri())) {
                mResult.setXmlnsPrefix(prefix);
            }
        }
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            mResult.setElement(localName);
            throw new ResultFoundException();
        }
    }
}
