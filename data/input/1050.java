public class ChordAction extends AbstractAction {
    public ChordAction() {
        super(ACTION_CHORD_NAME);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (dlg == null) {
            dlg = new JDialog(ApplicationContext.getInstance().getDefaultDialogOwner(), ResourceFactory.getString(ACTION_CHORD_NAME), false);
            dlg.setLayout(new BorderLayout());
            dlg.add(new ChordPane(), BorderLayout.CENTER);
            dlg.pack();
        }
        if (dlg.isVisible()) {
            dlg.toFront();
        } else {
            dlg.setVisible(true);
        }
    }
    private JDialog dlg;
}
