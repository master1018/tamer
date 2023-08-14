public class TagElement {
    Element elem;
    HTML.Tag htmlTag;
    boolean insertedByErrorRecovery;
    public TagElement ( Element elem ) {
        this(elem, false);
    }
    public TagElement (Element elem, boolean fictional) {
        this.elem = elem;
        htmlTag = HTML.getTag(elem.getName());
        if (htmlTag == null) {
            htmlTag = new HTML.UnknownTag(elem.getName());
        }
        insertedByErrorRecovery = fictional;
    }
    public boolean breaksFlow() {
        return htmlTag.breaksFlow();
    }
    public boolean isPreformatted() {
        return htmlTag.isPreformatted();
    }
    public Element getElement() {
        return elem;
    }
    public HTML.Tag getHTMLTag() {
        return htmlTag;
    }
    public boolean fictional() {
        return insertedByErrorRecovery;
    }
}
