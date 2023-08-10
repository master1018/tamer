public class JPQLDefinition {
    private List<JPQLCause> causeList = new ArrayList<JPQLCause>();
    public void addCause(JPQLCause cause) {
        getCauseList().add(cause);
    }
    public List<JPQLCause> getCauseList() {
        return causeList;
    }
    public void setCauseList(List<JPQLCause> causeList) {
        this.causeList = causeList;
    }
}
