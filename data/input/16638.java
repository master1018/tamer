public final class SwingAccessor {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private SwingAccessor() {
    }
    public interface JTextComponentAccessor {
        TransferHandler.DropLocation dropLocationForPoint(JTextComponent textComp, Point p);
        Object setDropLocation(JTextComponent textComp, TransferHandler.DropLocation location,
                               Object state, boolean forDrop);
    }
    private static JTextComponentAccessor jtextComponentAccessor;
    public static void setJTextComponentAccessor(JTextComponentAccessor jtca) {
         jtextComponentAccessor = jtca;
    }
    public static JTextComponentAccessor getJTextComponentAccessor() {
        if (jtextComponentAccessor == null) {
            unsafe.ensureClassInitialized(JTextComponent.class);
        }
        return jtextComponentAccessor;
    }
}
