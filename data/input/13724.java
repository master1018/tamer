public class InterfaceTypeImpl extends ReferenceTypeImpl
                               implements InterfaceType {
    private SoftReference superInterfacesCache = null;
    private SoftReference subInterfacesCache = null;
    private SoftReference implementorsCache = null;
    protected InterfaceTypeImpl(VirtualMachine aVm, InstanceKlass aRef) {
        super(aVm, aRef);
    }
    public List superinterfaces() throws ClassNotPreparedException {
        List superinterfaces = (superInterfacesCache != null)? (List) superInterfacesCache.get() : null;
        if (superinterfaces == null) {
            checkPrepared();
            superinterfaces = Collections.unmodifiableList(getInterfaces());
            superInterfacesCache = new SoftReference(superinterfaces);
        }
        return superinterfaces;
    }
    public List subinterfaces() {
        List subinterfaces = (subInterfacesCache != null)? (List) subInterfacesCache.get() : null;
        if (subinterfaces == null) {
            List all = vm.allClasses();
            subinterfaces = new ArrayList();
            Iterator iter = all.iterator();
            while (iter.hasNext()) {
                ReferenceType refType = (ReferenceType)iter.next();
                if (refType instanceof InterfaceType) {
                    InterfaceType interfaze = (InterfaceType)refType;
                    if (interfaze.isPrepared() && interfaze.superinterfaces().contains(this)) {
                        subinterfaces.add(interfaze);
                    }
               }
            }
            subinterfaces = Collections.unmodifiableList(subinterfaces);
            subInterfacesCache = new SoftReference(subinterfaces);
        }
        return subinterfaces;
    }
    public List implementors() {
        List implementors = (implementorsCache != null)? (List) implementorsCache.get() : null;
        if (implementors == null) {
            List all = vm.allClasses();
            implementors = new ArrayList();
            Iterator iter = all.iterator();
            while (iter.hasNext()) {
                ReferenceType refType = (ReferenceType)iter.next();
                if (refType instanceof ClassType) {
                    ClassType clazz = (ClassType)refType;
                    if (clazz.isPrepared() && clazz.interfaces().contains(this)) {
                        implementors.add(clazz);
                    }
                }
            }
            implementors = Collections.unmodifiableList(implementors);
            implementorsCache = new SoftReference(implementors);
        }
        return implementors;
    }
    void addVisibleMethods(Map methodMap) {
        Iterator iter = superinterfaces().iterator();
        while (iter.hasNext()) {
            InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
            interfaze.addVisibleMethods(methodMap);
        }
        addToMethodMap(methodMap, methods());
    }
    List getAllMethods() {
        ArrayList list = new ArrayList(methods());
        List interfaces = allSuperinterfaces();
        Iterator iter = interfaces.iterator();
        while (iter.hasNext()) {
            InterfaceType interfaze = (InterfaceType)iter.next();
            list.addAll(interfaze.methods());
        }
        return list;
    }
    List allSuperinterfaces() {
        ArrayList list = new ArrayList();
        addSuperinterfaces(list);
        return list;
    }
    void addSuperinterfaces(List list) {
        List immediate = new ArrayList(superinterfaces());
        Iterator iter = immediate.iterator();
        while (iter.hasNext()) {
            InterfaceType interfaze = (InterfaceType)iter.next();
            if (list.contains(interfaze)) {
                iter.remove();
            }
        }
        list.addAll(immediate);
        iter = immediate.iterator();
        while (iter.hasNext()) {
            InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
            interfaze.addSuperinterfaces(list);
        }
    }
    boolean isAssignableTo(ReferenceType type) {
        if (this.equals(type)) {
            return true;
        } else {
            List supers = superinterfaces();
            Iterator iter = supers.iterator();
            while (iter.hasNext()) {
                InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
                if (interfaze.isAssignableTo(type)) {
                    return true;
                }
            }
            return false;
        }
    }
    List inheritedTypes() {
        return superinterfaces();
    }
    public boolean isInitialized() {
        return isPrepared();
    }
    public String toString() {
       return "interface " + name() + " (" + loaderString() + ")";
    }
}
