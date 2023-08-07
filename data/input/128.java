public class CloseAllAction extends Action {
    private static final String ACTION_TEXT = Messages.getString("CloseAllAction.0");
    private MultiPageEditorPart editor;
    private CTabFolder folder;
    public CloseAllAction(MultiPageEditorPart editor, CTabFolder folder) {
        super();
        this.editor = editor;
        this.folder = folder;
    }
    public String getText() {
        return ACTION_TEXT;
    }
    public void run() {
        for (int i = folder.getItemCount() - 1; i > 0; i--) {
            editor.removePage(i);
        }
    }
}
