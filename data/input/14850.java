public abstract class BaseExecutableMemberTaglet extends BaseTaglet {
    public boolean inField() {
        return false;
    }
    public boolean inOverview() {
        return false;
    }
    public boolean inPackage() {
        return false;
    }
    public boolean inType() {
        return false;
    }
    public boolean isInlineTag() {
        return false;
    }
}
