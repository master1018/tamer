public class GuardedAction {
    private static Guard trueGuard = new GuardBase( "true" ) {
        public Guard.Result evaluate( FSM fsm, Input in )
        {
            return Guard.Result.ENABLED ;
        }
    } ;
    private Guard guard ;
    private Action action ;
    private State nextState ;
    public GuardedAction( Action action, State nextState )
    {
        this.guard = trueGuard ;
        this.action = action ;
        this.nextState = nextState ;
    }
    public GuardedAction( Guard guard, Action action, State nextState )
    {
        this.guard = guard ;
        this.action = action ;
        this.nextState = nextState ;
    }
    public String toString()
    {
        return "GuardedAction[action=" + action + " guard=" + guard +
            " nextState=" + nextState + "]" ;
    }
    public Action getAction() { return action ; }
    public Guard getGuard() { return guard ; }
    public State getNextState() { return nextState ; }
}
