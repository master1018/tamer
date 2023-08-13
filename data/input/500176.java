public final class CustomViewDescriptorService {
    private static CustomViewDescriptorService sThis = new CustomViewDescriptorService();
    private HashMap<IProject, HashMap<String, ElementDescriptor>> mCustomDescriptorMap =
        new HashMap<IProject, HashMap<String, ElementDescriptor>>();
    @SuppressWarnings("unused")
    private ICustomViewDescriptorListener mListener;
    public interface ICustomViewDescriptorListener {
        public void updatedClassInfo(IProject project, String className, ElementDescriptor descriptor);
    }
    public static CustomViewDescriptorService getInstance() {
        return sThis;
    }
    public void setListener(ICustomViewDescriptorListener listener) {
        mListener = listener;
    }
    public ElementDescriptor getDescriptor(IProject project, String fqcn) {
        synchronized (mCustomDescriptorMap) {
            HashMap<String, ElementDescriptor> map = mCustomDescriptorMap.get(project);
            if (map != null) {
                ElementDescriptor descriptor = map.get(fqcn);
                if (descriptor != null) {
                    return descriptor;
                }
            }
            try {
                IJavaProject javaProject = JavaCore.create(project);
                String javaClassName = fqcn.replaceAll("\\$", "\\."); 
                IType type = javaProject.findType(javaClassName);
                if (type != null && type.exists()) {
                    ITypeHierarchy hierarchy = type.newSupertypeHierarchy(
                            new NullProgressMonitor());
                    ElementDescriptor parentDescriptor = getDescriptor(
                            hierarchy.getSuperclass(type), project, hierarchy);
                    if (parentDescriptor != null) {
                        ViewElementDescriptor descriptor = new ViewElementDescriptor(fqcn,
                                fqcn, 
                                fqcn, 
                                null, 
                                null, 
                                getAttributeDescriptor(type, parentDescriptor),
                                null, 
                                null, 
                                false );
                        synchronized (mCustomDescriptorMap) {
                            map = mCustomDescriptorMap.get(project);
                            if (map == null) {
                                map = new HashMap<String, ElementDescriptor>();
                                mCustomDescriptorMap.put(project, map);
                            }
                            map.put(fqcn, descriptor);
                        }
                        return descriptor;
                    }
                }
            } catch (JavaModelException e) {
            }
        }
        return null;
    }
    private ViewElementDescriptor getDescriptor(IType type, IProject project,
            ITypeHierarchy typeHierarchy) {
        List<ElementDescriptor> builtInList = null;
        Sdk currentSdk = Sdk.getCurrent();
        if (currentSdk != null) {
            IAndroidTarget target = currentSdk.getTarget(project);
            if (target != null) {
                AndroidTargetData data = currentSdk.getTargetData(target);
                builtInList = data.getLayoutDescriptors().getViewDescriptors();
            }
        }
        if (type == null) {
            return null;
        }
        String fqcn = type.getFullyQualifiedName();
        if (builtInList != null) {
            for (ElementDescriptor desc : builtInList) {
                if (desc instanceof ViewElementDescriptor) {
                    ViewElementDescriptor viewDescriptor = (ViewElementDescriptor)desc;
                    if (fqcn.equals(viewDescriptor.getFullClassName())) {
                        return viewDescriptor;
                    }
                }
            }
        }
        if (typeHierarchy == null) {
            return null;
        }
        IType parentType = typeHierarchy.getSuperclass(type);
        if (parentType != null) {
            ViewElementDescriptor parentDescriptor = getDescriptor(parentType, project,
                    typeHierarchy);
            if (parentDescriptor != null) {
                ViewElementDescriptor descriptor = new ViewElementDescriptor(fqcn,
                        fqcn, 
                        fqcn, 
                        null, 
                        null, 
                        getAttributeDescriptor(type, parentDescriptor),
                        null, 
                        null, 
                        false );
                synchronized (mCustomDescriptorMap) {
                    HashMap<String, ElementDescriptor> map = mCustomDescriptorMap.get(project);
                    if (map == null) {
                        map = new HashMap<String, ElementDescriptor>();
                        mCustomDescriptorMap.put(project, map);
                    }
                    map.put(fqcn, descriptor);
                }
                return descriptor;
            }
        }
        return null;
    }
    private AttributeDescriptor[] getAttributeDescriptor(IType type,
            ElementDescriptor parentDescriptor) {
        return parentDescriptor.getAttributes();
    }
}
