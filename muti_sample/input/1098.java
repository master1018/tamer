public class Test4498236 {
    public static void main(String[] args) throws Exception {
        PropertyChangeEvent event = new PropertyChangeEvent("source", null, null, null);
        event.setPropagationId("id");
        test("[propertyName=null; oldValue=null; newValue=null; propagationId=id; source=source]", event);
        test("[propertyName=name; oldValue=old; newValue=new; propagationId=null; source=source]",
             new PropertyChangeEvent("source", "name", "old", "new")
        );
        test("[propertyName=array; index=5; oldValue=old; newValue=new; propagationId=null; source=source]",
             new IndexedPropertyChangeEvent("source", "array", "old", "new", 5)
        );
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("n");
        fd.setDisplayName("dn");
        fd.setShortDescription("sd");
        fd.setPreferred(true);
        fd.setHidden(true);
        fd.setExpert(true);
        fd.setValue("first", "value");
        test("[name=n; displayName=dn; shortDescription=sd; preferred; hidden; expert; values={first=value}]", fd);
        test("[name=String; beanClass=class java.lang.String]",
             new BeanDescriptor(String.class)
        );
        test("[name=Object; beanClass=class java.lang.Object; customizerClass=class java.lang.String]",
             new BeanDescriptor(Object.class, String.class)
        );
        test("[name=Object; beanClass=class java.lang.Object; customizerClass=class java.lang.String]",
             new BeanDescriptor(Object.class, String.class)
        );
        test("[name=equals; method=public boolean java.lang.Object.equals(java.lang.Object)]",
             new MethodDescriptor(Object.class.getMethod("equals", Object.class))
        );
        test("[name=equals; method=public boolean java.lang.Object.equals(java.lang.Object); parameterDescriptors={java.beans.ParameterDescriptor[name=null]}]",
             new MethodDescriptor(Object.class.getMethod("equals", Object.class), new ParameterDescriptor[] {
                     new ParameterDescriptor()
             })
        );
        Class type = KeyListener.class;
        String[] names = { "keyTyped", "keyPressed", "keyReleased" };
        Method[] methods = new Method[names.length];
        for (int i = 0; i < names.length; i++) {
            methods[i] = type.getMethod(names[i], KeyEvent.class);
        }
        test("[name=key; inDefaultEventSet; listenerType=interface java.awt.event.KeyListener; getListenerMethod=public java.awt.event.KeyListener Test4498236.getKeyListeners(); addListenerMethod=public void Test4498236.addKeyListener(java.awt.event.KeyListener); removeListenerMethod=public void Test4498236.removeKeyListener(java.awt.event.KeyListener)]",
             new EventSetDescriptor(Test4498236.class, "key", type, names[0])
        );
        test("[name=$$$; inDefaultEventSet; listenerType=interface java.awt.event.KeyListener; addListenerMethod=public void Test4498236.add(java.awt.event.KeyListener); removeListenerMethod=public void Test4498236.remove(java.awt.event.KeyListener)]",
             new EventSetDescriptor(Test4498236.class, "$$$", type, names, "add", "remove")
        );
        test("[name=$$$; inDefaultEventSet; listenerType=interface java.awt.event.KeyListener; getListenerMethod=public java.awt.event.KeyListener Test4498236.get(); addListenerMethod=public void Test4498236.add(java.awt.event.KeyListener); removeListenerMethod=public void Test4498236.remove(java.awt.event.KeyListener)]",
             new EventSetDescriptor(Test4498236.class, "$$$", type, names, "add", "remove", "get")
        );
        test("[name=$$$; inDefaultEventSet; listenerType=interface java.awt.event.KeyListener; addListenerMethod=public void Test4498236.add(java.awt.event.KeyListener); removeListenerMethod=public void Test4498236.remove(java.awt.event.KeyListener)]",
             new EventSetDescriptor("$$$", type, methods, Test4498236.class.getMethod("add", type), Test4498236.class.getMethod("remove", type))
        );
        test("[name=$$$; inDefaultEventSet; listenerType=interface java.awt.event.KeyListener; getListenerMethod=public java.awt.event.KeyListener Test4498236.get(); addListenerMethod=public void Test4498236.add(java.awt.event.KeyListener); removeListenerMethod=public void Test4498236.remove(java.awt.event.KeyListener)]",
             new EventSetDescriptor("$$$", type, methods, Test4498236.class.getMethod("add", type), Test4498236.class.getMethod("remove", type), Test4498236.class.getMethod("get"))
        );
        test("[name=value; propertyType=boolean; readMethod=public boolean Test4498236.isValue(); writeMethod=public void Test4498236.setValue(boolean)]",
             new PropertyDescriptor("value", Test4498236.class)
        );
        test("[name=$$$]",
             new PropertyDescriptor("$$$", Test4498236.class, null, null)
        );
        test("[name=$$$; propertyType=boolean; readMethod=public boolean Test4498236.getValue()]",
             new PropertyDescriptor("$$$", Test4498236.class, "getValue", null)
        );
        test("[name=$$$; propertyType=boolean; readMethod=public boolean Test4498236.getValue(); writeMethod=public void Test4498236.setValue(boolean)]",
             new PropertyDescriptor("$$$", Test4498236.class, "getValue", "setValue")
        );
        test("[name=$$$]",
             new PropertyDescriptor("$$$", null, null)
        );
        test("[name=$$$; propertyType=boolean; readMethod=public boolean Test4498236.getValue()]",
             new PropertyDescriptor("$$$", Test4498236.class.getMethod("getValue"), null)
        );
        test("[name=$$$; propertyType=boolean; readMethod=public boolean Test4498236.getValue(); writeMethod=public void Test4498236.setValue(boolean)]",
             new PropertyDescriptor("$$$", Test4498236.class.getMethod("getValue"), Test4498236.class.getMethod("setValue", boolean.class))
        );
        test("[name=index; propertyType=class [I; readMethod=public int[] Test4498236.getIndex(); writeMethod=public void Test4498236.setIndex(int[]); indexedPropertyType=int; indexedReadMethod=public int Test4498236.getIndex(int); indexedWriteMethod=public void Test4498236.setIndex(int,int)]",
             new IndexedPropertyDescriptor("index", Test4498236.class)
        );
        test("[name=$$$; propertyType=class [I; readMethod=public int[] Test4498236.getIndex(); writeMethod=public void Test4498236.setIndex(int[]); indexedPropertyType=int; indexedReadMethod=public int Test4498236.getIndex(int); indexedWriteMethod=public void Test4498236.setIndex(int,int)]",
             new IndexedPropertyDescriptor("$$$", Test4498236.class, "getIndex", "setIndex", "getIndex", "setIndex")
        );
        test("[name=$$$; propertyType=class [I; readMethod=public int[] Test4498236.getIndex(); writeMethod=public void Test4498236.setIndex(int[]); indexedPropertyType=int; indexedReadMethod=public int Test4498236.getIndex(int); indexedWriteMethod=public void Test4498236.setIndex(int,int)]",
             new IndexedPropertyDescriptor("$$$", Test4498236.class.getMethod("getIndex"), Test4498236.class.getMethod("setIndex", new int[0].getClass()), Test4498236.class.getMethod("getIndex", int.class), Test4498236.class.getMethod("setIndex", int.class, int.class) )
        );
    }
    public void addKeyListener(KeyListener listener) {
        add(listener);
    }
    public void removeKeyListener(KeyListener listener) {
        remove(listener);
    }
    public KeyListener getKeyListeners() {
        return null;
    }
    public void add(KeyListener listener) {
    }
    public void remove(KeyListener listener) {
    }
    public KeyListener get() {
        return null;
    }
    public boolean isValue() {
        return true;
    }
    public boolean getValue() {
        return true;
    }
    public void setValue(boolean value) {
    }
    public int[] getIndex() {
        return null;
    }
    public int getIndex(int index) {
        return 0;
    }
    public void setIndex(int index, int value) {
    }
    public void setIndex(int[] value) {
    }
    private static void test(String expected, Object object) {
        String actual = object.toString();
        if (!actual.equals(object.getClass().getName() + expected)) {
            throw new Error(actual);
        }
    }
}
