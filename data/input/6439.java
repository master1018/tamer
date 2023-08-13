public class ModificationItem implements java.io.Serializable {
    private int mod_op;
    private Attribute attr;
    public ModificationItem(int mod_op, Attribute attr) {
        switch (mod_op) {
        case DirContext.ADD_ATTRIBUTE:
        case DirContext.REPLACE_ATTRIBUTE:
        case DirContext.REMOVE_ATTRIBUTE:
            if (attr == null)
                throw new IllegalArgumentException("Must specify non-null attribute for modification");
            this.mod_op = mod_op;
            this.attr = attr;
            break;
        default:
            throw new IllegalArgumentException("Invalid modification code " + mod_op);
        }
    }
    public int getModificationOp() {
        return mod_op;
    }
    public Attribute getAttribute() {
        return attr;
    }
    public String toString() {
        switch (mod_op) {
        case DirContext.ADD_ATTRIBUTE:
            return ("Add attribute: " + attr.toString());
        case DirContext.REPLACE_ATTRIBUTE:
            return ("Replace attribute: " + attr.toString());
        case DirContext.REMOVE_ATTRIBUTE:
            return ("Remove attribute: " + attr.toString());
        }
        return "";      
    }
    private static final long serialVersionUID = 7573258562534746850L;
}
