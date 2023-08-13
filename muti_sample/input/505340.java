public class NetworkState {
    public static final int TO_DISCONNECTION = 0; 
    public static final int TO_CONNECTION = 1; 
    public static final int DO_NOTHING = -1;   
    private final String LOG_TAG = "NetworkState";
    private List<State> mStateDepository;
    private State mTransitionTarget;
    private int mTransitionDirection;
    private String mReason = null;         
    public NetworkState() {
        mStateDepository = new ArrayList<State>();
        mTransitionDirection = DO_NOTHING;
        mTransitionTarget = State.UNKNOWN;
    }
    public NetworkState(State currentState) {
        mStateDepository = new ArrayList<State>();
        mStateDepository.add(currentState);
        mTransitionDirection = DO_NOTHING;
        mTransitionTarget = State.UNKNOWN;
    }
    public void resetNetworkState() {
        mStateDepository.clear();
        mTransitionDirection = DO_NOTHING;
        mTransitionTarget = State.UNKNOWN;
    }
    public void setStateTransitionCriteria(State initState, int transitionDir, State targetState) {
        if (!mStateDepository.isEmpty()) {
            mStateDepository.clear();
        }
        mStateDepository.add(initState);
        mTransitionDirection = transitionDir;
        mTransitionTarget = targetState;
        Log.v(LOG_TAG, "setStateTransitionCriteria: " + printStates());
    }
    public void recordState(State currentState) {
        mStateDepository.add(currentState);
    }
    public boolean validateStateTransition() {
        Log.v(LOG_TAG, "print state depository: " + printStates());
        if (mTransitionDirection == DO_NOTHING) {
            if (mStateDepository.isEmpty()) {
                Log.v(LOG_TAG, "no state is recorded");
                mReason = "no state is recorded.";
                return false;
            } else if (mStateDepository.size() > 1) {
                Log.v(LOG_TAG, "no broadcast is expected, " +
                        "instead broadcast is probably received");
                mReason = "no broadcast is expected, instead broadcast is probably received";
                return false;
            } else if (mStateDepository.get(0) != mTransitionTarget) {
                Log.v(LOG_TAG, mTransitionTarget + " is expected, but it is " +
                        mStateDepository.get(0));
                mReason = mTransitionTarget + " is expected, but it is " + mStateDepository.get(0);
                return false;
            }
            return true;
        } else if (mTransitionDirection == TO_CONNECTION) {
            Log.v(LOG_TAG, "transition to CONNECTED");
            return transitToConnection();
        } else {
            Log.v(LOG_TAG, "transition to DISCONNECTED");
            return transitToDisconnection();
        }
    }
    public boolean transitToDisconnection () {
        mReason = "states: " + printStates();
        if (mStateDepository.get(0) != State.CONNECTED) {
            mReason += " initial state should be CONNECTED, but it is " +
                    mStateDepository.get(0) + ".";
            return false;
        }
        State lastState = mStateDepository.get(mStateDepository.size() - 1);
        if ( lastState != mTransitionTarget) {
            mReason += " the last state should be DISCONNECTED, but it is " + lastState;
            return false;
        }
        for (int i = 1; i < mStateDepository.size() - 1; i++) {
            State preState = mStateDepository.get(i-1);
            State curState = mStateDepository.get(i);
            if ((preState == State.CONNECTED) && ((curState == State.DISCONNECTING) ||
                    (curState == State.DISCONNECTED))) {
                continue;
            } else if ((preState == State.DISCONNECTING) && (curState == State.DISCONNECTED)) {
                continue;
            } else if ((preState == State.DISCONNECTED) && (curState == State.DISCONNECTED)) {
                continue;
            } else {
                mReason += " Transition state from " + preState.toString() + " to " +
                        curState.toString() + " is not valid.";
                return false;
            }
        }
        return true;
    }
    public boolean transitToConnection() {
        mReason = "states: " + printStates();
        if (mStateDepository.get(0) != State.DISCONNECTED) {
            mReason += " initial state should be DISCONNECTED, but it is " +
                    mStateDepository.get(0) + ".";
            return false;
        }
        State lastState = mStateDepository.get(mStateDepository.size() - 1);
        if ( lastState != mTransitionTarget) {
            mReason += "The last state should be " + mTransitionTarget + ", but it is " + lastState;
            return false;
        }
        for (int i = 1; i < mStateDepository.size(); i++) {
            State preState = mStateDepository.get(i-1);
            State curState = mStateDepository.get(i);
            if ((preState == State.DISCONNECTED) && ((curState == State.CONNECTING) ||
                    (curState == State.CONNECTED) || (curState == State.DISCONNECTED))) {
                continue;
            } else if ((preState == State.CONNECTING) && (curState == State.CONNECTED)) {
                continue;
            } else if ((preState == State.CONNECTED) && (curState == State.CONNECTED)) {
                continue;
            } else {
                mReason += " Transition state from " + preState.toString() + " to " +
                        curState.toString() + " is not valid.";
                return false;
            }
        }
        return true;
    }
    public List<State> getTransitionStates() {
        return mStateDepository;
    }
    public String getReason() {
        return mReason;
    }
    public String printStates() {
        StringBuilder stateBuilder = new StringBuilder("");
        for (int i = 0; i < mStateDepository.size(); i++) {
            stateBuilder.append(" ").append(mStateDepository.get(i).toString()).append("->");
        }
        return stateBuilder.toString();
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(" ");
        builder.append("mTransitionDirection: ").append(Integer.toString(mTransitionDirection)).
                append("; ").append("states:").
                append(printStates()).append("; ");
        return builder.toString();
    }
}
