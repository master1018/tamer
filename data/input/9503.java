public class bug6547087 {
    public static void main(String[] args) throws Exception {
        try {
            invokeAndWait(
                new Callable<Void>() {
                    public Void call() throws Exception {
                        test();
                        return null;
                    }
            });
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NullPointerException) {
                throw new RuntimeException("failed");
            }
        }
    }
    static void test() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JPopupMenu.Separator separator = new JPopupMenu.Separator();
        separator.getPreferredSize();
    }
    static <T> T invokeAndWait(Callable<T> callable) throws Exception {
        FutureTask<T> future = new FutureTask<T>(callable);
        SwingUtilities.invokeLater(future);
        return future.get();
    }
}
