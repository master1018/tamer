        String getStubName( String className ) ;
        PresentationManager.StubFactory createStubFactory( String className,
            boolean isIDLStub, String remoteCodeBase, Class expectedClass,
            ClassLoader classLoader);
        Tie getTie( Class cls ) ;
        boolean createsDynamicStubs() ;
    }
    public interface StubFactory
    {
        org.omg.CORBA.Object makeStub() ;
        String[] getTypeIds() ;
    }
    public interface ClassData
    {
        Class getMyClass() ;
        IDLNameTranslator getIDLNameTranslator() ;
        String[] getTypeIds() ;
        InvocationHandlerFactory getInvocationHandlerFactory() ;
        Map getDictionary() ;
    }
    ClassData getClassData( Class cls ) ;
    DynamicMethodMarshaller getDynamicMethodMarshaller( Method method ) ;
    StubFactoryFactory getStubFactoryFactory( boolean isDynamic ) ;
    void setStubFactoryFactory( boolean isDynamic, StubFactoryFactory sff ) ;
    Tie getTie() ;
    boolean useDynamicStubs() ;
}
