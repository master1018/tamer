public class ClassRestrictions {
    public interface Bar {
        int foo();
    }
    public interface Baz {
        long foo();
    }
    interface Bashful {
        void foo();
    }
    public static void main(String[] args) {
        System.err.println(
            "\nTest of restrictions on parameters to Proxy.getProxyClass\n");
        try {
            ClassLoader loader = ClassRestrictions.class.getClassLoader();
            Class<?>[] interfaces;
            Class<?> proxyClass;
            try {
                interfaces = new Class<?>[] { Object.class };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with java.lang.Object as interface");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            try {
                interfaces = new Class<?>[] { Integer.TYPE };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with int.class as interface");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            try {
                interfaces = new Class<?>[] { Bar.class, Bar.class };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with repeated interfaces");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            ClassLoader altLoader = new URLClassLoader(
                ((URLClassLoader) loader).getURLs(), null);
            Class altBarClass;
            altBarClass = Class.forName(Bar.class.getName(), false, altLoader);
            try {
                interfaces = new Class<?>[] { altBarClass };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with interface " +
                    "not visible to class loader");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            Class<?> nonPublic1 = Bashful.class;
            Class<?> nonPublic2 = null;
            String[] nonPublicInterfaces = new String[] {
                "java.awt.Conditional",
                "java.util.zip.ZipConstants",
                "javax.swing.GraphicsWrapper",
                "javax.swing.JPopupMenu$Popup",
                "javax.swing.JTable$Resizable2",
                "javax.swing.JTable$Resizable3",
                "javax.swing.ToolTipManager$Popup",
                "sun.audio.Format",
                "sun.audio.HaePlayable",
                "sun.tools.agent.StepConstants",
            };
            for (int i = 0; i < nonPublicInterfaces.length; i++) {
                try {
                    nonPublic2 = Class.forName(nonPublicInterfaces[i]);
                    break;
                } catch (ClassNotFoundException e) {
                }
            }
            if (nonPublic2 == null) {
                throw new RuntimeException(
                    "no second non-public interface found for test");
            }
            try {
                interfaces = new Class<?>[] { nonPublic1, nonPublic2 };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with two non-public interfaces " +
                    "in different packages");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            try {
                interfaces = new Class<?>[] { Bar.class, Baz.class };
                proxyClass = Proxy.getProxyClass(loader, interfaces);
                throw new RuntimeException(
                    "proxy class created with conflicting methods");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println();
            }
            System.err.println("\nTEST PASSED");
        } catch (Exception e) {
            System.err.println("\nTEST FAILED:");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}
