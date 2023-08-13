public class AttrImpl extends NodeImpl implements Attr {
    private String mName;
    private String mValue;
	protected AttrImpl(DocumentImpl owner, String name) {
		super(owner);
		mName = name;
	}
	public String getName() {
		return mName;
	}
	public Element getOwnerElement() {
		return null;
	}
	public boolean getSpecified() {
		return mValue != null;
	}
	public String getValue() {
		return mValue;
	}
	public void setValue(String value) throws DOMException {
		mValue = value;
	}
	@Override
	public String getNodeName() {
		return mName;
	}
	@Override
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}
	@Override
	public Node getParentNode() {
		return null;
	}
	@Override 
	public Node getPreviousSibling() {
		return null;
	}
	@Override
	public Node getNextSibling() {
		return null;
	}
	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
        setValue(nodeValue);
    }
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }
    public boolean isId() {
        return false;
    }
}
