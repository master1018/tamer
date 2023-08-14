public class SigPackage extends SigAnnotatableElement implements IPackage,
        Serializable {
    private String name;
    private Set<IClassDefinition> classes = Uninitialized.unset();
    public SigPackage(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public List<String> getPackageFragments() {
        return Arrays.asList(name.split("\\."));
    }
    public Set<IClassDefinition> getClasses() {
        return classes;
    }
    public void setClasses(Set<IClassDefinition> classes) {
        this.classes = classes;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("package: ");
        builder.append(getName());
        return builder.toString();
    }
}
