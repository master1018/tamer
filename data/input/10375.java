public class StateImpl extends NameBase implements State {
    private Action defaultAction ;
    private State defaultNextState ;
    private Map inputToGuardedActions ;
    public StateImpl( String name )
    {
        super( name ) ;
        defaultAction = null ;
        inputToGuardedActions = new HashMap() ;
    }
    public void preAction( FSM fsm )
    {
    }
    public void postAction( FSM fsm )
    {
    }
    public State getDefaultNextState()
    {
        return defaultNextState ;
    }
    public void setDefaultNextState( State defaultNextState )
    {
        this.defaultNextState = defaultNextState ;
    }
    public Action getDefaultAction()
    {
        return defaultAction ;
    }
    public void setDefaultAction( Action defaultAction )
    {
        this.defaultAction = defaultAction ;
    }
    public void addGuardedAction( Input in, GuardedAction ga )
    {
        Set gas = (Set)inputToGuardedActions.get( in ) ;
        if (gas == null) {
            gas = new HashSet() ;
            inputToGuardedActions.put( in, gas ) ;
        }
        gas.add( ga ) ;
    }
    public Set getGuardedActions( Input in )
    {
        return (Set)inputToGuardedActions.get( in ) ;
    }
}
