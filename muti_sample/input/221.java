class NewElementHandler extends ElementHandler {
    private List<Object> arguments = new ArrayList<Object>();
    private ValueObject value = ValueObjectImpl.VOID;
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
    protected final void addArgument(Object argument) {
        if (this.arguments == null) {
            throw new IllegalStateException("Could not add argument to evaluated element");
        }
        this.arguments.add(argument);
    }
    @Override
    protected final Object getContextBean() {
        return (this.type != null)
                ? this.type
                : super.getContextBean();
    }
    @Override
    protected final ValueObject getValueObject() {
        if (this.arguments != null) {
            try {
                this.value = getValueObject(this.type, this.arguments.toArray());
            }
            catch (Exception exception) {
                getOwner().handleException(exception);
            }
            finally {
                this.arguments = null;
            }
        }
        return this.value;
    }
    ValueObject getValueObject(Class<?> type, Object[] args) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Class name is not set");
        }
        Class<?>[] types = getArgumentTypes(args);
        Constructor<?> constructor = ConstructorFinder.findConstructor(type, types);
        if (constructor.isVarArgs()) {
            args = getArguments(args, constructor.getParameterTypes());
        }
        return ValueObjectImpl.create(constructor.newInstance(args));
    }
    static Class<?>[] getArgumentTypes(Object[] arguments) {
        Class<?>[] types = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] != null) {
                types[i] = arguments[i].getClass();
            }
        }
        return types;
    }
    static Object[] getArguments(Object[] arguments, Class<?>[] types) {
        int index = types.length - 1;
        if (types.length == arguments.length) {
            Object argument = arguments[index];
            if (argument == null) {
                return arguments;
            }
            Class<?> type = types[index];
            if (type.isAssignableFrom(argument.getClass())) {
                return arguments;
            }
        }
        int length = arguments.length - index;
        Class<?> type = types[index].getComponentType();
        Object array = Array.newInstance(type, length);
        System.arraycopy(arguments, index, array, 0, length);
        Object[] args = new Object[types.length];
        System.arraycopy(arguments, 0, args, 0, index);
        args[index] = array;
        return args;
    }
}
