final class SwingTest implements Runnable {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    public static void start(Class<?> type) throws Throwable {
        new SwingTest(type).start();
    }
    private final Class<?> type;
    private final Iterator<Method> methods;
    private JFrame frame;
    private Object object;
    private Method method;
    private Throwable error;
    private SwingTest(Class<?> type) {
        Set<Method> methods = new TreeSet<Method>(new Comparator<Method>() {
            public int compare(Method first, Method second) {
                return first.getName().compareTo(second.getName());
            }
        });
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass().equals(type)) {
                if (method.getReturnType().equals(void.class)) {
                    if (0 == method.getParameterTypes().length) {
                        methods.add(method);
                    }
                }
            }
        }
        this.type = type;
        this.methods = methods.iterator();
    }
    public void run() {
        try {
            if (this.object == null) {
                System.out.println(this.type);
                this.frame = new JFrame(this.type.getSimpleName());
                this.frame.setSize(WIDTH, HEIGHT);
                this.frame.setLocationRelativeTo(null);
                this.object = this.type.getConstructor(this.frame.getClass()).newInstance(this.frame);
                this.frame.setVisible(true);
            }
            else if (this.method != null) {
                System.out.println(this.method);
                this.method.invoke(this.object);
            }
            else {
                System.out.println((this.error == null) ? "PASSED" : "FAILED"); 
                this.frame.dispose();
                this.frame = null;
            }
        }
        catch (NoSuchMethodException exception) {
            this.error = exception;
        }
        catch (SecurityException exception) {
            this.error = exception;
        }
        catch (IllegalAccessException exception) {
            this.error = exception;
        }
        catch (IllegalArgumentException exception) {
            this.error = exception;
        }
        catch (InstantiationException exception) {
            this.error = exception;
        }
        catch (InvocationTargetException exception) {
            this.error = exception.getTargetException();
        }
        System.out.flush();
        this.method = this.methods.hasNext() && (this.error == null)
                ? this.methods.next()
                : null;
    }
    private void start() throws Throwable {
        do {
            if ((this.method != null) && Modifier.isStatic(this.method.getModifiers())) {
                run(); 
            }
            else {
                SwingUtilities.invokeLater(this); 
            }
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                SunToolkit stk = (SunToolkit) tk;
                stk.realSync(); 
            }
        }
        while (this.frame != null);
        if (this.error != null) {
            throw this.error;
        }
    }
}
