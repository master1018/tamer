public class StateEngineImpl implements StateEngine
{
    private static Action emptyAction = new ActionBase( "Empty" )
    {
        public void doIt( FSM fsm, Input in )
        {
        }
    } ;
    private boolean initializing ;
    private Action defaultAction ;
    public StateEngineImpl()
    {
        initializing = true ;
        defaultAction = new ActionBase("Invalid Transition")
            {
                public void doIt( FSM fsm, Input in )
                {
                    throw new INTERNAL(
                        "Invalid transition attempted from " +
                            fsm.getState() + " under " + in ) ;
                }
            } ;
    }
    public StateEngine add( State oldState, Input input, Guard guard, Action action,
        State newState ) throws IllegalArgumentException,
        IllegalStateException
    {
        mustBeInitializing() ;
        StateImpl oldStateImpl = (StateImpl)oldState ;
        GuardedAction ga = new GuardedAction( guard, action, newState ) ;
        oldStateImpl.addGuardedAction( input, ga ) ;
        return this ;
    }
    public StateEngine add( State oldState, Input input, Action action,
        State newState ) throws IllegalArgumentException,
        IllegalStateException
    {
        mustBeInitializing() ;
        StateImpl oldStateImpl = (StateImpl)oldState ;
        GuardedAction ta = new GuardedAction( action, newState ) ;
        oldStateImpl.addGuardedAction( input, ta ) ;
        return this ;
    }
    public StateEngine setDefault( State oldState, Action action, State newState )
        throws IllegalArgumentException, IllegalStateException
    {
        mustBeInitializing() ;
        StateImpl oldStateImpl = (StateImpl)oldState ;
        oldStateImpl.setDefaultAction( action ) ;
        oldStateImpl.setDefaultNextState( newState ) ;
        return this ;
    }
    public StateEngine setDefault( State oldState, State newState )
        throws IllegalArgumentException, IllegalStateException
    {
        return setDefault( oldState, emptyAction, newState ) ;
    }
    public StateEngine setDefault( State oldState )
        throws IllegalArgumentException, IllegalStateException
    {
        return setDefault( oldState, oldState ) ;
    }
    public void done() throws IllegalStateException
    {
        mustBeInitializing() ;
        initializing = false ;
    }
    public void setDefaultAction( Action act ) throws IllegalStateException
    {
        mustBeInitializing() ;
        defaultAction = act ;
    }
    public void doIt( FSM fsm, Input in, boolean debug )
    {
        if (debug)
            ORBUtility.dprint( this, "doIt enter: currentState = " +
                fsm.getState() + " in = " + in ) ;
        try {
            innerDoIt( fsm, in, debug ) ;
        } finally {
            if (debug)
                ORBUtility.dprint( this, "doIt exit" ) ;
        }
    }
    private StateImpl getDefaultNextState( StateImpl currentState )
    {
        StateImpl nextState = (StateImpl)currentState.getDefaultNextState() ;
        if (nextState == null)
            nextState = currentState ;
        return nextState ;
    }
    private Action getDefaultAction( StateImpl currentState )
    {
        Action action = currentState.getDefaultAction() ;
        if (action == null)
            action = defaultAction ;
        return action ;
    }
    private void innerDoIt( FSM fsm, Input in, boolean debug )
    {
        if (debug) {
            ORBUtility.dprint( this, "Calling innerDoIt with input " + in ) ;
        }
        StateImpl currentState = null ;
        StateImpl nextState = null ;
        Action action = null ;
        boolean deferral = false ;
        do {
            deferral = false ; 
            currentState = (StateImpl)fsm.getState() ;
            nextState = getDefaultNextState( currentState ) ;
            action = getDefaultAction( currentState ) ;
            if (debug) {
                ORBUtility.dprint( this, "currentState      = " + currentState ) ;
                ORBUtility.dprint( this, "in                = " + in ) ;
                ORBUtility.dprint( this, "default nextState = " + nextState    ) ;
                ORBUtility.dprint( this, "default action    = " + action ) ;
            }
            Set gas = currentState.getGuardedActions(in) ;
            if (gas != null) {
                Iterator iter = gas.iterator() ;
                while (iter.hasNext()) {
                    GuardedAction ga = (GuardedAction)iter.next() ;
                    Guard.Result gr = ga.getGuard().evaluate( fsm, in ) ;
                    if (debug)
                        ORBUtility.dprint( this,
                            "doIt: evaluated " + ga + " with result " + gr ) ;
                    if (gr == Guard.Result.ENABLED) {
                        nextState = (StateImpl)ga.getNextState() ;
                        action = ga.getAction() ;
                        if (debug) {
                            ORBUtility.dprint( this, "nextState = " + nextState ) ;
                            ORBUtility.dprint( this, "action    = " + action ) ;
                        }
                        break ;
                    } else if (gr == Guard.Result.DEFERED) {
                        deferral = true ;
                        break ;
                    }
                }
            }
        } while (deferral) ;
        performStateTransition( fsm, in, nextState, action, debug ) ;
    }
    private void performStateTransition( FSM fsm, Input in,
        StateImpl nextState, Action action, boolean debug )
    {
        StateImpl currentState = (StateImpl)fsm.getState() ;
        boolean different = !currentState.equals( nextState ) ;
        if (different) {
            if (debug)
                ORBUtility.dprint( this,
                    "doIt: executing postAction for state " + currentState ) ;
            try {
                currentState.postAction( fsm ) ;
            } catch (Throwable thr) {
                if (debug)
                    ORBUtility.dprint( this,
                        "doIt: postAction threw " + thr ) ;
                if (thr instanceof ThreadDeath)
                    throw (ThreadDeath)thr ;
            }
        }
        try {
            if (action != null)
                action.doIt( fsm, in ) ;
        } finally {
            if (different) {
                if (debug)
                    ORBUtility.dprint( this,
                        "doIt: executing preAction for state " + nextState ) ;
                try {
                    nextState.preAction( fsm ) ;
                } catch (Throwable thr) {
                    if (debug)
                        ORBUtility.dprint( this,
                            "doIt: preAction threw " + thr ) ;
                    if (thr instanceof ThreadDeath)
                        throw (ThreadDeath)thr ;
                }
                ((FSMImpl)fsm).internalSetState( nextState ) ;
            }
            if (debug)
                ORBUtility.dprint( this, "doIt: state is now " + nextState ) ;
        }
    }
    public FSM makeFSM( State startState ) throws IllegalStateException
    {
        mustNotBeInitializing() ;
        return new FSMImpl( this, startState ) ;
    }
    private void mustBeInitializing() throws IllegalStateException
    {
        if (!initializing)
            throw new IllegalStateException(
                "Invalid method call after initialization completed" ) ;
    }
    private void mustNotBeInitializing() throws IllegalStateException
    {
        if (initializing)
            throw new IllegalStateException(
                "Invalid method call before initialization completed" ) ;
    }
}
