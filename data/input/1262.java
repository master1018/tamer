public class OccupiesAssociationLabelViewFactory extends AbstractLabelViewFactory {
    public View createView(IAdaptable semanticAdapter, View containerView, String semanticHint, int index, boolean persisted, PreferencesHint preferencesHint) {
        Node view = (Node) super.createView(semanticAdapter, containerView, semanticHint, index, persisted, preferencesHint);
        return view;
    }
    protected List createStyles(View view) {
        List styles = new ArrayList();
        return styles;
    }
}
