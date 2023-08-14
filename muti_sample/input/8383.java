final class FieldElementHandler extends AccessorElementHandler {
    private Class<?> type;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("class")) { 
            this.type = getOwner().findClass(value);
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected boolean isArgument() {
        return super.isArgument() && (this.type != null); 
    }
    @Override
    protected Object getContextBean() {
        return (this.type != null)
                ? this.type
                : super.getContextBean();
    }
    @Override
    protected Object getValue(String name) {
        try {
            return getFieldValue(getContextBean(), name);
        }
        catch (Exception exception) {
            getOwner().handleException(exception);
        }
        return null;
    }
    @Override
    protected void setValue(String name, Object value) {
        try {
            setFieldValue(getContextBean(), name, value);
        }
        catch (Exception exception) {
            getOwner().handleException(exception);
        }
    }
    static Object getFieldValue(Object bean, String name) throws IllegalAccessException, NoSuchFieldException {
        return findField(bean, name).get(bean);
    }
    private static void setFieldValue(Object bean, String name, Object value) throws IllegalAccessException, NoSuchFieldException {
        findField(bean, name).set(bean, value);
    }
    private static Field findField(Object bean, String name) throws NoSuchFieldException {
        return (bean instanceof Class<?>)
                ? FieldFinder.findStaticField((Class<?>) bean, name)
                : FieldFinder.findField(bean.getClass(), name);
    }
}
