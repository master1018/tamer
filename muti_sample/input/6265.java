public final class IORInfoImpl
    extends LocalObject
    implements IORInfo, IORInfoExt
{
    private static final int STATE_INITIAL = 0 ;
    private static final int STATE_ESTABLISHED = 1 ;
    private static final int STATE_DONE = 2 ;
    private int state = STATE_INITIAL ;
    private ObjectAdapter adapter;
    private ORB orb ;
    private ORBUtilSystemException orbutilWrapper ;
    private InterceptorsSystemException wrapper ;
    private OMGSystemException omgWrapper ;
    IORInfoImpl( ObjectAdapter adapter ) {
        this.orb = adapter.getORB() ;
        orbutilWrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        wrapper = InterceptorsSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        omgWrapper = OMGSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        this.adapter = adapter;
    }
    public Policy get_effective_policy (int type) {
        checkState( STATE_INITIAL, STATE_ESTABLISHED ) ;
        return adapter.getEffectivePolicy( type );
    }
    public void add_ior_component (TaggedComponent tagged_component) {
        checkState( STATE_INITIAL ) ;
        if( tagged_component == null ) nullParam();
        addIORComponentToProfileInternal( tagged_component,
                                          adapter.getIORTemplate().iterator());
    }
    public void add_ior_component_to_profile (
        TaggedComponent tagged_component, int profile_id )
    {
        checkState( STATE_INITIAL ) ;
        if( tagged_component == null ) nullParam();
        addIORComponentToProfileInternal(
            tagged_component, adapter.getIORTemplate().iteratorById(
            profile_id ) );
    }
    public int getServerPort(String type)
        throws UnknownType
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED ) ;
        int port =
            orb.getLegacyServerSocketManager()
                .legacyGetTransientOrPersistentServerPort(type);
        if (port == -1) {
            throw new UnknownType();
        }
        return port;
    }
    public ObjectAdapter getObjectAdapter()
    {
        return adapter;
    }
    public int manager_id()
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED) ;
        return adapter.getManagerId() ;
    }
    public short state()
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED) ;
        return adapter.getState() ;
    }
    public ObjectReferenceTemplate adapter_template()
    {
        checkState( STATE_ESTABLISHED) ;
        return adapter.getAdapterTemplate() ;
    }
    public ObjectReferenceFactory current_factory()
    {
        checkState( STATE_ESTABLISHED) ;
        return adapter.getCurrentFactory() ;
    }
    public void current_factory( ObjectReferenceFactory factory )
    {
        checkState( STATE_ESTABLISHED) ;
        adapter.setCurrentFactory( factory ) ;
    }
    private void addIORComponentToProfileInternal(
        TaggedComponent tagged_component, Iterator iterator )
    {
        TaggedComponentFactoryFinder finder =
            orb.getTaggedComponentFactoryFinder();
        Object newTaggedComponent = finder.create( orb, tagged_component );
        boolean found = false;
        while( iterator.hasNext() ) {
            found = true;
            TaggedProfileTemplate taggedProfileTemplate =
                (TaggedProfileTemplate)iterator.next();
            taggedProfileTemplate.add( newTaggedComponent );
        }
        if( !found ) {
            throw omgWrapper.invalidProfileId() ;
        }
    }
    private void nullParam()
    {
        throw orbutilWrapper.nullParam() ;
    }
    private void checkState( int expectedState )
    {
        if (expectedState != state)
            throw wrapper.badState1( new Integer(expectedState), new Integer(state) ) ;
    }
    private void checkState( int expectedState1, int expectedState2 )
    {
        if ((expectedState1 != state) && (expectedState2 != state))
            throw wrapper.badState2( new Integer(expectedState1),
                new Integer(expectedState2), new Integer(state) ) ;
    }
    void makeStateEstablished()
    {
        checkState( STATE_INITIAL ) ;
        state = STATE_ESTABLISHED ;
    }
    void makeStateDone()
    {
        checkState( STATE_ESTABLISHED ) ;
        state = STATE_DONE ;
    }
}
