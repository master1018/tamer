public class SystemSelectDialog extends JeriDialog {
    private JPanel labelPanel = new JPanel();
    private JPanel fieldPanel = new JPanel();
    private JPanel outerButtonPanel = new JPanel();
    private JPanel innerButtonPanel = new JPanel();
    private GridLayout labelPanelLayout = new GridLayout();
    private JLabel systemLabel = new JLabel();
    private JLabel subsystemLabel = new JLabel();
    private GridLayout fieldPanelLayout = new GridLayout();
    private JComboBox systemCombo = new JComboBox();
    private JComboBox subsystemCombo = new JComboBox();
    private BorderLayout outerButtonPanelLayout = new BorderLayout();
    private GridLayout innerButtonPanelLayout = new GridLayout();
    private JButton okButton = new JButton();
    private JButton cancelButton = new JButton();
    public static final int OK = 1;
    public static final int CANCEL = 0;
    private int result = CANCEL;
    private DataSource connectionPool;
    private SystemSelect systemSelect = new SystemSelect();
    public SystemSelectDialog() {
        this(null, "", false);
    }
    public SystemSelectDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        try {
            jbInit();
            pack();
            setSize(300, getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void jbInit() throws Exception {
        labelPanel.setLayout(labelPanelLayout);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        fieldPanel.setLayout(fieldPanelLayout);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
        outerButtonPanel.setLayout(outerButtonPanelLayout);
        outerButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        innerButtonPanel.setLayout(innerButtonPanelLayout);
        labelPanelLayout.setRows(2);
        labelPanelLayout.setHgap(5);
        labelPanelLayout.setVgap(5);
        systemLabel.setText("System:");
        systemLabel.setDisplayedMnemonic('S');
        systemLabel.setLabelFor(systemCombo);
        subsystemLabel.setText("Subsystem:");
        subsystemLabel.setDisplayedMnemonic('u');
        subsystemLabel.setLabelFor(subsystemCombo);
        fieldPanelLayout.setRows(2);
        fieldPanelLayout.setVgap(5);
        systemCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                combo_itemStateChanged(e);
            }
        });
        subsystemCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                combo_itemStateChanged(e);
            }
        });
        innerButtonPanelLayout.setHgap(5);
        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.setMnemonic('O');
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButton_actionPerformed(e);
            }
        });
        cancelButton.setText("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton_actionPerformed(e);
            }
        });
        labelPanel.add(systemLabel, null);
        labelPanel.add(subsystemLabel, null);
        this.getContentPane().add(labelPanel, BorderLayout.WEST);
        fieldPanel.add(systemCombo, null);
        fieldPanel.add(subsystemCombo, null);
        this.getContentPane().add(fieldPanel, BorderLayout.CENTER);
        innerButtonPanel.add(okButton, null);
        innerButtonPanel.add(cancelButton, null);
        outerButtonPanel.add(innerButtonPanel, BorderLayout.EAST);
        this.getContentPane().add(outerButtonPanel, BorderLayout.SOUTH);
    }
    private void okButton_actionPerformed(ActionEvent e) {
        result = OK;
        setVisible(false);
    }
    private void cancelButton_actionPerformed(ActionEvent e) {
        result = CANCEL;
        setVisible(false);
    }
    public DataSource getDataSource() {
        return connectionPool;
    }
    public void setDataSource(DataSource connectionPool) {
        this.connectionPool = connectionPool;
        systemSelect.setDataSource(connectionPool);
    }
    public int getResult() {
        return result;
    }
    public void refresh() throws java.sql.SQLException {
        systemCombo.removeAllItems();
        systemCombo.addItem("Any");
        EpicsSystem[] systems = systemSelect.loadSystems();
        for (int i = 0; i < systems.length; i++) systemCombo.addItem(systems[i]);
        subsystemCombo.removeAllItems();
        subsystemCombo.addItem("Any");
        EpicsSubsystem[] subsystems = systemSelect.loadSubsystems();
        for (int i = 0; i < subsystems.length; i++) subsystemCombo.addItem(subsystems[i]);
    }
    public EpicsSystem getSystem() {
        Object selectedSystem = systemCombo.getSelectedItem();
        if (selectedSystem instanceof EpicsSystem) return (EpicsSystem) selectedSystem; else return null;
    }
    public EpicsSubsystem getSubsystem() {
        Object selectedSubsystem = subsystemCombo.getSelectedItem();
        if (selectedSubsystem instanceof EpicsSubsystem) return (EpicsSubsystem) selectedSubsystem; else return null;
    }
    private void this_windowClosing(WindowEvent e) {
        result = CANCEL;
    }
    private void combo_itemStateChanged(ItemEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object selectedSystem = systemCombo.getSelectedItem();
                Object selectedSubsystem = subsystemCombo.getSelectedItem();
                boolean enable = selectedSystem instanceof EpicsSystem || selectedSubsystem instanceof EpicsSubsystem;
                okButton.setEnabled(enable);
            }
        });
    }
}
