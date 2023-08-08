public class FoldingActionGroup extends ActionGroup {
    private abstract static class PreferenceAction extends ResourceAction implements IUpdate {
        PreferenceAction(ResourceBundle bundle, String prefix, int style) {
            super(bundle, prefix, style);
        }
    }
    private class FoldingAction extends PreferenceAction {
        FoldingAction(ResourceBundle bundle, String prefix) {
            super(bundle, prefix, IAction.AS_PUSH_BUTTON);
        }
        public void update() {
            setEnabled(FoldingActionGroup.this.isEnabled() && fViewer.isProjectionMode());
        }
    }
    private ProjectionViewer fViewer;
    private final PreferenceAction fToggle;
    private final TextOperationAction fExpand;
    private final TextOperationAction fCollapse;
    private final TextOperationAction fExpandAll;
    private final PreferenceAction fRestoreDefaults;
    private final FoldingAction fCollapseMembers;
    private final FoldingAction fCollapseComments;
    private final TextOperationAction fCollapseAll;
    private final IProjectionListener fProjectionListener;
    public FoldingActionGroup(final ITextEditor editor, ITextViewer viewer) {
        if (!(viewer instanceof ProjectionViewer)) {
            fToggle = null;
            fExpand = null;
            fCollapse = null;
            fExpandAll = null;
            fCollapseAll = null;
            fRestoreDefaults = null;
            fCollapseMembers = null;
            fCollapseComments = null;
            fProjectionListener = null;
            return;
        }
        fViewer = (ProjectionViewer) viewer;
        fProjectionListener = new IProjectionListener() {
            public void projectionEnabled() {
                update();
            }
            public void projectionDisabled() {
                update();
            }
        };
        fViewer.addProjectionListener(fProjectionListener);
        fToggle = new PreferenceAction(FoldingMessages.getResourceBundle(), "Projection.Toggle.", IAction.AS_CHECK_BOX) {
            public void run() {
                IPreferenceStore store = RubyPlugin.getDefault().getPreferenceStore();
                boolean current = store.getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
                store.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, !current);
            }
            public void update() {
                ITextOperationTarget target = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
                boolean isEnabled = (target != null && target.canDoOperation(ProjectionViewer.TOGGLE));
                setEnabled(isEnabled);
            }
        };
        fToggle.setChecked(true);
        fToggle.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
        editor.setAction("FoldingToggle", fToggle);
        fExpandAll = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.ExpandAll.", editor, ProjectionViewer.EXPAND_ALL, true);
        fExpandAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        editor.setAction("FoldingExpandAll", fExpandAll);
        fCollapseAll = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.CollapseAll.", editor, ProjectionViewer.COLLAPSE_ALL, true);
        fCollapseAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
        editor.setAction("FoldingCollapseAll", fCollapseAll);
        fExpand = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.Expand.", editor, ProjectionViewer.EXPAND, true);
        fExpand.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        editor.setAction("FoldingExpand", fExpand);
        fCollapse = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.Collapse.", editor, ProjectionViewer.COLLAPSE, true);
        fCollapse.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        editor.setAction("FoldingCollapse", fCollapse);
        fRestoreDefaults = new FoldingAction(FoldingMessages.getResourceBundle(), "Projection.Restore.") {
            public void run() {
                if (editor instanceof RubyEditor) {
                    RubyEditor javaEditor = (RubyEditor) editor;
                    javaEditor.resetProjection();
                }
            }
        };
        fRestoreDefaults.setActionDefinitionId(IFoldingCommandIds.FOLDING_RESTORE);
        editor.setAction("FoldingRestore", fRestoreDefaults);
        fCollapseMembers = new FoldingAction(FoldingMessages.getResourceBundle(), "Projection.CollapseMembers.") {
            public void run() {
                if (editor instanceof RubyEditor) {
                    RubyEditor javaEditor = (RubyEditor) editor;
                    javaEditor.collapseMembers();
                }
            }
        };
        fCollapseMembers.setActionDefinitionId(IRubyEditorActionDefinitionIds.FOLDING_COLLAPSE_MEMBERS);
        editor.setAction("FoldingCollapseMembers", fCollapseMembers);
        fCollapseComments = new FoldingAction(FoldingMessages.getResourceBundle(), "Projection.CollapseComments.") {
            public void run() {
                if (editor instanceof RubyEditor) {
                    RubyEditor javaEditor = (RubyEditor) editor;
                    javaEditor.collapseComments();
                }
            }
        };
        fCollapseComments.setActionDefinitionId(IRubyEditorActionDefinitionIds.FOLDING_COLLAPSE_COMMENTS);
        editor.setAction("FoldingCollapseComments", fCollapseComments);
    }
    protected boolean isEnabled() {
        return fViewer != null;
    }
    public void dispose() {
        if (isEnabled()) {
            fViewer.removeProjectionListener(fProjectionListener);
            fViewer = null;
        }
        super.dispose();
    }
    protected void update() {
        if (isEnabled()) {
            fToggle.update();
            fToggle.setChecked(fViewer.isProjectionMode());
            fExpand.update();
            fExpandAll.update();
            fCollapse.update();
            fCollapseAll.update();
            fRestoreDefaults.update();
            fCollapseMembers.update();
            fCollapseComments.update();
        }
    }
    public void fillMenu(IMenuManager manager) {
        if (isEnabled()) {
            update();
            manager.add(fToggle);
            manager.add(fExpandAll);
            manager.add(fExpand);
            manager.add(fCollapse);
            manager.add(fCollapseAll);
            manager.add(fRestoreDefaults);
            manager.add(fCollapseMembers);
            manager.add(fCollapseComments);
        }
    }
    public void updateActionBars() {
        update();
    }
}
