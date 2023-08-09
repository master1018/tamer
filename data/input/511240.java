public class ReferenceChooserDialog extends SelectionStatusDialog {
    private static Pattern sResourcePattern = Pattern.compile("@(.*)/(.+)"); 
    private static Pattern sInlineIdResourcePattern = Pattern.compile("@\\+id/(.+)"); 
    private static IDialogSettings sDialogSettings = new DialogSettings("");
    private IResourceRepository mResources;
    private String mCurrentResource;
    private FilteredTree mFilteredTree;
    private Button mNewResButton;
    private final IProject mProject;
    private TreeViewer mTreeViewer;
    public ReferenceChooserDialog(IProject project, IResourceRepository resources, Shell parent) {
        super(parent);
        mProject = project;
        mResources = resources;
        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
        setTitle("Reference Chooser");
        setMessage(String.format("Choose a resource"));
        setDialogBoundsSettings(sDialogSettings, getDialogBoundsStrategy());
    }
    public void setCurrentResource(String resource) {
        mCurrentResource = resource;
    }
    public String getCurrentResource() {
        return mCurrentResource;
    }
    @Override
    protected void computeResult() {
        TreePath treeSelection = getSelection();
        if (treeSelection != null) {
            if (treeSelection.getSegmentCount() == 2) {
                ResourceType resourceType = (ResourceType)treeSelection.getFirstSegment();
                ResourceItem resourceItem = (ResourceItem)treeSelection.getLastSegment();
                mCurrentResource = resourceType.getXmlString(resourceItem, false ); 
            }
        }
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite top = (Composite)super.createDialogArea(parent);
        createMessageArea(top);
        createFilteredTree(top);
        setupInitialSelection();
        createNewResButtons(top);
        return top;
    }
    private void createNewResButtons(Composite top) {
        mNewResButton = new Button(top, SWT.NONE);
        mNewResButton.addSelectionListener(new OnNewResButtonSelected());
        updateNewResButton();
    }
    private void createFilteredTree(Composite parent) {
        mFilteredTree = new FilteredTree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION,
                new PatternFilter());
        GridData data = new GridData();
        data.widthHint = convertWidthInCharsToPixels(60);
        data.heightHint = convertHeightInCharsToPixels(18);
        data.grabExcessVerticalSpace = true;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        mFilteredTree.setLayoutData(data);
        mFilteredTree.setFont(parent.getFont());
        mTreeViewer = mFilteredTree.getViewer();
        Tree tree = mTreeViewer.getTree();
        tree.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDoubleClick();
            }
            public void widgetSelected(SelectionEvent e) {
                handleSelection();
            }
        });
        mTreeViewer.setLabelProvider(new ResourceLabelProvider());
        mTreeViewer.setContentProvider(new ResourceContentProvider(false ));
        mTreeViewer.setInput(mResources);
    }
    protected void handleSelection() {
        validateCurrentSelection();
        updateNewResButton();
    }
    protected void handleDoubleClick() {
        if (validateCurrentSelection()) {
            buttonPressed(IDialogConstants.OK_ID);
        }
    }
    private TreePath getSelection() {
        ISelection selection = mFilteredTree.getViewer().getSelection();
        if (selection instanceof TreeSelection) {
            TreeSelection treeSelection = (TreeSelection)selection;
            TreePath[] treePaths = treeSelection.getPaths();
            if (treePaths.length > 0) {
                return treePaths[0];
            }
        }
        return null;
    }
    private boolean validateCurrentSelection() {
        TreePath treeSelection = getSelection();
        IStatus status;
        if (treeSelection != null) {
            if (treeSelection.getSegmentCount() == 2) {
                status = new Status(IStatus.OK, AdtPlugin.PLUGIN_ID,
                        IStatus.OK, "", 
                        null);
            } else {
                status = new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                        IStatus.ERROR, "You must select a Resource Item",
                        null);
            }
        } else {
            status = new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                    IStatus.ERROR, "", 
                    null);
        }
        updateStatus(status);
        return status.isOK();
    }
    private void updateNewResButton() {
        ResourceType type = getSelectedResourceType();
        mNewResButton.setEnabled(type == ResourceType.STRING);
        String title = String.format("New %1$s...",
                type == null ? "Resource" : type.getDisplayName());
        mNewResButton.setText(title);
        mNewResButton.pack();
    }
    private class OnNewResButtonSelected extends SelectionAdapter {
        @Override
         public void widgetSelected(SelectionEvent e) {
             super.widgetSelected(e);
             ResourceType type = getSelectedResourceType();
             if (type == ResourceType.STRING) {
                 ExtractStringRefactoring ref = new ExtractStringRefactoring(
                         mProject, true );
                 RefactoringWizard wizard = new ExtractStringWizard(ref, mProject);
                 RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
                 try {
                     IWorkbench w = PlatformUI.getWorkbench();
                     if (op.run(w.getDisplay().getActiveShell(), wizard.getDefaultPageTitle()) ==
                             IDialogConstants.OK_ID) {
                         mTreeViewer.refresh();
                         setupInitialSelection(type, ref.getXmlStringId());
                     }
                 } catch (InterruptedException ex) {
                 }
             }
         } 
     }
    private ResourceType getSelectedResourceType() {
        ResourceType type = null;
        TreePath selection = getSelection();
        if (selection != null && selection.getSegmentCount() > 0) {
            Object first = selection.getFirstSegment();
            if (first instanceof ResourceType) {
                type = (ResourceType) first;
            }
        }
        return type;
    }
    private void setupInitialSelection() {
        Matcher m = sInlineIdResourcePattern.matcher(mCurrentResource);
        if (m.matches()) {
            String resourceName = m.group(1);
            setupInitialSelection(ResourceType.ID, resourceName);
        } else {
            m = sResourcePattern.matcher(mCurrentResource);
            if (m.matches()) {
                ResourceType resourceType = ResourceType.getEnum(m.group(1));
                if (resourceType != null) {
                    String resourceName = m.group(2);
                    setupInitialSelection(resourceType, resourceName);
                }
            }
        }
    }
    private void setupInitialSelection(ResourceType resourceType, String resourceName) {
        ResourceItem[] resourceItems = mResources.getResources(resourceType);
        for (ResourceItem resourceItem : resourceItems) {
            if (resourceName.equals(resourceItem.getName())) {
                TreePath treePath = new TreePath(new Object[] { resourceType, resourceItem });
                mFilteredTree.getViewer().setSelection(
                        new TreeSelection(treePath),
                        true );
                return;
            }
        }
        TreePath treePath = new TreePath(new Object[] { resourceType });
        mFilteredTree.getViewer().setSelection(
                new TreeSelection(treePath),
                true );
        mFilteredTree.getViewer().setExpandedState(resourceType, true );
    }
}
