public final class WidgetClassLoader implements IAndroidClassLoader {
    private final static class ClassDescriptor implements IClassDescriptor {
        private String mFqcn;
        private String mSimpleName;
        private ClassDescriptor mSuperClass;
        private ClassDescriptor mEnclosingClass;
        private final ArrayList<IClassDescriptor> mDeclaredClasses =
                new ArrayList<IClassDescriptor>();
        private boolean mIsInstantiable = false;
        ClassDescriptor(String fqcn) {
            mFqcn = fqcn;
            mSimpleName = getSimpleName(fqcn);
        }
        public String getFullClassName() {
            return mFqcn;
        }
        public String getSimpleName() {
            return mSimpleName;
        }
        public IClassDescriptor[] getDeclaredClasses() {
            return mDeclaredClasses.toArray(new IClassDescriptor[mDeclaredClasses.size()]);
        }
        private void addDeclaredClass(ClassDescriptor declaredClass) {
            mDeclaredClasses.add(declaredClass);
        }
        public IClassDescriptor getEnclosingClass() {
            return mEnclosingClass;
        }
        void setEnclosingClass(ClassDescriptor enclosingClass) {
            mEnclosingClass = enclosingClass;
            mEnclosingClass.addDeclaredClass(this);
            mFqcn = enclosingClass.mFqcn + "$" + mFqcn.substring(enclosingClass.mFqcn.length() + 1);
        }
        public IClassDescriptor getSuperclass() {
            return mSuperClass;
        }
        void setSuperClass(ClassDescriptor superClass) {
            mSuperClass = superClass;
        }
        @Override
        public boolean equals(Object clazz) {
            if (clazz instanceof ClassDescriptor) {
                return mFqcn.equals(((ClassDescriptor)clazz).mFqcn);
            }
            return super.equals(clazz);
        }
        @Override
        public int hashCode() {
            return mFqcn.hashCode();
        }
        public boolean isInstantiable() {
            return mIsInstantiable;
        }
        void setInstantiable(boolean state) {
            mIsInstantiable = state;
        }
        private String getSimpleName(String fqcn) {
            String[] segments = fqcn.split("\\.");
            return segments[segments.length-1];
        }
    }
    private BufferedReader mReader;
    private final Map<String, ClassDescriptor> mMap = new TreeMap<String, ClassDescriptor>();
    private final Map<String, ClassDescriptor> mWidgetMap = new TreeMap<String, ClassDescriptor>();
    private final Map<String, ClassDescriptor> mLayoutMap = new TreeMap<String, ClassDescriptor>();
    private final Map<String, ClassDescriptor> mLayoutParamsMap =
        new HashMap<String, ClassDescriptor>();
    private String mOsFilePath;
    WidgetClassLoader(String osFilePath) throws FileNotFoundException {
        mOsFilePath = osFilePath;
        mReader = new BufferedReader(new FileReader(osFilePath));
    }
    public String getSource() {
        return mOsFilePath;
    }
    boolean parseWidgetList(IProgressMonitor monitor) {
        try {
            String line;
            while ((line = mReader.readLine()) != null) {
                if (line.length() > 0) {
                    char prefix = line.charAt(0);
                    String[] classes = null;
                    ClassDescriptor clazz = null;
                    switch (prefix) {
                        case 'W':
                            classes = line.substring(1).split(" ");
                            clazz = processClass(classes, 0, null );
                            if (clazz != null) {
                                clazz.setInstantiable(true);
                                mWidgetMap.put(classes[0], clazz);
                            }
                            break;
                        case 'L':
                            classes = line.substring(1).split(" ");
                            clazz = processClass(classes, 0, null );
                            if (clazz != null) {
                                clazz.setInstantiable(true);
                                mLayoutMap.put(classes[0], clazz);
                            }
                            break;
                        case 'P':
                            classes = line.substring(1).split(" ");
                            clazz = processClass(classes, 0, mLayoutParamsMap);
                            if (clazz != null) {
                                clazz.setInstantiable(true);
                            }
                            break;
                        case '#':
                            break;
                        default:
                                throw new IllegalArgumentException();
                    }
                }
            }
            postProcess();
            return true;
        } catch (IOException e) {
        } finally {
            try {
                mReader.close();
            } catch (IOException e) {
            }
        }
        return false;
    }
    private ClassDescriptor processClass(String[] classes, int index,
            Map<String, ClassDescriptor> map) {
        if (index >= classes.length) {
            return null;
        }
        String fqcn = classes[index];
        if ("java.lang.Object".equals(fqcn)) { 
            return null;
        }
        if (mMap.containsKey(fqcn)) {
            return mMap.get(fqcn);
        }
        ClassDescriptor clazz = new ClassDescriptor(fqcn);
        mMap.put(fqcn, clazz);
        if (map != null) {
            map.put(fqcn, clazz);
        }
        ClassDescriptor superClass = processClass(classes, index+1, map);
        if (superClass != null) {
            clazz.setSuperClass(superClass);
        }
        return clazz;
    }
    private void postProcess() {
        Collection<ClassDescriptor> params = mLayoutParamsMap.values();
        for (ClassDescriptor param : params) {
            String fqcn = param.getFullClassName();
            String enclosed = getEnclosedName(fqcn);
            ClassDescriptor enclosingType = mMap.get(enclosed);
            if (enclosingType != null) {
                param.setEnclosingClass(enclosingType);
                mMap.remove(fqcn);
                mMap.put(param.getFullClassName(), param);
            }
        }
    }
    private String getEnclosedName(String fqcn) {
        int index = fqcn.lastIndexOf('.');
        return fqcn.substring(0, index);
    }
    public HashMap<String, ArrayList<IClassDescriptor>> findClassesDerivingFrom(String rootPackage,
            String[] superClasses) throws IOException, InvalidAttributeValueException,
            ClassFormatError {
        HashMap<String, ArrayList<IClassDescriptor>> map =
                new HashMap<String, ArrayList<IClassDescriptor>>();
        ArrayList<IClassDescriptor> list = new ArrayList<IClassDescriptor>();
        list.addAll(mWidgetMap.values());
        map.put(AndroidConstants.CLASS_VIEW, list);
        list = new ArrayList<IClassDescriptor>();
        list.addAll(mLayoutMap.values());
        map.put(AndroidConstants.CLASS_VIEWGROUP, list);
        list = new ArrayList<IClassDescriptor>();
        list.addAll(mLayoutParamsMap.values());
        map.put(AndroidConstants.CLASS_VIEWGROUP_LAYOUTPARAMS, list);
        return map;
    }
    public IClassDescriptor getClass(String className) throws ClassNotFoundException {
        return mMap.get(className);
    }
}
