public class GetContDirCtx {
    public static void main(String[] args) throws Exception {
        CannotProceedException cpe = new CannotProceedException();
        Hashtable env = new Hashtable(1);
        cpe.setEnvironment(env);
        Reference ref = new Reference("java.lang.Object",
                                    "DummyObjectFactory",
                                    null);
        cpe.setResolvedObj(ref);
        Context contCtx = null;
        try {
            contCtx  = DirectoryManager.getContinuationDirContext(cpe);
        } catch (CannotProceedException e) {
        }
        Hashtable contEnv = contCtx.getEnvironment();
        if (contEnv.get(NamingManager.CPE) != cpe) {
            throw new Exception("Test failed: CPE property not set" +
                        " in the continuation context");
        }
    }
}
