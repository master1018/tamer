public class SwingLazyValue implements UIDefaults.LazyValue {
    private String className;
    private String methodName;
    private Object[] args;
    public SwingLazyValue(String c) {
        this(c, (String)null);
    }
    public SwingLazyValue(String c, String m) {
        this(c, m, null);
    }
    public SwingLazyValue(String c, Object[] o) {
        this(c, null, o);
    }
    public SwingLazyValue(String c, String m, Object[] o) {
        className = c;
        methodName = m;
        if (o != null) {
            args = o.clone();
        }
    }
    public Object createValue(final UIDefaults table) {
        try {
            Object cl;
            Class<?> c = Class.forName(className, true, null);
            if (methodName != null) {
                Class[] types = getClassArray(args);
                Method m = c.getMethod(methodName, types);
                makeAccessible(m);
                return m.invoke(c, args);
            } else {
                Class[] types = getClassArray(args);
                Constructor constructor = c.getConstructor(types);
                makeAccessible(constructor);
                return constructor.newInstance(args);
            }
        } catch (Exception e) {
        }
        return null;
    }
    private void makeAccessible(final AccessibleObject object) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                object.setAccessible(true);
                return null;
            }
        });
    }
    private Class[] getClassArray(Object[] args) {
        Class[] types = null;
        if (args!=null) {
            types = new Class[args.length];
            for (int i = 0; i< args.length; i++) {
                if (args[i] instanceof java.lang.Integer) {
                    types[i]=Integer.TYPE;
                } else if (args[i] instanceof java.lang.Boolean) {
                    types[i]=Boolean.TYPE;
                } else if (args[i] instanceof javax.swing.plaf.ColorUIResource) {
                    types[i]=java.awt.Color.class;
                } else {
                    types[i]=args[i].getClass();
                }
            }
        }
        return types;
    }
}
