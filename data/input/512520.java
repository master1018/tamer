public class NewXmlFileWizard extends Wizard implements INewWizard {
    private static final String PROJECT_LOGO_LARGE = "android_large"; 
    protected static final String MAIN_PAGE_NAME = "newAndroidXmlFilePage"; 
    private NewXmlFileCreationPage mMainPage;
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setHelpAvailable(false); 
        setWindowTitle("New Android XML File");
        setImageDescriptor();
        mMainPage = createMainPage();
        mMainPage.setTitle("New Android XML File");
        mMainPage.setDescription("Creates a new Android XML file.");
        mMainPage.setInitialSelection(selection);
    }
    protected NewXmlFileCreationPage createMainPage() {
        return new NewXmlFileCreationPage(MAIN_PAGE_NAME);
    }
    @Override
    public void addPages() {
        addPage(mMainPage);
    }
    @Override
    public boolean performFinish() {
        IFile file = createXmlFile();
        if (file == null) {
            return false;
        } else {
            IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (win != null) {
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    try {
                        IDE.openEditor(page, file);
                    } catch (PartInitException e) {
                        AdtPlugin.log(e, "Failed to create %1$s: missing type",  
                                file.getFullPath().toString());
                    }
                }
            }
            return true;
        }
    }
    private IFile createXmlFile() {
        IFile file = mMainPage.getDestinationFile();
        String name = file.getFullPath().toString();
        boolean need_delete = false;
        if (file.exists()) {
            if (!AdtPlugin.displayPrompt("New Android XML File",
                String.format("Do you want to overwrite the file %1$s ?", name))) {
                return null;
            }
            need_delete = true;
        } else {
            createWsParentDirectory(file.getParent());
        }
        TypeInfo type = mMainPage.getSelectedType();
        if (type == null) {
            AdtPlugin.log(IStatus.ERROR, "Failed to create %1$s: missing type", name);  
            return null;
        }
        String xmlns = type.getXmlns();
        String root = mMainPage.getRootElement();
        if (root == null) {
            AdtPlugin.log(IStatus.ERROR, "Failed to create %1$s: missing root element", 
                    file.toString());
            return null;
        }
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");   
        sb.append('<').append(root);
        if (xmlns != null) {
            sb.append('\n').append("  xmlns:android=\"").append(xmlns).append("\"");  
        }
        String attrs = type.getDefaultAttrs();
        if (attrs != null) {
            sb.append("\n  ");                       
            sb.append(attrs.replace("\n", "\n  "));  
        }
        sb.append(">\n");                            
        sb.append("</").append(root).append(">\n");  
        String result = sb.toString();
        String error = null;
        try {
            byte[] buf = result.getBytes("UTF8");
            InputStream stream = new ByteArrayInputStream(buf);
            if (need_delete) {
                file.delete(IFile.KEEP_HISTORY | IFile.FORCE, null );
            }
            file.create(stream, true , null );
            return file;
        } catch (UnsupportedEncodingException e) {
            error = e.getMessage();
        } catch (CoreException e) {
            error = e.getMessage();
        }
        error = String.format("Failed to generate %1$s: %2$s", name, error);
        AdtPlugin.displayError("New Android XML File", error);
        return null;
    }
    private boolean createWsParentDirectory(IContainer wsPath) {
        if (wsPath.getType() == IContainer.FOLDER) {
            if (wsPath == null || wsPath.exists()) {
                return true;
            }
            IFolder folder = (IFolder) wsPath;
            try {
                if (createWsParentDirectory(wsPath.getParent())) {
                    folder.create(true , true , null );
                    return true;
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private void setImageDescriptor() {
        ImageDescriptor desc = IconFactory.getInstance().getImageDescriptor(PROJECT_LOGO_LARGE);
        setDefaultPageImageDescriptor(desc);
    }
}
