final class ManifestEditorContributor extends MultiPageEditorActionBarContributor {
    private IEditorPart mActiveEditorPart;
    private ManifestEditorContributor() {
        super();
    }
    protected IAction getAction(ITextEditor editor, String actionID) {
        return (editor == null ? null : editor.getAction(actionID));
    }
    @Override
    public void setActivePage(IEditorPart part) {
        if (mActiveEditorPart == part)
            return;
        mActiveEditorPart = part;
        IActionBars actionBars = getActionBars();
        if (actionBars != null) {
            ITextEditor editor =
                (part instanceof ITextEditor) ? (ITextEditor)part : null;
            actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
                    getAction(editor, ITextEditorActionConstants.DELETE));
            actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
                    getAction(editor, ITextEditorActionConstants.UNDO));
            actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
                    getAction(editor, ITextEditorActionConstants.REDO));
            actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
                    getAction(editor, ITextEditorActionConstants.CUT));
            actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                    getAction(editor, ITextEditorActionConstants.COPY));
            actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                    getAction(editor, ITextEditorActionConstants.PASTE));
            actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                    getAction(editor, ITextEditorActionConstants.SELECT_ALL));
            actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
                    getAction(editor, ITextEditorActionConstants.FIND));
            actionBars.setGlobalActionHandler(
                    IDEActionFactory.BOOKMARK.getId(), getAction(editor,
                            IDEActionFactory.BOOKMARK.getId()));
            actionBars.updateActionBars();
        }
    }
}
