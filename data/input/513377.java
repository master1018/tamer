 final class ReflectionAccessImpl implements ReflectionAccess {
     static final ReflectionAccessImpl THE_ONE =
        new ReflectionAccessImpl();
    private ReflectionAccessImpl() {
    }
    public Method clone(Method method) {
        return new Method(method);
    }
    public Field clone(Field field) {
        return new Field(field);
    }
    public Method accessibleClone(Method method) {
        Method result = new Method(method);
        result.setAccessibleNoCheck(true);
        return result;
    }
    public void setAccessibleNoCheck(AccessibleObject ao, boolean accessible) {
        ao.setAccessibleNoCheck(accessible);
    }
}
