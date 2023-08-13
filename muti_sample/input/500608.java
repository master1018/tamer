public class ImpsTransactionManager {
    private ImpsConnection mConnection;
    private int mTransactionId;
    private HashMap<String, AsyncTransaction> mClientTransactions;
    private HashMap<String, ServerTransactionListener> mServerTransactionListeners;
    ImpsTransactionManager(ImpsConnection connection) {
        this.mConnection = connection;
        mClientTransactions = new HashMap<String, AsyncTransaction>();
        mServerTransactionListeners = new HashMap<String, ServerTransactionListener>();
    }
    public void setTransactionListener(String type, ServerTransactionListener listener) {
        synchronized(mServerTransactionListeners) {
            if (listener == null) {
                mServerTransactionListeners.remove(type);
            } else {
                mServerTransactionListeners.put(type, listener);
            }
        }
    }
    void beginClientTransaction(AsyncTransaction tx) {
        synchronized(mClientTransactions) {
            tx.setTransactionInfo(nextTransactionId(), mConnection);
            mClientTransactions.put(tx.getId(), tx);
        }
    }
    void endClientTransaction(AsyncTransaction tx) {
        synchronized(mClientTransactions) {
            mClientTransactions.remove(tx.getId());
        }
    }
    void reassignTransactionId(Primitive p) {
        synchronized (mClientTransactions) {
            AsyncTransaction tx = mClientTransactions.remove(p.getTransactionID());
            if(tx != null) {
                String newId = nextTransactionId();
                tx.setTransactionInfo(newId, mConnection);
                p.setTransactionId(newId);
                mClientTransactions.put(newId, tx);
            }
        }
    }
    public void notifyErrorResponse(String transactionId,
            int code, String info) {
        AsyncTransaction tx;
        synchronized(mClientTransactions) {
            tx = mClientTransactions.get(transactionId);
        }
        if (tx != null) {
            tx.notifyError(new ImErrorInfo(code, info));
        } else {
            ImpsLog.log("Ignoring possible server transaction error " + code + info);
        }
    }
    public void notifyIncomingPrimitive(Primitive primitive) {
        String transactionId = primitive.getTransactionID();
        if (primitive.getTransactionMode() == TransactionMode.Response) {
            AsyncTransaction tx;
            synchronized(mClientTransactions) {
                tx = mClientTransactions.get(transactionId);
            }
            if (tx != null) {
                tx.notifyResponse(primitive);
            }
        } else {
            ServerTransaction serverTx = new ServerTransaction(transactionId,
                    mConnection, primitive);
            ServerTransactionListener listener;
            synchronized(mServerTransactionListeners) {
                listener = mServerTransactionListeners.get(primitive.getType());
            }
            if (listener != null) {
                listener.notifyServerTransaction(serverTx);
            } else {
                ImpsLog.log("Unhandled Server transaction: " + primitive.getType());
            }
        }
    }
    private synchronized String nextTransactionId() {
        if(mTransactionId >= 999) {
            mTransactionId = 0;
        }
        return String.valueOf(++mTransactionId);
    }
}
