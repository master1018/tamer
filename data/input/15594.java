public class Test6311051 {
    public static void main(String[] args) throws IntrospectionException, NoSuchMethodException {
        EventSetDescriptor esd = new EventSetDescriptor(
                "foo",
                FooListener.class,
                new Method[] {
                        FooListener.class.getMethod("fooHappened", EventObject.class),
                        FooListener.class.getMethod("moreFooHappened", EventObject.class, Object.class),
                        FooListener.class.getMethod("lessFooHappened"),
                },
                Bean.class.getMethod("addFooListener", FooListener.class),
                Bean.class.getMethod("removeFooListener", FooListener.class)
        );
        System.gc();
        for (Method method : esd.getListenerMethods()) {
            System.out.println(method);
        }
    }
    public static class Bean {
        public void addFooListener(FooListener listener) {
        }
        public void removeFooListener(FooListener listener) {
        }
    }
    public static interface FooListener {
        void fooHappened(EventObject event);
        void moreFooHappened(EventObject event, Object data);
        void lessFooHappened();
    }
}
