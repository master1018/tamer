public class CloseBoardActionResult extends ActionResult {
    public CloseBoardActionResult(boolean refreshParentBoard) {
        setRefreshParent(refreshParentBoard);
    }
    @Override
    public Object perform(String operationId, EvaluationContext trustedCtx, EvaluationContext parameters) throws Exception {
        throw new UnsupportedOperationException("No operation available");
    }
}
