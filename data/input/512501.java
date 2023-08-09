public class UiManifestPkgAttrNode extends UiTextAttributeNode {
    public UiManifestPkgAttrNode(AttributeDescriptor attributeDescriptor, UiElementNode uiParent) {
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
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                getTextWidget().getShell(),
                new ILabelProvider() {
                    public Image getImage(Object element) {
                        return null;
                    }
                    public String getText(Object element) {
                        return element.toString();
                    }
                    public void addListener(ILabelProviderListener listener) {
                    }
                    public void dispose() {
                    }
                    public boolean isLabelProperty(Object element, String property) {
                        return false;
                    }
                    public void removeListener(ILabelProviderListener listener) {
                    }
                });
        dialog.setTitle("Android Manifest Package Selection");
        dialog.setMessage("Select the Android Manifest package to target.");
        dialog.setElements(getPossibleValues(null));
        if (dialog.open() == Window.OK) {
            String result = (String) dialog.getFirstResult();
            if (result != null && result.length() > 0) {
                getTextWidget().setText(result);
            }
        }
    }
    private void doLabelClick() {
        String package_name = getTextWidget().getText().trim();
        if (package_name.length() == 0) {
            createNewProject();
        } else {
            displayExistingManifest(package_name);
        }
    }
    private void displayExistingManifest(String package_name) {
        for (IJavaProject project : BaseProjectHelper.getAndroidProjects(null )) {
            IFile manifestFile = AndroidManifestParser.getManifest(project.getProject());
            if (manifestFile == null) {
                continue;
            }
            AndroidManifestParser parser = null;
            try {
                parser = AndroidManifestParser.parseForData(manifestFile);
            } catch (CoreException e) {
            }
            if (parser == null) {
                continue;
            }
            if (package_name.equals(parser.getPackage())) {
                IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (win != null) {
                    IWorkbenchPage page = win.getActivePage();
                    if (page != null) {
                        try {
                            page.openEditor(
                                    new FileEditorInput(manifestFile),
                                    ManifestEditor.ID,
                                    true, 
                                    IWorkbenchPage.MATCH_INPUT);
                        } catch (PartInitException e) {
                            AdtPlugin.log(e,
                                    "Opening editor failed for %s",  
                                    manifestFile.getFullPath());
                        }
                    }
                }
                return;
            }
        }
    }
    private void createNewProject() {
        NewProjectAction npwAction = new NewProjectAction();
        npwAction.run(null );
        if (npwAction.getDialogResult() == Dialog.OK) {
            NewProjectWizard npw = (NewProjectWizard) npwAction.getWizard();
            String name = npw.getPackageName();
            if (name != null && name.length() > 0) {
                getTextWidget().setText(name);
            }
        }
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        TreeSet<String> packages = new TreeSet<String>();
        for (IJavaProject project : BaseProjectHelper.getAndroidProjects(null )) {
            IFile manifestFile = AndroidManifestParser.getManifest(project.getProject());
            if (manifestFile == null) {
                continue;
            }
            AndroidManifestParser parser = null;
            try {
                parser = AndroidManifestParser.parseForData(manifestFile);
            } catch (CoreException e) {
            }
            if (parser == null) {
                continue;
            }
            packages.add(parser.getPackage());
        }
        return packages.toArray(new String[packages.size()]);
    }
}
