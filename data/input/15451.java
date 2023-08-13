public class ServiceNameClone {
    public static void main(String[] args) throws Exception {
        ServiceName sn = new ServiceName("me@HERE");
        if (sn.clone().getClass() != ServiceName.class) {
            throw new Exception("ServiceName's clone is not a ServiceName");
        }
        if (!sn.clone().equals(sn)) {
            throw new Exception("ServiceName's clone changed");
        }
    }
}
