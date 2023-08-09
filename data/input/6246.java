public class MXBeanRefTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        ObjectName productName = new ObjectName("d:type=Product,n=1");
        ObjectName product2Name = new ObjectName("d:type=Product,n=2");
        ObjectName moduleName = new ObjectName("d:type=Module");
        mbs.registerMBean(product, productName);
        mbs.registerMBean(product2, product2Name);
        mbs.registerMBean(module, moduleName);
        ModuleMXBean moduleProxy =
                JMX.newMXBeanProxy(mbs, moduleName, ModuleMXBean.class);
        ObjectName on;
        on = (ObjectName) mbs.getAttribute(moduleName, "Product");
        check("ObjectName attribute value", on.equals(productName));
        ProductMXBean productProxy = moduleProxy.getProduct();
        MBeanServerInvocationHandler mbsih = (MBeanServerInvocationHandler)
                Proxy.getInvocationHandler(productProxy);
        check("ObjectName in proxy", mbsih.getObjectName().equals(productName));
        mbs.setAttribute(moduleName, new Attribute("Product", product2Name));
        ProductMXBean product2Proxy = module.getProduct();
        mbsih = (MBeanServerInvocationHandler)
                Proxy.getInvocationHandler(product2Proxy);
        check("Proxy after setAttribute",
                mbsih.getObjectName().equals(product2Name));
        moduleProxy.setProduct(productProxy);
        ProductMXBean productProxyAgain = module.getProduct();
        mbsih = (MBeanServerInvocationHandler)
                Proxy.getInvocationHandler(productProxyAgain);
        check("Proxy after proxied set",
                mbsih.getObjectName().equals(productName));
        MBeanServer mbs2 = MBeanServerFactory.createMBeanServer();
        ProductMXBean productProxy2 =
                JMX.newMXBeanProxy(mbs2, productName, ProductMXBean.class);
        try {
            moduleProxy.setProduct(productProxy2);
            check("Proxy for wrong MBeanServer worked but shouldn't", false);
        } catch (Exception e) {
            if (e instanceof UndeclaredThrowableException &&
                    e.getCause() instanceof OpenDataException)
                check("Proxy for wrong MBeanServer correctly rejected", true);
            else {
                e.printStackTrace(System.out);
                check("Proxy for wrong MBeanServer got wrong exception", false);
            }
        }
        ObjectName dup = new ObjectName("a:b=c");
        mbs.registerMBean(new MBeanServerDelegate(), dup);
        try {
            mbs.registerMBean(new ProductImpl(), dup);
            check("Duplicate register succeeded but should fail", false);
        } catch (InstanceAlreadyExistsException e) {
            check("Got correct exception from duplicate name", true);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            check("Got wrong exception from duplicate name", false);
        }
        if (failure != null)
            throw new Exception("TEST FAILED: " + failure);
        System.out.println("TEST PASSED");
    }
    private static void check(String what, boolean ok) {
        if (ok)
            System.out.println("OK: " + what);
        else {
            System.out.println("FAILED: " + what);
            failure = what;
        }
    }
    public static interface ProductMXBean {
        ModuleMXBean[] getModules();
    }
    public static interface ModuleMXBean {
        ProductMXBean getProduct();
        void setProduct(ProductMXBean p);
    }
    public static class ProductImpl implements ProductMXBean {
        public ModuleMXBean[] getModules() {
            return modules;
        }
    }
    public static class ModuleImpl implements ModuleMXBean {
        public ModuleImpl(ProductMXBean p) {
            setProduct(p);
        }
        public ProductMXBean getProduct() {
            return prod;
        }
        public void setProduct(ProductMXBean p) {
            this.prod = p;
        }
        private ProductMXBean prod;
    }
    private static final ProductMXBean product = new ProductImpl();
    private static final ProductMXBean product2 = new ProductImpl();
    private static final ModuleMXBean module = new ModuleImpl(product);
    private static final ModuleMXBean[] modules = new ModuleMXBean[] {module};
    private static String failure;
}
