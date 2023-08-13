public class TOAImpl extends ObjectAdapterBase implements TOA
{
    private TransientObjectManager servants ;
    public TOAImpl( ORB orb, TransientObjectManager tom, String codebase )
    {
        super( orb ) ;
        servants = tom ;
        int serverid = ((ORB)getORB()).getTransientServerId();
        int scid = ORBConstants.TOA_SCID ;
        ObjectKeyTemplate oktemp = new JIDLObjectKeyTemplate( orb, scid, serverid ) ;
        Policies policies = Policies.defaultPolicies;
        initializeTemplate( oktemp, true,
                            policies,
                            codebase,
                            null, 
                            oktemp.getObjectAdapterId()
                            ) ;
    }
    public ObjectCopierFactory getObjectCopierFactory()
    {
        CopierManager cm = getORB().getCopierManager() ;
        return cm.getDefaultObjectCopierFactory() ;
    }
    public org.omg.CORBA.Object getLocalServant( byte[] objectId )
    {
        return (org.omg.CORBA.Object)(servants.lookupServant( objectId ) ) ;
    }
    public void getInvocationServant( OAInvocationInfo info )
    {
        java.lang.Object servant = servants.lookupServant( info.id() ) ;
        if (servant == null)
            servant = new NullServantImpl( lifecycleWrapper().nullServant() ) ;
        info.setServant( servant ) ;
    }
    public void returnServant()
    {
    }
    public String[] getInterfaces( Object servant, byte[] objectId )
    {
        return StubAdapter.getTypeIds( servant ) ;
    }
    public Policy getEffectivePolicy( int type )
    {
        return null ;
    }
    public int getManagerId()
    {
        return -1 ;
    }
    public short getState()
    {
        return ACTIVE.value ;
    }
    public void enter() throws OADestroyed
    {
    }
    public void exit()
    {
    }
    public void connect( org.omg.CORBA.Object objref)
    {
        byte[] key = servants.storeServant(objref, null);
        String id = StubAdapter.getTypeIds( objref )[0] ;
        ObjectReferenceFactory orf = getCurrentFactory() ;
        org.omg.CORBA.Object obj = orf.make_object( id, key ) ;
        org.omg.CORBA.portable.Delegate delegate = StubAdapter.getDelegate(
            obj ) ;
        CorbaContactInfoList ccil = (CorbaContactInfoList)
            ((ClientDelegate)delegate).getContactInfoList() ;
        LocalClientRequestDispatcher lcs =
            ccil.getLocalClientRequestDispatcher() ;
        if (lcs instanceof JIDLLocalCRDImpl) {
            JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
            jlcs.setServant( objref ) ;
        } else {
            throw new RuntimeException(
                "TOAImpl.connect can not be called on " + lcs ) ;
        }
        StubAdapter.setDelegate( objref, delegate ) ;
    }
    public void disconnect( org.omg.CORBA.Object objref )
    {
        org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate(
            objref ) ;
        CorbaContactInfoList ccil = (CorbaContactInfoList)
            ((ClientDelegate)del).getContactInfoList() ;
        LocalClientRequestDispatcher lcs =
            ccil.getLocalClientRequestDispatcher() ;
        if (lcs instanceof JIDLLocalCRDImpl) {
            JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
            byte[] oid = jlcs.getObjectId() ;
            servants.deleteServant(oid);
            jlcs.unexport() ;
        } else {
            throw new RuntimeException(
                "TOAImpl.disconnect can not be called on " + lcs ) ;
        }
    }
}
