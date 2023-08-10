public class TheItem implements Serializable {
    private int parentId;
    private int id;
    private String fullName;
    public TheItem(int id, String fullName) {
        this.id = id;
        if (fullName != null) this.fullName = fullName; else this.fullName = "";
    }
    public TheItem() {
        this.fullName = "";
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        if (fullName != null) this.fullName = fullName; else this.fullName = "";
    }
    public int getParentId() {
        return this.parentId;
    }
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
