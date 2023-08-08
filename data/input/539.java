public class DistributionFilterGroupEditor extends Composite implements Editor<DistributionFilterGroup> {
    interface DistributionFilterGroupEditorUIBinder extends UiBinder<Widget, DistributionFilterGroupEditor> {
    }
    private static DistributionFilterGroupEditorUIBinder uiBinder = GWT.create(DistributionFilterGroupEditorUIBinder.class);
    @UiField
    GroupNameEditor nameEditor;
    @UiField
    CheckBox invertedEditor;
    public DistributionFilterGroupEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
