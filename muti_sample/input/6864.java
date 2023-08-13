public class Test4072197 {
    public static void main(String[] args) {
        try {
            new EventSetDescriptor(
                    SourceClass.class,
                    "action",
                    Listener.class,
                    new String[] {"action", "event", null, "dummy"},
                    "addActionListener",
                    "removeActionListener"
            );
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        } catch (NullPointerException exception) {
            return; 
        }
        throw new Error("NullPointerException expected");
    }
    public static class SourceClass {
        public void addActionListener(ActionListener listener) {
        }
        public void removeActionListener(ActionListener listener) {
        }
    }
    public static class Listener {
        public void action(EventObject event) {
        }
        public void event(EventObject event) {
        }
        public void dummy(EventObject event) {
        }
    }
}
