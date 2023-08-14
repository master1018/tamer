public class ImplementedMethods {
    private Map<MethodDoc,Type> interfaces = new HashMap<MethodDoc,Type>();
    private List<MethodDoc> methlist = new ArrayList<MethodDoc>();
    private Configuration configuration;
    private final ClassDoc classdoc;
    private final MethodDoc method;
    public ImplementedMethods(MethodDoc method, Configuration configuration) {
        this.method = method;
        this.configuration = configuration;
        classdoc = method.containingClass();
    }
    public MethodDoc[] build(boolean sort) {
        buildImplementedMethodList(sort);
        return methlist.toArray(new MethodDoc[methlist.size()]);
    }
    public MethodDoc[] build() {
        return build(true);
    }
    public Type getMethodHolder(MethodDoc methodDoc) {
        return interfaces.get(methodDoc);
    }
    private void buildImplementedMethodList(boolean sort) {
        List<Type> intfacs = Util.getAllInterfaces(classdoc, configuration, sort);
        for (Iterator<Type> iter = intfacs.iterator(); iter.hasNext(); ) {
            Type interfaceType = iter.next();
            MethodDoc found = Util.findMethod(interfaceType.asClassDoc(), method);
            if (found != null) {
                removeOverriddenMethod(found);
                if (!overridingMethodFound(found)) {
                    methlist.add(found);
                    interfaces.put(found, interfaceType);
                }
            }
        }
    }
    private void removeOverriddenMethod(MethodDoc method) {
        ClassDoc overriddenClass = method.overriddenClass();
        if (overriddenClass != null) {
            for (int i = 0; i < methlist.size(); i++) {
                ClassDoc cd = methlist.get(i).containingClass();
                if (cd == overriddenClass || overriddenClass.subclassOf(cd)) {
                    methlist.remove(i);  
                    return;
                }
            }
        }
    }
    private boolean overridingMethodFound(MethodDoc method) {
        ClassDoc containingClass = method.containingClass();
        for (int i = 0; i < methlist.size(); i++) {
            MethodDoc listmethod = methlist.get(i);
            if (containingClass == listmethod.containingClass()) {
                return true;
            }
            ClassDoc cd = listmethod.overriddenClass();
            if (cd == null) {
                continue;
            }
            if (cd == containingClass || cd.subclassOf(containingClass)) {
                return true;
            }
        }
        return false;
    }
}
