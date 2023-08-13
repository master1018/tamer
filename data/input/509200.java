public class ResourceExplorerView extends ViewPart implements ISelectionListener,
        IResourceEventListener {
    private final static String PREFS_COLUMN_RES =
        AndroidConstants.EDITORS_NAMESPACE + "ResourceExplorer.Col1"; 
    private final static String PREFS_COLUMN_2 =
        AndroidConstants.EDITORS_NAMESPACE + "ResourceExplorer.Col2"; 
    private Tree mTree;
    private TreeViewer mTreeViewer;
    private IProject mCurrentProject;
    public ResourceExplorerView() {
    }
    @Override
    public void createPartControl(Composite parent) {
        mTree = new Tree(parent, SWT.SINGLE | SWT.VIRTUAL);
        mTree.setLayoutData(new GridData(GridData.FILL_BOTH));
        mTree.setHeaderVisible(true);
        mTree.setLinesVisible(true);
        final IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
        createTreeColumn(mTree, "Resources", SWT.LEFT,
                "abcdefghijklmnopqrstuvwxz", -1, PREFS_COLUMN_RES, store); 
        createTreeColumn(mTree, "Info", SWT.LEFT,
                "0123456789", -1, PREFS_COLUMN_2, store); 
        mTreeViewer = new TreeViewer(mTree);
        mTreeViewer.setContentProvider(new ResourceContentProvider(true ));
        mTreeViewer.setLabelProvider(new ResourceLabelProvider());
        IWorkbenchPage page = getSite().getPage();
        page.addSelectionListener(this);
        selectionChanged(getSite().getPart(), page.getSelection());
        mTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection sel = event.getSelection();
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) sel;
                    if (selection.size() == 1) {
                        Object element = selection.getFirstElement();
                        if (element instanceof ResourceFile) {
                            try {
                                IAbstractFile iAbstractFile = ((ResourceFile)element).getFile();
                                if (iAbstractFile instanceof IFileWrapper) {
                                    IDE.openEditor(getSite().getWorkbenchWindow().getActivePage(),
                                            ((IFileWrapper)iAbstractFile).getIFile());
                                }
                            } catch (PartInitException e) {
                            }
                        } else if (element instanceof ProjectResourceItem) {
                            ProjectResourceItem item = (ProjectResourceItem)element;
                            if (item.isEditableDirectly()) {
                                ResourceFile[] files = item.getSourceFileArray();
                                if (files[0] != null) {
                                    try {
                                        IAbstractFile iAbstractFile = files[0].getFile();
                                        if (iAbstractFile instanceof IFileWrapper) {
                                            IDE.openEditor(
                                                    getSite().getWorkbenchWindow().getActivePage(),
                                                    ((IFileWrapper)iAbstractFile).getIFile());
                                        }
                                    } catch (PartInitException e) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        AdtPlugin.getDefault().getResourceMonitor().addResourceEventListener(this);
    }
    @Override
    public void dispose() {
        AdtPlugin.getDefault().getResourceMonitor().removeResourceEventListener(this);
        super.dispose();
    }
    @Override
    public void setFocus() {
        mTree.setFocus();
    }
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof IEditorPart) {
            IEditorInput input = ((IEditorPart)part).getEditorInput();
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput)input).getFile();
                IProject project = file.getProject();
                handleProjectSelection(project);
            }
        } else if (selection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator();
                    it.hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IResource) {
                    project = ((IResource) element).getProject();
                } else if (element instanceof IJavaElement) {
                    IJavaElement javaElement = (IJavaElement)element;
                    IJavaProject javaProject = javaElement.getJavaProject();
                    if (javaProject != null) {
                        project = javaProject.getProject();
                    }
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element)
                            .getAdapter(IProject.class);
                }
                if (project != null) {
                    if (handleProjectSelection(project)) {
                        return;
                    }
                }
            }
        }
    }
    private boolean handleProjectSelection(IProject project) {
        try {
            if (project.hasNature(AndroidConstants.NATURE)) {
                if (mCurrentProject != project) {
                    ProjectResources projRes = ResourceManager.getInstance().getProjectResources(
                            project);
                    if (projRes != null) {
                        mTreeViewer.setInput(projRes);
                        mCurrentProject = project;
                        return true;
                    }
                }
            }
        } catch (CoreException e) {
        }
        return false;
    }
    public void createTreeColumn(Tree parent, String header, int style,
            String sample_text, int fixedSize, final String pref_name,
            final IPreferenceStore prefs) {
        TreeColumn col = new TreeColumn(parent, style);
        if (fixedSize != -1) {
            col.setWidth(fixedSize);
            col.setResizable(false);
        } else {
            if (prefs == null || prefs.contains(pref_name) == false) {
                col.setText(sample_text);
                col.pack();
                if (prefs != null) {
                    prefs.setValue(pref_name, col.getWidth());
                }
            } else {
                col.setWidth(prefs.getInt(pref_name));
            }
            if (prefs != null && pref_name != null) {
                col.addControlListener(new ControlListener() {
                    public void controlMoved(ControlEvent e) {
                    }
                    public void controlResized(ControlEvent e) {
                        int w = ((TreeColumn)e.widget).getWidth();
                        prefs.setValue(pref_name, w);
                    }
                });
            }
        }
        col.setText(header);
    }
    public void resourceChangeEventStart() {
    }
    public void resourceChangeEventEnd() {
        try {
            mTree.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (mTree.isDisposed() == false) {
                        mTreeViewer.refresh();
                    }
                }
            });
        } catch (SWTException e) {
        }
    }
}
