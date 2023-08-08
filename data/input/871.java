public class ProjectTO extends TO<Project, Integer> {
    private static final long serialVersionUID = 4490836491379722645L;
    private int userID;
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public ProjectTO() {
        this(new Project());
    }
    public ProjectTO(Project model) {
        super(model);
    }
    @Override
    public Integer getModelID() {
        return getModel().getId();
    }
    @Override
    public void setModelID(Integer id) {
        getModel().setId(id);
    }
}
