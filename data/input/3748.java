public class CorbaClientDelegateImpl extends CorbaClientDelegate
{
    private ORB orb;
    private ORBUtilSystemException wrapper ;
    private CorbaContactInfoList contactInfoList;
    public CorbaClientDelegateImpl(ORB orb,
                                   CorbaContactInfoList contactInfoList)
    {
        this.orb = orb;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        this.contactInfoList = contactInfoList;
    }
    public Broker getBroker()
    {
        return orb;
    }
    public ContactInfoList getContactInfoList()
    {
        return contactInfoList;
    }
    public OutputStream request(org.omg.CORBA.Object self,
                                String operation,
                                boolean responseExpected)
    {
        ClientInvocationInfo invocationInfo =
            orb.createOrIncrementInvocationInfo();
        Iterator contactInfoListIterator =
            invocationInfo.getContactInfoListIterator();
        if (contactInfoListIterator == null) {
            contactInfoListIterator = contactInfoList.iterator();
            invocationInfo.setContactInfoListIterator(contactInfoListIterator);
        }
        if (! contactInfoListIterator.hasNext()) {
            throw ((CorbaContactInfoListIterator)contactInfoListIterator)
                .getFailureException();
        }
        CorbaContactInfo contactInfo = (CorbaContactInfo) contactInfoListIterator.next();
        ClientRequestDispatcher subcontract = contactInfo.getClientRequestDispatcher();
        invocationInfo.setClientRequestDispatcher(subcontract);
        return (OutputStream)
            subcontract.beginRequest(self, operation,
                                     !responseExpected, contactInfo);
    }
    public InputStream invoke(org.omg.CORBA.Object self, OutputStream output)
        throws
            ApplicationException,
            RemarshalException
    {
        ClientRequestDispatcher subcontract = getClientRequestDispatcher();
        return (InputStream)
            subcontract.marshalingComplete((Object)self, (OutputObject)output);
    }
    public void releaseReply(org.omg.CORBA.Object self, InputStream input)
    {
        ClientRequestDispatcher subcontract = getClientRequestDispatcher();
        subcontract.endRequest(orb, self, (InputObject)input);
        orb.releaseOrDecrementInvocationInfo();
    }
    private ClientRequestDispatcher getClientRequestDispatcher()
    {
        return (ClientRequestDispatcher)
            ((CorbaInvocationInfo)orb.getInvocationInfo())
            .getClientRequestDispatcher();
    }
    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object obj)
    {
        InputStream is = null;
        org.omg.CORBA.Object stub = null ;
        try {
            OutputStream os = request(null, "_interface", true);
            is = (InputStream) invoke((org.omg.CORBA.Object)null, os);
            org.omg.CORBA.Object objimpl =
                (org.omg.CORBA.Object) is.read_Object();
            if ( !objimpl._is_a("IDL:omg.org/CORBA/InterfaceDef:1.0") )
                throw wrapper.wrongInterfaceDef(CompletionStatus.COMPLETED_MAYBE);
            try {
                stub = (org.omg.CORBA.Object)
                    JDKBridge.loadClass("org.omg.CORBA._InterfaceDefStub").
                        newInstance();
            } catch (Exception ex) {
                throw wrapper.noInterfaceDefStub( ex ) ;
            }
            org.omg.CORBA.portable.Delegate del =
                StubAdapter.getDelegate( objimpl ) ;
            StubAdapter.setDelegate( stub, del ) ;
        } catch (ApplicationException e) {
            throw wrapper.applicationExceptionInSpecialMethod( e ) ;
        } catch (RemarshalException e) {
            return get_interface_def(obj);
        } finally {
            releaseReply((org.omg.CORBA.Object)null, (InputStream)is);
        }
        return stub;
    }
    public boolean is_a(org.omg.CORBA.Object obj, String dest)
    {
        String [] repositoryIds = StubAdapter.getTypeIds( obj ) ;
        String myid = contactInfoList.getTargetIOR().getTypeId();
        if ( dest.equals(myid) ) {
            return true;
        }
        for ( int i=0; i<repositoryIds.length; i++ ) {
            if ( dest.equals(repositoryIds[i]) ) {
                return true;
            }
        }
        InputStream is = null;
        try {
            OutputStream os = request(null, "_is_a", true);
            os.write_string(dest);
            is = (InputStream) invoke((org.omg.CORBA.Object) null, os);
            return is.read_boolean();
        } catch (ApplicationException e) {
            throw wrapper.applicationExceptionInSpecialMethod( e ) ;
        } catch (RemarshalException e) {
            return is_a(obj, dest);
        } finally {
            releaseReply((org.omg.CORBA.Object)null, (InputStream)is);
        }
    }
    public boolean non_existent(org.omg.CORBA.Object obj)
    {
        InputStream is = null;
        try {
            OutputStream os = request(null, "_non_existent", true);
            is = (InputStream) invoke((org.omg.CORBA.Object)null, os);
            return is.read_boolean();
        } catch (ApplicationException e) {
            throw wrapper.applicationExceptionInSpecialMethod( e ) ;
        } catch (RemarshalException e) {
            return non_existent(obj);
        } finally {
            releaseReply((org.omg.CORBA.Object)null, (InputStream)is);
        }
    }
    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object obj)
    {
        return obj;
    }
    public void release(org.omg.CORBA.Object obj)
    {
    }
    public boolean is_equivalent(org.omg.CORBA.Object obj,
                                 org.omg.CORBA.Object ref)
    {
        if ( ref == null )
            return false;
        if (!StubAdapter.isStub(ref))
            return false ;
        Delegate del = StubAdapter.getDelegate(ref) ;
        if (del == null)
            return false ;
        if (del == this)
            return true;
        if (!(del instanceof CorbaClientDelegateImpl))
            return false ;
        CorbaClientDelegateImpl corbaDelegate = (CorbaClientDelegateImpl)del ;
        CorbaContactInfoList ccil =
            (CorbaContactInfoList)corbaDelegate.getContactInfoList() ;
        return this.contactInfoList.getTargetIOR().isEquivalent(
            ccil.getTargetIOR() );
    }
    public boolean equals(org.omg.CORBA.Object self, java.lang.Object other)
    {
        if (other == null)
            return false ;
        if (!StubAdapter.isStub(other)) {
            return false;
        }
        Delegate delegate = StubAdapter.getDelegate( other ) ;
        if (delegate == null)
            return false ;
        if (delegate instanceof CorbaClientDelegateImpl) {
            CorbaClientDelegateImpl otherDel = (CorbaClientDelegateImpl)
                delegate ;
            IOR otherIor = otherDel.contactInfoList.getTargetIOR();
            return this.contactInfoList.getTargetIOR().equals(otherIor);
        }
        return false;
    }
    public int hashCode(org.omg.CORBA.Object obj)
    {
        return this.hashCode() ;
    }
    public int hash(org.omg.CORBA.Object obj, int maximum)
    {
        int h = this.hashCode();
        if ( h > maximum )
            return 0;
        return h;
    }
    public Request request(org.omg.CORBA.Object obj, String operation)
    {
        return new RequestImpl(orb, obj, null, operation, null, null, null,
                               null);
    }
    public Request create_request(org.omg.CORBA.Object obj,
                                  Context ctx,
                                  String operation,
                                  NVList arg_list,
                                  NamedValue result)
    {
        return new RequestImpl(orb, obj, ctx, operation, arg_list,
                               result, null, null);
    }
    public Request create_request(org.omg.CORBA.Object obj,
                                  Context ctx,
                                  String operation,
                                  NVList arg_list,
                                  NamedValue result,
                                  ExceptionList exclist,
                                  ContextList ctxlist)
    {
        return new RequestImpl(orb, obj, ctx, operation, arg_list, result,
                               exclist, ctxlist);
    }
    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object obj)
    {
        return this.orb;
    }
    public boolean is_local(org.omg.CORBA.Object self)
    {
        return contactInfoList.getEffectiveTargetIOR().getProfile().
            isLocal();
    }
    public ServantObject servant_preinvoke(org.omg.CORBA.Object self,
                                           String operation,
                                           Class expectedType)
    {
        return
            contactInfoList.getLocalClientRequestDispatcher()
            .servant_preinvoke(self, operation, expectedType);
    }
    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servant)
    {
        contactInfoList.getLocalClientRequestDispatcher()
            .servant_postinvoke(self, servant);
    }
    public String get_codebase(org.omg.CORBA.Object self)
    {
        if (contactInfoList.getTargetIOR() != null) {
            return contactInfoList.getTargetIOR().getProfile().getCodebase();
        }
        return null;
    }
    public String toString(org.omg.CORBA.Object self)
    {
        return contactInfoList.getTargetIOR().stringify();
    }
    public int hashCode()
    {
        return this.contactInfoList.hashCode();
    }
}
