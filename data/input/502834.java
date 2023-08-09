public abstract class Call {
    public enum State {
        IDLE, ACTIVE, HOLDING, DIALING, ALERTING, INCOMING, WAITING, DISCONNECTED, DISCONNECTING;
        public boolean isAlive() {
            return !(this == IDLE || this == DISCONNECTED || this == DISCONNECTING);
        }
        public boolean isRinging() {
            return this == INCOMING || this == WAITING;
        }
        public boolean isDialing() {
            return this == DIALING || this == ALERTING;
        }
    }
    public State state = State.IDLE;
    protected boolean isGeneric = false;
    protected final String LOG_TAG = "Call";
    public abstract List<Connection> getConnections();
    public abstract Phone getPhone();
    public abstract boolean isMultiparty();
    public abstract void hangup() throws CallStateException;
    public boolean hasConnection(Connection c) {
        return c.getCall() == this;
    }
    public boolean hasConnections() {
        List connections = getConnections();
        if (connections == null) {
            return false;
        }
        return connections.size() > 0;
    }
    public State getState() {
        return state;
    }
    public boolean isIdle() {
        return !getState().isAlive();
    }
    public Connection
    getEarliestConnection() {
        List l;
        long time = Long.MAX_VALUE;
        Connection c;
        Connection earliest = null;
        l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        for (int i = 0, s = l.size() ; i < s ; i++) {
            c = (Connection) l.get(i);
            long t;
            t = c.getCreateTime();
            if (t < time) {
                earliest = c;
                time = t;
            }
        }
        return earliest;
    }
    public long
    getEarliestCreateTime() {
        List l;
        long time = Long.MAX_VALUE;
        l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        for (int i = 0, s = l.size() ; i < s ; i++) {
            Connection c = (Connection) l.get(i);
            long t;
            t = c.getCreateTime();
            time = t < time ? t : time;
        }
        return time;
    }
    public long
    getEarliestConnectTime() {
        long time = Long.MAX_VALUE;
        List l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        for (int i = 0, s = l.size() ; i < s ; i++) {
            Connection c = (Connection) l.get(i);
            long t;
            t = c.getConnectTime();
            time = t < time ? t : time;
        }
        return time;
    }
    public boolean
    isDialingOrAlerting() {
        return getState().isDialing();
    }
    public boolean
    isRinging() {
        return getState().isRinging();
    }
    public Connection
    getLatestConnection() {
        List l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        long time = 0;
        Connection latest = null;
        for (int i = 0, s = l.size() ; i < s ; i++) {
            Connection c = (Connection) l.get(i);
            long t = c.getCreateTime();
            if (t > time) {
                latest = c;
                time = t;
            }
        }
        return latest;
    }
    public boolean isGeneric() {
        return isGeneric;
    }
    public void setGeneric(boolean generic) {
        isGeneric = generic;
    }
    public void hangupIfAlive() {
        if (getState().isAlive()) {
            try {
                hangup();
            } catch (CallStateException ex) {
                Log.w(LOG_TAG, " hangupIfActive: caught " + ex);
            }
        }
    }
}
