public class SmilElementImpl extends ElementImpl implements SMILElement {
    SmilElementImpl(SmilDocumentImpl owner, String tagName)
    {
        super(owner, tagName.toLowerCase());
    }
    public String getId() {
        return null;
    }
    public void setId(String id) throws DOMException {
    }
}
