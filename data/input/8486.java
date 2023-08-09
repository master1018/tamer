public class TestGuiAvailable implements Runnable {
    public static void main(String[] args) throws InterruptedException {
        if (Beans.isGuiAvailable() == GraphicsEnvironment.isHeadless()) {
            throw new Error("unexpected GuiAvailable property");
        }
        Beans.setGuiAvailable(!Beans.isGuiAvailable());
        ThreadGroup group = new ThreadGroup("$$$");
        Thread thread = new Thread(group, new TestGuiAvailable());
        thread.start();
        thread.join();
    }
    public void run() {
        SunToolkit.createNewAppContext();
        if (Beans.isGuiAvailable() == GraphicsEnvironment.isHeadless()) {
            throw new Error("shared GuiAvailable property");
        }
    }
}
