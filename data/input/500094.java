public class NewItemSelectionDialog extends AbstractElementListSelectionDialog {
    private UiElementNode mSelectedUiNode;
    private UiElementNode mChosenRootNode;
    private UiElementNode mLocalRootNode;
    private ElementDescriptor[] mDescriptorFilters;
    private String mLastUsedKey;
    private static final Map<String, String> sLastUsedXmlName = new HashMap<String, String>();
    private String mInitialXmlName;
    public NewItemSelectionDialog(Shell shell, ILabelProvider labelProvider,
            ElementDescriptor[] descriptorFilters,
            UiElementNode ui_node,
            UiElementNode root_node) {
        super(shell, labelProvider);
        mDescriptorFilters = descriptorFilters;
        mLocalRootNode = root_node;
        if (ui_node != null && ui_node != mLocalRootNode) {
            if (ui_node.getDescriptor().hasChildren()) {
                mSelectedUiNode = ui_node;
            } else {
                UiElementNode parent = ui_node.getUiParent();
                if (parent != null && parent != mLocalRootNode) {
                    mSelectedUiNode = parent;
                }
            }
        }
        setHelpAvailable(false);
        setMultipleSelection(false);
        setValidator(new ISelectionStatusValidator() {
            public IStatus validate(Object[] selection) {
                if (selection.length == 1 && selection[0] instanceof ViewElementDescriptor) {
                    return new Status(IStatus.OK, 
                            AdtPlugin.PLUGIN_ID, 
                            IStatus.OK, 
                            ((ViewElementDescriptor) selection[0]).getFullClassName(), 
                            null); 
                } else if (selection.length == 1 && selection[0] instanceof ElementDescriptor) {
                    return new Status(IStatus.OK, 
                            AdtPlugin.PLUGIN_ID, 
                            IStatus.OK, 
                            "", 
                            null); 
                } else {
                    return new Status(IStatus.ERROR, 
                            AdtPlugin.PLUGIN_ID, 
                            IStatus.ERROR, 
                            "Invalid selection", 
                            null); 
                }
            }
        });
        String xmlName = getLastUsedXmlName(root_node);
        if (xmlName == null) {
            xmlName = getMostUsedXmlName(root_node);
        }
        if (xmlName == null) {
            xmlName = getLeafFileName(root_node);
        }
        mInitialXmlName = xmlName;
    }
    private String getLeafFileName(UiElementNode ui_node) {
        if (ui_node != null) {
            AndroidEditor editor = ui_node.getEditor();
            if (editor != null) {
                IEditorInput editorInput = editor.getEditorInput();
                if (editorInput instanceof FileEditorInput) {
                    IFile f = ((FileEditorInput) editorInput).getFile();
                    if (f != null) {
                        String leafName = f.getFullPath().removeFileExtension().lastSegment();
                        return "*" + leafName; 
                    }
                }
            }
        }
        return null;
    }
    private String getLastUsedXmlName(UiElementNode ui_node) {
        if (ui_node != null) {
            AndroidEditor editor = ui_node.getEditor();
            if (editor != null) {
                IEditorInput editorInput = editor.getEditorInput();
                if (editorInput instanceof FileEditorInput) {
                    IFile f = ((FileEditorInput) editorInput).getFile();
                    if (f != null) {
                        mLastUsedKey = f.getFullPath().toPortableString();
                        return sLastUsedXmlName.get(mLastUsedKey);
                    }
                }
            }
        }
        return null;
    }
    private void setLastUsedXmlName(Object[] objects) {
        if (mLastUsedKey != null &&
                objects != null &&
                objects.length > 0 &&
                objects[0] instanceof ElementDescriptor) {
            ElementDescriptor desc = (ElementDescriptor) objects[0];
            sLastUsedXmlName.put(mLastUsedKey, desc.getXmlName());
        }
    }
    private String getMostUsedXmlName(UiElementNode ui_node) {
        if (ui_node != null) {
            TreeMap<String, Integer> counts = new TreeMap<String, Integer>();
            int max = -1;
            for (UiElementNode child : ui_node.getUiChildren()) {
                String name = child.getDescriptor().getXmlName();
                Integer i = counts.get(name);
                int count = i == null ? 1 : i.intValue() + 1;
                counts.put(name, count);
                max = Math.max(max, count);
            }
            if (max > 0) {
                for (Entry<String, Integer> entry : counts.entrySet()) {
                    if (entry.getValue().intValue() == max) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }
    public UiElementNode getChosenRootNode() {
        return mChosenRootNode;
    }
    @Override
    protected void computeResult() {
        setResult(Arrays.asList(getSelectedElements()));
        setLastUsedXmlName(getSelectedElements());
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite contents = (Composite) super.createDialogArea(parent);
        createRadioControl(contents);
        createFilterText(contents);
        createFilteredList(contents);
        chooseNode(mChosenRootNode);
        setInitialSelection(mChosenRootNode);
        return contents;
    }
    private void setInitialSelection(UiElementNode rootNode) {
        ElementDescriptor initialElement = null;
        if (mInitialXmlName != null && mInitialXmlName.length() > 0) {
            String name = mInitialXmlName;
            boolean partial = name.startsWith("*");   
            if (partial) {
                name = name.substring(1).toLowerCase();
            }
            for (ElementDescriptor desc : getAllowedDescriptors(rootNode)) {
                if (!partial && desc.getXmlName().equals(name)) {
                    initialElement = desc;
                    break;
                } else if (partial) {
                    String name2 = desc.getXmlLocalName().toLowerCase();
                    if (name.startsWith(name2) || name2.startsWith(name)) {
                        initialElement = desc;
                        break;
                    }
                }
            }
        }
        setSelection(initialElement == null ? null : new ElementDescriptor[] { initialElement });
    }
    private Composite createRadioControl(Composite content) {
        if (mSelectedUiNode != null) {
            Button radio1 = new Button(content, SWT.RADIO);
            radio1.setText(String.format("Create a new element at the top level, in %1$s.",
                    mLocalRootNode.getShortDescription()));
            Button radio2 = new Button(content, SWT.RADIO);
            radio2.setText(String.format("Create a new element in the selected element, %1$s.",
                    mSelectedUiNode.getBreadcrumbTrailDescription(false )));
            radio1.setSelection(false);
            radio2.setSelection(true);
            mChosenRootNode = mSelectedUiNode;
            radio1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    super.widgetSelected(e);
                    chooseNode(mLocalRootNode);
                }
            });
            radio2.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    super.widgetSelected(e);
                    chooseNode(mSelectedUiNode);
                }
            });
        } else {
            setMessage(String.format("Create a new element at the top level, in %1$s.",
                    mLocalRootNode.getShortDescription()));
            createMessageArea(content);
            mChosenRootNode = mLocalRootNode;
        }
        return content;
    }
    private void chooseNode(UiElementNode ui_node) {
        mChosenRootNode = ui_node;
        setListElements(getAllowedDescriptors(ui_node));
    }
    private ElementDescriptor[] getAllowedDescriptors(UiElementNode ui_node) {
        if (ui_node == mLocalRootNode &&
                mDescriptorFilters != null &&
                mDescriptorFilters.length != 0) {
            return mDescriptorFilters;
        } else {
            return ui_node.getDescriptor().getChildren();
        }
    }
}
