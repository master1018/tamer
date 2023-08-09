public class OkCancelDialog extends JDialog
    implements ActionListener
{
    public OkCancelDialog(String title, JPanel panel)
    {
        this(title, panel, true);
    }
    public OkCancelDialog(String title, JPanel panel, boolean modal)
    {
        setTitle(title);
        setModal(modal);
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(panel, "Center");
        pane.add(new OkCancelButtonPanel(this), "South");
        pack();
        CommonUI.centerComponent(this);
    }
    public boolean isOk()
    {
        return okPressed;
    }
    public void actionPerformed(ActionEvent evt)
    {
        String command = evt.getActionCommand();
        if(command.equals("ok-command"))
        {
            okPressed = true;
            setVisible(false);
            dispose();
        } else
        if(command.equals("cancel-command"))
        {
            okPressed = false;
            setVisible(false);
            dispose();
        }
    }
    private boolean okPressed;
}
