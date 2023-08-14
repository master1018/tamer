public class ResourceChooser extends AbstractElementListSelectionDialog {
    private Pattern mProjectResourcePattern;
    private ResourceType mResourceType;
    private IResourceRepository mProjectResources;
    private final static boolean SHOW_SYSTEM_RESOURCE = false;  
    private Pattern mSystemResourcePattern;
    private IResourceRepository mSystemResources;
    private Button mProjectButton;
    private Button mSystemButton;
    private String mCurrentResource;
    private final IProject mProject;
    public ResourceChooser(IProject project, ResourceType type,
            IResourceRepository projectResources,
            IResourceRepository systemResources,
            Shell parent) {
        super(parent, new ResourceLabelProvider());
        mProject = project;
        mResourceType = type;
        mProjectResources = projectResources;
        mProjectResourcePattern = Pattern.compile(
                "@" + mResourceType.getName() + "/(.+)"); 
        if (SHOW_SYSTEM_RESOURCE) {
            mSystemResources = systemResources;
            mSystemResourcePattern = Pattern.compile(
                    "@android:" + mResourceType.getName() + "/(.+)"); 
        }
        setTitle("Resource Chooser");
        setMessage(String.format("Choose a %1$s resource",
                mResourceType.getDisplayName().toLowerCase()));
    }
    public void setCurrentResource(String resource) {
        mCurrentResource = resource;
    }
    public String getCurrentResource() {
        return mCurrentResource;
    }
    @Override
    protected void computeResult() {
        Object[] elements = getSelectedElements();
        if (elements.length == 1 && elements[0] instanceof ResourceItem) {
            ResourceItem item = (ResourceItem)elements[0];
            mCurrentResource = mResourceType.getXmlString(item,
                    SHOW_SYSTEM_RESOURCE && mSystemButton.getSelection()); 
        }
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite top = (Composite)super.createDialogArea(parent);
        createMessageArea(top);
        createButtons(top);
        createFilterText(top);
        createFilteredList(top);
        createNewResButtons(top);
        setupResourceList();
        selectResourceString(mCurrentResource);
        return top;
    }
    private void createButtons(Composite top) {
        if (!SHOW_SYSTEM_RESOURCE) {
            return;
        }
        mProjectButton = new Button(top, SWT.RADIO);
        mProjectButton.setText("Project Resources");
        mProjectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (mProjectButton.getSelection()) {
                    setListElements(mProjectResources.getResources(mResourceType));
                }
            }
        });
        mSystemButton = new Button(top, SWT.RADIO);
        mSystemButton.setText("System Resources");
        mSystemButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (mProjectButton.getSelection()) {
                    setListElements(mSystemResources.getResources(mResourceType));
                }
            }
        });
    }
    private void createNewResButtons(Composite top) {
        Button newResButton = new Button(top, SWT.NONE);
        String title = String.format("New %1$s...", mResourceType.getDisplayName());
        newResButton.setText(title);
        newResButton.setEnabled(mResourceType == ResourceType.STRING);
        newResButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (mResourceType == ResourceType.STRING) {
                    createNewString();
                }
            }
        });
    }
    private void createNewString() {
        ExtractStringRefactoring ref = new ExtractStringRefactoring(
                mProject, true );
        RefactoringWizard wizard = new ExtractStringWizard(ref, mProject);
        RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
        try {
            IWorkbench w = PlatformUI.getWorkbench();
            if (op.run(w.getDisplay().getActiveShell(), wizard.getDefaultPageTitle()) ==
                    IDialogConstants.OK_ID) {
                setupResourceList();
                selectItemName(ref.getXmlStringId());
            }
        } catch (InterruptedException ex) {
        }
    }
    private IResourceRepository getCurrentRepository() {
        IResourceRepository repo = mProjectResources;
        if (SHOW_SYSTEM_RESOURCE && mSystemButton.getSelection()) {
            repo = mSystemResources;
        }
        return repo;
    }
    private void setupResourceList() {
        IResourceRepository repo = getCurrentRepository();
        setListElements(repo.getResources(mResourceType));
    }
    private void selectItemName(String itemName) {
        if (itemName == null) {
            return;
        }
        IResourceRepository repo = getCurrentRepository();
        ResourceItem[] items = repo.getResources(mResourceType); 
        for (ResourceItem item : items) {
            if (itemName.equals(item.getName())) {
                setSelection(new Object[] { item });
                break;
            }
        }
    }
    private void selectResourceString(String resourceString) {
        boolean isSystem = false;
        String itemName = null;
        if (SHOW_SYSTEM_RESOURCE) {
            Matcher m = mSystemResourcePattern.matcher(resourceString);
            if (m.matches()) {
                itemName = m.group(1);
                isSystem = true;
            }
        }
        if (!isSystem && itemName == null) {
            Matcher m = mProjectResourcePattern.matcher(resourceString);
            if (m.matches()) {
                itemName = m.group(1);
            }
        }
        if (SHOW_SYSTEM_RESOURCE) {
            mProjectButton.setSelection(!isSystem);
            mSystemButton.setSelection(isSystem);
        }
        setupResourceList();
        if (itemName != null) {
            selectItemName(itemName);
        }
    }
}
