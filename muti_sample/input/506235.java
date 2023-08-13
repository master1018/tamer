public class DataCallState {
    public int cid;
    public int active;
    public String type;
    public String apn;
    public String address;
    @Override
    public String toString() {
        return "DataCallState: {" + " cid: " + cid + ", active: " + active + ", type: " + type
                + ", apn: " + apn + ", address: " + address + " }";
    }
}
