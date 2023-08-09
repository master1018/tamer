public class ClassPathPackageInfo {
    private final ClassPathPackageInfoSource source;
    private final String packageName;
    private final Set<String> subpackageNames;
    private final Set<Class<?>> topLevelClasses;
    ClassPathPackageInfo(ClassPathPackageInfoSource source, String packageName,
            Set<String> subpackageNames, Set<Class<?>> topLevelClasses) {
        this.source = source;
        this.packageName = packageName;
        this.subpackageNames = Collections.unmodifiableSet(subpackageNames);
        this.topLevelClasses = Collections.unmodifiableSet(topLevelClasses);
    }
    public Set<ClassPathPackageInfo> getSubpackages() {
        Set<ClassPathPackageInfo> info = Sets.newHashSet();
        for (String name : subpackageNames) {
            info.add(source.getPackageInfo(name));
        }
        return info;
    }
    public Set<Class<?>> getTopLevelClassesRecursive() {
        Set<Class<?>> set = Sets.newHashSet();
        addTopLevelClassesTo(set);
        return set;
    }
    private void addTopLevelClassesTo(Set<Class<?>> set) {
        set.addAll(topLevelClasses);
        for (ClassPathPackageInfo info : getSubpackages()) {
            info.addTopLevelClassesTo(set);
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassPathPackageInfo) {
            ClassPathPackageInfo that = (ClassPathPackageInfo) obj;
            return (this.packageName).equals(that.packageName);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return packageName.hashCode();
    }
}
