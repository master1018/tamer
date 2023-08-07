final class ControlImpl extends _ControlImplBase implements Control, RecoveryCoordinator {
    protected final TransactionImpl _tx;
    protected final TransIdentity[] _parents;
    private PropagationContext _pgContext;
    private Terminator _terminator;
    private Coordinator _coordinator;
    ControlImpl(TransactionImpl tx, PropagationContext pgContext) {
        if (tx == null) throw new IllegalArgumentException("Argument tx is null");
        if (pgContext == null) throw new IllegalArgumentException("Argument pgContext is null");
        _tx = tx;
        _parents = new TransIdentity[pgContext.parents.length + 1];
        for (int i = pgContext.parents.length; i-- > 0; ) _parents[i + 1] = pgContext.parents[i];
        _parents[0] = pgContext.current;
    }
    ControlImpl(TransactionImpl tx) {
        ControlImpl parent;
        if (tx == null) throw new IllegalArgumentException("Argument tx is null");
        _tx = tx;
        if (tx.getParent() != null) {
            tx = (TransactionImpl) tx.getParent();
            parent = (ControlImpl) tx.getControl();
            if (parent._parents == null) {
                _parents = new TransIdentity[] { parent.getIdentity() };
            } else {
                _parents = new TransIdentity[parent._parents.length + 1];
                for (int i = parent._parents.length; i-- > 0; ) _parents[i + 1] = parent._parents[i];
                _parents[0] = parent.getIdentity();
            }
        } else _parents = null;
    }
    public Terminator get_terminator() throws Unavailable {
        int status;
        status = _tx.getStatus();
        if (status == javax.transaction.Status.STATUS_ACTIVE || status == javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
            return getTerminator();
        }
        throw new Unavailable();
    }
    public Coordinator get_coordinator() throws Unavailable {
        int status;
        status = _tx.getStatus();
        if (status == javax.transaction.Status.STATUS_ACTIVE || status == javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
            return getCoordinator();
        }
        throw new Unavailable();
    }
    protected Terminator getTerminator() {
        ORB orb;
        if (_terminator != null) return _terminator;
        _terminator = new TerminatorImpl(this);
        orb = _tx._txDomain._orb;
        if (orb != null) orb.connect(_terminator);
        return _terminator;
    }
    protected Coordinator getCoordinator() {
        ORB orb;
        if (_coordinator != null) return _coordinator;
        _coordinator = new CoordinatorImpl(this);
        orb = _tx._txDomain._orb;
        if (orb != null) orb.connect(_coordinator);
        return _coordinator;
    }
    protected synchronized PropagationContext getPropagationContext() {
        ORB orb;
        Any any;
        Xid xid;
        otid_t otid;
        byte[] global;
        TransIdentity identity;
        if (_pgContext != null) return _pgContext;
        xid = _tx._xid;
        global = xid.getGlobalTransactionId();
        otid = new otid_t(xid.getFormatId(), global.length, global);
        identity = new TransIdentity(getCoordinator(), getTerminator(), otid);
        orb = _tx._txDomain._orb;
        if (orb == null) _pgContext = new PropagationContext(_tx._txDomain.getTransactionTimeout(_tx), identity, _parents != null ? _parents : new TransIdentity[0], null); else {
            any = orb.create_any();
            _pgContext = new PropagationContext(_tx._txDomain.getTransactionTimeout(_tx), identity, _parents != null ? _parents : new TransIdentity[0], any);
        }
        return _pgContext;
    }
    protected TransIdentity getIdentity() {
        Xid xid;
        otid_t otid;
        byte[] global;
        TransIdentity identity;
        xid = _tx._xid;
        global = xid.getGlobalTransactionId();
        otid = new otid_t(xid.getFormatId(), global.length, global);
        identity = new TransIdentity(getCoordinator(), getTerminator(), otid);
        return identity;
    }
    protected void deactivate() {
        ORB orb;
        orb = _tx._txDomain._orb;
        if (orb != null) {
            orb.disconnect(this);
            if (_coordinator != null) orb.disconnect(_coordinator);
            if (_terminator != null) orb.disconnect(_terminator);
        }
    }
    public Status replay_completion(Resource resource) {
        return CoordinatorImpl.fromJTAStatus(_tx.getStatus());
    }
    TransactionImpl getTransaction() {
        return _tx;
    }
}
