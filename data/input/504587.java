public class UiPackageAttributeNode extends UiTextAttributeNode {
    public UiPackageAttributeNode(AttributeDescriptor attributeDescriptor, UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public void createUiControl(final Composite parent, final IManagedForm managedForm) {
        setManagedForm(managedForm);
        FormToolkit toolkit = managedForm.getToolkit();
        TextAttributeDescriptor desc = (TextAttributeDescriptor) getDescriptor();
        StringBuilder label = new StringBuilder();
        label.append("<form><p><a href='unused'>");  
        label.append(desc.getUiName());
        label.append("</a></p></form>");  
        FormText formText = SectionHelper.createFormText(parent, toolkit, true ,
                label.toString(), true );
        formText.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                super.linkActivated(e);
                doLabelClick();
            }
        });
        formText.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
        SectionHelper.addControlTooltip(formText, desc.getTooltip());
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
        setTextWidget(text);
        Button browseButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                doBrowseClick();
            }
        });
    }
    @Override
    protected void onAddValidators(final Text text) {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String package_name = text.getText();
                if (package_name.indexOf('.') < 1) {
                    getManagedForm().getMessageManager().addMessage(text,
                            "Package name should contain at least two identifiers.",
                            null , IMessageProvider.ERROR, text);
                } else {
                    getManagedForm().getMessageManager().removeMessage(text, text);
                }
            }
        };
        text.addModifyListener(listener);
        text.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                getManagedForm().getMessageManager().removeMessage(text, text);
            }
        });
        listener.modifyText(null);
    }
    private void doBrowseClick() {
        Text text = getTextWidget();
        IProject project = getProject();
        if (project != null) {
            try {
                SelectionDialog dlg = JavaUI.createPackageDialog(text.getShell(),
                        JavaCore.create(project), 0);
                dlg.setTitle("Select Android Package");
                dlg.setMessage("Select the package for the Android project.");
                SelectionDialog.setDefaultImage(AdtPlugin.getAndroidLogo());
                if (dlg.open() == Window.OK) {
                    Object[] results = dlg.getResult();
                    if (results.length == 1) {
                        setPackageTextField((IPackageFragment)results[0]);
                    }
                }
            } catch (JavaModelException e1) {
            }
        }
    }
    private void doLabelClick() {
        String package_name = getTextWidget().getText().trim();
        if (package_name.length() == 0) {
            createNewPackage();
        } else {
            IProject project = getProject();
            if (project == null) {
                AdtPlugin.log(IStatus.ERROR, "Failed to get project for UiPackageAttribute"); 
                return;
            }
            IWorkbenchPartSite site = getUiParent().getEditor().getSite();
            if (site == null) {
                AdtPlugin.log(IStatus.ERROR, "Failed to get editor site for UiPackageAttribute"); 
                return;
            }
            for (IPackageFragmentRoot root : getPackageFragmentRoots(project)) {
                IPackageFragment fragment = root.getPackageFragment(package_name);
                if (fragment != null && fragment.exists()) {
                    ShowInPackageViewAction action = new ShowInPackageViewAction(site);
                    action.run(fragment);
                    return;
                }
            }
        }
    }
    private IProject getProject() {
        UiElementNode uiNode = getUiParent();
        AndroidEditor editor = uiNode.getEditor();
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            return file.getProject();
        }
        return null;
    }
    private IPackageFragmentRoot[] getPackageFragmentRoots(IProject project) {
        ArrayList<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
        try {
            IJavaProject javaProject = JavaCore.create(project);
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                IClasspathEntry entry = roots[i].getRawClasspathEntry();
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    result.add(roots[i]);
                }
            }
        } catch (JavaModelException e) {
        }
        return result.toArray(new IPackageFragmentRoot[result.size()]);
    }
    private void setPackageTextField(IPackageFragment type) {
        Text text = getTextWidget();
        String name = type.getElementName();
        text.setText(name);
    }
    private void createNewPackage() {
        OpenNewPackageWizardAction action = new OpenNewPackageWizardAction();
        IProject project = getProject();
        action.setSelection(new StructuredSelection(project));
        action.run();
        IJavaElement element = action.getCreatedElement();
        if (element != null &&
                element.exists() &&
                element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
            setPackageTextField((IPackageFragment) element);
        }
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        return null;
    }
}
