public class NotExtending implements Remote {
    private Remote stub;
    private int hashValue;
    private boolean hashValueInitialized = false;
    public NotExtending() throws RemoteException {
        stub = UnicastRemoteObject.exportObject(this);
        setHashValue(stub.hashCode());
    }
    private void setHashValue(int value) {
        hashValue = value;
        hashValueInitialized = true;
    }
    public int hashCode() {
        if (!hashValueInitialized) {
            throw new RuntimeException(
                "hashCode() invoked before hashValue initialized");
        }
        return hashValue;
    }
    public boolean equals(Object obj) {
        return stub.equals(obj);
    }
    public static void main(String[] args) throws Exception {
        Object dummy = new Object();
        NotExtending server;
        try {
            server = new NotExtending();
            System.err.println("Server exported without invoking hashCode().");
            if (server.equals(server.stub)) {
                System.err.println(
                    "Passing stub to server's equals() method succeeded.");
            } else {
                throw new RuntimeException(
                    "passing stub to server's equals() method failed");
            }
            if (server.stub.equals(server)) {
                System.err.println(
                    "Passing server to stub's equals() method succeeded.");
            } else {
                throw new RuntimeException(
                    "passing server to stub's equals() method failed");
            }
        } finally {
            server = null;
            flushCachedRefs();
        }
    }
    public static void flushCachedRefs() {
        java.util.Vector chain = new java.util.Vector();
        try {
            while (true) {
                int[] hungry = new int[65536];
                chain.addElement(hungry);
            }
        } catch (OutOfMemoryError e) {
        }
    }
}
