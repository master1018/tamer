public class TableColumnOneToManyAssociationName extends TableColumnNakedObjectMemberName<OneToManyAssociation> {
    public TableColumnOneToManyAssociationName(final ObjectSpecification noSpec, final AuthenticationSession session, final ObjectAdapter nakedObject, final ResourceContext resourceContext) {
        super(noSpec, session, nakedObject, resourceContext);
    }
    @Override
    public Element doTd(final OneToManyAssociation oneToManyAssociation) {
        final String memberName = oneToManyAssociation.getIdentifier().getMemberName();
        final String memberType = "collection";
        final String uri = MessageFormat.format("{0}/specs/{1}/{2}/{3}", getContextPath(), getNoSpec().getFullName(), memberType, memberName);
        return new Element(xhtmlRenderer.aHref(uri, oneToManyAssociation.getName(), "propertySpec", memberType, HtmlClass.COLLECTION));
    }
}
