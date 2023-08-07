public class TermPartLister extends AElementLister {
    public TermPartLister(HibernateConnector connector, ListCoordinator coordinator) {
        super(connector, coordinator);
    }
    @Override
    protected void listInternal(IElement element, List<IElement> list) {
        IVocabularyEntry part = (IVocabularyEntry) connector.loadKeepAlive(((ITermPart) element).getNormalization());
        coordinator.listElements(part, list);
    }
}
