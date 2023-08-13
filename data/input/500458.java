public class PasteAction extends Action {
    private UiElementNode mUiNode;
    private final AndroidEditor mEditor;
    private final Clipboard mClipboard;
    public PasteAction(AndroidEditor editor, Clipboard clipboard, UiElementNode ui_node) {
        super("Paste");
        mEditor = editor;
        mClipboard = clipboard;
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setDisabledImageDescriptor(
                images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        mUiNode = ui_node;
    }
    @Override
    public void run() {
        super.run();
        final String data = (String) mClipboard.getContents(TextTransfer.getInstance());
        if (data != null) {
            IStructuredModel model = mEditor.getModelForEdit();
            try {
                IStructuredDocument sse_doc = mEditor.getStructuredDocument();
                if (sse_doc != null) {
                    if (mUiNode.getDescriptor().hasChildren()) {
                        if (mUiNode.getUiChildren().size() > 0) {
                            Node xml_node = mUiNode.getUiChildren().get(0).getXmlNode();
                            if (xml_node instanceof IndexedRegion) { 
                                IndexedRegion region = (IndexedRegion) xml_node;
                                sse_doc.replace(region.getStartOffset(), 0, data);
                                return; 
                            }                                
                        }
                        Node xml_node = mUiNode.getXmlNode();
                        if (xml_node instanceof NodeContainer) {
                            NodeContainer container = (NodeContainer) xml_node;
                            IStructuredDocumentRegion start_tag =
                                container.getStartStructuredDocumentRegion();
                            if (start_tag != null) {
                                sse_doc.replace(start_tag.getEndOffset(), 0, data);
                                return; 
                            }
                        }
                    }
                    if (!(mUiNode.getUiParent() instanceof UiDocumentNode)) {
                        Node xml_node = mUiNode.getXmlNode();
                        if (xml_node instanceof IndexedRegion) {
                            IndexedRegion region = (IndexedRegion) xml_node;
                            sse_doc.replace(region.getEndOffset(), 0, data);
                        }
                    }
                }
            } catch (BadLocationException e) {
                AdtPlugin.log(e, "ParseAction failed for UI Node %2$s, content '%1$s'", 
                        mUiNode.getBreadcrumbTrailDescription(true), data);
            } finally {
                model.releaseFromEdit();
            }
        }
    }
}
