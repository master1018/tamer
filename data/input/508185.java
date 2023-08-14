public class UiElementNode implements IPropertySource {
    private static String[] ID_PREFIXES = {
        "@android:id/", 
        "@+id/", "@id/", "@+", "@" }; 
    private ElementDescriptor mDescriptor;
    private UiElementNode mUiParent;
    private AndroidEditor mEditor;
    private Document mXmlDocument;
    private Node mXmlNode;
    private ArrayList<UiElementNode> mUiChildren;
    private HashMap<AttributeDescriptor, UiAttributeNode> mUiAttributes;
    private HashSet<UiAttributeNode> mUnknownUiAttributes;
    private List<UiElementNode> mReadOnlyUiChildren;
    private Collection<UiAttributeNode> mReadOnlyUiAttributes;
    private Map<String, AttributeDescriptor> mCachedHiddenAttributes;
    private ArrayList<IUiUpdateListener> mUiUpdateListeners;
    private IUnknownDescriptorProvider mUnknownDescProvider;
    private boolean mHasError;
    private Object mEditData;
    public UiElementNode(ElementDescriptor elementDescriptor) {
        mDescriptor = elementDescriptor;
        clearContent();
    }
     void clearContent() {
        mXmlNode = null;
        mXmlDocument = null;
        mEditor = null;
        clearAttributes();
        mReadOnlyUiChildren = null;
        if (mUiChildren == null) {
            mUiChildren = new ArrayList<UiElementNode>();
        } else {
            for (int i = mUiChildren.size() - 1; i >= 0; --i) {
                removeUiChildAtIndex(i);
            }
        }
    }
    private void clearAttributes() {
        mUiAttributes = null;
        mReadOnlyUiAttributes = null;
        mCachedHiddenAttributes = null;
        mUnknownUiAttributes = new HashSet<UiAttributeNode>();
    }
    private HashMap<AttributeDescriptor, UiAttributeNode> getInternalUiAttributes() {
        if (mUiAttributes == null) {
            AttributeDescriptor[] attr_list = getAttributeDescriptors();
            mUiAttributes = new HashMap<AttributeDescriptor, UiAttributeNode>(attr_list.length);
            for (AttributeDescriptor desc : attr_list) {
                UiAttributeNode ui_node = desc.createUiNode(this);
                if (ui_node != null) {  
                    mUiAttributes.put(desc, ui_node);
                }
            }
        }
        return mUiAttributes;
    }
    public String getShortDescription() {
        if (mXmlNode != null && mXmlNode instanceof Element && mXmlNode.hasAttributes()) {
            Element elem = (Element) mXmlNode;
            String attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                              AndroidManifestDescriptors.ANDROID_NAME_ATTR);
            if (attr == null || attr.length() == 0) {
                attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                           AndroidManifestDescriptors.ANDROID_LABEL_ATTR);
            }
            if (attr == null || attr.length() == 0) {
                attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                           XmlDescriptors.PREF_KEY_ATTR);
            }
            if (attr == null || attr.length() == 0) {
                attr = elem.getAttribute(ResourcesDescriptors.NAME_ATTR);
            }
            if (attr == null || attr.length() == 0) {
                attr = elem.getAttributeNS(SdkConstants.NS_RESOURCES,
                                           LayoutDescriptors.ID_ATTR);
                if (attr != null && attr.length() > 0) {
                    for (String prefix : ID_PREFIXES) {
                        if (attr.startsWith(prefix)) {
                            attr = attr.substring(prefix.length());
                            break;
                        }
                    }
                }
            }
            if (attr != null && attr.length() > 0) {
                return String.format("%1$s (%2$s)", attr, mDescriptor.getUiName());
            }
        }
        return String.format("%1$s", mDescriptor.getUiName());
    }
    public String getBreadcrumbTrailDescription(boolean include_root) {
        StringBuilder sb = new StringBuilder(getShortDescription());
        for (UiElementNode ui_node = getUiParent();
                ui_node != null;
                ui_node = ui_node.getUiParent()) {
            if (!include_root && ui_node.getUiParent() == null) {
                break;
            }
            sb.insert(0, String.format("%1$s > ", ui_node.getShortDescription())); 
        }
        return sb.toString();
    }
    public void setXmlDocument(Document xml_doc) {
        if (mUiParent == null) {
            mXmlDocument = xml_doc;
        } else {
            mUiParent.setXmlDocument(xml_doc);
        }
    }
    public Document getXmlDocument() {
        if (mXmlDocument != null) {
            return mXmlDocument;
        } else if (mUiParent != null) {
            return mUiParent.getXmlDocument();
        }
        return null;
    }
    public Node getXmlNode() {
        return mXmlNode;
    }
    public ElementDescriptor getDescriptor() {
        return mDescriptor;
    }
    public AttributeDescriptor[] getAttributeDescriptors() {
        return mDescriptor.getAttributes();
    }
    private Map<String, AttributeDescriptor> getHiddenAttributeDescriptors() {
        if (mCachedHiddenAttributes == null) {
            mCachedHiddenAttributes = new HashMap<String, AttributeDescriptor>();
            for (AttributeDescriptor attr_desc : getAttributeDescriptors()) {
                if (attr_desc instanceof XmlnsAttributeDescriptor) {
                    mCachedHiddenAttributes.put(
                            ((XmlnsAttributeDescriptor) attr_desc).getXmlNsName(),
                            attr_desc);
                }
            }
        }
        return mCachedHiddenAttributes;
    }
    protected void setUiParent(UiElementNode parent) {
        mUiParent = parent;
        clearAttributes();
    }
    public UiElementNode getUiParent() {
        return mUiParent;
    }
    public UiElementNode getUiRoot() {
        UiElementNode root = this;
        while (root.mUiParent != null) {
            root = root.mUiParent;
        }
        return root;
    }
    public UiElementNode getUiPreviousSibling() {
        if (mUiParent != null) {
            List<UiElementNode> childlist = mUiParent.getUiChildren();
            if (childlist != null && childlist.size() > 1 && childlist.get(0) != this) {
                int index = childlist.indexOf(this);
                return index > 0 ? childlist.get(index - 1) : null;
            }
        }
        return null;
    }
    public UiElementNode getUiNextSibling() {
        if (mUiParent != null) {
            List<UiElementNode> childlist = mUiParent.getUiChildren();
            if (childlist != null) {
                int size = childlist.size();
                if (size > 1 && childlist.get(size - 1) != this) {
                    int index = childlist.indexOf(this);
                    return index >= 0 && index < size - 1 ? childlist.get(index + 1) : null;
                }
            }
        }
        return null;
    }
    public void setEditor(AndroidEditor editor) {
        if (mUiParent == null) {
            mEditor = editor;
        } else {
            mUiParent.setEditor(editor);
        }
    }
    public AndroidEditor getEditor() {
        return mUiParent == null ? mEditor : mUiParent.getEditor();
    }
    public AndroidTargetData getAndroidTarget() {
        return getEditor().getTargetData();
    }
    public List<UiElementNode> getUiChildren() {
        if (mReadOnlyUiChildren == null) {
            mReadOnlyUiChildren = Collections.unmodifiableList(mUiChildren);
        }
        return mReadOnlyUiChildren;
    }
    public Collection<UiAttributeNode> getUiAttributes() {
        if (mReadOnlyUiAttributes == null) {
            mReadOnlyUiAttributes = Collections.unmodifiableCollection(
                    getInternalUiAttributes().values());
        }
        return mReadOnlyUiAttributes;
    }
    public Collection<UiAttributeNode> getUnknownUiAttributes() {
        return Collections.unmodifiableCollection(mUnknownUiAttributes);
    }
    public final void setHasError(boolean errorFlag) {
        mHasError = errorFlag;
    }
    public final boolean hasError() {
        if (mHasError) {
            return true;
        }
        Collection<UiAttributeNode> attributes = getInternalUiAttributes().values();
        for (UiAttributeNode attribute : attributes) {
            if (attribute.hasError()) {
                return true;
            }
        }
        for (UiElementNode child : mUiChildren) {
            if (child.hasError()) {
                return true;
            }
        }
        return false;
    }
    public IUnknownDescriptorProvider getUnknownDescriptorProvider() {
        if (mUiParent != null) {
            return mUiParent.getUnknownDescriptorProvider();
        }
        if (mUnknownDescProvider == null) {
            mUnknownDescProvider = new IUnknownDescriptorProvider() {
                private final HashMap<String, ElementDescriptor> mMap =
                    new HashMap<String, ElementDescriptor>();
                public ElementDescriptor getDescriptor(String xmlLocalName) {
                    ElementDescriptor desc = mMap.get(xmlLocalName);
                    if (desc == null) {
                        desc = new ElementDescriptor(xmlLocalName);
                        mMap.put(xmlLocalName, desc);
                    }
                    return desc;
                }
            };
        }
        return mUnknownDescProvider;
    }
    public void setUnknownDescriptorProvider(IUnknownDescriptorProvider unknownDescProvider) {
        if (mUiParent == null) {
            mUnknownDescProvider = unknownDescProvider;
        } else {
            mUiParent.setUnknownDescriptorProvider(unknownDescProvider);
        }
    }
    public void addUpdateListener(IUiUpdateListener listener) {
       if (mUiUpdateListeners == null) {
           mUiUpdateListeners = new ArrayList<IUiUpdateListener>();
       }
       if (!mUiUpdateListeners.contains(listener)) {
           mUiUpdateListeners.add(listener);
       }
    }
    public void removeUpdateListener(IUiUpdateListener listener) {
       if (mUiUpdateListeners != null) {
           mUiUpdateListeners.remove(listener);
       }
    }
    public UiElementNode findUiChildNode(String path) {
        String[] items = path.split("/");  
        UiElementNode ui_node = this;
        for (String item : items) {
            boolean next_segment = false;
            for (UiElementNode c : ui_node.mUiChildren) {
                if (c.getDescriptor().getXmlName().equals(item)) {
                    ui_node = c;
                    next_segment = true;
                    break;
                }
            }
            if (!next_segment) {
                return null;
            }
        }
        return ui_node;
    }
    public UiElementNode findXmlNode(Node xmlNode) {
        if (xmlNode == null) {
            return null;
        }
        if (getXmlNode() == xmlNode) {
            return this;
        }
        for (UiElementNode uiChild : mUiChildren) {
            UiElementNode found = uiChild.findXmlNode(xmlNode);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    public UiAttributeNode findUiAttribute(AttributeDescriptor attr_desc) {
        return getInternalUiAttributes().get(attr_desc);
    }
    public boolean loadFromXmlNode(Node xml_node) {
        boolean structure_changed = (mXmlNode != xml_node);
        mXmlNode = xml_node;
        if (xml_node != null) {
            updateAttributeList(xml_node);
            structure_changed |= updateElementList(xml_node);
            invokeUiUpdateListeners(structure_changed ? UiUpdateState.CHILDREN_CHANGED
                                                      : UiUpdateState.ATTR_UPDATED);
        }
        return structure_changed;
    }
    public void reloadFromXmlNode(Node xml_node) {
        AndroidEditor editor = getEditor();
        clearContent();
        setEditor(editor);
        if (xml_node != null) {
            setXmlDocument(xml_node.getOwnerDocument());
        }
        loadFromXmlNode(xml_node);
    }
    public Node prepareCommit() {
        if (getDescriptor().isMandatory()) {
            createXmlNode();
        }
        return getXmlNode();
    }
    public void commit() {
        for (UiAttributeNode ui_attr : getInternalUiAttributes().values()) {
            ui_attr.commit();
        }
        for (UiAttributeNode ui_attr : mUnknownUiAttributes) {
            ui_attr.commit();
        }
    }
    public boolean isDirty() {
        for (UiAttributeNode ui_attr : getInternalUiAttributes().values()) {
            if (ui_attr.isDirty()) {
                return true;
            }
        }
        for (UiAttributeNode ui_attr : mUnknownUiAttributes) {
            if (ui_attr.isDirty()) {
                return true;
            }
        }
        return false;
    }
    public Node createXmlNode() {
        if (mXmlNode != null) {
            return null;
        }
        Node parentXmlNode = null;
        if (mUiParent != null) {
            parentXmlNode = mUiParent.prepareCommit();
            if (parentXmlNode == null) {
                return null;
            }
        }
        String element_name = getDescriptor().getXmlName();
        Document doc = getXmlDocument();
        if (doc == null) {
            throw new RuntimeException(
                    String.format("Missing XML document for %1$s XML node.", element_name));
        }
        if (parentXmlNode == null) {
            parentXmlNode = doc;
        }
        mXmlNode = doc.createElement(element_name);
        Node xmlNextSibling = null;
        UiElementNode uiNextSibling = getUiNextSibling();
        if (uiNextSibling != null) {
            xmlNextSibling = uiNextSibling.getXmlNode();
        }
        parentXmlNode.insertBefore(mXmlNode, xmlNextSibling);
        Text sep = doc.createTextNode("\n");
        parentXmlNode.appendChild(sep);
        for (AttributeDescriptor attr_desc : getAttributeDescriptors()) {
            if (attr_desc instanceof XmlnsAttributeDescriptor) {
                XmlnsAttributeDescriptor desc = (XmlnsAttributeDescriptor) attr_desc;
                Attr attr = doc.createAttributeNS(XmlnsAttributeDescriptor.XMLNS_URI,
                        desc.getXmlNsName());
                attr.setValue(desc.getValue());
                attr.setPrefix(desc.getXmlNsPrefix());
                mXmlNode.getAttributes().setNamedItemNS(attr);
            } else {
                UiAttributeNode ui_attr = getInternalUiAttributes().get(attr_desc);
                commitAttributeToXml(ui_attr, ui_attr.getCurrentValue());
            }
        }
        invokeUiUpdateListeners(UiUpdateState.CREATED);
        return mXmlNode;
    }
    public Node deleteXmlNode() {
        if (mXmlNode == null) {
            return null;
        }
        Node old_xml_node = mXmlNode;
        clearContent();
        Node xml_parent = old_xml_node.getParentNode();
        if (xml_parent == null) {
            xml_parent = getXmlDocument();
        }
        old_xml_node = xml_parent.removeChild(old_xml_node);
        invokeUiUpdateListeners(UiUpdateState.DELETED);
        return old_xml_node;
    }
    protected boolean updateElementList(Node xml_node) {
        boolean structure_changed = false;
        int ui_index = 0;
        Node xml_child = xml_node.getFirstChild();
        while (xml_child != null) {
            if (xml_child.getNodeType() == Node.ELEMENT_NODE) {
                String element_name = xml_child.getNodeName();
                UiElementNode ui_node = null;
                if (mUiChildren.size() <= ui_index) {
                    ElementDescriptor desc = mDescriptor.findChildrenDescriptor(element_name,
                            false );
                    if (desc == null) {
                        IUnknownDescriptorProvider p = getUnknownDescriptorProvider();
                        desc = p.getDescriptor(element_name);
                    }
                    structure_changed = true;
                    ui_node = appendNewUiChild(desc);
                    ui_index++;
                } else {
                    UiElementNode ui_child;
                    int n = mUiChildren.size();
                    for (int j = ui_index; j < n; j++) {
                        ui_child = mUiChildren.get(j);
                        if (ui_child.getXmlNode() != null && ui_child.getXmlNode() == xml_child) {
                            if (j > ui_index) {
                                mUiChildren.remove(j);
                                mUiChildren.add(ui_index, ui_child);
                                structure_changed = true;
                            }
                            ui_node = ui_child;
                            ui_index++;
                            break;
                        }
                    }
                    if (ui_node == null) {
                        for (int j = ui_index; j < n; j++) {
                            ui_child = mUiChildren.get(j);
                            if (ui_child.getXmlNode() == null &&
                                    ui_child.getDescriptor().isMandatory() &&
                                    ui_child.getDescriptor().getXmlName().equals(element_name)) {
                                if (j > ui_index) {
                                    mUiChildren.remove(j);
                                    mUiChildren.add(ui_index, ui_child);
                                }
                                ui_child.mXmlNode = xml_child;
                                structure_changed = true;
                                ui_node = ui_child;
                                ui_index++;
                            }
                        }
                    }
                    if (ui_node == null) {
                        ElementDescriptor desc = mDescriptor.findChildrenDescriptor(element_name,
                                false );
                        if (desc == null) {
                            AdtPlugin.log(IStatus.WARNING,
                                    "AndroidManifest: Ignoring unknown '%s' XML element", 
                                    element_name);
                        } else {
                            structure_changed = true;
                            ui_node = insertNewUiChild(ui_index, desc);
                            ui_index++;
                        }
                    }
                }
                if (ui_node != null) {
                    structure_changed |= ui_node.loadFromXmlNode(xml_child);
                }
            }
            xml_child = xml_child.getNextSibling();
        }
        for (int index = mUiChildren.size() - 1; index >= ui_index; --index) {
             structure_changed |= removeUiChildAtIndex(index);
        }
        return structure_changed;
    }
    private boolean removeUiChildAtIndex(int ui_index) {
        UiElementNode ui_node = mUiChildren.get(ui_index);
        ElementDescriptor desc = ui_node.getDescriptor();
        try {
            if (ui_node.getDescriptor().isMandatory()) {
                boolean keepNode = true;
                for (UiElementNode child : mUiChildren) {
                    if (child != ui_node && child.getDescriptor() == desc) {
                        keepNode = false;
                        break;
                    }
                }
                if (keepNode) {
                    boolean xml_exists = (ui_node.getXmlNode() != null);
                    ui_node.clearContent();
                    return xml_exists;
                }
            }
            mUiChildren.remove(ui_index);
            return true;
        } finally {
            invokeUiUpdateListeners(UiUpdateState.DELETED);
        }
    }
    public UiElementNode appendNewUiChild(ElementDescriptor descriptor) {
        UiElementNode ui_node;
        ui_node = descriptor.createUiNode();
        mUiChildren.add(ui_node);
        ui_node.setUiParent(this);
        ui_node.invokeUiUpdateListeners(UiUpdateState.CREATED);
        return ui_node;
    }
    public UiElementNode insertNewUiChild(int index, ElementDescriptor descriptor) {
        UiElementNode ui_node;
        ui_node = descriptor.createUiNode();
        mUiChildren.add(index, ui_node);
        ui_node.setUiParent(this);
        ui_node.invokeUiUpdateListeners(UiUpdateState.CREATED);
        return ui_node;
    }
    protected void updateAttributeList(Node xmlNode) {
        NamedNodeMap xmlAttrMap = xmlNode.getAttributes();
        HashSet<Node> visited = new HashSet<Node>();
        for (UiAttributeNode uiAttr : getInternalUiAttributes().values()) {
            AttributeDescriptor desc = uiAttr.getDescriptor();
            if (!(desc instanceof SeparatorAttributeDescriptor)) {
                Node xmlAttr = xmlAttrMap == null ? null :
                    xmlAttrMap.getNamedItemNS(desc.getNamespaceUri(), desc.getXmlLocalName());
                uiAttr.updateValue(xmlAttr);
                visited.add(xmlAttr);
            }
        }
        @SuppressWarnings("unchecked") 
        HashSet<UiAttributeNode> deleted = (HashSet<UiAttributeNode>) mUnknownUiAttributes.clone();
        Map<String, AttributeDescriptor> hiddenAttrDesc = getHiddenAttributeDescriptors();
        if (xmlAttrMap != null) {
            for (int i = 0; i < xmlAttrMap.getLength(); i++) {
                Node xmlAttr = xmlAttrMap.item(i);
                if (visited.contains(xmlAttr)) {
                    continue;
                }
                String xmlFullName = xmlAttr.getNodeName();
                if (hiddenAttrDesc.containsKey(xmlFullName)) {
                    continue;
                }
                String xmlAttrLocalName = xmlAttr.getLocalName();
                String xmlNsUri = xmlAttr.getNamespaceURI();
                UiAttributeNode uiAttr = null;
                for (UiAttributeNode a : mUnknownUiAttributes) {
                    String aLocalName = a.getDescriptor().getXmlLocalName();
                    String aNsUri = a.getDescriptor().getNamespaceUri();
                    if (aLocalName.equals(xmlAttrLocalName) &&
                            (aNsUri == xmlNsUri || (aNsUri != null && aNsUri.equals(xmlNsUri)))) {
                        uiAttr = a;
                        deleted.remove(a);
                        break;
                    }
                }
                if (uiAttr == null) {
                    TextAttributeDescriptor desc = new TextAttributeDescriptor(
                            xmlAttrLocalName, 
                            xmlFullName, 
                            xmlNsUri, 
                            "Unknown XML attribute"); 
                    uiAttr = desc.createUiNode(this);
                    mUnknownUiAttributes.add(uiAttr);
                }
                uiAttr.updateValue(xmlAttr);
            }
            for (UiAttributeNode a : deleted) {
                mUnknownUiAttributes.remove(a);
            }
        }
    }
    protected void invokeUiUpdateListeners(UiUpdateState state) {
        if (mUiUpdateListeners != null) {
            for (IUiUpdateListener listener : mUiUpdateListeners) {
                try {
                    listener.uiElementNodeUpdated(this, state);
                } catch (Exception e) {
                    AdtPlugin.log(e, "UIElement Listener failed: %s, state=%s",  
                            getBreadcrumbTrailDescription(true),
                            state.toString());
                }
            }
        }
    }
    protected void setXmlNode(Node xml_node) {
        mXmlNode = xml_node;
    }
    public void setEditData(Object data) {
        mEditData = data;
    }
    public Object getEditData() {
        return mEditData;
    }
    public void refreshUi() {
        invokeUiUpdateListeners(UiUpdateState.ATTR_UPDATED);
    }
    public boolean commitAttributeToXml(UiAttributeNode uiAttr, String newValue) {
        Node element = prepareCommit();
        if (element != null && uiAttr != null) {
            String attrLocalName = uiAttr.getDescriptor().getXmlLocalName();
            String attrNsUri = uiAttr.getDescriptor().getNamespaceUri();
            NamedNodeMap attrMap = element.getAttributes();
            if (newValue == null || newValue.length() == 0) {
                if (attrMap.getNamedItemNS(attrNsUri, attrLocalName) != null) {
                    attrMap.removeNamedItemNS(attrNsUri, attrLocalName);
                    return true;
                }
            } else {
                Document doc = element.getOwnerDocument();
                if (doc != null) {
                    Attr attr = doc.createAttributeNS(attrNsUri, attrLocalName);
                    attr.setValue(newValue);
                    attr.setPrefix(lookupNamespacePrefix(element, attrNsUri));
                    attrMap.setNamedItemNS(attr);
                    return true;
                }
            }
        }
        return false;
    }
    public boolean commitDirtyAttributesToXml() {
        boolean result = false;
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        for (Entry<AttributeDescriptor, UiAttributeNode> entry : attributeMap.entrySet()) {
            UiAttributeNode ui_attr = entry.getValue();
            if (ui_attr.isDirty()) {
                result |= commitAttributeToXml(ui_attr, ui_attr.getCurrentValue());
                ui_attr.setDirty(false);
            }
        }
        return result;
    }
    private String lookupNamespacePrefix(Node node, String nsUri) {
        if (nsUri == null) {
            return null;
        }
        if (XmlnsAttributeDescriptor.XMLNS_URI.equals(nsUri)) {
            return "xmlns"; 
        }
        HashSet<String> visited = new HashSet<String>();
        Document doc = node == null ? null : node.getOwnerDocument();
        for (; node != null && node.getNodeType() == Node.ELEMENT_NODE;
               node = node.getParentNode()) {
            NamedNodeMap attrs = node.getAttributes();
            for (int n = attrs.getLength() - 1; n >= 0; --n) {
                Node attr = attrs.item(n);
                if ("xmlns".equals(attr.getPrefix())) {  
                    String uri = attr.getNodeValue();
                    String nsPrefix = attr.getLocalName();
                    if (nsUri.equals(uri)) {
                        return nsPrefix;
                    }
                    visited.add(nsPrefix);
                }
            }
        }
        String prefix = SdkConstants.NS_RESOURCES.equals(nsUri) ? "android" : "ns"; 
        String base = prefix;
        for (int i = 1; visited.contains(prefix); i++) {
            prefix = base + Integer.toString(i);
        }
        if (doc != null) {
            node = doc.getFirstChild();
            while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
            }
            if (node != null) {
                Attr attr = doc.createAttributeNS(XmlnsAttributeDescriptor.XMLNS_URI, prefix);
                attr.setValue(nsUri);
                attr.setPrefix("xmlns"); 
                node.getAttributes().setNamedItemNS(attr);
            }
        }
        return prefix;
    }
    public UiAttributeNode setAttributeValue(String attrXmlName, String value, boolean override) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        if (value == null) {
            value = ""; 
        }
        for (Entry<AttributeDescriptor, UiAttributeNode> entry : attributeMap.entrySet()) {
            AttributeDescriptor ui_desc = entry.getKey();
            if (ui_desc.getXmlLocalName().equals(attrXmlName)) {
                UiAttributeNode ui_attr = entry.getValue();
                if (ui_attr instanceof IUiSettableAttributeNode) {
                    String current = ui_attr.getCurrentValue();
                    if (override || current == null || !current.equals(value)) {
                        ((IUiSettableAttributeNode) ui_attr).setCurrentValue(value);
                        ui_attr.setDirty(true);
                        return ui_attr;
                    }
                }
                break;
            }
        }
        return null;
    }
    public String getAttributeValue(String attrXmlName) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        for (Entry<AttributeDescriptor, UiAttributeNode> entry : attributeMap.entrySet()) {
            AttributeDescriptor ui_desc = entry.getKey();
            if (ui_desc.getXmlLocalName().equals(attrXmlName)) {
                UiAttributeNode ui_attr = entry.getValue();
                return ui_attr.getCurrentValue();
            }
        }
        return null;
    }
    public Object getEditableValue() {
        return null;
    }
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> propDescs = new ArrayList<IPropertyDescriptor>();
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        Set<AttributeDescriptor> keys = attributeMap.keySet();
        for (AttributeDescriptor key : keys) {
            if (key instanceof IPropertyDescriptor) {
                propDescs.add((IPropertyDescriptor)key);
            }
        }
        for (UiAttributeNode unknownNode : mUnknownUiAttributes) {
            if (unknownNode.getDescriptor() instanceof IPropertyDescriptor) {
                propDescs.add((IPropertyDescriptor)unknownNode.getDescriptor());
            }
        }
        return propDescs.toArray(new IPropertyDescriptor[propDescs.size()]);
    }
    public Object getPropertyValue(Object id) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        UiAttributeNode attribute = attributeMap.get(id);
        if (attribute == null) {
            for (UiAttributeNode unknownAttr : mUnknownUiAttributes) {
                if (id == unknownAttr.getDescriptor()) {
                    return unknownAttr;
                }
            }
        }
        return attribute;
    }
    public boolean isPropertySet(Object id) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        UiAttributeNode attribute = attributeMap.get(id);
        if (attribute != null) {
            return attribute.getCurrentValue().length() > 0;
        }
        for (UiAttributeNode unknownAttr : mUnknownUiAttributes) {
            if (id == unknownAttr.getDescriptor()) {
                return unknownAttr.getCurrentValue().length() > 0;
            }
        }
        return false;
    }
    public void resetPropertyValue(Object id) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        UiAttributeNode attribute = attributeMap.get(id);
        if (attribute != null) {
            return;
        }
        for (UiAttributeNode unknownAttr : mUnknownUiAttributes) {
            if (id == unknownAttr.getDescriptor()) {
                return;
            }
        }
    }
    public void setPropertyValue(Object id, Object value) {
        HashMap<AttributeDescriptor, UiAttributeNode> attributeMap = getInternalUiAttributes();
        UiAttributeNode attribute = attributeMap.get(id);
        if (attribute == null) {
            for (UiAttributeNode unknownAttr : mUnknownUiAttributes) {
                if (id == unknownAttr.getDescriptor()) {
                    attribute = unknownAttr;
                    break;
                }
            }
        }
        if (attribute != null) {
            String oldValue = attribute.getCurrentValue();
            final String newValue = (String)value;
            if (oldValue.equals(newValue)) {
                return;
            }
            final UiAttributeNode fAttribute = attribute;
            AndroidEditor editor = getEditor();
            editor.editXmlModel(new Runnable() {
                public void run() {
                    commitAttributeToXml(fAttribute, newValue);
                }
            });
        }
    }
}
