public class FileExplorerView extends ViewPart implements ISelectionListener {
    public static final String ID =
        "com.android.ide.eclipse.ddms.views.FileExplorerView"; 
    private final static String COLUMN_NAME =
        DdmsPlugin.PLUGIN_ID + ".explorer.name"; 
    private final static  String COLUMN_SIZE =
        DdmsPlugin.PLUGIN_ID + ".explorer.size"; 
    private final static String COLUMN_DATE =
        DdmsPlugin.PLUGIN_ID + ".explorer.data"; 
    private final static String COLUMN_TIME =
        DdmsPlugin.PLUGIN_ID + ".explorer.time"; 
    private final static String COLUMN_PERMISSIONS =
        DdmsPlugin.PLUGIN_ID +".explorer.permissions"; 
    private final static String COLUMN_INFO =
        DdmsPlugin.PLUGIN_ID + ".explorer.info"; 
    private DeviceExplorer mExplorer;
    public FileExplorerView() {
    }
    @Override
    public void createPartControl(Composite parent) {
        DeviceExplorer.COLUMN_NAME = COLUMN_NAME;
        DeviceExplorer.COLUMN_SIZE = COLUMN_SIZE;
        DeviceExplorer.COLUMN_DATE = COLUMN_DATE;
        DeviceExplorer.COLUMN_TIME = COLUMN_TIME;
        DeviceExplorer.COLUMN_PERMISSIONS = COLUMN_PERMISSIONS;
        DeviceExplorer.COLUMN_INFO = COLUMN_INFO;
        mExplorer = new DeviceExplorer();
        mExplorer.setImages(PlatformUI.getWorkbench()
                .getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE),
                PlatformUI.getWorkbench() .getSharedImages().getImage(
                        ISharedImages.IMG_OBJ_FOLDER),
                DdmsPlugin.getImageLoader().loadDescriptor("android.png") 
                        .createImage(),
                PlatformUI.getWorkbench() .getSharedImages().getImage(
                        ISharedImages.IMG_OBJ_ELEMENT));
        CommonAction pushAction = new CommonAction("Push File...") {
            @Override
            public void run() {
                mExplorer.pushIntoSelection();
            }
        };
        pushAction.setToolTipText("Push a file onto the device");
        pushAction.setImageDescriptor(DdmsPlugin.getImageLoader()
                .loadDescriptor("push.png")); 
        pushAction.setEnabled(false);
        CommonAction pullAction = new CommonAction("Pull File...") {
            @Override
            public void run() {
                mExplorer.pullSelection();
            }
        };
        pullAction.setToolTipText("Pull a file from the device");
        pullAction.setImageDescriptor(DdmsPlugin.getImageLoader()
                .loadDescriptor("pull.png")); 
        pullAction.setEnabled(false);
        CommonAction deleteAction = new CommonAction("Delete") {
            @Override
            public void run() {
                mExplorer.deleteSelection();
            }
        };
        deleteAction.setToolTipText("Delete the selection");
        deleteAction.setImageDescriptor(DdmsPlugin.getImageLoader()
                .loadDescriptor("delete.png")); 
        deleteAction.setEnabled(false);
        mExplorer.setActions(pushAction, pullAction, deleteAction);
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager menuManager = actionBars.getMenuManager();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        menuManager.add(pullAction);
        menuManager.add(pushAction);
        menuManager.add(new Separator());
        menuManager.add(deleteAction);
        toolBarManager.add(pullAction);
        toolBarManager.add(pushAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(deleteAction);
        mExplorer.createPanel(parent);
        DdmsPlugin.getDefault().addSelectionListener(this);
    }
    @Override
    public void setFocus() {
        mExplorer.setFocus();
    }
    public void selectionChanged(Client selectedClient) {
    }
    public void selectionChanged(IDevice selectedDevice) {
        mExplorer.switchDevice(selectedDevice);
    }
    public void selectionRemoved() {
    }
}
