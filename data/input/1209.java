public abstract class KnowledgeModifier {
    protected TreeMap<IdI, ? extends Location> facilities;
    public KnowledgeModifier(TreeMap<IdI, ? extends Location> facilities) {
        this.facilities = facilities;
    }
    public abstract void modify(Knowledge knowledge);
}
