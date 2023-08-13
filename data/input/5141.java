final class MethodElementHandler extends NewElementHandler {
    private String name;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("name")) { 
            this.name = value;
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected ValueObject getValueObject(Class<?> type, Object[] args) throws Exception {
        Object bean = getContextBean();
        Class<?>[] types = getArgumentTypes(args);
        Method method = (type != null)
                ? MethodFinder.findStaticMethod(type, this.name, types)
                : MethodFinder.findMethod(bean.getClass(), this.name, types);
        if (method.isVarArgs()) {
            args = getArguments(args, method.getParameterTypes());
        }
        Object value = method.invoke(bean, args);
        return method.getReturnType().equals(void.class)
                ? ValueObjectImpl.VOID
                : ValueObjectImpl.create(value);
    }
}
