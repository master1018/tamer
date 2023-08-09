public class StateEngineFactory {
    private StateEngineFactory() {}
    public static StateEngine create()
    {
        return new StateEngineImpl() ;
    }
}
