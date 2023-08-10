public class PolicyClsesPanel extends ClsesPanel {
    protected PolicyFrameworkModel policyFrameworkModel;
    protected AllowableAction createPolicyClassAction;
    public PolicyClsesPanel(Project project) {
        super(project);
        policyFrameworkModel = new PolicyFrameworkModel(project.getKnowledgeBase());
    }
    public Action getCreatePolicyClassAction() {
        Icon icon;
        try {
            icon = Icons.getCreateClsIcon();
        } catch (Throwable th) {
            URL url = getClass().getResource("/res/class.create.gif");
            icon = new ImageIcon(url);
        }
        createPolicyClassAction = new AllowableAction("Add class with policy", icon, null) {
            public void actionPerformed(ActionEvent arg0) {
                Collection parents = _subclassPane.getSelection();
                if (!parents.isEmpty()) {
                    Cls cls = getKnowledgeBase().createCls(null, parents, policyFrameworkModel.getPolicyMetaCls());
                    policyFrameworkModel.setPolicyType(cls, "D");
                    _subclassPane.extendSelection(cls);
                }
            }
        };
        return createPolicyClassAction;
    }
    protected AllowableAction getCreateClsAction() {
        return (AllowableAction) getCreatePolicyClassAction();
    }
}
