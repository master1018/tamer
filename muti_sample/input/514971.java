class GsmCall extends Call {
     ArrayList<Connection> connections = new ArrayList<Connection>();
     GsmCallTracker owner;
    static State
    stateFromDCState (DriverCall.State dcState) {
        switch (dcState) {
            case ACTIVE:        return State.ACTIVE;
            case HOLDING:       return State.HOLDING;
            case DIALING:       return State.DIALING;
            case ALERTING:      return State.ALERTING;
            case INCOMING:      return State.INCOMING;
            case WAITING:       return State.WAITING;
            default:            throw new RuntimeException ("illegal call state:" + dcState);
        }
    }
    GsmCall (GsmCallTracker owner) {
        this.owner = owner;
    }
    public void dispose() {
    }
    public List<Connection>
    getConnections() {
        return connections;
    }
    public Phone
    getPhone() {
        return null;
    }
    public boolean
    isMultiparty() {
        return connections.size() > 1;
    }
    public void
    hangup() throws CallStateException {
        owner.hangup(this);
    }
    public String
    toString() {
        return state.toString();
    }
     void
    attach(Connection conn, DriverCall dc) {
        connections.add(conn);
        state = stateFromDCState (dc.state);
    }
     void
    attachFake(Connection conn, State state) {
        connections.add(conn);
        this.state = state;
    }
    void
    connectionDisconnected(GsmConnection conn) {
        if (state != State.DISCONNECTED) {
            boolean hasOnlyDisconnectedConnections = true;
            for (int i = 0, s = connections.size()  ; i < s; i ++) {
                if (connections.get(i).getState()
                    != State.DISCONNECTED
                ) {
                    hasOnlyDisconnectedConnections = false;
                    break;
                }
            }
            if (hasOnlyDisconnectedConnections) {
                state = State.DISCONNECTED;
            }
        }
    }
     void
    detach(GsmConnection conn) {
        connections.remove(conn);
        if (connections.size() == 0) {
            state = State.IDLE;
        }
    }
     boolean
    update (GsmConnection conn, DriverCall dc) {
        State newState;
        boolean changed = false;
        newState = stateFromDCState(dc.state);
        if (newState != state) {
            state = newState;
            changed = true;
        }
        return changed;
    }
     boolean
    isFull() {
        return connections.size() == GsmCallTracker.MAX_CONNECTIONS_PER_CALL;
    }
    void
    onHangupLocal() {
        for (int i = 0, s = connections.size()
                ; i < s; i++
        ) {
            GsmConnection cn = (GsmConnection)connections.get(i);
            cn.onHangupLocal();
        }
        state = State.DISCONNECTING;
    }
    void
    clearDisconnected() {
        for (int i = connections.size() - 1 ; i >= 0 ; i--) {
            GsmConnection cn = (GsmConnection)connections.get(i);
            if (cn.getState() == State.DISCONNECTED) {
                connections.remove(i);
            }
        }
        if (connections.size() == 0) {
            state = State.IDLE;
        }
    }
}
