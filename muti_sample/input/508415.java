final class ServerTransaction extends ImpsTransaction {
    private Primitive mRequest;
    ServerTransaction(String id, ImpsConnection connection, Primitive request) {
        setTransactionInfo(id, connection);
        mRequest = request;
        request.setTransaction(this);
    }
    public Primitive getRequest() {
        return mRequest;
    }
    public void sendResponse(Primitive response) {
        response.setTransactionMode(TransactionMode.Response);
        sendPrimitive(response);
    }
    public void sendStatusResponse(String code) {
        Primitive status = new Primitive(ImpsTags.Status);
        status.setTransactionMode(TransactionMode.Response);
        status.addElement(ImpsTags.Result).addChild(ImpsTags.Code, code);
        sendPrimitive(status);
    }
}
