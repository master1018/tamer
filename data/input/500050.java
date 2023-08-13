public class UiClassAttributeNode extends UiTextAttributeNode {
    private String mReferenceClass;
    private IPostTypeCreationAction mPostCreationAction;
    private boolean mMandatory;
    private final boolean mDefaultToProjectOnly;
    private class HierarchyTypeSelection extends TypeSelectionExtension {
        private IJavaProject mJavaProject;
        private IType mReferenceType;
        private Button mProjectOnly;
        private boolean mUseProjectOnly;
        public HierarchyTypeSelection(IProject project, String referenceClass)
                throws JavaModelException {
            mJavaProject = JavaCore.create(project);
            mReferenceType = mJavaProject.findType(referenceClass);
        }
        @Override
        public ITypeInfoFilterExtension getFilterExtension() {
            return new ITypeInfoFilterExtension() {
                public boolean select(ITypeInfoRequestor typeInfoRequestor) {
                    boolean projectOnly = mUseProjectOnly;
                    String packageName = typeInfoRequestor.getPackageName();
                    String typeName = typeInfoRequestor.getTypeName();
                    String enclosingType = typeInfoRequestor.getEnclosingName();
                    StringBuilder sb = new StringBuilder(packageName);
                    sb.append('.');
                    if (enclosingType.length() > 0) {
                        sb.append(enclosingType);
                        sb.append('.');
                    }
                    sb.append(typeName);
                    String className = sb.toString();
                    try {
                        IType type = mJavaProject.findType(className);
                        if (type == null) {
                            return false;
                        }
                        if ((type.getFlags() & Flags.AccAbstract) != 0) {
                            return false;
                        }
                        if (projectOnly) {
                            IPackageFragment frag = type.getPackageFragment();
                            if (frag == null || frag.getKind() != IPackageFragmentRoot.K_SOURCE) {
                                return false;
                            }
                        }
                        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(
                                new NullProgressMonitor());
                        IType[] supertypes = hierarchy.getAllSupertypes(type);
                        int n = supertypes.length;
                        for (int i = 0; i < n; i++) {
                            IType st = supertypes[i];
                            if (mReferenceType.equals(st)) {
                                return true;
                            }
                        }
                    } catch (JavaModelException e) {
                    }
                    return false;
                }
            };
        }
        @Override
        public Control createContentArea(Composite parent) {
            mProjectOnly = new Button(parent, SWT.CHECK);
            mProjectOnly.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mProjectOnly.setText(String.format("Display classes from sources of project '%s' only",
                    mJavaProject.getProject().getName()));
            mUseProjectOnly = mDefaultToProjectOnly;
            mProjectOnly.setSelection(mUseProjectOnly);
            mProjectOnly.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    super.widgetSelected(e);
                    mUseProjectOnly = mProjectOnly.getSelection();
                    getTypeSelectionComponent().triggerSearch();
                }
            });
            return super.createContentArea(parent);
        }
    }
    public static interface IPostTypeCreationAction {
        public void processNewType(IType newType);
    }
    public UiClassAttributeNode(String referenceClass, IPostTypeCreationAction postCreationAction,
            boolean mandatory, AttributeDescriptor attributeDescriptor, UiElementNode uiParent,
            boolean defaultToProjectOnly) {
        super(attributeDescriptor, uiParent);
        mReferenceClass = referenceClass;
        mPostCreationAction = postCreationAction;
        mMandatory = mandatory;
        mDefaultToProjectOnly = defaultToProjectOnly;
    }
    @Override
    public void createUiControl(final Composite parent, IManagedForm managedForm) {
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
                handleLabelClick();
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
        Button browseButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        setTextWidget(text);
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                handleBrowseClick();
            }
        });
    }
    @Override
    protected void onAddValidators(final Text text) {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                    String textValue = text.getText().trim();
                    if (textValue.length() == 0) {
                        if (mMandatory) {
                            setErrorMessage("Value is mandatory", text);
                        } else {
                            setErrorMessage(null, text);
                        }
                        return;
                    }
                    String javaPackage = getManifestPackage();
                    String className = AndroidManifest.combinePackageAndClassName(
                            javaPackage, textValue);
                    boolean testVisibility = AndroidConstants.CLASS_ACTIVITY.equals(
                            mReferenceClass);
                    setErrorMessage(BaseProjectHelper.testClassForManifest(
                            BaseProjectHelper.getJavaProject(getProject()), className,
                            mReferenceClass, testVisibility), text);
                } catch (CoreException ce) {
                    setErrorMessage(ce.getMessage(), text);
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
    private void handleBrowseClick() {
        Text text = getTextWidget();
        IProject project = getProject();
        if (project != null) {
            IPackageFragmentRoot[] packageFragmentRoots = getPackageFragmentRoots(project,
                    true );
            IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                    packageFragmentRoots,
                    false);
            try {
                SelectionDialog dlg = JavaUI.createTypeDialog(text.getShell(),
                    PlatformUI.getWorkbench().getProgressService(),
                    scope,
                    IJavaElementSearchConstants.CONSIDER_CLASSES,  
                    false, 
                    "**",  
                    new HierarchyTypeSelection(project, mReferenceClass));
                dlg.setMessage(String.format("Select class name for element %1$s:",
                        getUiParent().getBreadcrumbTrailDescription(false )));
                if (dlg instanceof ITypeSelectionComponent) {
                    ((ITypeSelectionComponent)dlg).triggerSearch();
                }
                if (dlg.open() == Window.OK) {
                    Object[] results = dlg.getResult();
                    if (results.length == 1) {
                        handleNewType((IType)results[0]);
                    }
                }
            } catch (JavaModelException e1) {
                AdtPlugin.log(e1, "UiClassAttributeNode HandleBrowser failed");
            }
        }
    }
    private void handleLabelClick() {
        String className = getTextWidget().getText().trim();
        String packageName = getManifestPackage();
        if (className.length() == 0) {
            createNewClass(packageName, null );
        } else {
            String fullClassName = className;
            if (className.startsWith(".")) { 
                fullClassName = packageName + className;
            } else {
                String[] segments = className.split(AndroidConstants.RE_DOT);
                if (segments.length == 1) {
                    fullClassName = packageName + "." + className; 
                }
            }
            fullClassName = fullClassName.replaceAll("\\$", "\\."); 
            IProject project = getProject();
            IJavaProject javaProject = JavaCore.create(project);
            try {
                IType result = javaProject.findType(fullClassName);
                if (result != null) {
                    JavaUI.openInEditor(result);
                } else {
                    int index = fullClassName.lastIndexOf('.');
                    if (index != -1) {
                        createNewClass(fullClassName.substring(0, index),
                                fullClassName.substring(index+1));
                    } else {
                        createNewClass(packageName, className);
                    }
                }
            } catch (JavaModelException e) {
                AdtPlugin.log(e, "UiClassAttributeNode HandleLabel failed");
            } catch (PartInitException e) {
                AdtPlugin.log(e, "UiClassAttributeNode HandleLabel failed");
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
    private String getManifestPackage() {
        UiElementNode rootNode = getUiParent().getUiRoot();
        Element xmlElement = (Element) rootNode.getXmlNode();
        if (xmlElement != null) {
            return xmlElement.getAttribute(AndroidManifestDescriptors.PACKAGE_ATTR);
        }
        return ""; 
    }
    private IPackageFragmentRoot[] getPackageFragmentRoots(IProject project,
            boolean include_containers) {
        ArrayList<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
        try {
            IJavaProject javaProject = JavaCore.create(project);
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                IClasspathEntry entry = roots[i].getRawClasspathEntry();
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE ||
                        (include_containers &&
                                entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER)) {
                    result.add(roots[i]);
                }
            }
        } catch (JavaModelException e) {
        }
        return result.toArray(new IPackageFragmentRoot[result.size()]);
    }
    private void handleNewType(IType type) {
        Text text = getTextWidget();
        String name = type.getFullyQualifiedName('$');
        String packageValue = getManifestPackage();
        if (packageValue.length() > 0 && name.startsWith(packageValue)) {
            name = name.substring(packageValue.length() + 1);
            String[] packages = name.split(AndroidConstants.RE_DOT);
            if (packages.length == 1) {
                text.setText(name);
            } else {
                text.setText("." + name); 
            }
        } else {
            text.setText(name);
        }
    }
    private void createNewClass(String packageName, String className) {
        NewClassWizardPage page = new NewClassWizardPage();
        page.setSuperClass(mReferenceClass, true );
        IPackageFragmentRoot[] roots = getPackageFragmentRoots(getProject(),
                true );
        IPackageFragmentRoot currentRoot = null;
        IPackageFragment currentFragment = null;
        int packageMatchCount = -1;
        for (IPackageFragmentRoot root : roots) {
            IPackageFragment fragment = root.getPackageFragment(packageName);
            if (fragment != null && fragment.exists()) {
                currentRoot = root;
                currentFragment = fragment;
                packageMatchCount = -1;
                break;
            } else {
                try {
                    IJavaElement[] children;
                    children = root.getChildren();
                    for (IJavaElement child : children) {
                        if (child instanceof IPackageFragment) {
                            fragment = (IPackageFragment)child;
                            if (packageName.startsWith(fragment.getElementName())) {
                                String[] segments = fragment.getElementName().split("\\."); 
                                if (segments.length > packageMatchCount) {
                                    packageMatchCount = segments.length;
                                    currentFragment = fragment;
                                    currentRoot = root;
                                }
                            }
                        }
                    }
                } catch (JavaModelException e) {
                }
            }
        }
        ArrayList<IPackageFragment> createdFragments = null;
        if (currentRoot != null) {
            if (packageMatchCount == -1) {
                page.setPackageFragmentRoot(currentRoot, true );
                page.setPackageFragment(currentFragment, true );
            } else {
                try {
                    createdFragments = new ArrayList<IPackageFragment>();
                    int totalCount = packageName.split("\\.").length; 
                    int count = 0;
                    int index = -1;
                    while (count < packageMatchCount) {
                        index = packageName.indexOf('.', index+1);
                        count++;
                    }
                    while (count < totalCount - 1) {
                        index = packageName.indexOf('.', index+1);
                        count++;
                        createdFragments.add(currentRoot.createPackageFragment(
                                packageName.substring(0, index),
                                true , new NullProgressMonitor()));
                    }
                    createdFragments.add(currentRoot.createPackageFragment(
                            packageName, true , new NullProgressMonitor()));
                    page.setPackageFragmentRoot(currentRoot, true );
                    page.setPackageFragment(createdFragments.get(createdFragments.size()-1),
                            true );
                } catch (JavaModelException e) {
                    for (IPackageFragmentRoot root : roots) {
                        IPackageFragment fragment = root.getPackageFragment(packageName);
                        if (fragment != null && fragment.exists()) {
                            page.setPackageFragmentRoot(root, true );
                            page.setPackageFragment(fragment, true );
                            break;
                        }
                    }
                }
            }
        } else if (roots.length > 0) {
            page.setPackageFragmentRoot(roots[0], true );
        }
        if (className != null) {
            page.setTypeName(className, true );
        }
        OpenNewClassWizardAction action = new OpenNewClassWizardAction();
        action.setConfiguredWizardPage(page);
        action.run();
        IJavaElement element = action.getCreatedElement();
        if (element != null) {
            if (element.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)element;
                if (mPostCreationAction != null) {
                    mPostCreationAction.processNewType(type);
                }
                handleNewType(type);
            }
        } else {
            if (createdFragments != null) {
                try {
                    for (int i = createdFragments.size() - 1 ; i >= 0 ; i--) {
                        createdFragments.get(i).delete(true , new NullProgressMonitor());
                    }
                } catch (JavaModelException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private final void setErrorMessage(String message, Text textWidget) {
        if (message != null) {
            setHasError(true);
            getManagedForm().getMessageManager().addMessage(textWidget, message, null ,
                    IMessageProvider.ERROR, textWidget);
        } else {
            setHasError(false);
            getManagedForm().getMessageManager().removeMessage(textWidget, textWidget);
        }
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        return null;
    }
}
