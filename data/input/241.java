public class DetailDegreeCurricularPlanView extends AcademicCloudView implements DetailDegreeCurricularPlanPresenter.Display {
    private Button btnBack = new Button();
    private Button btnCancel = new Button();
    private DegreeCurricularPlanDataComponent componentDegreeCurricularPlan;
    @Override
    public HasClickHandlers getBackButton() {
        return btnBack;
    }
    @Override
    public HasClickHandlers getCancelButton() {
        return btnCancel;
    }
    public void init(DetailDegreeCurricularPlanViewInitDataDTO data) {
        componentDegreeCurricularPlan.init(data.getDegreeCurricularPlan());
    }
    public void setData(DegreeCurricularPlanDTO data) {
        componentDegreeCurricularPlan.setData(data);
        clearErrorFlag();
    }
    public DegreeCurricularPlanDTO getData() {
        return componentDegreeCurricularPlan.getData();
    }
    public DetailDegreeCurricularPlanView() {
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setWidth("100%");
        initWidget(contentPanel);
        componentDegreeCurricularPlan = new DegreeCurricularPlanDataComponent();
        setReadOnlyFields(componentDegreeCurricularPlan);
        contentPanel.add(componentDegreeCurricularPlan);
        HorizontalPanel footerPanel = new HorizontalPanel();
        contentPanel.add(footerPanel);
        footerPanel.setSpacing(5);
        footerPanel.add(btnBack);
        btnBack.setText(buttonConstants.btnBack_text());
        btnBack.setStyleName(WidgetUtils.BUTTON_STYLE);
        btnBack.addStyleName(WidgetUtils.ICON_BACK_STYLE);
        footerPanel.add(btnCancel);
        btnCancel.setText(buttonConstants.btnCancel_text());
        btnCancel.setStyleName(WidgetUtils.BUTTON_STYLE);
        btnCancel.addStyleName(WidgetUtils.ICON_CANCEL_STYLE);
    }
    private void setReadOnlyFields(DegreeCurricularPlanDataComponent component) {
        component.setReadOnly(true);
    }
}
