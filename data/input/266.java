abstract class POAPolicyMediatorFactory {
    static POAPolicyMediator create( Policies policies, POAImpl poa )
    {
        if (policies.retainServants()) {
            if (policies.useActiveMapOnly())
                return new POAPolicyMediatorImpl_R_AOM( policies, poa ) ;
            else if (policies.useDefaultServant())
                return new POAPolicyMediatorImpl_R_UDS( policies, poa ) ;
            else if (policies.useServantManager())
                return new POAPolicyMediatorImpl_R_USM( policies, poa ) ;
            else
                throw poa.invocationWrapper().pmfCreateRetain() ;
        } else {
            if (policies.useDefaultServant())
                return new POAPolicyMediatorImpl_NR_UDS( policies, poa ) ;
            else if (policies.useServantManager())
                return new POAPolicyMediatorImpl_NR_USM( policies, poa ) ;
            else
                throw poa.invocationWrapper().pmfCreateNonRetain() ;
        }
    }
}
