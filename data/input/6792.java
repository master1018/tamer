public class Test4092906 {
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    public static void main(String[] args) {
        VetoableChangeSupport pcs = new VetoableChangeSupport(args);
        pcs.addVetoableChangeListener(PUBLIC, new PublicListener());
        pcs.addVetoableChangeListener(PRIVATE, new PrivateListener());
        if (!pcs.hasListeners(PUBLIC)) {
            throw new Error("no public listener");
        }
        if (!pcs.hasListeners(PRIVATE)) {
            throw new Error("no private listener");
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(baos);
            output.writeObject(pcs);
            output.flush();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream input = new ObjectInputStream(bais);
            pcs = (VetoableChangeSupport) input.readObject();
        } catch (Exception exception) {
            throw new Error("unexpected exception", exception);
        }
        if (!pcs.hasListeners(PUBLIC)) {
            throw new Error("no public listener");
        }
        if (pcs.hasListeners(PRIVATE)) {
            throw new Error("unexpected private listener");
        }
    }
    public static class PublicListener implements VetoableChangeListener, Serializable {
        public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
        }
    }
    private static class PrivateListener implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
        }
    }
}
