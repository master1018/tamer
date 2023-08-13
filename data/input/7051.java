public class Test4328406 {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            BeanContextServices container = new BeanContextServicesSupport();
            BeanContextChild ms1 = new MyService1();
            BeanContextServices ms2 = new MyService2();
            BeanContextChild mb = new MyBean();
            container.add(ms1);
            container.add(ms2);
            ms2.add(mb);
            container.remove(ms2);
        }
    }
}
class MyService1 extends BeanContextChildSupport implements BeanContextServiceProvider {
    protected void initializeBeanContextResources() {
        super.initializeBeanContextResources();
        BeanContextServices bcs = (BeanContextServices) getBeanContext();
        bcs.addService(this.getClass(), this);
    }
    public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object serviceSelector) {
        return this;
    }
    public void releaseService(BeanContextServices bcs, Object requestor, Object
            service) {
    }
    public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
        return null;
    }
}
class MyService2 extends BeanContextServicesSupport implements BeanContextServiceProvider {
    protected void initializeBeanContextResources() {
        super.initializeBeanContextResources();
        BeanContextServicesSupport bcs = (BeanContextServicesSupport) getBeanContext();
        try {
            bcs.getService(this, this, MyService1.class, null, this);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        bcs.addService(this.getClass(), this);
    }
    protected void releaseBeanContextResources() {
        super.releaseBeanContextResources();
        BeanContextServices bcs = (BeanContextServices) getBeanContext();
        bcs.revokeService(this.getClass(), this, true);
    }
    public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object serviceSelector) {
        return this;
    }
    public void releaseService(BeanContextServices bcs, Object requestor, Object service) {
    }
    public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
        return null;
    }
}
class MyBean extends BeanContextChildSupport {
    protected void initializeBeanContextResources() {
        super.initializeBeanContextResources();
        BeanContextServices bcs = (BeanContextServices) getBeanContext();
        try {
            bcs.getService(this, this, MyService1.class, null, this);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        try {
            bcs.getService(this, this, MyService2.class, null, this);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
