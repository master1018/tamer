public class UiFlagAttributeNode extends UiTextAttributeNode {
    public UiFlagAttributeNode(FlagAttributeDescriptor attributeDescriptor,
            UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public void createUiControl(Composite parent, IManagedForm managedForm) {
        setManagedForm(managedForm);
        FormToolkit toolkit = managedForm.getToolkit();
        TextAttributeDescriptor desc = (TextAttributeDescriptor) getDescriptor();
        Label label = toolkit.createLabel(parent, desc.getUiName());
        label.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
        SectionHelper.addControlTooltip(label, DescriptorsUtils.formatTooltip(desc.getTooltip()));
        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE));
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = gl.marginWidth = 0;
        composite.setLayout(gl);
        toolkit.paintBordersFor(composite);
        final Text text = toolkit.createText(composite, getCurrentValue());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalIndent = 1;  
        text.setLayoutData(gd);
        final Button selectButton = toolkit.createButton(composite, "Select...", SWT.PUSH);
        setTextWidget(text);
        selectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                String currentText = getTextWidgetValue();
                String result = showDialog(selectButton.getShell(), currentText);
                if (result != null) {
                    setTextWidgetValue(result);
                }
            }
        });
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        String attr_name = getDescriptor().getXmlLocalName();
        String element_name = getUiParent().getDescriptor().getXmlName();
        String[] values = null;
        if (getDescriptor() instanceof FlagAttributeDescriptor &&
                ((FlagAttributeDescriptor) getDescriptor()).getNames() != null) {
            values = ((FlagAttributeDescriptor) getDescriptor()).getNames();
        }
        if (values == null) {
            UiElementNode uiNode = getUiParent();
            AndroidEditor editor = uiNode.getEditor();
            AndroidTargetData data = editor.getTargetData();
            if (data != null) {
                values = data.getAttributeValues(element_name, attr_name);
            }
        }
        return values;
    }
    public String showDialog(Shell shell, String currentValue) {
        FlagSelectionDialog dlg = new FlagSelectionDialog(
                shell, currentValue.trim().split("\\s*\\|\\s*")); 
        dlg.open();
        Object[] result = dlg.getResult();
        if (result != null) {
            StringBuilder buf = new StringBuilder();
            for (Object name : result) {
                if (name instanceof String) {
                    if (buf.length() > 0) {
                        buf.append("|"); 
                    }
                    buf.append(name);
                }
            }
            return buf.toString();
        }
        return null;
    }
    private class FlagSelectionDialog extends SelectionStatusDialog {
        private Set<String> mCurrentSet;
        private Table mTable;
        public FlagSelectionDialog(Shell parentShell, String[] currentNames) {
            super(parentShell);
            mCurrentSet = new HashSet<String>();
            for (String name : currentNames) {
                if (name.length() > 0) {
                    mCurrentSet.add(name);
                }
            }
            int shellStyle = getShellStyle();
            setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
        }
        @Override
        protected void computeResult() {
            if (mTable != null) {
                ArrayList<String> results = new ArrayList<String>();
                for (TableItem item : mTable.getItems()) {
                    if (item.getChecked()) {
                        results.add((String)item.getData());
                    }
                }
                setResult(results);
            }
        }
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite= new Composite(parent, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            composite.setLayout(new GridLayout(1, true));
            composite.setFont(parent.getFont());
            Label label = new Label(composite, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            label.setText(String.format("Select the flag values for attribute %1$s:",
                    ((FlagAttributeDescriptor) getDescriptor()).getUiName()));
            mTable = new Table(composite, SWT.CHECK | SWT.BORDER);
            GridData data = new GridData();
            data.widthHint = convertWidthInCharsToPixels(60);
            data.heightHint = convertHeightInCharsToPixels(18);
            data.grabExcessVerticalSpace = true;
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.FILL;
            mTable.setLayoutData(data);
            mTable.setHeaderVisible(false);
            final TableColumn column = new TableColumn(mTable, SWT.NONE);
            String[] names = getPossibleValues(null);
            if (names != null) {
                for (String name : names) {
                    TableItem item = new TableItem(mTable, SWT.NONE);
                    item.setText(name);
                    item.setData(name);
                    boolean hasName = mCurrentSet.contains(name);
                    item.setChecked(hasName);
                    if (hasName) {
                        mCurrentSet.remove(name);
                    }
                }
            }
            if (!mCurrentSet.isEmpty()) {
                FontDescriptor fontDesc = JFaceResources.getDialogFontDescriptor();
                fontDesc = fontDesc.withStyle(SWT.ITALIC);
                Font font = fontDesc.createFont(JFaceResources.getDialogFont().getDevice());
                for (String name : mCurrentSet) {
                    TableItem item = new TableItem(mTable, SWT.NONE);
                    item.setText(String.format("%1$s (unknown flag)", name));
                    item.setData(name);
                    item.setChecked(true);
                    item.setFont(font);
                }
            }
            ControlAdapter listener = new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    Rectangle r = mTable.getClientArea();
                    column.setWidth(r.width);
                }
            };
            mTable.addControlListener(listener);
            listener.controlResized(null );
            mTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    if (e.item instanceof TableItem) {
                        TableItem i = (TableItem) e.item;
                        i.setChecked(!i.getChecked());
                    }
                    super.widgetDefaultSelected(e);
                } 
            });
            Dialog.applyDialogFont(composite);            
            setHelpAvailable(false);
            return composite;
        }
    }
}
