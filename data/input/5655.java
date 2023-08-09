public class bug6464022 {
    private static JOptionPane pane;
    public static void main(String[] args) throws Exception {
        final List<WeakReference<JDialog>> references = new ArrayList<WeakReference<JDialog>>();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                pane = new JOptionPane(null, JOptionPane.UNDEFINED_CONDITION);
                for (int i = 0; i < 10; i++) {
                    JDialog dialog = pane.createDialog(null, "Test " + i);
                    references.add(new WeakReference<JDialog>(dialog));
                    dialog.dispose();
                    System.out.println("Disposing Dialog:" + dialog.hashCode());
                }
            }
        });
        Util.generateOOME();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                int allocatedCount = 0;
                for (WeakReference<JDialog> ref : references) {
                    if (ref.get() != null) {
                        allocatedCount++;
                        System.out.println(ref.get().hashCode() + " is still allocated");
                    }
                }
                if (allocatedCount > 0) {
                    throw new RuntimeException("Some dialogs still exist in memory. Test failed");
                } else {
                    System.out.println("All dialogs were GCed. Test passed.");
                }
            }
        });
    }
}
