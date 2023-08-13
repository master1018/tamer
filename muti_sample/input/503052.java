public class Document extends Node {
    protected int rootIndex = -1;
    String encoding;
    Boolean standalone;
    public String getEncoding () {
        return encoding;
    }
    public void setEncoding(String enc) {
        this.encoding = enc;
    }
    public void setStandalone (Boolean standalone) {
        this.standalone = standalone;
    }
    public Boolean getStandalone() {
        return standalone;
    }
    public String getName() {
        return "#document";
    }
    public void addChild(int index, int type, Object child) {
        if (type == ELEMENT) {
            rootIndex = index;
        }
        else if (rootIndex >= index)
            rootIndex++;
        super.addChild(index, type, child);
    }
    public void parse(XmlPullParser parser)
        throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_DOCUMENT, null, null);
        parser.nextToken ();            
        encoding = parser.getInputEncoding();
        standalone = (Boolean)parser.getProperty ("http:
        super.parse(parser);
        if (parser.getEventType() != XmlPullParser.END_DOCUMENT)
            throw new RuntimeException("Document end expected!");
    }
    public void removeChild(int index) {
        if (index == rootIndex)
            rootIndex = -1;
        else if (index < rootIndex)
            rootIndex--;
        super.removeChild(index);
    }
    public Element getRootElement() {
        if (rootIndex == -1)
            throw new RuntimeException("Document has no root element!");
        return (Element) getChild(rootIndex);
    }
    public void write(XmlSerializer writer)
        throws IOException {
        writer.startDocument(encoding, standalone);
        writeChildren(writer);
        writer.endDocument();
    }
}