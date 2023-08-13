class ApplicationLoaders
{
    public static ApplicationLoaders getDefault()
    {
        return gApplicationLoaders;
    }
    public ClassLoader getClassLoader(String zip, String appDataDir,
            ClassLoader parent)
    {
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (mLoaders) {
            if (parent == null) {
                parent = baseParent;
            }
            if (parent == baseParent) {
                ClassLoader loader = (ClassLoader)mLoaders.get(zip);
                if (loader != null) {
                    return loader;
                }
                PathClassLoader pathClassloader =
                    new PathClassLoader(zip, appDataDir + "/lib", parent);
                mLoaders.put(zip, pathClassloader);
                return pathClassloader;
            }
            return new PathClassLoader(zip, parent);
        }
    }
    private final HashMap mLoaders = new HashMap();
    private static final ApplicationLoaders gApplicationLoaders
        = new ApplicationLoaders();
}
