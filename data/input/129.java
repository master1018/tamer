public class TypeZone extends Zone {
    private CLabel gTypeLabel;
    private Text gTypeText;
    private Button gTypeButton;
    public TypeZone(Composite pParent) {
        super(pParent, false);
    }
    @Override
    public void addItemsToZone() {
        gTypeLabel = this.getWidgetFactory().createCLabel(getZone(), PJPropertiesMessages.LBL_TYPE.getMessage());
        gTypeText = this.getWidgetFactory().createText(getZone(), "", SWT.NONE);
        gTypeButton = this.getWidgetFactory().createButton(getZone(), PJPropertiesMessages.BTN_CHANGETYPE.getMessage(), SWT.PUSH);
        gTypeText.setEditable(false);
        gTypeText.setEnabled(false);
    }
    @Override
    public void addLayoutsToItems() {
        fData = new FormData();
        fData.left = new FormAttachment(0, 5);
        fData.top = new FormAttachment(0, 3);
        fData.width = 120;
        gTypeLabel.setLayoutData(fData);
        fData = new FormData();
        fData.left = new FormAttachment(gTypeLabel, 5);
        fData.right = new FormAttachment(gTypeButton, -5);
        fData.top = new FormAttachment(0, 5);
        gTypeText.setLayoutData(fData);
        fData = new FormData();
        fData.right = new FormAttachment(100, -5);
        fData.top = new FormAttachment(0, 2);
        fData.width = 90;
        fData.height = 20;
        gTypeButton.setLayoutData(fData);
    }
    @Override
    public void addListenersToItems() {
        SelectionChangeHelper selectionChangeHelper = new SelectionChangeHelper() {
            @Override
            public void buttonSelected(Control control) {
                TypeProcessorFactory[] factories = new TypeProcessorFactory[] { new PrimitiveTypeProcessorFactory(), new JavaTypeProcessorFactory() };
                TypeSelector selector = new org.parallelj.tools.typeselector.core.selector.TypeSelector(factories, Tools.getJavaProjectFromEObject(getEObject()).getProject());
                selector.setFilter(JavaTypeFilters.getMostUsedJavaTypesFilter());
                selector.run();
                TypeInfo savedTypeInfo = selector.getSavedTypeInfo();
                if (savedTypeInfo == null) return;
                String newType = null;
                if (savedTypeInfo instanceof PrimitiveTypeInfo) newType = savedTypeInfo.getElementName(); else if (savedTypeInfo instanceof JavaTypeInfo) newType = savedTypeInfo.getPackageName() + "." + savedTypeInfo.getElementName();
                if (getEObject() != null && getEObject() instanceof Data) if (!newType.equals(((Data) getEObject()).getType())) Commands.doSetValue(getEditingDomain(), getEObject(), ControlFlowPackage.eINSTANCE.getData_Type(), newType, getEditPart());
            }
        };
        selectionChangeHelper.startListeningTo(gTypeButton);
    }
    @Override
    public void updateItemsValues() {
        String vType = getEObject() instanceof Data ? ((Data) getEObject()).getType() : "";
        gTypeText.setText(vType == null ? "" : vType);
    }
}
