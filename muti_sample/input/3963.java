public abstract class AbstractMemberBuilder extends AbstractBuilder {
    public AbstractMemberBuilder(Configuration configuration) {
        super(configuration);
    }
    public void build() throws DocletAbortException {
        throw new DocletAbortException();
    }
    @Override
    public void build(XMLNode node, Content contentTree) {
        if (hasMembersToDocument()) {
            super.build(node, contentTree);
        }
    }
    public abstract boolean hasMembersToDocument();
}
