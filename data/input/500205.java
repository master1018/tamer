public class CopyCutAction extends Action {
    private List<UiElementNode> mUiNodes;
    private boolean mPerformCut;
    private final AndroidEditor mEditor;
    private final Clipboard mClipboard;
    private final ICommitXml mXmlCommit;
    public CopyCutAction(AndroidEditor editor, Clipboard clipboard, ICommitXml xmlCommit,
            UiElementNode selected, boolean perform_cut) {
        this(editor, clipboard, xmlCommit, toList(selected), perform_cut);
    }
    public CopyCutAction(AndroidEditor editor, Clipboard clipboard, ICommitXml xmlCommit,
            List<UiElementNode> selected, boolean perform_cut) {
        super(perform_cut ? "Cut" : "Copy");
        mEditor = editor;
        mClipboard = clipboard;
        mXmlCommit = xmlCommit;
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        if (perform_cut) {
            setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
            setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
            setDisabledImageDescriptor(
                    images.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        } else {
            setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
            setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
            setDisabledImageDescriptor(
                    images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        }
        mUiNodes = selected;
        mPerformCut = perform_cut;
    }
    @Override
    public void run() {
        super.run();
        if (mUiNodes == null || mUiNodes.size() < 1) {
            return;
        }
        if (mXmlCommit != null) {
            mXmlCommit.commitPendingXmlChanges();
        }
        StringBuilder allText = new StringBuilder();
        ArrayList<UiElementNode> nodesToCut = mPerformCut ? new ArrayList<UiElementNode>() : null;
        for (UiElementNode uiNode : mUiNodes) {
            try {            
                Node xml_node = uiNode.getXmlNode();
                if (xml_node == null) {
                    return;
                }
                String data = getXmlTextFromEditor(xml_node);
                if (data == null) {
                    data = getXmlTextFromSerialization(xml_node);
                }
                if (data != null) {
                    allText.append(data);
                    if (mPerformCut) {
                        nodesToCut.add(uiNode);
                    }
                }
            } catch (Exception e) {
                AdtPlugin.log(e, "CopyCutAction failed for UI node %1$s", 
                        uiNode.getBreadcrumbTrailDescription(true));
            }
        } 
        if (allText != null && allText.length() > 0) {
            mClipboard.setContents(
                    new Object[] { allText.toString() },
                    new Transfer[] { TextTransfer.getInstance() });
            if (mPerformCut) {
                for (UiElementNode uiNode : nodesToCut) {
                    uiNode.deleteXmlNode();
                }
            }
        }
    }
    private String getXmlTextFromEditor(Node xml_node) {
        String data = null;
        IStructuredModel model = mEditor.getModelForRead();
        try {
            IStructuredDocument sse_doc = mEditor.getStructuredDocument();
            if (xml_node instanceof NodeContainer) {
                data = ((NodeContainer) xml_node).getSource();
            } else  if (xml_node instanceof IndexedRegion && sse_doc != null) {
                IndexedRegion region = (IndexedRegion) xml_node;
                int start = region.getStartOffset();
                int end = region.getEndOffset();
                if (end > start) {
                    data = sse_doc.get(start, end - start);
                }
            }
        } catch (BadLocationException e) {
        } finally {
            model.releaseFromRead();
        }
        return data;
    }
    private String getXmlTextFromSerialization(Node xml_node) throws IOException {
        String data;
        StringWriter sw = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(sw,
                new OutputFormat(Method.XML,
                        OutputFormat.Defaults.Encoding ,
                        true ));
        serializer.serialize((Element) xml_node);
        data = sw.toString();
        return data;
    }
    private static ArrayList<UiElementNode> toList(UiElementNode selected) {
        ArrayList<UiElementNode> list = null;
        if (selected != null) {
            list = new ArrayList<UiElementNode>(1);
            list.add(selected);
        }
        return list;
    }
}
