public class UserRoleDTO implements Serializable {
    private static final long serialVersionUID = 3093580994368353068L;
    private String id;
    private String description;
    private String name;
    public UserRoleDTO() {
        super();
    }
    public UserRoleDTO(Role role) {
        this();
        this.setDescription(role.getDescription());
        this.setName(role.getName());
        this.setId(role.getIdAsString());
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
