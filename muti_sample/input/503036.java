public class TextImpl extends CharacterDataImpl implements Text {
    public TextImpl(DocumentImpl document, String data) {
        super(document, data);
    }
    @Override
    public String getNodeName() {
        return "#text";
    }
    @Override
    public short getNodeType() {
        return Node.TEXT_NODE;
    }
    public final Text splitText(int offset) throws DOMException {
        Text newText = document.createTextNode(
                substringData(offset, getLength() - offset));
        deleteData(0, offset);
        Node refNode = getNextSibling();
        if (refNode == null) {
            getParentNode().appendChild(newText);
        } else {
            getParentNode().insertBefore(newText, refNode);
        }
        return this;
    }
    public final boolean isElementContentWhitespace() {
        return false;
    }
    public final String getWholeText() {
        StringBuilder result = new StringBuilder();
        for (TextImpl n = firstTextNodeInCurrentRun(); n != null; n = n.nextTextNode()) {
            n.appendDataTo(result);
        }
        return result.toString();
    }
    public final Text replaceWholeText(String content) throws DOMException {
        Node parent = getParentNode();
        Text result = null;
        for (TextImpl n = firstTextNodeInCurrentRun(); n != null; ) {
            if (n == this && content != null && content.length() > 0) {
                setData(content);
                result = this;
                n = n.nextTextNode();
            } else {
                Node toRemove = n; 
                n = n.nextTextNode();
                parent.removeChild(toRemove);
            }
        }
        return result;
    }
    private TextImpl firstTextNodeInCurrentRun() {
        TextImpl firstTextInCurrentRun = this;
        for (Node p = getPreviousSibling(); p != null; p = p.getPreviousSibling()) {
            short nodeType = p.getNodeType();
            if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                firstTextInCurrentRun = (TextImpl) p;
            } else {
                break;
            }
        }
        return firstTextInCurrentRun;
    }
    private TextImpl nextTextNode() {
        Node nextSibling = getNextSibling();
        if (nextSibling == null) {
            return null;
        }
        short nodeType = nextSibling.getNodeType();
        return nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE
                ? (TextImpl) nextSibling
                : null;
    }
    public final TextImpl minimize() {
        if (getLength() == 0) {
            parent.removeChild(this);
            return null;
        }
        Node previous = getPreviousSibling();
        if (previous == null || previous.getNodeType() != Node.TEXT_NODE) {
            return this;
        }
        TextImpl previousText = (TextImpl) previous;
        previousText.buffer.append(buffer);
        parent.removeChild(this);
        return previousText;
    }
}
