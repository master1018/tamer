public class bug6639507 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                assertEmptyTitle(new Dialog((Frame) null), "new Dialog((Frame) null)");
                assertEmptyTitle(new Dialog((Frame) null, true), "new Dialog((Frame) null, true)");
                assertEmptyTitle(new Dialog((Dialog) null), "new Dialog((Dialog) null)");
                assertEmptyTitle(new Dialog((Window) null), "new Dialog((Window) null)");
                assertEmptyTitle(new Dialog(new Dialog((Window) null), Dialog.ModalityType.APPLICATION_MODAL),
                        "new Dialog((Window) null), Dialog.ModalityType.APPLICATION_MODAL");
                assertEmptyTitle(new JDialog((Frame) null), "new JDialog((Frame) null)");
                assertEmptyTitle(new JDialog((Frame) null, true), "new JDialog((Frame) null, true)");
                assertEmptyTitle(new JDialog((Dialog) null), "new JDialog((Dialog) null)");
                assertEmptyTitle(new JDialog((Dialog) null, true), "new JDialog((Dialog) null, true)");
                assertEmptyTitle(new JDialog((Window) null), "new JDialog((Window) null)");
                assertEmptyTitle(new JDialog((Window) null, Dialog.ModalityType.APPLICATION_MODAL),
                        "new JDialog((Window) null, Dialog.ModalityType.APPLICATION_MODAL)");
            }
        });
    }
    private static void assertEmptyTitle(Dialog dialog, String ctr) {
        String title = dialog.getTitle();
        if (title == null || title.length() > 0) {
            throw new RuntimeException("Title is not empty for constructor " + ctr);
        }
    }
}
