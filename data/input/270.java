public class StubFactoryFactoryStaticImpl extends
    StubFactoryFactoryBase
{
    private ORBUtilSystemException wrapper = ORBUtilSystemException.get(
        CORBALogDomains.RPC_PRESENTATION ) ;
    public PresentationManager.StubFactory createStubFactory(
        String className, boolean isIDLStub, String remoteCodeBase, Class
        expectedClass, ClassLoader classLoader)
    {
        String stubName = null ;
        if (isIDLStub)
            stubName = Utility.idlStubName( className ) ;
        else
            stubName = Utility.stubNameForCompiler( className ) ;
        ClassLoader expectedTypeClassLoader =
            (expectedClass == null ? classLoader :
            expectedClass.getClassLoader());
        String firstStubName = stubName ;
        String secondStubName = stubName ;
        if (PackagePrefixChecker.hasOffendingPrefix(stubName))
            firstStubName = PackagePrefixChecker.packagePrefix() + stubName ;
        else
            secondStubName = PackagePrefixChecker.packagePrefix() + stubName ;
        Class clz = null;
        try {
            clz = Util.loadClass( firstStubName, remoteCodeBase,
                expectedTypeClassLoader ) ;
        } catch (ClassNotFoundException e1) {
            wrapper.classNotFound1( CompletionStatus.COMPLETED_MAYBE,
                e1, firstStubName ) ;
            try {
                clz = Util.loadClass( secondStubName, remoteCodeBase,
                    expectedTypeClassLoader ) ;
            } catch (ClassNotFoundException e2) {
                throw wrapper.classNotFound2(
                    CompletionStatus.COMPLETED_MAYBE, e2, secondStubName ) ;
            }
        }
        if ((clz == null) ||
            ((expectedClass != null) && !expectedClass.isAssignableFrom(clz))) {
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null)
                    cl = ClassLoader.getSystemClassLoader();
                clz = cl.loadClass(className);
            } catch (Exception exc) {
                IllegalStateException ise = new IllegalStateException(
                    "Could not load class " + stubName ) ;
                ise.initCause( exc ) ;
                throw ise ;
            }
        }
        return new StubFactoryStaticImpl( clz ) ;
    }
    public Tie getTie( Class cls )
    {
        Class tieClass = null ;
        String className = Utility.tieName(cls.getName());
        try {
            try {
                tieClass = Utility.loadClassForClass(className, Util.getCodebase(cls),
                    null, cls, cls.getClassLoader());
                return (Tie) tieClass.newInstance();
            } catch (Exception err) {
                tieClass = Utility.loadClassForClass(
                    PackagePrefixChecker.packagePrefix() + className,
                    Util.getCodebase(cls), null, cls, cls.getClassLoader());
                return (Tie) tieClass.newInstance();
            }
        } catch (Exception err) {
            return null;
        }
    }
    public boolean createsDynamicStubs()
    {
        return false ;
    }
}
