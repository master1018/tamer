public final class ProjectCallback implements IProjectCallback {
    private final HashMap<String, Class<?>> mLoadedClasses = new HashMap<String, Class<?>>();
    private final IProject mProject;
    private final ClassLoader mParentClassLoader;
    private final ProjectResources mProjectRes;
    private boolean mUsed = false;
    private String mNamespace;
    public ProjectCallback(ClassLoader classLoader, ProjectResources projectRes, IProject project) {
        mParentClassLoader = classLoader;
        mProjectRes = projectRes;
        mProject = project;
    }
    @SuppressWarnings("unchecked")
    public Object loadView(String className, Class[] constructorSignature,
            Object[] constructorParameters)
            throws ClassNotFoundException, Exception {
        Class<?> clazz = mLoadedClasses.get(className);
        if (clazz != null) {
            return instantiateClass(clazz, constructorSignature, constructorParameters);
        }
        ProjectClassLoader loader = new ProjectClassLoader(mParentClassLoader, mProject);
        try {
            clazz = loader.loadClass(className);
            if (clazz != null) {
                mUsed = true;
                mLoadedClasses.put(className, clazz);
                return instantiateClass(clazz, constructorSignature, constructorParameters);
            }
        } catch (Error e) {
            AdtPlugin.log(e, "ProjectCallback.loadView failed to find class %1$s", className); 
        }
        return null;
    }
    public String getNamespace() {
        if (mNamespace == null) {
            IFile manifestFile = AndroidManifestParser.getManifest(mProject);
            try {
                AndroidManifestParser data = AndroidManifestParser.parseForData(manifestFile);
                String javaPackage = data.getPackage();
                mNamespace = String.format(AndroidConstants.NS_CUSTOM_RESOURCES, javaPackage);
            } catch (CoreException e) {
            }
        }
        return mNamespace;
    }
    public String[] resolveResourceValue(int id) {
        if (mProjectRes != null) {
            return mProjectRes.resolveResourceValue(id);
        }
        return null;
    }
    public String resolveResourceValue(int[] id) {
        if (mProjectRes != null) {
            return mProjectRes.resolveResourceValue(id);
        }
        return null;
    }
    public Integer getResourceValue(String type, String name) {
        if (mProjectRes != null) {
            return mProjectRes.getResourceValue(type, name);
        }
        return null;
    }
    public boolean isUsed() {
        return mUsed;
    }
    @SuppressWarnings("unchecked")
    private Object instantiateClass(Class<?> clazz, Class[] constructorSignature,
            Object[] constructorParameters) throws Exception {
        Constructor<?> constructor = clazz.getConstructor(constructorSignature);
        constructor.setAccessible(true);
        return constructor.newInstance(constructorParameters);
    }
}
