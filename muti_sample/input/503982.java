final class SimpleAsyncTransaction extends AsyncTransaction {
    public SimpleAsyncTransaction(ImpsTransactionManager manager,
            AsyncCompletion completion) {
        super(manager, completion);
        if (completion == null) {
            throw new NullPointerException();
        }
    }
    @Override
    public void onResponseError(ImpsErrorInfo error) {
    }
    @Override
    public void onResponseOk(Primitive response) {
    }
}
