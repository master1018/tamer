public class MyCRL extends CRL {
    public MyCRL(String type) {
        super(type);
    }
    public String toString() {
        return "MyCRL: [" + getType() + "]";
    }
    public boolean isRevoked(Certificate cert) {
        return false;
    }
}
