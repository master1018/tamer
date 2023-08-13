class UiElementDetail implements IDetailsPage {
    private ManifestSectionPart mMasterPart;
    private Section mMasterSection;
    private UiElementNode mCurrentUiElementNode;
    private Composite mCurrentTable;
    private boolean mIsDirty;
    private IManagedForm mManagedForm;
    private final UiTreeBlock mTree;
    public UiElementDetail(UiTreeBlock tree) {
        mTree = tree;
        mMasterPart = mTree.getMasterPart();
        mManagedForm = mMasterPart.getManagedForm();
    }
    public void initialize(IManagedForm form) {
        mManagedForm = form;
    }
    public void createContents(Composite parent) {
        mMasterSection = createMasterSection(parent);
    }
    public void selectionChanged(IFormPart part, ISelection selection) {
        if (part == mMasterPart &&
                !selection.isEmpty() &&
                selection instanceof ITreeSelection) {
            ITreeSelection tree_selection = (ITreeSelection) selection;
            Object first = tree_selection.getFirstElement();
            if (first instanceof UiElementNode) {
                UiElementNode ui_node = (UiElementNode) first;
                createUiAttributeControls(mManagedForm, ui_node);
            }
        }
    }
    public void commit(boolean onSave) {
        IStructuredModel model = mTree.getEditor().getModelForEdit();
        try {
            model.aboutToChangeModel();
            if (mCurrentUiElementNode != null) {
                mCurrentUiElementNode.commit();
            }
            mIsDirty = false;
        } catch (Exception e) {
            AdtPlugin.log(e, "Detail node failed to commit XML attribute!"); 
        } finally {
            model.changedModel();
            model.releaseFromEdit();
        }
    }
    public void dispose() {
    }
    public boolean isDirty() {
        if (mCurrentUiElementNode != null && mCurrentUiElementNode.isDirty()) {
            markDirty();
        }
        return mIsDirty;
    }
    public boolean isStale() {
        return false;
    }
    public void refresh() {
        if (mCurrentTable != null) {
            mCurrentTable.dispose();
            mCurrentTable = null;
        }
        mCurrentUiElementNode = null;
        mMasterSection.getParent().pack(true );
    }
    public void setFocus() {
    }
    public boolean setFormInput(Object input) {
        return false;
    }
    private Section createMasterSection(Composite parent) {
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 0;
        parent.setLayout(layout);
        FormToolkit toolkit = mManagedForm.getToolkit();
        Section section = toolkit.createSection(parent, Section.TITLE_BAR);
        section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        return section;
    }
    private void createUiAttributeControls(
            final IManagedForm managedForm,
            final UiElementNode ui_node) {
        final ElementDescriptor elem_desc = ui_node.getDescriptor();
        mMasterSection.setText(String.format("Attributes for %1$s", ui_node.getShortDescription()));
        if (mCurrentUiElementNode != ui_node) {
            if (mIsDirty) {
                commit(false);
            }
            if (mCurrentTable != null) {
                mCurrentTable.dispose();
                mCurrentTable = null;
            }
            AttributeDescriptor[] attr_desc_list = ui_node.getAttributeDescriptors();
            boolean useSubsections = false;
            for (AttributeDescriptor attr_desc : attr_desc_list) {
                if (attr_desc instanceof SeparatorAttributeDescriptor) {
                    useSubsections = true;
                    break;
                }
            }
            FormToolkit toolkit = managedForm.getToolkit();
            Composite masterTable = SectionHelper.createTableLayout(mMasterSection,
                    toolkit, useSubsections ? 1 : 2 );
            mCurrentTable = masterTable;
            mCurrentUiElementNode = ui_node;
            if (elem_desc.getTooltip() != null) {
                String tooltip;
                if (Sdk.getCurrent() != null &&
                        Sdk.getCurrent().getDocumentationBaseUrl() != null) {
                    tooltip = DescriptorsUtils.formatFormText(elem_desc.getTooltip(),
                            elem_desc,
                            Sdk.getCurrent().getDocumentationBaseUrl());
                } else {
                    tooltip = elem_desc.getTooltip();
                }
                try {
                    FormText text = SectionHelper.createFormText(masterTable, toolkit,
                            true , tooltip, true );
                    text.addHyperlinkListener(mTree.getEditor().createHyperlinkListener());
                    Image icon = elem_desc.getIcon();
                    if (icon != null) {
                        text.setImage(DescriptorsUtils.IMAGE_KEY, icon);
                    }
                } catch(Exception e) {
                    AdtPlugin.log(e,
                            "Malformed javadoc, rejected by FormText for node %1$s: '%2$s'", 
                            ui_node.getDescriptor().getXmlName(),
                            tooltip);
                    tooltip = DescriptorsUtils.formatTooltip(elem_desc.getTooltip());
                    Label label = SectionHelper.createLabel(masterTable, toolkit,
                            tooltip, tooltip);
                }
            }
            Composite table = useSubsections ? null : masterTable;
            for (AttributeDescriptor attr_desc : attr_desc_list) {
                if (attr_desc instanceof XmlnsAttributeDescriptor) {
                    continue;
                } else if (table == null || attr_desc instanceof SeparatorAttributeDescriptor) {
                    String title = null;
                    if (attr_desc instanceof SeparatorAttributeDescriptor) {
                        title = attr_desc.getXmlLocalName();
                    } else {
                        title = String.format("Attributes from %1$s", elem_desc.getUiName());
                    }
                    table = createSubSectionTable(toolkit, masterTable, title);
                    if (attr_desc instanceof SeparatorAttributeDescriptor) {
                        continue;
                    }
                }
                UiAttributeNode ui_attr = ui_node.findUiAttribute(attr_desc);
                if (ui_attr != null) {
                    ui_attr.createUiControl(table, managedForm);
                    if (ui_attr.getCurrentValue() != null &&
                            ui_attr.getCurrentValue().length() > 0) {
                        ((Section) table.getParent()).setExpanded(true);
                    }
                } else {
                    AdtPlugin.log(IStatus.INFO,
                            "Attribute %1$s not declared in node %2$s, ignored.", 
                            attr_desc.getXmlLocalName(),
                            ui_node.getDescriptor().getXmlName());
                }
            }
            final Composite unknownTable = createSubSectionTable(toolkit, masterTable,
                    "Unknown XML Attributes");
            unknownTable.getParent().setVisible(false); 
            final HashSet<UiAttributeNode> reference = new HashSet<UiAttributeNode>();
            final IUiUpdateListener updateListener = new IUiUpdateListener() {
                public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
                    if (state == UiUpdateState.ATTR_UPDATED) {
                        updateUnknownAttributesSection(ui_node, unknownTable, managedForm,
                                reference);
                    }
                }
            };
            ui_node.addUpdateListener(updateListener);
            unknownTable.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    ui_node.removeUpdateListener(updateListener);
                }
            });
            updateUnknownAttributesSection(ui_node, unknownTable, managedForm, reference);
            mMasterSection.getParent().pack(true );
        }
    }
    private Composite createSubSectionTable(FormToolkit toolkit,
            Composite masterTable, String title) {
        int parentNumCol = ((TableWrapLayout) masterTable.getLayout()).numColumns;
        if (parentNumCol > 1) {
            masterTable = SectionHelper.createTableLayout(masterTable, toolkit, 1);
            TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
            twd.maxWidth = AndroidEditor.TEXT_WIDTH_HINT;
            twd.colspan = parentNumCol;
            masterTable.setLayoutData(twd);
        }
        Composite table;
        Section section = toolkit.createSection(masterTable,
                Section.TITLE_BAR | Section.TWISTIE);
        section.addExpansionListener(new IExpansionListener() {
            public void expansionStateChanged(ExpansionEvent e) {
                reflowMasterSection();
            }
            public void expansionStateChanging(ExpansionEvent e) {
            }
        });
        section.setText(title);
        section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB,
                                                TableWrapData.TOP));
        table = SectionHelper.createTableLayout(section, toolkit, 2 );
        return table;
    }
    private void reflowMasterSection() {
        for(Composite c = mMasterSection; c != null; c = c.getParent()) {
            if (c instanceof SharedScrolledComposite) {
                ((SharedScrolledComposite) c).reflow(true );
                break;
            }
        }
    }
    private void updateUnknownAttributesSection(UiElementNode ui_node,
            final Composite unknownTable, final IManagedForm managedForm,
            HashSet<UiAttributeNode> reference) {
        Collection<UiAttributeNode> ui_attrs = ui_node.getUnknownUiAttributes();
        Section section = ((Section) unknownTable.getParent());
        boolean needs_reflow = false;
        if (ui_attrs.size() > 0 && !section.isVisible()) {
            section.setVisible(true);
            needs_reflow = true;
        }
        boolean has_differences = ui_attrs.size() != reference.size();
        if (!has_differences) {
            for (UiAttributeNode ui_attr : ui_attrs) {
                if (!reference.contains(ui_attr)) {
                    has_differences = true;
                    break;
                }
            }
        }
        if (has_differences) {
            needs_reflow = true;
            reference.clear();
            for (Control c : unknownTable.getChildren()) {
                c.dispose();
            }
            for (UiAttributeNode ui_attr : ui_attrs) {
                reference.add(ui_attr);
                ui_attr.createUiControl(unknownTable, managedForm);
                if (ui_attr.getCurrentValue() != null && ui_attr.getCurrentValue().length() > 0) {
                    section.setExpanded(true);
                }
            }
        }
        if (needs_reflow) {
            reflowMasterSection();
        }
    }
    private void markDirty() {
        if (!mIsDirty) {
            mIsDirty = true;
            mManagedForm.dirtyStateChanged();
        }
    }
}
