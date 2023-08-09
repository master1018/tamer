public class OkCancelButtonPanel extends JPanel
{
    public OkCancelButtonPanel(ActionListener listener)
    {
        DelegateAction okAction = new OkAction();
        okAction.addActionListener(listener);
        DelegateAction cancelAction = new CancelAction();
        cancelAction.addActionListener(listener);
        add(CommonUI.createButton(okAction));
        add(CommonUI.createButton(cancelAction));
    }
    public static final String OK_COMMAND = "ok-command";
    public static final String CANCEL_COMMAND = "cancel-command";
}
