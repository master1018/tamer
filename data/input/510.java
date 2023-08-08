public class RequireRelationReorientCommand extends EditElementCommand {
    private final int reorientDirection;
    private final EObject oldEnd;
    private final EObject newEnd;
    public RequireRelationReorientCommand(ReorientRelationshipRequest request) {
        super(request.getLabel(), request.getRelationship(), request);
        reorientDirection = request.getDirection();
        oldEnd = request.getOldRelationshipEnd();
        newEnd = request.getNewRelationshipEnd();
    }
    public boolean canExecute() {
        if (false == getElementToEdit() instanceof RequireRelation) {
            return false;
        }
        if (reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
            return canReorientSource();
        }
        if (reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
            return canReorientTarget();
        }
        return false;
    }
    protected boolean canReorientSource() {
        if (!(oldEnd instanceof BaseFeatureNode && newEnd instanceof BaseFeatureNode)) {
            return false;
        }
        BaseFeatureNode target = getLink().getTargetFeatureNode();
        return Fd2BaseItemSemanticEditPolicy.LinkConstraints.canExistRequireRelation_4006(getNewSource(), target);
    }
    protected boolean canReorientTarget() {
        if (!(oldEnd instanceof BaseFeatureNode && newEnd instanceof BaseFeatureNode)) {
            return false;
        }
        if (!(getLink().eContainer() instanceof BaseFeatureNode)) {
            return false;
        }
        BaseFeatureNode source = (BaseFeatureNode) getLink().eContainer();
        return Fd2BaseItemSemanticEditPolicy.LinkConstraints.canExistRequireRelation_4006(source, getNewTarget());
    }
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        if (!canExecute()) {
            throw new ExecutionException("Invalid arguments in reorient link command");
        }
        if (reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
            return reorientSource();
        }
        if (reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
            return reorientTarget();
        }
        throw new IllegalStateException();
    }
    protected CommandResult reorientSource() throws ExecutionException {
        getOldSource().getChildRelations().remove(getLink());
        getNewSource().getChildRelations().add(getLink());
        return CommandResult.newOKCommandResult(getLink());
    }
    protected CommandResult reorientTarget() throws ExecutionException {
        getLink().setTargetFeatureNode(getNewTarget());
        return CommandResult.newOKCommandResult(getLink());
    }
    protected RequireRelation getLink() {
        return (RequireRelation) getElementToEdit();
    }
    protected BaseFeatureNode getOldSource() {
        return (BaseFeatureNode) oldEnd;
    }
    protected BaseFeatureNode getNewSource() {
        return (BaseFeatureNode) newEnd;
    }
    protected BaseFeatureNode getOldTarget() {
        return (BaseFeatureNode) oldEnd;
    }
    protected BaseFeatureNode getNewTarget() {
        return (BaseFeatureNode) newEnd;
    }
}
