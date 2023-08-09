public abstract class TransportDefault {
    private TransportDefault() {}
    public static CorbaContactInfoListFactory makeCorbaContactInfoListFactory(
        final ORB broker )
    {
        return new CorbaContactInfoListFactory() {
            public void setORB(ORB orb) { }
            public CorbaContactInfoList create( IOR ior ) {
                return new CorbaContactInfoListImpl(
                    (com.sun.corba.se.spi.orb.ORB)broker, ior ) ;
            }
        };
    }
    public static ClientDelegateFactory makeClientDelegateFactory(
        final ORB broker )
    {
        return new ClientDelegateFactory() {
            public CorbaClientDelegate create( CorbaContactInfoList info ) {
                return new CorbaClientDelegateImpl(
                    (com.sun.corba.se.spi.orb.ORB)broker, info ) ;
            }
        };
    }
    public static IORTransformer makeIORTransformer(
        final ORB broker )
    {
        return null ;
    }
    public static ReadTimeoutsFactory makeReadTimeoutsFactory()
    {
        return new ReadTimeoutsFactory() {
            public ReadTimeouts create(int initial_wait_time,
                                       int max_wait_time,
                                       int max_giop_hdr_wait_time,
                                       int backoff_percent_factor)
            {
                return new ReadTCPTimeoutsImpl(
                    initial_wait_time,
                    max_wait_time,
                    max_giop_hdr_wait_time,
                    backoff_percent_factor);
            };
        };
    }
}
