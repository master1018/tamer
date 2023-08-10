public class AddIndicationMenuAction extends Action implements ISelectionListener, ActionFactory.IWorkbenchAction {
    private final IWorkbenchWindow window;
    public static final String ID = "org.gello.client.actions.contextMenu.AddIndication";
    private IStructuredSelection selection;
    public AddIndicationMenuAction(IWorkbenchWindow window) {
        this.window = window;
        setId(ID);
        setText("&Add Indication");
        setToolTipText("Add Indication.");
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, IImageKeys.ADD_INDICATION));
        window.getSelectionService().addSelectionListener(this);
    }
    public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
        if (part instanceof Browser) {
            if (incoming instanceof IStructuredSelection) {
                selection = (IStructuredSelection) incoming;
                setEnabled(true);
            } else setEnabled(false);
        }
    }
    public void dispose() {
        window.getSelectionService().removeSelectionListener(this);
    }
    public void run() {
        GelloNode gelloNode = (GelloNode) selection.getFirstElement();
        try {
            GelloNode structure = Application.getManager().addStructure(Session.getInstance().getCurrentProject().getTemplateType(), "Indication", gelloNode.getPath(), null);
            if (structure != null) gelloNode.getChildren().add(structure);
        } catch (ServerException_Exception e) {
            e.printStackTrace();
        }
        Browser.viewer.refresh();
    }
}
