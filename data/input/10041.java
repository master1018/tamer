public class Test4092905 {
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    public static void main(String[] args) {
        PropertyChangeSupport pcs = new PropertyChangeSupport(args);
        pcs.addPropertyChangeListener(PUBLIC, new PublicListener());
        pcs.addPropertyChangeListener(PRIVATE, new PrivateListener());
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
            pcs = (PropertyChangeSupport) input.readObject();
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
    public static class PublicListener implements PropertyChangeListener, Serializable {
        public void propertyChange(PropertyChangeEvent event) {
        }
    }
    private static class PrivateListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
        }
    }
}
