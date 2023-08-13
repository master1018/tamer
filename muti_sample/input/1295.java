public class HandlerLoop {
    public static void main(String args[]) throws Exception {
        URL.setURLStreamHandlerFactory(
            new HandlerFactory("sun.net.www.protocol"));
        URL url = new URL("file:
        System.out.println("url = " + url);
        url.openConnection();
    }
    private static class HandlerFactory implements URLStreamHandlerFactory {
        private String pkg;
        HandlerFactory(String pkg) {
            this.pkg = pkg;
        }
        public URLStreamHandler createURLStreamHandler(String protocol) {
            String name = pkg + "." + protocol + ".Handler";
            System.out.println("Loading handler class: " + name);
            new Dummy();
            try {
                Class c = Class.forName(name);
                return (URLStreamHandler)c.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private static class Dummy {
    }
}
