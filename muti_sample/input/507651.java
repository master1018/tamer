abstract class AsyncTransaction extends ImpsTransaction {
    private final AsyncCompletion mCompletionCallback;
    private boolean mCompletionNotified;
    protected final ImpsTransactionManager mTransManager;
    AsyncTransaction(ImpsTransactionManager manager) {
        this(manager, null);
    }
    AsyncTransaction(ImpsTransactionManager manager, AsyncCompletion completion) {
        mTransManager = manager;
        mCompletionCallback = completion;
        manager.beginClientTransaction(this);
    }
    public void sendRequest(Primitive request) {
        sendPrimitive(request);
    }
    final void notifyError(ImErrorInfo error) {
        notifyErrorResponse(new ImpsErrorInfo(error.getCode(), error.getDescription(), null));
    }
    final void notifyResponse(Primitive response) {
        response.setTransaction(this);
        ImpsErrorInfo error = ImpsUtils.checkResultError(response);
        if (error != null) {
            notifyErrorResponse(error);
        } else {
            notifySuccessResponse(response);
        }
    }
    protected void notifyErrorResponse(ImpsErrorInfo error) {
        onResponseError(error);
        mTransManager.endClientTransaction(this);
        notifyAsyncCompletionError(error);
    }
    protected void notifySuccessResponse(Primitive response) {
        onResponseOk(response);
        mTransManager.endClientTransaction(this);
        notifyAsyncCompletionSuccess();
    }
    public abstract void onResponseError(ImpsErrorInfo error);
    public abstract void onResponseOk(Primitive response);
    protected void notifyAsyncCompletionError(ImErrorInfo error) {
        if (!mCompletionNotified) {
            mCompletionNotified = true;
            if (mCompletionCallback != null)
                mCompletionCallback.onError(error);
        }
    }
    protected void notifyAsyncCompletionSuccess() {
        if (!mCompletionNotified) {
            mCompletionNotified = true;
            if (mCompletionCallback != null)
                mCompletionCallback.onComplete();
        }
    }
}
