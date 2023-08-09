public final class CompiledResourcesMonitor implements IFileListener, IProjectListener {
    private final static CompiledResourcesMonitor sThis = new CompiledResourcesMonitor();
    public static void setupMonitor(GlobalProjectMonitor monitor) {
        monitor.addFileListener(sThis, IResourceDelta.ADDED | IResourceDelta.CHANGED);
        monitor.addProjectListener(sThis);
    }
    private CompiledResourcesMonitor() {
    }
    public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind) {
        if (file.getName().equals(AndroidConstants.FN_COMPILED_RESOURCE_CLASS)) {
            loadAndParseRClass(file.getProject());
        }
    }
    public void projectClosed(IProject project) {
    }
    public void projectDeleted(IProject project) {
    }
    public void projectOpened(IProject project) {
    }
    public void projectRenamed(IProject project, IPath from) {
    }
    public void projectOpenedWithWorkspace(IProject project) {
        try {
            if (project.hasNature(AndroidConstants.NATURE)) {
                loadAndParseRClass(project);
            }
        } catch (CoreException e) {
        }
    }
    private void loadAndParseRClass(IProject project) {
        try {
            ProjectResources projectResources = ResourceManager.getInstance().getProjectResources(
                    project);
            if (projectResources != null) {
                String className = getRClassName(project);
                if (className == null) {
                    AdtPlugin.log(IStatus.ERROR,
                            "loadAndParseRClass: failed to find manifest package for project %1$s", 
                            project.getName());
                    return;
                }
                ProjectClassLoader loader = new ProjectClassLoader(null ,
                        project);
                try {
                    Class<?> clazz = loader.loadClass(className);
                    if (clazz != null) {
                        Map<String, Map<String, Integer>> resourceValueMap =
                            new HashMap<String, Map<String, Integer>>();
                        Map<Integer, String[]> genericValueToNameMap =
                            new HashMap<Integer, String[]>();
                        Map<IntArrayWrapper, String> styleableValueToNameMap =
                            new HashMap<IntArrayWrapper, String>();
                        if (parseClass(clazz, genericValueToNameMap, styleableValueToNameMap,
                                resourceValueMap)) {
                            projectResources.setCompiledResources(genericValueToNameMap,
                                    styleableValueToNameMap, resourceValueMap);
                        }
                    }
                } catch (Error e) {
                    AdtPlugin.log(e, "loadAndParseRClass failed to find class %1$s", className); 
                }
            }
        } catch (ClassNotFoundException e) {
        }
    }
    private boolean parseClass(Class<?> rClass, Map<Integer, String[]> genericValueToNameMap,
            Map<IntArrayWrapper, String> styleableValueToNameMap, Map<String,
            Map<String, Integer>> resourceValueMap) {
        try {
            for (Class<?> inner : rClass.getDeclaredClasses()) {
                String resType = inner.getSimpleName();
                Map<String, Integer> fullMap = new HashMap<String, Integer>();
                resourceValueMap.put(resType, fullMap);
                for (Field f : inner.getDeclaredFields()) {
                    int modifiers = f.getModifiers();
                    if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                        Class<?> type = f.getType();
                        if (type.isArray() && type.getComponentType() == int.class) {
                            styleableValueToNameMap.put(new IntArrayWrapper((int[]) f.get(null)),
                                    f.getName());
                        } else if (type == int.class) {
                            Integer value = (Integer) f.get(null);
                            genericValueToNameMap.put(value, new String[] { f.getName(), resType });
                            fullMap.put(f.getName(), value);
                        } else {
                            assert false;
                        }
                    }
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        return false;
    }
    private String getRClassName(IProject project) {
        try {
            IFile manifestFile = AndroidManifestParser.getManifest(project);
            if (manifestFile != null && manifestFile.isSynchronized(IResource.DEPTH_ZERO)) {
                AndroidManifestParser data = AndroidManifestParser.parseForData(manifestFile);
                if (data != null) {
                    String javaPackage = data.getPackage();
                    return javaPackage + ".R"; 
                }
            }
        } catch (CoreException e) {
            AdtPlugin.logAndPrintError(e,
                    "Android Resources",
                    "Failed to find the package of the AndroidManifest of project %1$s. Reason: %2$s",
                    project.getName(),
                    e.getMessage());
        }
        return null;
    }
}
