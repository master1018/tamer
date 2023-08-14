public abstract class AbstractBody implements Body {
    private Entity parent = null;
    public Entity getParent() {
        return parent;
    }
    public void setParent(Entity parent) {
        this.parent = parent;
    }
}
