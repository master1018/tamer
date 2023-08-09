public class RemoveUnregedListener {
    public static void main(String[] args) throws Exception {
        Preferences userRoot = null;
        Preferences N1 = null;
        NodeChangeListenerTestAdd ncl = new NodeChangeListenerTestAdd();
        NodeChangeListenerTestAdd ncl2 = new NodeChangeListenerTestAdd();
        NodeChangeListenerTestAdd ncl3 = new NodeChangeListenerTestAdd();
        try {
            userRoot = Preferences.userRoot();
            N1 = userRoot.node("N1");
            userRoot.flush();
            N1.addNodeChangeListener(ncl);
            N1.addNodeChangeListener(ncl2);
            N1.removeNodeChangeListener(ncl3);
            throw new RuntimeException();
        } catch (IllegalArgumentException iae) {
            System.out.println("Test Passed!");
        } catch (Exception e) {
            System.out.println("Test Failed");
            throw e;
        }
    }
}
class NodeChangeListenerTestAdd implements NodeChangeListener {
    public void childAdded(NodeChangeEvent evt) {}
    public void childRemoved(NodeChangeEvent evt) {}
}
