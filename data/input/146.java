public class ExtLoadedImpl implements CheckLoader {
    public ExtLoadedImpl(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        Activatable.exportObject(this, id, 0);
    }
    public boolean isCorrectContextLoader() {
        ClassLoader contextLoader =
            Thread.currentThread().getContextClassLoader();
        ClassLoader implLoader = this.getClass().getClassLoader();
        if (contextLoader == implLoader) {
            System.err.println("contextLoader same as implLoader");
            return false;
        } else if (contextLoader.getParent() == implLoader) {
            System.err.println("contextLoader is child of implLoader");
            return true;
        } else {
            System.err.println("unknown loader relationship");
            return false;
        }
    }
}
