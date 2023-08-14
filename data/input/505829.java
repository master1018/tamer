public final class UiManifestElementNode extends UiElementNode {
    public UiManifestElementNode(ManifestElementDescriptor elementDescriptor) {
        super(elementDescriptor);
    }
    @Override
    public String getShortDescription() {
        AndroidTargetData target = getAndroidTarget();
        AndroidManifestDescriptors manifestDescriptors = null;
        if (target != null) {
            manifestDescriptors = target.getManifestDescriptors();
        }
        if (manifestDescriptors != null &&
                getXmlNode() != null &&
                getXmlNode() instanceof Element &&
                getXmlNode().hasAttributes()) {
            ElementDescriptor desc = getDescriptor();
            if (desc != manifestDescriptors.getManifestElement() &&
                    desc != manifestDescriptors.getApplicationElement()) {
                Element elem = (Element) getXmlNode();
                String attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                                  AndroidManifestDescriptors.ANDROID_NAME_ATTR);
                if (attr == null || attr.length() == 0) {
                    attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                               AndroidManifestDescriptors.ANDROID_LABEL_ATTR);
                }
                if (attr != null && attr.length() > 0) {
                    return String.format("%1$s (%2$s)", attr, getDescriptor().getUiName());
                }
            }
        }
        return String.format("%1$s", getDescriptor().getUiName());
    }
}
