public class EJBProcessor implements BeanPostProcessor {
    private final Log logger = LogFactory.getLog(getClass());
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Processing bean " + bean + " with bean name " + beanName);
        }
        Class<?> clazz = bean.getClass();
        EJB ejb = clazz.getAnnotation(EJB.class);
        if (ejb != null) {
            handleEJB(ejb, bean, null);
        }
        return null;
    }
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }
    protected final void handleEJB(EJB ejb, Object bean, Class<?> type) {
    }
    protected void injectReference(Object obj, Class classType, Object proxy) {
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor pd = this.findPropertyDescriptorByType(obj, classType);
        wrapper.setPropertyValue(pd.getName(), proxy);
    }
    private PropertyDescriptor findPropertyDescriptorByType(Object obj, Class type) {
        Method[] methods = getAllMethods(obj);
        for (Method m : methods) {
            if (isSetterMethod(m)) {
                Class<?> _type = m.getParameterTypes()[0];
                if (_type.isAssignableFrom(type)) {
                    PropertyDescriptor p = BeanUtils.findPropertyForMethod(m);
                    return p;
                }
            }
        }
        return null;
    }
    private Method[] getAllMethods(Object obj) {
        Class beanClass = obj.getClass();
        List<Method> methods = new ArrayList<Method>();
        while (beanClass != Object.class) {
            methods.addAll(Arrays.asList(beanClass.getDeclaredMethods()));
            beanClass = beanClass.getSuperclass();
        }
        return methods.toArray(new Method[methods.size()]);
    }
    private boolean isSetterMethod(Method m) {
        return m != null && m.getName().startsWith("set") && m.getParameterTypes().length == 1;
    }
}
