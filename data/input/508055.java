public class ElementCreateCommand extends Command {
    private final ElementDescriptor mDescriptor;
    private final UiElementEditPart mParentPart;
    private final Point mTargetPoint;
    public ElementCreateCommand(ElementDescriptor descriptor,
            UiElementEditPart targetPart, Point targetPoint) {
                mDescriptor = descriptor;
                mParentPart = targetPart;
                mTargetPoint = targetPoint;
    }
    @Override
    public boolean canExecute() {
        return mDescriptor != null &&
            mParentPart != null &&
            mParentPart.getUiNode() != null &&
            mParentPart.getUiNode().getEditor() instanceof LayoutEditor;
    }
    @Override
    public void execute() {
        super.execute();
        UiElementNode uiParent = mParentPart.getUiNode();
        if (uiParent != null) {
            final AndroidEditor editor = uiParent.getEditor();
            if (editor instanceof LayoutEditor) {
                ((LayoutEditor) editor).wrapUndoRecording(
                        String.format("Create %1$s", mDescriptor.getXmlLocalName()),
                        new Runnable() {
                    public void run() {
                        UiEditorActions actions = ((LayoutEditor) editor).getUiEditorActions();
                        if (actions != null) {
                            DropFeedback.addElementToXml(mParentPart, mDescriptor, mTargetPoint,
                                    actions);
                        }
                    }
                });
            }
        }
    }
    @Override
    public void redo() {
        throw new UnsupportedOperationException("redo not supported by this command"); 
    }
    @Override
    public void undo() {
        throw new UnsupportedOperationException("undo not supported by this command"); 
    }
}
