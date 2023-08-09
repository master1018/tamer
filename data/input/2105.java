public class BeanInfoUtils
{
    public static final String BOUND = "bound";
    public static final String CONSTRAINED = "constrained";
    public static final String PROPERTYEDITORCLASS = "propertyEditorClass";
    public static final String READMETHOD = "readMethod";
    public static final String WRITEMETHOD = "writeMethod";
    public static final String DISPLAYNAME = "displayName";
    public static final String EXPERT = "expert";
    public static final String HIDDEN = "hidden";
    public static final String PREFERRED = "preferred";
    public static final String SHORTDESCRIPTION = "shortDescription";
    public static final String CUSTOMIZERCLASS = "customizerClass";
    static private void initFeatureDescriptor(FeatureDescriptor fd, String key, Object value)
    {
        if (DISPLAYNAME.equals(key)) {
            fd.setDisplayName((String)value);
        }
        if (EXPERT.equals(key)) {
            fd.setExpert(((Boolean)value).booleanValue());
        }
        if (HIDDEN.equals(key)) {
            fd.setHidden(((Boolean)value).booleanValue());
        }
        if (PREFERRED.equals(key)) {
            fd.setPreferred(((Boolean)value).booleanValue());
        }
        else if (SHORTDESCRIPTION.equals(key)) {
            fd.setShortDescription((String)value);
        }
        else {
            fd.setValue(key, value);
        }
    }
    public static PropertyDescriptor createPropertyDescriptor(Class cls, String name, Object[] args)
    {
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(name, cls);
        } catch (IntrospectionException e) {
            try {
                pd = createReadOnlyPropertyDescriptor(name, cls);
            } catch (IntrospectionException ie) {
                throwError(ie, "Can't create PropertyDescriptor for " + name + " ");
            }
        }
        for(int i = 0; i < args.length; i += 2) {
            String key = (String)args[i];
            Object value = args[i + 1];
            if (BOUND.equals(key)) {
                pd.setBound(((Boolean)value).booleanValue());
            }
            else if (CONSTRAINED.equals(key)) {
                pd.setConstrained(((Boolean)value).booleanValue());
            }
            else if (PROPERTYEDITORCLASS.equals(key)) {
                pd.setPropertyEditorClass((Class)value);
            }
            else if (READMETHOD.equals(key)) {
                String methodName = (String)value;
                Method method;
                try {
                    method = cls.getMethod(methodName, new Class[0]);
                    pd.setReadMethod(method);
                }
                catch(Exception e) {
                    throwError(e, cls + " no such method as \"" + methodName + "\"");
                }
            }
            else if (WRITEMETHOD.equals(key)) {
                String methodName = (String)value;
                Method method;
                try {
                    Class type = pd.getPropertyType();
                    method = cls.getMethod(methodName, new Class[]{type});
                    pd.setWriteMethod(method);
                }
                catch(Exception e) {
                    throwError(e, cls + " no such method as \"" + methodName + "\"");
                }
            }
            else {
                initFeatureDescriptor(pd, key, value);
            }
        }
        return pd;
    }
    public static BeanDescriptor createBeanDescriptor(Class cls, Object[] args)
    {
        Class customizerClass = null;
        for(int i = 0; i < args.length; i += 2) {
            if (CUSTOMIZERCLASS.equals((String)args[i])) {
                customizerClass = (Class)args[i + 1];
                break;
            }
        }
        BeanDescriptor bd = new BeanDescriptor(cls, customizerClass);
        for(int i = 0; i < args.length; i += 2) {
            String key = (String)args[i];
            Object value = args[i + 1];
            initFeatureDescriptor(bd, key, value);
        }
        return bd;
    }
    static private PropertyDescriptor createReadOnlyPropertyDescriptor(
        String name, Class cls) throws IntrospectionException {
        Method readMethod = null;
        String base = capitalize(name);
        Class[] parameters = new Class[0];
        try {
            readMethod = cls.getMethod("is" + base, parameters);
        } catch (Exception ex) {}
        if (readMethod == null) {
            try {
                readMethod = cls.getMethod("get" + base, parameters);
            } catch (Exception ex2) {}
        }
        if (readMethod != null) {
            return new PropertyDescriptor(name, readMethod, null);
        }
        try {
            parameters = new Class[1];
            parameters[0] = int.class;
            readMethod = cls.getMethod("get" + base, parameters);
        } catch (NoSuchMethodException nsme) {
            throw new IntrospectionException(
                "cannot find accessor method for " + name + " property.");
        }
        return new IndexedPropertyDescriptor(name, null, null, readMethod, null);
    }
    private static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    public static void throwError(Exception e, String s) {
        throw new Error(e.toString() + " " + s);
    }
}
