public final class CDATASectionImpl extends TextImpl implements CDATASection {
    public CDATASectionImpl(DocumentImpl document, String data) {
        super(document, data);
    }
    @Override
    public String getNodeName() {
        return "#cdata-section";
    }
    @Override
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }
    public void split() {
        if (!needsSplitting()) {
            return;
        }
        Node parent = getParentNode();
        String[] parts = getData().split("\\]\\]>");
        parent.insertBefore(new CDATASectionImpl(document, parts[0] + "]]"), this);
        for (int p = 1; p < parts.length - 1; p++) {
            parent.insertBefore(new CDATASectionImpl(document, ">" + parts[p] + "]]"), this);
        }
        setData(">" + parts[parts.length - 1]);
    }
    public boolean needsSplitting() {
        return buffer.indexOf("]]>") != -1;
    }
    public TextImpl replaceWithText() {
        TextImpl replacement = new TextImpl(document, getData());
        parent.insertBefore(replacement, this);
        parent.removeChild(this);
        return replacement;
    }
}
