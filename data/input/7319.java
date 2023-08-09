public class ClassLoaderDeadlock {
    public static void main(String[] args) throws Exception {
        URL url = new URL("file:provider/");
        final DelayClassLoader cl = new DelayClassLoader(url);
        Class clazz = cl.loadClass("HashProvider");
        Provider p = (Provider)clazz.newInstance();
        Security.insertProviderAt(p, 1);
        cl.delay = 1000;
        new Thread() {
            public void run() {
                try {
                    Class c1 = cl.loadClass("java.lang.String");
                    System.out.println(c1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(200);
        Class c2 = Class.forName("com.abc.Tst1");
        System.out.println(c2);
        System.out.println("OK");
    }
    static class DelayClassLoader extends URLClassLoader {
        volatile int delay;
        DelayClassLoader(URL url) {
            super(new URL[] {url});
        }
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            System.out.println("-loadClass(" + name + "," + resolve + ")");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) { e.printStackTrace(); }
            return super.loadClass(name, resolve);
        }
    }
}
