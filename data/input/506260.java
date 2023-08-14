public class SyncQueue {
    private static final String TAG = "SyncManager";
    private SyncStorageEngine mSyncStorageEngine;
    private final HashMap<String, SyncOperation> mOperationsMap = Maps.newHashMap();
    public SyncQueue(SyncStorageEngine syncStorageEngine) {
        mSyncStorageEngine = syncStorageEngine;
        ArrayList<SyncStorageEngine.PendingOperation> ops
                = mSyncStorageEngine.getPendingOperations();
        final int N = ops.size();
        for (int i=0; i<N; i++) {
            SyncStorageEngine.PendingOperation op = ops.get(i);
            SyncOperation syncOperation = new SyncOperation(
                    op.account, op.syncSource, op.authority, op.extras, 0 );
            syncOperation.expedited = op.expedited;
            syncOperation.pendingOperation = op;
            add(syncOperation, op);
        }
    }
    public boolean add(SyncOperation operation) {
        return add(operation, null );
    }
    private boolean add(SyncOperation operation,
            SyncStorageEngine.PendingOperation pop) {
        final String operationKey = operation.key;
        final SyncOperation existingOperation = mOperationsMap.get(operationKey);
        if (existingOperation != null) {
            boolean changed = false;
            if (existingOperation.expedited == operation.expedited) {
                final long newRunTime =
                        Math.min(existingOperation.earliestRunTime, operation.earliestRunTime);
                if (existingOperation.earliestRunTime != newRunTime) {
                    existingOperation.earliestRunTime = newRunTime;
                    changed = true;
                }
            } else {
                if (operation.expedited) {
                    existingOperation.expedited = true;
                    changed = true;
                }
            }
            return changed;
        }
        operation.pendingOperation = pop;
        if (operation.pendingOperation == null) {
            pop = new SyncStorageEngine.PendingOperation(
                            operation.account, operation.syncSource,
                            operation.authority, operation.extras, operation.expedited);
            pop = mSyncStorageEngine.insertIntoPending(pop);
            if (pop == null) {
                throw new IllegalStateException("error adding pending sync operation "
                        + operation);
            }
            operation.pendingOperation = pop;
        }
        mOperationsMap.put(operationKey, operation);
        return true;
    }
    public void remove(SyncOperation operation) {
        SyncOperation operationToRemove = mOperationsMap.remove(operation.key);
        if (operationToRemove == null) {
            return;
        }
        if (!mSyncStorageEngine.deleteFromPending(operationToRemove.pendingOperation)) {
            final String errorMessage = "unable to find pending row for " + operationToRemove;
            Log.e(TAG, errorMessage, new IllegalStateException(errorMessage));
        }
    }
    public Pair<SyncOperation, Long> nextOperation() {
        SyncOperation best = null;
        long bestRunTime = 0;
        boolean bestSyncableIsUnknownAndNotARetry = false;
        for (SyncOperation op : mOperationsMap.values()) {
            long opRunTime = op.earliestRunTime;
            if (!op.extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, false)) {
                Pair<Long, Long> backoff = mSyncStorageEngine.getBackoff(op.account, op.authority);
                long delayUntil = mSyncStorageEngine.getDelayUntilTime(op.account, op.authority);
                opRunTime = Math.max(
                        Math.max(opRunTime, delayUntil),
                        backoff != null ? backoff.first : 0);
            }
            final boolean syncableIsUnknownAndNotARetry =
                    !op.extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)
                    && mSyncStorageEngine.getIsSyncable(op.account, op.authority) < 0;
            if (best == null
                    || ((bestSyncableIsUnknownAndNotARetry == syncableIsUnknownAndNotARetry)
                        ? (best.expedited == op.expedited
                           ? opRunTime < bestRunTime
                           : op.expedited)
                        : syncableIsUnknownAndNotARetry)) {
                best = op;
                bestSyncableIsUnknownAndNotARetry = syncableIsUnknownAndNotARetry;
                bestRunTime = opRunTime;
            }
        }
        if (best == null) {
            return null;
        }
        return Pair.create(best, bestRunTime);
    }
    public Pair<SyncOperation, Long> nextReadyToRun(long now) {
        Pair<SyncOperation, Long> nextOpAndRunTime = nextOperation();
        if (nextOpAndRunTime == null || nextOpAndRunTime.second > now) {
            return null;
        }
        return nextOpAndRunTime;
    }
    public void remove(Account account, String authority) {
        Iterator<Map.Entry<String, SyncOperation>> entries = mOperationsMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, SyncOperation> entry = entries.next();
            SyncOperation syncOperation = entry.getValue();
            if (account != null && !syncOperation.account.equals(account)) {
                continue;
            }
            if (authority != null && !syncOperation.authority.equals(authority)) {
                continue;
            }
            entries.remove();
            if (!mSyncStorageEngine.deleteFromPending(syncOperation.pendingOperation)) {
                final String errorMessage = "unable to find pending row for " + syncOperation;
                Log.e(TAG, errorMessage, new IllegalStateException(errorMessage));
            }
        }
    }
    public void dump(StringBuilder sb) {
        sb.append("SyncQueue: ").append(mOperationsMap.size()).append(" operation(s)\n");
        for (SyncOperation operation : mOperationsMap.values()) {
            sb.append(operation).append("\n");
        }
    }
}
