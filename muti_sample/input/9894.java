public class AOMEntry extends FSMImpl {
    private final Thread[] etherealizer ;   
    private final int[] counter ;           
    private final CondVar wait ;            
    final POAImpl poa ;
    public static final State INVALID = new StateImpl( "Invalid" ) ;
    public static final State INCARN  = new StateImpl( "Incarnating" ) {
        public void postAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            entry.wait.broadcast() ;
        }
    };
    public static final State VALID   = new StateImpl( "Valid" ) ;
    public static final State ETHP    = new StateImpl( "EtherealizePending" ) ;
    public static final State ETH     = new StateImpl( "Etherealizing" ) {
        public void preAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            Thread etherealizer = entry.etherealizer[0] ;
            if (etherealizer != null)
                etherealizer.start() ;
        }
        public void postAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            entry.wait.broadcast() ;
        }
    };
    public static final State DESTROYED = new StateImpl( "Destroyed" ) ;
    static final Input START_ETH    = new InputImpl( "startEtherealize" ) ;
    static final Input ETH_DONE     = new InputImpl( "etherealizeDone" ) ;
    static final Input INC_DONE     = new InputImpl( "incarnateDone" ) ;
    static final Input INC_FAIL     = new InputImpl( "incarnateFailure" ) ;
    static final Input ACTIVATE     = new InputImpl( "activateObject" ) ;
    static final Input ENTER        = new InputImpl( "enter" ) ;
    static final Input EXIT         = new InputImpl( "exit" ) ;
    private static Action incrementAction = new ActionBase( "increment" ) {
        public void doIt( FSM fsm, Input in ) {
            AOMEntry entry = (AOMEntry)fsm ;
            entry.counter[0]++ ;
        }
    } ;
    private static Action decrementAction = new ActionBase( "decrement" ) {
        public void doIt( FSM fsm, Input in ) {
            AOMEntry entry = (AOMEntry)fsm ;
            if (entry.counter[0] > 0)
                entry.counter[0]-- ;
            else
                throw entry.poa.lifecycleWrapper().aomEntryDecZero() ;
        }
    } ;
    private static Action throwIllegalStateExceptionAction = new ActionBase(
        "throwIllegalStateException" ) {
        public void doIt( FSM fsm, Input in ) {
            throw new IllegalStateException(
                "No transitions allowed from the DESTROYED state" ) ;
        }
    } ;
    private static Action oaaAction = new ActionBase( "throwObjectAlreadyActive" ) {
         public void doIt( FSM fsm, Input in ) {
             throw new RuntimeException( new ObjectAlreadyActive() ) ;
         }
    } ;
    private static Guard waitGuard = new GuardBase( "wait" ) {
        public Guard.Result evaluate( FSM fsm, Input in ) {
            AOMEntry entry = (AOMEntry)fsm ;
            try {
                entry.wait.await() ;
            } catch (InterruptedException exc) {
            }
            return Guard.Result.DEFERED ;
        }
    } ;
    private static class CounterGuard extends GuardBase {
        private int value ;
        public CounterGuard( int value )
        {
            super( "counter>" + value ) ;
            this.value = value ;
        }
        public Guard.Result evaluate( FSM fsm, Input in )
        {
            AOMEntry entry = (AOMEntry)fsm ;
            return Guard.Result.convert( entry.counter[0] > value ) ;
        }
    } ;
    private static GuardBase greaterZeroGuard = new CounterGuard( 0 ) ;
    private static Guard zeroGuard = new Guard.Complement( greaterZeroGuard ) ;
    private static GuardBase greaterOneGuard = new CounterGuard( 1 ) ;
    private static Guard oneGuard = new Guard.Complement( greaterOneGuard ) ;
    private static StateEngine engine ;
    static {
        engine = StateEngineFactory.create() ;
        engine.add( INVALID, ENTER,                             incrementAction,    INCARN      ) ;
        engine.add( INVALID, ACTIVATE,                          null,               VALID       ) ;
        engine.setDefault( INVALID ) ;
        engine.add( INCARN,  ENTER,     waitGuard,              null,               INCARN      ) ;
        engine.add( INCARN,  EXIT,                              null,               INCARN      ) ;
        engine.add( INCARN,  START_ETH, waitGuard,              null,               INCARN      ) ;
        engine.add( INCARN,  INC_DONE,                          null,               VALID       ) ;
        engine.add( INCARN,  INC_FAIL,                          decrementAction,    INVALID     ) ;
        engine.add( INCARN,  ACTIVATE,                          oaaAction,          INCARN      ) ;
        engine.add( VALID,   ENTER,                             incrementAction,    VALID       ) ;
        engine.add( VALID,   EXIT,                              decrementAction,    VALID       ) ;
        engine.add( VALID,   START_ETH, greaterZeroGuard,       null,               ETHP        ) ;
        engine.add( VALID,   START_ETH, zeroGuard,              null,               ETH         ) ;
        engine.add( VALID,   ACTIVATE,                          oaaAction,          VALID       ) ;
        engine.add( ETHP,    ENTER,     waitGuard,              null,               ETHP        ) ;
        engine.add( ETHP,    START_ETH,                         null,               ETHP        ) ;
        engine.add( ETHP,    EXIT,      greaterOneGuard,        decrementAction,    ETHP        ) ;
        engine.add( ETHP,    EXIT,      oneGuard,               decrementAction,    ETH         ) ;
        engine.add( ETHP,    ACTIVATE,                          oaaAction,          ETHP        ) ;
        engine.add( ETH,     START_ETH,                         null,               ETH         ) ;
        engine.add( ETH,     ETH_DONE,                          null,               DESTROYED   ) ;
        engine.add( ETH,     ACTIVATE,                          oaaAction,          ETH         ) ;
        engine.add( ETH,     ENTER,     waitGuard,              null,               ETH         ) ;
        engine.setDefault( DESTROYED, throwIllegalStateExceptionAction, DESTROYED ) ;
        engine.done() ;
    }
    public AOMEntry( POAImpl poa )
    {
        super( engine, INVALID, ((ORB)poa.getORB()).poaFSMDebugFlag ) ;
        this.poa = poa ;
        etherealizer = new Thread[1] ;
        etherealizer[0] = null ;
        counter = new int[1] ;
        counter[0] = 0 ;
        wait = new CondVar( poa.poaMutex,
            ((ORB)poa.getORB()).poaConcurrencyDebugFlag ) ;
    }
    public void startEtherealize( Thread etherealizer )
    {
        this.etherealizer[0] = etherealizer ;
        doIt( START_ETH ) ;
    }
    public void etherealizeComplete() { doIt( ETH_DONE ) ; }
    public void incarnateComplete() { doIt( INC_DONE ) ; }
    public void incarnateFailure() { doIt( INC_FAIL ) ; }
    public void activateObject() throws ObjectAlreadyActive {
         try {
             doIt( ACTIVATE ) ;
         } catch (RuntimeException exc) {
             Throwable thr = exc.getCause() ;
             if (thr instanceof ObjectAlreadyActive)
                 throw (ObjectAlreadyActive)thr ;
             else
                 throw exc ;
         }
    }
    public void enter() { doIt( ENTER ) ; }
    public void exit() { doIt( EXIT ) ; }
}
