public class NodeProxy implements INode {
    private final UiViewElementNode mNode;
    private final Rect mBounds;
    private final NodeFactory mFactory;
     NodeProxy(UiViewElementNode uiNode, Rectangle bounds, NodeFactory factory) {
        mNode = uiNode;
        mFactory = factory;
        if (bounds == null) {
            mBounds = new Rect();
        } else {
            mBounds = new Rect(bounds);
        }
    }
    public void debugPrintf(String msg, Object...params) {
        AdtPlugin.printToConsole(
                mNode == null ? "Groovy" : mNode.getDescriptor().getXmlLocalName() + ".groovy",
                String.format(msg, params)
                );
    }
    public Rect getBounds() {
        return mBounds;
    }
     void setBounds(Rectangle bounds) {
        mBounds.set(bounds);
    }
     UiViewElementNode getNode() {
        return mNode;
    }
    public INode getParent() {
        if (mNode != null) {
            UiElementNode p = mNode.getUiParent();
            if (p instanceof UiViewElementNode) {
                return mFactory.create((UiViewElementNode) p);
            }
        }
        return null;
    }
    public INode[] getChildren() {
        if (mNode != null) {
            ArrayList<INode> nodes = new ArrayList<INode>();
            for (UiElementNode uiChild : mNode.getUiChildren()) {
                if (uiChild instanceof UiViewElementNode) {
                    nodes.add(mFactory.create((UiViewElementNode) uiChild));
                }
            }
            return nodes.toArray(new INode[nodes.size()]);
        }
        return new INode[0];
    }
    public void editXml(String undoName, final Closure c) {
        final AndroidEditor editor = mNode.getEditor();
        if (editor.isEditXmlModelPending()) {
            throw new RuntimeException("Error: calls to INode.editXml cannot be nested!");
        }
        if (editor instanceof LayoutEditor) {
            ((LayoutEditor) editor).wrapUndoRecording(
                    undoName,
                    new Runnable() {
                        public void run() {
                            editor.editXmlModel(new Runnable() {
                                public void run() {
                                    c.call(NodeProxy.this);
                                }
                            });
                        }
                    });
        }
    }
    private void checkEditOK() {
        final AndroidEditor editor = mNode.getEditor();
        if (!editor.isEditXmlModelPending()) {
            throw new RuntimeException("Error: XML edit call without using INode.editXml!");
        }
    }
    public INode appendChild(String viewFqcn) {
        checkEditOK();
        ViewElementDescriptor vd = getFqcnViewDescritor(viewFqcn);
        if (vd == null) {
            debugPrintf("Can't create a new %s element", viewFqcn);
            return null;
        }
        UiElementNode uiNew = mNode.appendNewUiChild(vd);
        DescriptorsUtils.setDefaultLayoutAttributes(uiNew, false );
        Node xmlNode = uiNew.createXmlNode();
        if (!(uiNew instanceof UiViewElementNode) || xmlNode == null) {
            debugPrintf("Failed to create a new %s element", viewFqcn);
            throw new RuntimeException("XML node creation failed."); 
        }
        return mFactory.create((UiViewElementNode) uiNew);
    }
    public INode insertChildAt(String viewFqcn, int index) {
        checkEditOK();
        ViewElementDescriptor vd = getFqcnViewDescritor(viewFqcn);
        if (vd == null) {
            debugPrintf("Can't create a new %s element", viewFqcn);
            return null;
        }
        int n = mNode.getUiChildren().size();
        UiElementNode uiNew = null;
        if (index < 0 || index >= n) {
            uiNew = mNode.appendNewUiChild(vd);
        } else {
            uiNew = mNode.insertNewUiChild(index, vd);
        }
        DescriptorsUtils.setDefaultLayoutAttributes(uiNew, false );
        Node xmlNode = uiNew.createXmlNode();
        if (!(uiNew instanceof UiViewElementNode) || xmlNode == null) {
            debugPrintf("Failed to create a new %s element", viewFqcn);
            throw new RuntimeException("XML node creation failed."); 
        }
        return mFactory.create((UiViewElementNode) uiNew);
    }
    public boolean setAttribute(String attributeName, String value) {
        checkEditOK();
        UiAttributeNode attr = mNode.setAttributeValue(attributeName, value, true );
        mNode.commitDirtyAttributesToXml();
        return attr != null;
    }
    public String getStringAttr(String attrName) {
        UiElementNode uiNode = mNode;
        if (uiNode.getXmlNode() != null) {
            Node xmlNode = uiNode.getXmlNode();
            if (xmlNode != null) {
                NamedNodeMap nodeAttributes = xmlNode.getAttributes();
                if (nodeAttributes != null) {
                    Node attr = nodeAttributes.getNamedItemNS(SdkConstants.NS_RESOURCES, attrName);
                    if (attr != null) {
                        return attr.getNodeValue();
                    }
                }
            }
        }
        return null;
    }
    private ViewElementDescriptor getFqcnViewDescritor(String fqcn) {
        AndroidEditor editor = mNode.getEditor();
        if (editor != null) {
            AndroidTargetData data = editor.getTargetData();
            if (data != null) {
                LayoutDescriptors layoutDesc = data.getLayoutDescriptors();
                if (layoutDesc != null) {
                    DocumentDescriptor docDesc = layoutDesc.getDescriptor();
                    if (docDesc != null) {
                        return internalFindFqcnViewDescritor(fqcn, docDesc.getChildren(), null);
                    }
                }
            }
        }
        return null;
    }
    private ViewElementDescriptor internalFindFqcnViewDescritor(String fqcn,
            ElementDescriptor[] descriptors,
            Set<ElementDescriptor> visited) {
        if (visited == null) {
            visited = new HashSet<ElementDescriptor>();
        }
        if (descriptors != null) {
            for (ElementDescriptor desc : descriptors) {
                if (visited.add(desc)) {
                    if (desc instanceof ViewElementDescriptor &&
                            fqcn.equals(((ViewElementDescriptor) desc).getFullClassName())) {
                        return (ViewElementDescriptor) desc;
                    }
                    ViewElementDescriptor vd =
                        internalFindFqcnViewDescritor(fqcn, desc.getChildren(), visited);
                    if (vd != null) {
                        return vd;
                    }
                }
            }
        }
        return null;
    }
}
