public class WorldGroupImpl extends GroupImpl {
    public WorldGroupImpl(String s) {
        super(s);
    }
    public boolean isMember(Principal member) {
        return true;
    }
}
