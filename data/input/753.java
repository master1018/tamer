public class PrivateContactData extends AbstractContactData {
    private String birthdate;
    private String remark;
    public PrivateContactData() {
        super();
        this.birthdate = "";
        this.remark = "";
    }
    public String getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
