public class HierarchicalStateMachine {
    private static final String TAG = "HierarchicalStateMachine";
    private String mName;
    public static final int HSM_QUIT_CMD = -1;
    private static class HsmHandler extends Handler {
        private boolean mDbg = false;
        private static final Object mQuitObj = new Object();
        private ProcessedMessages mProcessedMessages = new ProcessedMessages();
        private boolean mIsConstructionCompleted;
        private StateInfo mStateStack[];
        private int mStateStackTopIndex = -1;
        private StateInfo mTempStateStack[];
        private int mTempStateStackCount;
        private HaltingState mHaltingState = new HaltingState();
        private QuittingState mQuittingState = new QuittingState();
        private HierarchicalStateMachine mHsm;
        private class StateInfo {
            HierarchicalState state;
            StateInfo parentStateInfo;
            boolean active;
            @Override
            public String toString() {
                return "state=" + state.getName() + ",active=" + active
                        + ",parent=" + ((parentStateInfo == null) ?
                                        "null" : parentStateInfo.state.getName());
            }
        }
        private HashMap<HierarchicalState, StateInfo> mStateInfo =
            new HashMap<HierarchicalState, StateInfo>();
        private HierarchicalState mInitialState;
        private HierarchicalState mDestState;
        private ArrayList<Message> mDeferredMessages = new ArrayList<Message>();
        private class HaltingState extends HierarchicalState {
            @Override
            public boolean processMessage(Message msg) {
                mHsm.haltedProcessMessage(msg);
                return true;
            }
        }
        private class QuittingState extends HierarchicalState {
            @Override
            public boolean processMessage(Message msg) {
                return false;
            }
        }
        @Override
        public final void handleMessage(Message msg) {
            if (mDbg) Log.d(TAG, "handleMessage: E msg.what=" + msg.what);
            if (!mIsConstructionCompleted) {
                Log.e(TAG, "The start method not called, ignore msg: " + msg);
                return;
            }
            processMsg(msg);
            performTransitions();
            if (mDbg) Log.d(TAG, "handleMessage: X");
        }
        private void performTransitions() {
            HierarchicalState destState = null;
            while (mDestState != null) {
                if (mDbg) Log.d(TAG, "handleMessage: new destination call exit");
                destState = mDestState;
                mDestState = null;
                StateInfo commonStateInfo = setupTempStateStackWithStatesToEnter(destState);
                invokeExitMethods(commonStateInfo);
                int stateStackEnteringIndex = moveTempStateStackToStateStack();
                invokeEnterMethods(stateStackEnteringIndex);
                moveDeferredMessageAtFrontOfQueue();
            }
            if (destState != null) {
                if (destState == mQuittingState) {
                    mHsm.quitting();
                    if (mHsm.mHsmThread != null) {
                        getLooper().quit();
                    }
                } else if (destState == mHaltingState) {
                    mHsm.halting();
                }
            }
        }
        private final void completeConstruction() {
            if (mDbg) Log.d(TAG, "completeConstruction: E");
            int maxDepth = 0;
            for (StateInfo si : mStateInfo.values()) {
                int depth = 0;
                for (StateInfo i = si; i != null; depth++) {
                    i = i.parentStateInfo;
                }
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
            if (mDbg) Log.d(TAG, "completeConstruction: maxDepth=" + maxDepth);
            mStateStack = new StateInfo[maxDepth];
            mTempStateStack = new StateInfo[maxDepth];
            setupInitialStateStack();
            mIsConstructionCompleted = true;
            invokeEnterMethods(0);
            performTransitions();
            if (mDbg) Log.d(TAG, "completeConstruction: X");
        }
        private final void processMsg(Message msg) {
            StateInfo curStateInfo = mStateStack[mStateStackTopIndex];
            if (mDbg) {
                Log.d(TAG, "processMsg: " + curStateInfo.state.getName());
            }
            while (!curStateInfo.state.processMessage(msg)) {
                curStateInfo = curStateInfo.parentStateInfo;
                if (curStateInfo == null) {
                    mHsm.unhandledMessage(msg);
                    if (isQuit(msg)) {
                        transitionTo(mQuittingState);
                    }
                    break;
                }
                if (mDbg) {
                    Log.d(TAG, "processMsg: " + curStateInfo.state.getName());
                }
            }
            if (curStateInfo != null) {
                HierarchicalState orgState = mStateStack[mStateStackTopIndex].state;
                mProcessedMessages.add(msg, curStateInfo.state, orgState);
            } else {
                mProcessedMessages.add(msg, null, null);
            }
        }
        private final void invokeExitMethods(StateInfo commonStateInfo) {
            while ((mStateStackTopIndex >= 0) &&
                    (mStateStack[mStateStackTopIndex] != commonStateInfo)) {
                HierarchicalState curState = mStateStack[mStateStackTopIndex].state;
                if (mDbg) Log.d(TAG, "invokeExitMethods: " + curState.getName());
                curState.exit();
                mStateStack[mStateStackTopIndex].active = false;
                mStateStackTopIndex -= 1;
            }
        }
        private final void invokeEnterMethods(int stateStackEnteringIndex) {
            for (int i = stateStackEnteringIndex; i <= mStateStackTopIndex; i++) {
                if (mDbg) Log.d(TAG, "invokeEnterMethods: " + mStateStack[i].state.getName());
                mStateStack[i].state.enter();
                mStateStack[i].active = true;
            }
        }
        private final void moveDeferredMessageAtFrontOfQueue() {
            for (int i = mDeferredMessages.size() - 1; i >= 0; i-- ) {
                Message curMsg = mDeferredMessages.get(i);
                if (mDbg) Log.d(TAG, "moveDeferredMessageAtFrontOfQueue; what=" + curMsg.what);
                sendMessageAtFrontOfQueue(curMsg);
            }
            mDeferredMessages.clear();
        }
        private final int moveTempStateStackToStateStack() {
            int startingIndex = mStateStackTopIndex + 1;
            int i = mTempStateStackCount - 1;
            int j = startingIndex;
            while (i >= 0) {
                if (mDbg) Log.d(TAG, "moveTempStackToStateStack: i=" + i + ",j=" + j);
                mStateStack[j] = mTempStateStack[i];
                j += 1;
                i -= 1;
            }
            mStateStackTopIndex = j - 1;
            if (mDbg) {
                Log.d(TAG, "moveTempStackToStateStack: X mStateStackTop="
                      + mStateStackTopIndex + ",startingIndex=" + startingIndex
                      + ",Top=" + mStateStack[mStateStackTopIndex].state.getName());
            }
            return startingIndex;
        }
        private final StateInfo setupTempStateStackWithStatesToEnter(HierarchicalState destState) {
            mTempStateStackCount = 0;
            StateInfo curStateInfo = mStateInfo.get(destState);
            do {
                mTempStateStack[mTempStateStackCount++] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
            } while ((curStateInfo != null) && !curStateInfo.active);
            if (mDbg) {
                Log.d(TAG, "setupTempStateStackWithStatesToEnter: X mTempStateStackCount="
                      + mTempStateStackCount + ",curStateInfo: " + curStateInfo);
            }
            return curStateInfo;
        }
        private final void setupInitialStateStack() {
            if (mDbg) {
                Log.d(TAG, "setupInitialStateStack: E mInitialState="
                    + mInitialState.getName());
            }
            StateInfo curStateInfo = mStateInfo.get(mInitialState);
            for (mTempStateStackCount = 0; curStateInfo != null; mTempStateStackCount++) {
                mTempStateStack[mTempStateStackCount] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
            }
            mStateStackTopIndex = -1;
            moveTempStateStackToStateStack();
        }
        private final HierarchicalState getCurrentState() {
            return mStateStack[mStateStackTopIndex].state;
        }
        private final StateInfo addState(HierarchicalState state, HierarchicalState parent) {
            if (mDbg) {
                Log.d(TAG, "addStateInternal: E state=" + state.getName()
                        + ",parent=" + ((parent == null) ? "" : parent.getName()));
            }
            StateInfo parentStateInfo = null;
            if (parent != null) {
                parentStateInfo = mStateInfo.get(parent);
                if (parentStateInfo == null) {
                    parentStateInfo = addState(parent, null);
                }
            }
            StateInfo stateInfo = mStateInfo.get(state);
            if (stateInfo == null) {
                stateInfo = new StateInfo();
                mStateInfo.put(state, stateInfo);
            }
            if ((stateInfo.parentStateInfo != null) &&
                    (stateInfo.parentStateInfo != parentStateInfo)) {
                    throw new RuntimeException("state already added");
            }
            stateInfo.state = state;
            stateInfo.parentStateInfo = parentStateInfo;
            stateInfo.active = false;
            if (mDbg) Log.d(TAG, "addStateInternal: X stateInfo: " + stateInfo);
            return stateInfo;
        }
        private HsmHandler(Looper looper, HierarchicalStateMachine hsm) {
            super(looper);
            mHsm = hsm;
            addState(mHaltingState, null);
            addState(mQuittingState, null);
        }
        private final void setInitialState(HierarchicalState initialState) {
            if (mDbg) Log.d(TAG, "setInitialState: initialState" + initialState.getName());
            mInitialState = initialState;
        }
        private final void transitionTo(HierarchicalState destState) {
            if (mDbg) Log.d(TAG, "StateMachine.transitionTo EX destState" + destState.getName());
            mDestState = destState;
        }
        private final void deferMessage(Message msg) {
            if (mDbg) Log.d(TAG, "deferMessage: msg=" + msg.what);
            Message newMsg = obtainMessage();
            newMsg.copyFrom(msg);
            mDeferredMessages.add(newMsg);
        }
        private final void quit() {
            if (mDbg) Log.d(TAG, "quit:");
            sendMessage(obtainMessage(HSM_QUIT_CMD, mQuitObj));
        }
        private final boolean isQuit(Message msg) {
            return (msg.what == HSM_QUIT_CMD) && (msg.obj == mQuitObj);
        }
        private final boolean isDbg() {
            return mDbg;
        }
        private final void setDbg(boolean dbg) {
            mDbg = dbg;
        }
        private final void setProcessedMessagesSize(int maxSize) {
            mProcessedMessages.setSize(maxSize);
        }
        private final int getProcessedMessagesSize() {
            return mProcessedMessages.size();
        }
        private final int getProcessedMessagesCount() {
            return mProcessedMessages.count();
        }
        private final ProcessedMessages.Info getProcessedMessage(int index) {
            return mProcessedMessages.get(index);
        }
    }
    private HsmHandler mHsmHandler;
    private HandlerThread mHsmThread;
    private void initStateMachine(String name, Looper looper) {
        mName = name;
        mHsmHandler = new HsmHandler(looper, this);
    }
    protected HierarchicalStateMachine(String name) {
        mHsmThread = new HandlerThread(name);
        mHsmThread.start();
        Looper looper = mHsmThread.getLooper();
        initStateMachine(name, looper);
    }
    protected HierarchicalStateMachine(String name, Looper looper) {
        initStateMachine(name, looper);
    }
    protected final void addState(HierarchicalState state, HierarchicalState parent) {
        mHsmHandler.addState(state, parent);
    }
    protected final HierarchicalState getCurrentState() {
        return mHsmHandler.getCurrentState();
    }
    protected final void addState(HierarchicalState state) {
        mHsmHandler.addState(state, null);
    }
    protected final void setInitialState(HierarchicalState initialState) {
        mHsmHandler.setInitialState(initialState);
    }
    protected final void transitionTo(HierarchicalState destState) {
        mHsmHandler.transitionTo(destState);
    }
    protected final void transitionToHaltingState() {
        mHsmHandler.transitionTo(mHsmHandler.mHaltingState);
    }
    protected final void deferMessage(Message msg) {
        mHsmHandler.deferMessage(msg);
    }
    protected void unhandledMessage(Message msg) {
        Log.e(TAG, mName + " - unhandledMessage: msg.what=" + msg.what);
    }
    protected void haltedProcessMessage(Message msg) {
    }
    protected void halting() {
    }
    protected void quitting() {
    }
    public final String getName() {
        return mName;
    }
    public final void setProcessedMessagesSize(int maxSize) {
        mHsmHandler.setProcessedMessagesSize(maxSize);
    }
    public final int getProcessedMessagesSize() {
        return mHsmHandler.getProcessedMessagesSize();
    }
    public final int getProcessedMessagesCount() {
        return mHsmHandler.getProcessedMessagesCount();
    }
    public final ProcessedMessages.Info getProcessedMessage(int index) {
        return mHsmHandler.getProcessedMessage(index);
    }
    public final Handler getHandler() {
        return mHsmHandler;
    }
    public final Message obtainMessage()
    {
        return Message.obtain(mHsmHandler);
    }
    public final Message obtainMessage(int what) {
        return Message.obtain(mHsmHandler, what);
    }
    public final Message obtainMessage(int what, Object obj)
    {
        return Message.obtain(mHsmHandler, what, obj);
    }
    public final void sendMessage(int what) {
        mHsmHandler.sendMessage(obtainMessage(what));
    }
    public final void sendMessage(int what, Object obj) {
        mHsmHandler.sendMessage(obtainMessage(what,obj));
    }
    public final void sendMessage(Message msg) {
        mHsmHandler.sendMessage(msg);
    }
    public final void sendMessageDelayed(int what, long delayMillis) {
        mHsmHandler.sendMessageDelayed(obtainMessage(what), delayMillis);
    }
    public final void sendMessageDelayed(int what, Object obj, long delayMillis) {
        mHsmHandler.sendMessageDelayed(obtainMessage(what, obj), delayMillis);
    }
    public final void sendMessageDelayed(Message msg, long delayMillis) {
        mHsmHandler.sendMessageDelayed(msg, delayMillis);
    }
    protected final void sendMessageAtFrontOfQueue(int what, Object obj) {
        mHsmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, obj));
    }
    protected final void sendMessageAtFrontOfQueue(int what) {
        mHsmHandler.sendMessageAtFrontOfQueue(obtainMessage(what));
    }
    protected final void sendMessageAtFrontOfQueue(Message msg) {
        mHsmHandler.sendMessageAtFrontOfQueue(msg);
    }
    public final void quit() {
        mHsmHandler.quit();
    }
    protected final boolean isQuit(Message msg) {
        return mHsmHandler.isQuit(msg);
    }
    public boolean isDbg() {
        return mHsmHandler.isDbg();
    }
    public void setDbg(boolean dbg) {
        mHsmHandler.setDbg(dbg);
    }
    public void start() {
        mHsmHandler.completeConstruction();
    }
}
