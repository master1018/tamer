public final class MenuDescriptors implements IDescriptorProvider {
    public static final String MENU_ROOT_ELEMENT = "menu"; 
    private ElementDescriptor mDescriptor = null;
    public ElementDescriptor getDescriptor() {
        return mDescriptor;
    }
    public ElementDescriptor[] getRootElementDescriptors() {
        return mDescriptor.getChildren();
    }
    public synchronized void updateDescriptors(Map<String, DeclareStyleableInfo> styleMap) {
        if (mDescriptor == null) {
            mDescriptor = createElement(styleMap,
                MENU_ROOT_ELEMENT, 
                "Menu", 
                null, 
                null, 
                null, 
                true );
        }
        ElementDescriptor sub_item = createElement(styleMap,
                "item", 
                "Item", 
                null, 
                null, 
                null, 
                false );
        ElementDescriptor sub_group = createElement(styleMap,
                "group", 
                "Group", 
                null, 
                null, 
                new ElementDescriptor[] { sub_item }, 
                false );
        ElementDescriptor sub_menu = createElement(styleMap,
                MENU_ROOT_ELEMENT, 
                "Sub-Menu", 
                null, 
                null, 
                new ElementDescriptor[] { sub_item, sub_group }, 
                true );
        ElementDescriptor top_item = createElement(styleMap,
                "item", 
                "Item", 
                null, 
                null, 
                new ElementDescriptor[] { sub_menu }, 
                false );
        ElementDescriptor top_group = createElement(styleMap,
                "group", 
                "Group", 
                null, 
                null, 
                new ElementDescriptor[] { top_item }, 
                false );
        XmlnsAttributeDescriptor xmlns = new XmlnsAttributeDescriptor("android", 
                SdkConstants.NS_RESOURCES); 
        updateElement(mDescriptor, styleMap, "Menu", xmlns); 
        mDescriptor.setChildren(new ElementDescriptor[] { top_item, top_group });
    }
    private ElementDescriptor createElement(
            Map<String, DeclareStyleableInfo> styleMap, 
            String xmlName, String uiName, String sdkUrl,
            AttributeDescriptor extraAttribute,
            ElementDescriptor[] childrenElements, boolean mandatory) {
        ElementDescriptor element = new ElementDescriptor(xmlName, uiName, null, sdkUrl,
                null, childrenElements, mandatory);
        return updateElement(element, styleMap,
                getStyleName(xmlName),
                extraAttribute);
    }
    private ElementDescriptor updateElement(ElementDescriptor element,
            Map<String, DeclareStyleableInfo> styleMap,
            String styleName,
            AttributeDescriptor extraAttribute) {
        ArrayList<AttributeDescriptor> descs = new ArrayList<AttributeDescriptor>();
        DeclareStyleableInfo style = styleMap != null ? styleMap.get(styleName) : null;
        if (style != null) {
            DescriptorsUtils.appendAttributes(descs,
                    null,   
                    SdkConstants.NS_RESOURCES,
                    style.getAttributes(),
                    null,   
                    null);  
            element.setTooltip(style.getJavaDoc());
        }
        if (extraAttribute != null) {
            descs.add(extraAttribute);
        }
        element.setAttributes(descs.toArray(new AttributeDescriptor[descs.size()]));
        return element;
    }
    private String getStyleName(String xmlName) {
        String styleName = DescriptorsUtils.capitalize(xmlName);
        final String MENU_STYLE_BASE_NAME = "Menu"; 
        if (!styleName.equals(MENU_STYLE_BASE_NAME)) {        
            styleName = MENU_STYLE_BASE_NAME + styleName;
        }
        return styleName;
    }
}
