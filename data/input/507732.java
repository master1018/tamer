public final class ProjectClassLoader extends ClassLoader {
    private final IJavaProject mJavaProject;
    private URLClassLoader mJarClassLoader;
    private boolean mInsideJarClassLoader = false;
    public ProjectClassLoader(ClassLoader parentClassLoader, IProject project) {
        super(parentClassLoader);
        mJavaProject = JavaCore.create(project);
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IPath outputLocation = mJavaProject.getOutputLocation();
            IResource outRes = root.findMember(outputLocation);
            if (outRes == null) {
                throw new ClassNotFoundException(name);
            }
            File outFolder = new File(outRes.getLocation().toOSString());
            String[] segments = name.split("\\."); 
            File classFile = getFile(outFolder, segments, 0);
            if (classFile == null) {
                if (mInsideJarClassLoader == false) {
                    return loadClassFromJar(name);
                } else {
                    throw new ClassNotFoundException(name);
                }
            }
            FileInputStream fis = new FileInputStream(classFile);
            byte[] data = new byte[(int)classFile.length()];
            int read = 0;
            try {
                read = fis.read(data);
            } catch (IOException e) {
                data = null;
            }
            fis.close();
            if (data != null) {
                Class<?> clazz = defineClass(null, data, 0, read);
                if (clazz != null) {
                    return clazz;
                }
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(e.getMessage());
        }
        throw new ClassNotFoundException(name);
    }
    private File getFile(File parent, String[] segments, int index)
            throws FileNotFoundException {
        if (index == segments.length) {
            throw new FileNotFoundException();
        }
        String toMatch = segments[index];
        File[] files = parent.listFiles();
        if (index == segments.length - 1) {
            toMatch = toMatch + ".class"; 
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals(toMatch)) {
                        return file;
                    }
                }
            }
            throw new FileNotFoundException();
        }
        String innerClassName = null;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (toMatch.equals(file.getName())) {
                        return getFile(file, segments, index+1);
                    }
                } else if (file.getName().startsWith(toMatch)) {
                    if (innerClassName == null) {
                        StringBuilder sb = new StringBuilder(segments[index]);
                        for (int i = index + 1 ; i < segments.length ; i++) {
                            sb.append('$');
                            sb.append(segments[i]);
                        }
                        sb.append(".class");
                        innerClassName = sb.toString();
                    }
                    if (file.getName().equals(innerClassName)) {
                        return file;
                    }
                }
            }
        }
        return null;
    }
    private Class<?> loadClassFromJar(String name) throws ClassNotFoundException {
        if (mJarClassLoader == null) {
            URL[] jars = getExternalJars();
            mJarClassLoader = new URLClassLoader(jars, this );
        }
        try {
            mInsideJarClassLoader = true;
            return mJarClassLoader.loadClass(name);
        } finally {
            mInsideJarClassLoader = false;
        }
    }
    private final URL[] getExternalJars() {
        IJavaProject javaProject = JavaCore.create(mJavaProject.getProject());
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        ArrayList<URL> oslibraryList = new ArrayList<URL>();
        IClasspathEntry[] classpaths = javaProject.readRawClasspath();
        if (classpaths != null) {
            for (IClasspathEntry e : classpaths) {
                if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
                        e.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                    if (e.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                        e = JavaCore.getResolvedClasspathEntry(e); 
                    }
                    IPath path = e.getPath();
                    if (AndroidConstants.EXT_JAR.equalsIgnoreCase(path.getFileExtension())) {
                        boolean local = false;
                        IResource resource = wsRoot.findMember(path);
                        if (resource != null && resource.exists() &&
                                resource.getType() == IResource.FILE) {
                            local = true;
                            try {
                                oslibraryList.add(
                                        new File(resource.getLocation().toOSString()).toURL());
                            } catch (MalformedURLException mue) {
                            }
                        }
                        if (local == false) {
                            String osFullPath = path.toOSString();
                            File f = new File(osFullPath);
                            if (f.exists()) {
                                try {
                                    oslibraryList.add(f.toURL());
                                } catch (MalformedURLException mue) {
                                }
                            }
                        }
                    }
                }
            }
        }
        return oslibraryList.toArray(new URL[oslibraryList.size()]);
    }
}
