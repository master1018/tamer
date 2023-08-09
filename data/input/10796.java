public class BeanContextServiceRevokedEvent extends BeanContextEvent {
    private static final long serialVersionUID = -1295543154724961754L;
    public BeanContextServiceRevokedEvent(BeanContextServices bcs, Class sc, boolean invalidate) {
        super((BeanContext)bcs);
        serviceClass    = sc;
        invalidateRefs  = invalidate;
    }
    public BeanContextServices getSourceAsBeanContextServices() {
        return (BeanContextServices)getBeanContext();
    }
    public Class getServiceClass() { return serviceClass; }
    public boolean isServiceClass(Class service) {
        return serviceClass.equals(service);
    }
    public boolean isCurrentServiceInvalidNow() { return invalidateRefs; }
    protected Class                      serviceClass;
    private   boolean                    invalidateRefs;
}
