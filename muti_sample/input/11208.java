public abstract class ProbeSkeleton implements Probe {
    protected Class<?>[] parameters;
    protected ProbeSkeleton(Class<?>[] parameters) {
        this.parameters = parameters;
    }
    public abstract boolean isEnabled();  
    public abstract void uncheckedTrigger(Object[] args); 
    private static boolean isAssignable(Object o, Class<?> formal) {
        if (o != null) {
            if ( !formal.isInstance(o) ) {
                if ( formal.isPrimitive() ) { 
                    try {
                        Field f = o.getClass().getField("TYPE");
                        return formal.isAssignableFrom((Class<?>)f.get(null));
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        }
        return true;
    }
    public void trigger(Object ... args) {
        if (args.length != parameters.length) {
            throw new IllegalArgumentException("Wrong number of arguments");
        } else {
            for (int i = 0; i < parameters.length; ++i) {
                if ( !isAssignable(args[i], parameters[i]) ) {
                    throw new IllegalArgumentException(
                            "Wrong type of argument at position " + i);
                }
            }
            uncheckedTrigger(args);
        }
    }
}
