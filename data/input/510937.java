public final class AndroidManifestDescriptors implements IDescriptorProvider {
    private static final String MANIFEST_NODE_NAME = "manifest";                
    private static final String ANDROID_MANIFEST_STYLEABLE = "AndroidManifest"; 
    public static final String ANDROID_LABEL_ATTR = "label";    
    public static final String ANDROID_NAME_ATTR  = "name";     
    public static final String PACKAGE_ATTR       = "package";  
    private final ElementDescriptor MANIFEST_ELEMENT;
    private final ElementDescriptor APPLICATION_ELEMENT;
    private final ElementDescriptor INTRUMENTATION_ELEMENT;
    private final ElementDescriptor PERMISSION_ELEMENT;
    private final ElementDescriptor USES_PERMISSION_ELEMENT;
    private final ElementDescriptor USES_SDK_ELEMENT;
    private final ElementDescriptor PERMISSION_GROUP_ELEMENT;
    private final ElementDescriptor PERMISSION_TREE_ELEMENT;
    private final TextAttributeDescriptor PACKAGE_ATTR_DESC;
    public AndroidManifestDescriptors() {
        APPLICATION_ELEMENT = createElement("application", null, true); 
        INTRUMENTATION_ELEMENT = createElement("instrumentation"); 
        PERMISSION_ELEMENT = createElement("permission"); 
        USES_PERMISSION_ELEMENT = createElement("uses-permission"); 
        USES_SDK_ELEMENT = createElement("uses-sdk", null, true); 
        PERMISSION_GROUP_ELEMENT = createElement("permission-group"); 
        PERMISSION_TREE_ELEMENT = createElement("permission-tree"); 
        MANIFEST_ELEMENT = createElement(
                        MANIFEST_NODE_NAME, 
                        new ElementDescriptor[] {
                                        APPLICATION_ELEMENT,
                                        INTRUMENTATION_ELEMENT,
                                        PERMISSION_ELEMENT,
                                        USES_PERMISSION_ELEMENT,
                                        PERMISSION_GROUP_ELEMENT,
                                        PERMISSION_TREE_ELEMENT,
                                        USES_SDK_ELEMENT,
                        },
                        true );
        PACKAGE_ATTR_DESC = new PackageAttributeDescriptor(PACKAGE_ATTR,
                "Package",
                null ,
                "This attribute gives a unique name for the package, using a Java-style naming convention to avoid name collisions.\nFor example, applications published by Google could have names of the form com.google.app.appname");
    }
    public ElementDescriptor[] getRootElementDescriptors() {
        return new ElementDescriptor[] { MANIFEST_ELEMENT };
    }
    public ElementDescriptor getDescriptor() {
        return getManifestElement();
    }
    public ElementDescriptor getApplicationElement() {
        return APPLICATION_ELEMENT;
    }
    public ElementDescriptor getManifestElement() {
        return MANIFEST_ELEMENT;
    }
    public ElementDescriptor getUsesSdkElement() {
        return USES_SDK_ELEMENT;
    }
    public ElementDescriptor getInstrumentationElement() {
        return INTRUMENTATION_ELEMENT;
    }
    public ElementDescriptor getPermissionElement() {
        return PERMISSION_ELEMENT;
    }
    public ElementDescriptor getUsesPermissionElement() {
        return USES_PERMISSION_ELEMENT;
    }
    public ElementDescriptor getPermissionGroupElement() {
        return PERMISSION_GROUP_ELEMENT;
    }
    public ElementDescriptor getPermissionTreeElement() {
        return PERMISSION_TREE_ELEMENT;
    }
    public synchronized void updateDescriptors(
            Map<String, DeclareStyleableInfo> manifestMap) {
        XmlnsAttributeDescriptor xmlns = new XmlnsAttributeDescriptor(
                "android", 
                SdkConstants.NS_RESOURCES);
        Set<String> required = new HashSet<String>();
        required.add("provider/authorities");  
        Map<String, Object> overrides = new HashMap<String, Object>();
        overrides.put("*/icon", new DescriptorsUtils.ITextAttributeCreator() { 
            public TextAttributeDescriptor create(String xmlName, String uiName, String nsUri,
                    String tooltip) {
                return new ReferenceAttributeDescriptor(
                        ResourceType.DRAWABLE,
                        xmlName, uiName, nsUri,
                        tooltip);
            }
        });
        overrides.put("*/theme",         ThemeAttributeDescriptor.class);   
        overrides.put("*/permission",    ListAttributeDescriptor.class);    
        overrides.put("*/targetPackage", ManifestPkgAttrDescriptor.class);  
        overrides.put("uses-library/name", ListAttributeDescriptor.class);       
        overrides.put("action,category,uses-permission/" + ANDROID_NAME_ATTR,    
                      ListAttributeDescriptor.class);
        overrides.put("application/" + ANDROID_NAME_ATTR, ApplicationAttributeDescriptor.class);  
        overrideClassName(overrides, "activity", AndroidConstants.CLASS_ACTIVITY);           
        overrideClassName(overrides, "receiver", AndroidConstants.CLASS_BROADCASTRECEIVER);  
        overrideClassName(overrides, "service", AndroidConstants.CLASS_SERVICE);             
        overrideClassName(overrides, "provider", AndroidConstants.CLASS_CONTENTPROVIDER);    
        overrideClassName(overrides, "instrumentation", AndroidConstants.CLASS_INSTRUMENTATION);    
        HashMap<String, ElementDescriptor> elementDescs =
            new HashMap<String, ElementDescriptor>();
        elementDescs.put(MANIFEST_ELEMENT.getXmlLocalName(),         MANIFEST_ELEMENT);
        elementDescs.put(APPLICATION_ELEMENT.getXmlLocalName(),      APPLICATION_ELEMENT);
        elementDescs.put(INTRUMENTATION_ELEMENT.getXmlLocalName(),   INTRUMENTATION_ELEMENT);
        elementDescs.put(PERMISSION_ELEMENT.getXmlLocalName(),       PERMISSION_ELEMENT);
        elementDescs.put(USES_PERMISSION_ELEMENT.getXmlLocalName(),  USES_PERMISSION_ELEMENT);
        elementDescs.put(USES_SDK_ELEMENT.getXmlLocalName(),         USES_SDK_ELEMENT);
        elementDescs.put(PERMISSION_GROUP_ELEMENT.getXmlLocalName(), PERMISSION_GROUP_ELEMENT);
        elementDescs.put(PERMISSION_TREE_ELEMENT.getXmlLocalName(),  PERMISSION_TREE_ELEMENT);
        inflateElement(manifestMap,
                overrides,
                required,
                elementDescs,
                MANIFEST_ELEMENT,
                "AndroidManifest"); 
        insertAttribute(MANIFEST_ELEMENT, PACKAGE_ATTR_DESC);
        sanityCheck(manifestMap, MANIFEST_ELEMENT);
    }
    private static void overrideClassName(Map<String, Object> overrides,
            String elementName, final String className) {
        overrides.put(elementName + "/" + ANDROID_NAME_ATTR,
                new DescriptorsUtils.ITextAttributeCreator() {
            public TextAttributeDescriptor create(String xmlName, String uiName, String nsUri,
                    String tooltip) {
                uiName += "*";  
                if (AndroidConstants.CLASS_ACTIVITY.equals(className)) {
                    return new ClassAttributeDescriptor(
                            className,
                            PostActivityCreationAction.getAction(),
                            xmlName,
                            uiName,
                            nsUri,
                            tooltip,
                            true ,
                            true );
                } else if (AndroidConstants.CLASS_BROADCASTRECEIVER.equals(className)) {
                    return new ClassAttributeDescriptor(
                            className,
                            PostReceiverCreationAction.getAction(),
                            xmlName,
                            uiName,
                            nsUri,
                            tooltip,
                            true ,
                            true );
                } else if (AndroidConstants.CLASS_INSTRUMENTATION.equals(className)) {
                    return new ClassAttributeDescriptor(
                            className,
                            null, 
                            xmlName,
                            uiName,
                            nsUri,
                            tooltip,
                            true ,
                            false );
                } else {
                    return new ClassAttributeDescriptor(
                            className,
                            xmlName,
                            uiName,
                            nsUri,
                            tooltip,
                            true );
                }
            }
        });
    }
    private ElementDescriptor createElement(
            String xmlName,
            ElementDescriptor[] childrenElements,
            boolean mandatory) {
        String styleName = guessStyleName(xmlName);
        String sdkUrl = DescriptorsUtils.MANIFEST_SDK_URL + styleName;
        String uiName = getUiName(xmlName);
        ElementDescriptor element = new ManifestElementDescriptor(xmlName, uiName, null, sdkUrl,
                null, childrenElements, mandatory);
        return element;
    }
    private ElementDescriptor createElement(String xmlName) {
        return createElement(xmlName, null, false);
    }
    private void insertAttribute(ElementDescriptor element, AttributeDescriptor newAttr) {
        AttributeDescriptor[] attributes = element.getAttributes();
        for (AttributeDescriptor attr : attributes) {
            if (attr.getXmlLocalName().equals(newAttr.getXmlLocalName())) {
                return;
            }
        }
        AttributeDescriptor[] newArray = new AttributeDescriptor[attributes.length + 1];
        newArray[0] = newAttr;
        System.arraycopy(attributes, 0, newArray, 1, attributes.length);
        element.setAttributes(newArray);
    }
    private void inflateElement(
            Map<String, DeclareStyleableInfo> styleMap,
            Map<String, Object> overrides,
            Set<String> requiredAttributes,
            HashMap<String, ElementDescriptor> existingElementDescs,
            ElementDescriptor elemDesc,
            String styleName) {
        assert elemDesc != null;
        assert styleName != null;
        DeclareStyleableInfo style = styleMap != null ? styleMap.get(styleName) : null;
        if (style != null) {
            ArrayList<AttributeDescriptor> attrDescs = new ArrayList<AttributeDescriptor>();
            DescriptorsUtils.appendAttributes(attrDescs,
                    elemDesc.getXmlLocalName(),
                    SdkConstants.NS_RESOURCES,
                    style.getAttributes(),
                    requiredAttributes,
                    overrides);
            elemDesc.setTooltip(style.getJavaDoc());
            elemDesc.setAttributes(attrDescs.toArray(new AttributeDescriptor[attrDescs.size()]));
        }
        ArrayList<ElementDescriptor> children = new ArrayList<ElementDescriptor>();
        for (Entry<String, DeclareStyleableInfo> entry : styleMap.entrySet()) {
            DeclareStyleableInfo childStyle = entry.getValue();
            boolean isParent = false;
            String[] parents = childStyle.getParents();
            if (parents != null) {
                for (String parent: parents) {
                    if (styleName.equals(parent)) {
                        isParent = true;
                        break;
                    }
                }
            }
            if (isParent) {
                String childStyleName = entry.getKey();
                String childXmlName = guessXmlName(childStyleName);
                ElementDescriptor child = existingElementDescs.get(childXmlName);
                if (child == null) {
                    child = createElement(childXmlName);
                    existingElementDescs.put(childXmlName, child);
                }
                children.add(child);
                inflateElement(styleMap,
                        overrides,
                        requiredAttributes,
                        existingElementDescs,
                        child,
                        childStyleName);
            }
        }
        elemDesc.setChildren(children.toArray(new ElementDescriptor[children.size()]));
    }
    private String getUiName(String xmlName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : xmlName.toCharArray()) {
            if (capitalize && c >= 'a' && c <= 'z') {
                sb.append((char)(c + 'A' - 'a'));
                capitalize = false;
            } else if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
                sb.append(' ');
                capitalize = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private String guessStyleName(String xmlName) {
        StringBuilder sb = new StringBuilder();
        if (!xmlName.equals(MANIFEST_NODE_NAME)) {
            boolean capitalize = true;
            for (char c : xmlName.toCharArray()) {
                if (capitalize && c >= 'a' && c <= 'z') {
                    sb.append((char)(c + 'A' - 'a'));
                    capitalize = false;
                } else if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
                    capitalize = true;
                } else {
                    sb.append(c);
                }
            }
        }
        sb.insert(0, ANDROID_MANIFEST_STYLEABLE);
        return sb.toString();
    }
    private void sanityCheck(Map<String, DeclareStyleableInfo> manifestMap,
            ElementDescriptor manifestElement) {
        TreeSet<String> elementsDeclared = new TreeSet<String>();
        findAllElementNames(manifestElement, elementsDeclared);
        TreeSet<String> stylesDeclared = new TreeSet<String>();
        for (String styleName : manifestMap.keySet()) {
            if (styleName.startsWith(ANDROID_MANIFEST_STYLEABLE)) {
                stylesDeclared.add(styleName);
            }
        }
        for (Iterator<String> it = elementsDeclared.iterator(); it.hasNext();) {
            String xmlName = it.next();
            String styleName = guessStyleName(xmlName);
            if (stylesDeclared.remove(styleName)) {
                it.remove();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (!stylesDeclared.isEmpty()) {
            sb.append("Warning, ADT/SDK Mismatch! The following elements are declared by the SDK but unknown to ADT: ");
            for (String name : stylesDeclared) {
                name = guessXmlName(name);
                if (name != stylesDeclared.last()) {
                    sb.append(", ");    
                }
            }
            AdtPlugin.log(IStatus.WARNING, "%s", sb.toString());
            AdtPlugin.printToConsole((String)null, sb);
            sb.setLength(0);
        }
        if (!elementsDeclared.isEmpty()) {
            sb.append("Warning, ADT/SDK Mismatch! The following elements are declared by ADT but not by the SDK: ");
            for (String name : elementsDeclared) {
                sb.append(name);
                if (name != elementsDeclared.last()) {
                    sb.append(", ");    
                }
            }
            AdtPlugin.log(IStatus.WARNING, "%s", sb.toString());
            AdtPlugin.printToConsole((String)null, sb);
        }
    }
    private String guessXmlName(String name) {
        StringBuilder sb = new StringBuilder();
        if (ANDROID_MANIFEST_STYLEABLE.equals(name)) {
            sb.append(MANIFEST_NODE_NAME);
        } else {
            name = name.replace(ANDROID_MANIFEST_STYLEABLE, "");    
            boolean first_char = true;
            for (char c : name.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    if (!first_char) {
                        sb.append('-');
                    }
                    c = (char) (c - 'A' + 'a');
                }
                sb.append(c);
                first_char = false;
            }
        }
        return sb.toString();
    }
    private void findAllElementNames(ElementDescriptor element, TreeSet<String> declared) {
        declared.add(element.getXmlName());
        for (ElementDescriptor desc : element.getChildren()) {
            findAllElementNames(desc, declared);
        }
    }
}
