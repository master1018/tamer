public class SystemItemSemanticEditPolicy extends SystemBaseItemSemanticEditPolicy {
    protected Command getCreateCommand(CreateElementRequest req) {
        if (SystemElementTypes.Block_2002 == req.getElementType()) {
            if (req.getContainmentFeature() == null) {
                req.setContainmentFeature(SystemPackage.eINSTANCE.getSystem_Blocks());
            }
            return getGEFWrapper(new BlockCreateCommand(req));
        }
        return super.getCreateCommand(req);
    }
    protected Command getDuplicateCommand(DuplicateElementsRequest req) {
        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
        return getGEFWrapper(new DuplicateAnythingCommand(editingDomain, req));
    }
    private static class DuplicateAnythingCommand extends DuplicateEObjectsCommand {
        public DuplicateAnythingCommand(TransactionalEditingDomain editingDomain, DuplicateElementsRequest req) {
            super(editingDomain, req.getLabel(), req.getElementsToBeDuplicated(), req.getAllDuplicatedElementsMap());
        }
    }
}
