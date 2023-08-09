abstract class MultiPhaseTransaction extends AsyncTransaction {
    public enum TransactionStatus {
        TRANSACTION_COMPLETED,
        TRANSACTION_CONTINUE,
    }
    public MultiPhaseTransaction(ImpsTransactionManager manager) {
        super(manager);
    }
    @Override
    protected void notifySuccessResponse(Primitive response) {
        TransactionStatus status = processResponse(response);
        if (status != TransactionStatus.TRANSACTION_CONTINUE) {
            mTransManager.endClientTransaction(this);
        }
    }
    @Override
    protected void notifyErrorResponse(ImpsErrorInfo error) {
        TransactionStatus status = processResponseError(error);
        if (status != TransactionStatus.TRANSACTION_CONTINUE) {
            mTransManager.endClientTransaction(this);
        }
    }
    public abstract TransactionStatus processResponseError(ImpsErrorInfo error);
    public abstract TransactionStatus processResponse(Primitive response);
    @Override
    final public void onResponseError(ImpsErrorInfo error) { }
    @Override
    final public void onResponseOk(Primitive response) { }
}
