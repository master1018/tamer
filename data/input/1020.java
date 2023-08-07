public class M3ActionsValidationProvider extends AbstractContributionItemProvider {
    private static boolean constraintsActive = false;
    public static boolean shouldConstraintsBePrivate() {
        return false;
    }
    public static void runWithConstraints(TransactionalEditingDomain editingDomain, Runnable operation) {
        final Runnable op = operation;
        Runnable task = new Runnable() {
            public void run() {
                try {
                    constraintsActive = true;
                    op.run();
                } finally {
                    constraintsActive = false;
                }
            }
        };
        if (editingDomain != null) {
            try {
                editingDomain.runExclusive(task);
            } catch (Exception e) {
                M3ActionsDiagramEditorPlugin.getInstance().logError("Validation failed", e);
            }
        } else {
            task.run();
        }
    }
    protected IAction createAction(String actionId, IWorkbenchPartDescriptor partDescriptor) {
        if (hub.sam.mof.simulator.editor.diagram.part.ValidateAction.VALIDATE_ACTION_KEY.equals(actionId)) {
            return new ValidateAction(partDescriptor);
        }
        return super.createAction(actionId, partDescriptor);
    }
    static boolean isInDefaultEditorContext(Object object) {
        if (shouldConstraintsBePrivate() && !constraintsActive) {
            return false;
        }
        if (object instanceof View) {
            return constraintsActive && hub.sam.mof.simulator.editor.diagram.edit.parts.EPackageEditPart.MODEL_ID.equals(M3ActionsVisualIDRegistry.getModelID((View) object));
        }
        return true;
    }
}
