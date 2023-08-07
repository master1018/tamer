public class Organization extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Project> projects;
    @Id
    @Override
    public String getId() {
        return super.getId();
    }
    @Column
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @ManyToMany(mappedBy = "organizations")
    public List<Project> getProjects() {
        return projects;
    }
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
