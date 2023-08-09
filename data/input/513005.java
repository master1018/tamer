public class SigApi implements IApi, Serializable {
    private Set<IPackage> packages = Uninitialized.unset();
    private String description;
    private Visibility visibility;
    public SigApi(String description, Visibility visibility) {
        this.description = description;
        this.visibility = visibility;
    }
    public String getName() {
        return description;
    }
    public void setName(String description) {
        this.description = description;
    }
    public Set<IPackage> getPackages() {
        return packages;
    }
    public void setPackages(Set<IPackage> packages) {
        this.packages = packages;
    }
    public Visibility getVisibility() {
        return visibility;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        return builder.toString();
    }
}
