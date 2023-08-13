public class DefaultPersistenceDelegate extends PersistenceDelegate {
    private String[] constructor;
    private Boolean definesEquals;
    public DefaultPersistenceDelegate() {
        this(new String[0]);
    }
    public DefaultPersistenceDelegate(String[] constructorPropertyNames) {
        this.constructor = constructorPropertyNames;
    }
    private static boolean definesEquals(Class type) {
        try {
            return type == type.getMethod("equals", Object.class).getDeclaringClass();
        }
        catch(NoSuchMethodException e) {
            return false;
        }
    }
    private boolean definesEquals(Object instance) {
        if (definesEquals != null) {
            return (definesEquals == Boolean.TRUE);
        }
        else {
            boolean result = definesEquals(instance.getClass());
            definesEquals = result ? Boolean.TRUE : Boolean.FALSE;
            return result;
        }
    }
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (constructor.length == 0) || !definesEquals(oldInstance) ?
            super.mutatesTo(oldInstance, newInstance) :
            oldInstance.equals(newInstance);
    }
    protected Expression instantiate(Object oldInstance, Encoder out) {
        int nArgs = constructor.length;
        Class type = oldInstance.getClass();
        Object[] constructorArgs = new Object[nArgs];
        for(int i = 0; i < nArgs; i++) {
            try {
                Method method = findMethod(type, this.constructor[i]);
                constructorArgs[i] = MethodUtil.invoke(method, oldInstance, new Object[0]);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        return new Expression(oldInstance, oldInstance.getClass(), "new", constructorArgs);
    }
    private Method findMethod(Class type, String property) {
        if (property == null) {
            throw new IllegalArgumentException("Property name is null");
        }
        PropertyDescriptor pd = getPropertyDescriptor(type, property);
        if (pd == null) {
            throw new IllegalStateException("Could not find property by the name " + property);
        }
        Method method = pd.getReadMethod();
        if (method == null) {
            throw new IllegalStateException("Could not find getter for the property " + property);
        }
        return method;
    }
    private void doProperty(Class type, PropertyDescriptor pd, Object oldInstance, Object newInstance, Encoder out) throws Exception {
        Method getter = pd.getReadMethod();
        Method setter = pd.getWriteMethod();
        if (getter != null && setter != null) {
            Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{});
            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{});
            Object oldValue = oldGetExp.getValue();
            Object newValue = newGetExp.getValue();
            out.writeExpression(oldGetExp);
            if (!Objects.equals(newValue, out.get(oldValue))) {
                Object e = (Object[])pd.getValue("enumerationValues");
                if (e instanceof Object[] && Array.getLength(e) % 3 == 0) {
                    Object[] a = (Object[])e;
                    for(int i = 0; i < a.length; i = i + 3) {
                        try {
                           Field f = type.getField((String)a[i]);
                           if (f.get(null).equals(oldValue)) {
                               out.remove(oldValue);
                               out.writeExpression(new Expression(oldValue, f, "get", new Object[]{null}));
                           }
                        }
                        catch (Exception ex) {}
                    }
                }
                invokeStatement(oldInstance, setter.getName(), new Object[]{oldValue}, out);
            }
        }
    }
    static void invokeStatement(Object instance, String methodName, Object[] args, Encoder out) {
        out.writeStatement(new Statement(instance, methodName, args));
    }
    private void initBean(Class type, Object oldInstance, Object newInstance, Encoder out) {
        for (Field field : type.getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                continue;
            }
            try {
                Expression oldGetExp = new Expression(field, "get", new Object[] { oldInstance });
                Expression newGetExp = new Expression(field, "get", new Object[] { newInstance });
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!Objects.equals(newValue, out.get(oldValue))) {
                    out.writeStatement(new Statement(field, "set", new Object[] { oldInstance, oldValue }));
                }
            }
            catch (Exception exception) {
                out.getExceptionListener().exceptionThrown(exception);
            }
        }
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(type);
        } catch (IntrospectionException exception) {
            return;
        }
        for (PropertyDescriptor d : info.getPropertyDescriptors()) {
            if (d.isTransient()) {
                continue;
            }
            try {
                doProperty(type, d, oldInstance, newInstance, out);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        if (!java.awt.Component.class.isAssignableFrom(type)) {
            return; 
        }
        for (EventSetDescriptor d : info.getEventSetDescriptors()) {
            if (d.isTransient()) {
                continue;
            }
            Class listenerType = d.getListenerType();
            if (listenerType == java.awt.event.ComponentListener.class) {
                continue;
            }
            if (listenerType == javax.swing.event.ChangeListener.class &&
                type == javax.swing.JMenuItem.class) {
                continue;
            }
            EventListener[] oldL = new EventListener[0];
            EventListener[] newL = new EventListener[0];
            try {
                Method m = d.getGetListenerMethod();
                oldL = (EventListener[])MethodUtil.invoke(m, oldInstance, new Object[]{});
                newL = (EventListener[])MethodUtil.invoke(m, newInstance, new Object[]{});
            }
            catch (Exception e2) {
                try {
                    Method m = type.getMethod("getListeners", new Class[]{Class.class});
                    oldL = (EventListener[])MethodUtil.invoke(m, oldInstance, new Object[]{listenerType});
                    newL = (EventListener[])MethodUtil.invoke(m, newInstance, new Object[]{listenerType});
                }
                catch (Exception e3) {
                    return;
                }
            }
            String addListenerMethodName = d.getAddListenerMethod().getName();
            for (int i = newL.length; i < oldL.length; i++) {
                invokeStatement(oldInstance, addListenerMethodName, new Object[]{oldL[i]}, out);
            }
            String removeListenerMethodName = d.getRemoveListenerMethod().getName();
            for (int i = oldL.length; i < newL.length; i++) {
                invokeStatement(oldInstance, removeListenerMethodName, new Object[]{newL[i]}, out);
            }
        }
    }
    protected void initialize(Class<?> type,
                              Object oldInstance, Object newInstance,
                              Encoder out)
    {
        super.initialize(type, oldInstance, newInstance, out);
        if (oldInstance.getClass() == type) { 
            initBean(type, oldInstance, newInstance, out);
        }
    }
    private static PropertyDescriptor getPropertyDescriptor(Class type, String property) {
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (property.equals(pd.getName()))
                    return pd;
            }
        } catch (IntrospectionException exception) {
        }
        return null;
    }
}
