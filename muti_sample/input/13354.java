public class Test4646747 {
    public static void main(String[] args) {
        XMLEncoder encoder = new XMLEncoder(System.out);
        encoder.setPersistenceDelegate(Test4646747.class, new MyPersistenceDelegate());
        Object[] obs = new Object[10000];
        while (obs != null) {
            try {
                obs = new Object[obs.length + obs.length / 3];
            }
            catch (OutOfMemoryError error) {
                obs = null;
            }
        }
        PersistenceDelegate pd = encoder.getPersistenceDelegate(Test4646747.class);
        if (!(pd instanceof MyPersistenceDelegate))
            throw new Error("persistence delegate has been lost");
    }
    private static class MyPersistenceDelegate extends DefaultPersistenceDelegate {
    }
}
