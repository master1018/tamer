abstract class AbstractFinder<T> {
    private final Class<?>[] args;
    protected AbstractFinder(Class<?>[] args) {
        this.args = args;
    }
    protected abstract Class<?>[] getParameters(T method);
    protected abstract boolean isVarArgs(T method);
    protected abstract boolean isValid(T method);
    final T find(T[] methods) throws NoSuchMethodException {
        Map<T, Class<?>[]> map = new HashMap<T, Class<?>[]>();
        T oldMethod = null;
        Class<?>[] oldParams = null;
        boolean ambiguous = false;
        for (T newMethod : methods) {
            if (isValid(newMethod)) {
                Class<?>[] newParams = getParameters(newMethod);
                if (newParams.length == this.args.length) {
                    PrimitiveWrapperMap.replacePrimitivesWithWrappers(newParams);
                    if (isAssignable(newParams, this.args)) {
                        if (oldMethod == null) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                        } else {
                            boolean useNew = isAssignable(oldParams, newParams);
                            boolean useOld = isAssignable(newParams, oldParams);
                            if (useOld == useNew) {
                                ambiguous = true;
                            } else if (useNew) {
                                oldMethod = newMethod;
                                oldParams = newParams;
                                ambiguous = false;
                            }
                        }
                    }
                }
                if (isVarArgs(newMethod)) {
                    int length = newParams.length - 1;
                    if (length <= this.args.length) {
                        Class<?>[] array = new Class<?>[this.args.length];
                        System.arraycopy(newParams, 0, array, 0, length);
                        if (length < this.args.length) {
                            Class<?> type = newParams[length].getComponentType();
                            if (type.isPrimitive()) {
                                type = PrimitiveWrapperMap.getType(type.getName());
                            }
                            for (int i = length; i < this.args.length; i++) {
                                array[i] = type;
                            }
                        }
                        map.put(newMethod, array);
                    }
                }
            }
        }
        for (T newMethod : methods) {
            Class<?>[] newParams = map.get(newMethod);
            if (newParams != null) {
                if (isAssignable(newParams, this.args)) {
                    if (oldMethod == null) {
                        oldMethod = newMethod;
                        oldParams = newParams;
                    } else {
                        boolean useNew = isAssignable(oldParams, newParams);
                        boolean useOld = isAssignable(newParams, oldParams);
                        if (useOld == useNew) {
                            if (oldParams == map.get(oldMethod)) {
                                ambiguous = true;
                            }
                        } else if (useNew) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                            ambiguous = false;
                        }
                    }
                }
            }
        }
        if (ambiguous) {
            throw new NoSuchMethodException("Ambiguous methods are found");
        }
        if (oldMethod == null) {
            throw new NoSuchMethodException("Method is not found");
        }
        return oldMethod;
    }
    private boolean isAssignable(Class<?>[] min, Class<?>[] max) {
        for (int i = 0; i < this.args.length; i++) {
            if (null != this.args[i]) {
                if (!min[i].isAssignableFrom(max[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
