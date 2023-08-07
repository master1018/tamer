public class DelegationItemSemanticEditPolicy extends SaveccmBaseItemSemanticEditPolicy {
    protected Command getDestroyElementCommand(DestroyElementRequest req) {
        return getGEFWrapper(new DestroyElementCommand(req));
    }
}
