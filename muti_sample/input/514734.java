public class UiResourceAttributeNode extends UiTextAttributeNode {
    private ResourceType mType;
    public UiResourceAttributeNode(ResourceType type,
            AttributeDescriptor attributeDescriptor, UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
        mType = type;
    }
    @Override
    public void createUiControl(final Composite parent, IManagedForm managedForm) {
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
        Button browseButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        setTextWidget(text);
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = showDialog(parent.getShell(), text.getText().trim());
                if (result != null) {
                    text.setText(result);
                }
            }
        });
    }
    public String showDialog(Shell shell, String currentValue) {
        UiElementNode uiNode = getUiParent();
        AndroidEditor editor = uiNode.getEditor();
        IProject project = editor.getProject();
        if (project != null) {
            IResourceRepository projectRepository =
                ResourceManager.getInstance().getProjectResources(project);
            if (mType != null) {
                AndroidTargetData data = editor.getTargetData();
                IResourceRepository systemRepository = data.getSystemResources();
                ResourceChooser dlg = new ResourceChooser(project,
                        mType,
                        projectRepository,
                        systemRepository,
                        shell);
                dlg.setCurrentResource(currentValue);
                if (dlg.open() == Window.OK) {
                    return dlg.getCurrentResource();
                }
            } else {
                ReferenceChooserDialog dlg = new ReferenceChooserDialog(
                        project,
                        projectRepository,
                        shell);
                dlg.setCurrentResource(currentValue);
                if (dlg.open() == Window.OK) {
                    return dlg.getCurrentResource();
                }
            }
        }
        return null;
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        IResourceRepository repository = null;
        boolean isSystem = false;
        UiElementNode uiNode = getUiParent();
        AndroidEditor editor = uiNode.getEditor();
        if (prefix == null || prefix.indexOf("android:") < 0) {                 
            IProject project = editor.getProject();
            if (project != null) {
                repository = ResourceManager.getInstance().getProjectResources(project);
            }
        } else {
            AndroidTargetData data = editor.getTargetData();
            repository = data.getSystemResources();
            isSystem = true;
        }
        ResourceType[] resTypes = (repository != null) ?
                    repository.getAvailableResourceTypes() :
                    ResourceType.values();
        String typeName = null;
        if (prefix != null) {
            Matcher m = Pattern.compile(".*?([a-z]+)/.*").matcher(prefix);      
            if (m.matches()) {
                typeName = m.group(1);
            }
        }
        ArrayList<String> results = new ArrayList<String>();
        if (typeName == null) {
            for (ResourceType resType : resTypes) {
                results.add("@" + resType.getName() + "/");         
                if (resType == ResourceType.ID) {
                    results.add("@+" + resType.getName() + "/");    
                }
            }
        } else if (repository != null) {
            ResourceType resType = ResourceType.getEnum(typeName);
            if (resType != null) {
                StringBuilder sb = new StringBuilder();
                sb.append('@');
                if (prefix != null && prefix.indexOf('+') >= 0) {
                    sb.append('+');
                }
                if (isSystem) {
                    sb.append("android:");                                  
                }
                sb.append(typeName).append('/');
                String base = sb.toString();
                for (ResourceItem item : repository.getResources(resType)) {
                    results.add(base + item.getName());
                }
            }
        }
        return results.toArray(new String[results.size()]);
    }
}
