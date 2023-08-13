public abstract class UiActions implements ICommitXml {
    public UiActions() {
    }
    abstract protected UiElementNode getRootNode();
    abstract public void commitPendingXmlChanges();
    abstract protected void selectUiNode(UiElementNode uiNode);
    public void doAdd(UiElementNode uiNode, Shell shell) {
        doAdd(uiNode, null , shell, new UiModelTreeLabelProvider());
    }
    public void doAdd(UiElementNode uiNode,
            ElementDescriptor[] descriptorFilters,
            Shell shell, ILabelProvider labelProvider) {
        UiElementNode rootNode = getRootNode();
        if (rootNode instanceof UiDocumentNode && rootNode.getUiChildren().size() > 0) {
            rootNode = rootNode.getUiChildren().get(0);
        }
        NewItemSelectionDialog dlg = new NewItemSelectionDialog(
                shell,
                labelProvider,
                descriptorFilters,
                uiNode, rootNode);
        dlg.open();
        Object[] results = dlg.getResult();
        if (results != null && results.length > 0) {
            addElement(dlg.getChosenRootNode(), null, (ElementDescriptor) results[0],
                    true );
        }
    }
    public UiElementNode addElement(UiElementNode uiParent,
            UiElementNode uiSibling,
            ElementDescriptor descriptor,
            boolean updateLayout) {
        if (uiParent instanceof UiDocumentNode && uiParent.getUiChildren().size() > 0) {
            uiParent = uiParent.getUiChildren().get(0);
        }
        if (uiSibling != null && uiSibling.getUiParent() != uiParent) {
            uiSibling = null;
        }
        UiElementNode uiNew = addNewTreeElement(uiParent, uiSibling, descriptor, updateLayout);
        selectUiNode(uiNew);
        return uiNew;
    }
    public void doRemove(final List<UiElementNode> nodes, Shell shell) {
        if (nodes == null || nodes.size() == 0) {
            return;
        }
        final int len = nodes.size();
        StringBuilder sb = new StringBuilder();
        for (UiElementNode node : nodes) {
            sb.append("\n- "); 
            sb.append(node.getBreadcrumbTrailDescription(false ));
        }
        if (MessageDialog.openQuestion(shell,
                len > 1 ? "Remove elements from Android XML"  
                        : "Remove element from Android XML",
                String.format("Do you really want to remove %1$s?", sb.toString()))) {
            commitPendingXmlChanges();
            getRootNode().getEditor().editXmlModel(new Runnable() {
                public void run() {
                    UiElementNode previous = null;
                    UiElementNode parent = null;
                    for (int i = len - 1; i >= 0; i--) {
                        UiElementNode node = nodes.get(i);
                        previous = node.getUiPreviousSibling();
                        parent = node.getUiParent();
                        node.deleteXmlNode();
                    }
                    if (previous != null) {
                        selectUiNode(previous);
                    } else if (parent != null) {
                        selectUiNode(parent);
                    }
                }
            });
        }
    }
    public void doUp(final List<UiElementNode> nodes) {
        if (nodes == null || nodes.size() < 1) {
            return;
        }
        final Node[] select_xml_node = { null };
        UiElementNode last_node = null;
        UiElementNode search_root = null;
        for (int i = 0; i < nodes.size(); i++) {
            final UiElementNode node = last_node = nodes.get(i);
            search_root = node.getUiParent();
            if (search_root != null && search_root.getUiParent() != null) {
                search_root = search_root.getUiParent();
            }
            commitPendingXmlChanges();
            getRootNode().getEditor().editXmlModel(new Runnable() {
                public void run() {
                    Node xml_node = node.getXmlNode();
                    if (xml_node != null) {
                        Node xml_parent = xml_node.getParentNode();
                        if (xml_parent != null) {
                            UiElementNode ui_prev = node.getUiPreviousSibling();
                            if (ui_prev != null && ui_prev.getXmlNode() != null) {
                                Node xml_prev = ui_prev.getXmlNode();
                                if (ui_prev.getDescriptor().hasChildren()) {
                                    xml_prev.appendChild(xml_parent.removeChild(xml_node));
                                    select_xml_node[0] = xml_node;
                                } else {
                                    xml_parent.insertBefore(
                                            xml_parent.removeChild(xml_node),
                                            xml_prev);
                                    select_xml_node[0] = xml_node;
                                }
                            } else if (!(xml_parent instanceof Document) &&
                                    xml_parent.getParentNode() != null &&
                                    !(xml_parent.getParentNode() instanceof Document)) {
                                Node grand_parent = xml_parent.getParentNode();
                                grand_parent.insertBefore(xml_parent.removeChild(xml_node),
                                        xml_parent);
                                select_xml_node[0] = xml_node;
                            }
                        }
                    }
                }
            });
        }
        if (select_xml_node[0] == null) {
            selectUiNode(last_node);
        } else {
            if (search_root == null) {
                search_root = last_node.getUiRoot();
            }
            if (search_root != null) {
                selectUiNode(search_root.findXmlNode(select_xml_node[0]));
            }
        }
    }
    public void doDown(final List<UiElementNode> nodes) {
        if (nodes == null || nodes.size() < 1) {
            return;
        }
        final Node[] select_xml_node = { null };
        UiElementNode last_node = null;
        UiElementNode search_root = null;
        for (int i = nodes.size() - 1; i >= 0; i--) {
            final UiElementNode node = last_node = nodes.get(i);
            search_root = node.getUiParent();
            if (search_root != null && search_root.getUiParent() != null) {
                search_root = search_root.getUiParent();
            }
            commitPendingXmlChanges();
            getRootNode().getEditor().editXmlModel(new Runnable() {
                public void run() {
                    Node xml_node = node.getXmlNode();
                    if (xml_node != null) {
                        Node xml_parent = xml_node.getParentNode();
                        if (xml_parent != null) {
                            UiElementNode uiNext = node.getUiNextSibling();
                            if (uiNext != null && uiNext.getXmlNode() != null) {
                                Node xml_next = uiNext.getXmlNode();
                                if (uiNext.getDescriptor().hasChildren()) {
                                    xml_next.insertBefore(xml_parent.removeChild(xml_node),
                                            xml_next.getFirstChild());
                                    select_xml_node[0] = xml_node;
                                } else {
                                    xml_parent.insertBefore(xml_parent.removeChild(xml_node),
                                            xml_next.getNextSibling());
                                    select_xml_node[0] = xml_node;
                                }
                            } else if (!(xml_parent instanceof Document) &&
                                    xml_parent.getParentNode() != null &&
                                    !(xml_parent.getParentNode() instanceof Document)) {
                                Node grand_parent = xml_parent.getParentNode();
                                grand_parent.insertBefore(xml_parent.removeChild(xml_node),
                                        xml_parent.getNextSibling());
                                select_xml_node[0] = xml_node;
                            }
                        }
                    }
                }
            });
        }
        if (select_xml_node[0] == null) {
            selectUiNode(last_node);
        } else {
            if (search_root == null) {
                search_root = last_node.getUiRoot();
            }
            if (search_root != null) {
                selectUiNode(search_root.findXmlNode(select_xml_node[0]));
            }
        }
    }
    private UiElementNode addNewTreeElement(UiElementNode uiParent,
            final UiElementNode uiSibling,
            ElementDescriptor descriptor,
            final boolean updateLayout) {
        commitPendingXmlChanges();
        int index = 0;
        for (UiElementNode uiChild : uiParent.getUiChildren()) {
            if (uiChild == uiSibling) {
                break;
            }
            index++;
        }
        final UiElementNode uiNew = uiParent.insertNewUiChild(index, descriptor);
        UiElementNode rootNode = getRootNode();
        rootNode.getEditor().editXmlModel(new Runnable() {
            public void run() {
                DescriptorsUtils.setDefaultLayoutAttributes(uiNew, updateLayout);
                Node xmlNode = uiNew.createXmlNode();
            }
        });
        return uiNew;
    }
}
