public class bug7013453 {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Method getPaintManagerMethod = RepaintManager.class.getDeclaredMethod("getPaintManager");
                    getPaintManagerMethod.setAccessible(true);
                    final Object paintManager = getPaintManagerMethod.invoke(RepaintManager.currentManager(new JLabel()));
                    String paintManagerName = paintManager.getClass().getCanonicalName();
                    if (!paintManagerName.equals("javax.swing.BufferStrategyPaintManager")) {
                        System.out.println("The test is not suitable for the " + paintManagerName +
                                " paint manager. The test skipped.");
                        return;
                    }
                    final Field showingField = paintManager.getClass().getDeclaredField("showing");
                    showingField.setAccessible(true);
                    synchronized (paintManager) {
                        showingField.setBoolean(paintManager, true);
                    }
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            synchronized (paintManager) {
                                try {
                                    showingField.setBoolean(paintManager, false);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                    thread.start();
                    Method disposeMethod = paintManager.getClass().getDeclaredMethod("dispose");
                    disposeMethod.setAccessible(true);
                    disposeMethod.invoke(paintManager);
                    System.out.println("The test passed.");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
