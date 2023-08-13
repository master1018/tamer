public class RootElement extends Element {
    final Handler handler = new Handler();
    public RootElement(String uri, String localName) {
        super(null, uri, localName, 0);
    }
    public RootElement(String localName) {
        this("", localName);
    }
    public ContentHandler getContentHandler() {
        return this.handler;
    }
    class Handler extends DefaultHandler {
        Locator locator;
        int depth = -1;
        Element current = null;
        StringBuilder bodyBuilder = null;
        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            int depth = ++this.depth;
            if (depth == 0) {
                startRoot(uri, localName, attributes);
                return;
            }
            if (bodyBuilder != null) {
                throw new BadXmlException("Encountered mixed content"
                        + " within text element named " + current + ".",
                        locator);
            }
            if (depth == current.depth + 1) {
                Children children = current.children;
                if (children != null) {
                    Element child = children.get(uri, localName);
                    if (child != null) {
                        start(child, attributes);
                    }
                }
            }
        }
        void startRoot(String uri, String localName, Attributes attributes)
                throws SAXException {
            Element root = RootElement.this;
            if (root.uri.compareTo(uri) != 0
                    || root.localName.compareTo(localName) != 0) {
                throw new BadXmlException("Root element name does"
                        + " not match. Expected: " + root + ", Got: "
                        + Element.toString(uri, localName), locator);
            }
            start(root, attributes);
        }
        void start(Element e, Attributes attributes) {
            this.current = e;
            if (e.startElementListener != null) {
                e.startElementListener.start(attributes);
            }
            if (e.endTextElementListener != null) {
                this.bodyBuilder = new StringBuilder();
            }
            e.resetRequiredChildren();
            e.visited = true;
        }
        @Override
        public void characters(char[] buffer, int start, int length)
                throws SAXException {
            if (bodyBuilder != null) {
                bodyBuilder.append(buffer, start, length);
            }
        }
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            Element current = this.current;
            if (depth == current.depth) {
                current.checkRequiredChildren(locator);
                if (current.endElementListener != null) {
                    current.endElementListener.end();
                }
                if (bodyBuilder != null) {
                    String body = bodyBuilder.toString();
                    bodyBuilder = null;
                    current.endTextElementListener.end(body);
                }
                this.current = current.parent;
            }
            depth--;
        }
    }
}
