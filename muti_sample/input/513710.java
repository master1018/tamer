public class UiItemElementNode extends UiElementNode {
    public UiItemElementNode(ItemElementDescriptor elementDescriptor) {
        super(elementDescriptor);
    }
    @Override
    public String getShortDescription() {
        Node xmlNode = getXmlNode();
        if (xmlNode != null && xmlNode instanceof Element && xmlNode.hasAttributes()) {
            Element elem = (Element) xmlNode;
            String type = elem.getAttribute(ResourcesDescriptors.TYPE_ATTR);
            String name = elem.getAttribute(ResourcesDescriptors.NAME_ATTR);
            if (type != null && name != null && type.length() > 0 && name.length() > 0) {
                type = DescriptorsUtils.capitalize(type);
                return String.format("%1$s (%2$s %3$s)", name, type, getDescriptor().getUiName());
            }
        }
        return super.getShortDescription();
    }
}
