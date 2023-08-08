public class UserQuotaPanel extends SPanel {
    STextField quota_field = null;
    private static final String SAVE_LABEL = "Save";
    private SFrame parent = null;
    public UserQuotaPanel(SFrame parent) {
        this.parent = parent;
        this.setLayout(new SBorderLayout());
        SLineBorder border = new SLineBorder(1);
        this.setBorder(border);
        this.add(new SLabel("<html>USER STORAGE QUOTA: <br><br><br>" + "Enter the maximum amount of storage space each user should" + " be allocated: <br></html>"), SBorderLayout.NORTH);
        quota_field = new STextField();
        quota_field.setText(new Integer(PeerserverProperties.getInstance().getUserQuotaValue()).toString());
        SPanel input = new SPanel();
        input.add(quota_field);
        input.add(new SLabel("MB"));
        this.add(input, SBorderLayout.CENTER);
        SPanel save_panel = new SPanel(new SFlowDownLayout());
        save_panel.add(new SLabel("<html><br><br><br><br>Save Settings</html>"));
        SForm button = new SForm();
        button.add(new SButton(new ButtonActions(SAVE_LABEL)));
        save_panel.add(button);
        this.add(save_panel, SBorderLayout.SOUTH);
    }
    private class ButtonActions extends AbstractAction {
        public ButtonActions(String name) {
            super(name);
        }
        public ButtonActions(String name, Icon icon) {
            super(name, icon);
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(SAVE_LABEL)) {
                try {
                    PeerserverProperties.getInstance().setUserQuotaValue(new Integer(quota_field.getText()).intValue());
                    SOptionPane.showPlainMessageDialog(parent, "Your changes have been saved", "Changes Saved");
                } catch (Exception e2) {
                    SOptionPane.showPlainMessageDialog(parent, "Your changes have NOT been saved: " + e2, "Changes Saved");
                }
            }
        }
    }
}
