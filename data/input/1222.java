public class ListWrapper<E extends IEntity> {
    private Collection<E> items;
    public ListWrapper() {
    }
    @XmlElement(name = "item")
    public Collection<E> getItems() {
        return items;
    }
    public void setItems(Collection<E> i) {
        items = i;
    }
}
