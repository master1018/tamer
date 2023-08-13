public final class TableExample implements LayoutManager {
    static String[] ConnectOptionNames = { "Connect" };
    static String ConnectTitle = "Connection Information";
    Dimension origin = new Dimension(0, 0);
    JButton fetchButton;
    JButton showConnectionInfoButton;
    JPanel connectionPanel;
    JFrame frame; 
    JLabel userNameLabel;
    JTextField userNameField;
    JLabel passwordLabel;
    JTextField passwordField;
    JTextArea queryTextArea;
    JComponent queryAggregate;
    JLabel serverLabel;
    JTextField serverField;
    JLabel driverLabel;
    JTextField driverField;
    JPanel mainPanel;
    TableSorter sorter;
    JDBCAdapter dataBase;
    JScrollPane tableAggregate;
    void activateConnectionDialog() {
        if (JOptionPane.showOptionDialog(tableAggregate, connectionPanel,
                ConnectTitle,
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, ConnectOptionNames, ConnectOptionNames[0]) == 0) {
            connect();
            frame.setVisible(true);
        } else if (!frame.isVisible()) {
            System.exit(0);
        }
    }
    public void createConnectionDialog() {
        userNameLabel = new JLabel("User name: ", JLabel.RIGHT);
        userNameField = new JTextField("app");
        passwordLabel = new JLabel("Password: ", JLabel.RIGHT);
        passwordField = new JTextField("app");
        serverLabel = new JLabel("Database URL: ", JLabel.RIGHT);
        serverField = new JTextField("jdbc:derby:
        driverLabel = new JLabel("Driver: ", JLabel.RIGHT);
        driverField = new JTextField("org.apache.derby.jdbc.ClientDriver");
        connectionPanel = new JPanel(false);
        connectionPanel.setLayout(new BoxLayout(connectionPanel,
                BoxLayout.X_AXIS));
        JPanel namePanel = new JPanel(false);
        namePanel.setLayout(new GridLayout(0, 1));
        namePanel.add(userNameLabel);
        namePanel.add(passwordLabel);
        namePanel.add(serverLabel);
        namePanel.add(driverLabel);
        JPanel fieldPanel = new JPanel(false);
        fieldPanel.setLayout(new GridLayout(0, 1));
        fieldPanel.add(userNameField);
        fieldPanel.add(passwordField);
        fieldPanel.add(serverField);
        fieldPanel.add(driverField);
        connectionPanel.add(namePanel);
        connectionPanel.add(fieldPanel);
    }
    public TableExample() {
        mainPanel = new JPanel();
        createConnectionDialog();
        showConnectionInfoButton = new JButton("Configuration");
        showConnectionInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activateConnectionDialog();
            }
        });
        fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetch();
            }
        });
        queryTextArea = new JTextArea("SELECT * FROM APP.CUSTOMER", 25, 25);
        queryAggregate = new JScrollPane(queryTextArea);
        queryAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));
        tableAggregate = createTable();
        tableAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));
        mainPanel.add(fetchButton);
        mainPanel.add(showConnectionInfoButton);
        mainPanel.add(queryAggregate);
        mainPanel.add(tableAggregate);
        mainPanel.setLayout(this);
        frame = new JFrame("TableExample");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setBackground(Color.lightGray);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(false);
        frame.setBounds(200, 200, 640, 480);
        activateConnectionDialog();
    }
    public void connect() {
        dataBase = new JDBCAdapter(
                serverField.getText(),
                driverField.getText(),
                userNameField.getText(),
                passwordField.getText());
        sorter.setModel(dataBase);
    }
    public void fetch() {
        dataBase.executeQuery(queryTextArea.getText());
    }
    public JScrollPane createTable() {
        sorter = new TableSorter();
        JTable table = new JTable(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sorter.addMouseListenerToHeaderInTable(table);
        JScrollPane scrollpane = new JScrollPane(table);
        return scrollpane;
    }
    public static void main(String s[]) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TableExample.class.getName()).log(Level.SEVERE,
                    "Failed to apply Nimbus look and feel", ex);
        }
        new TableExample();
    }
    public Dimension preferredLayoutSize(Container c) {
        return origin;
    }
    public Dimension minimumLayoutSize(Container c) {
        return origin;
    }
    public void addLayoutComponent(String s, Component c) {
    }
    public void removeLayoutComponent(Component c) {
    }
    public void layoutContainer(Container c) {
        Rectangle b = c.getBounds();
        int topHeight = 90;
        int inset = 4;
        showConnectionInfoButton.setBounds(b.width - 2 * inset - 120, inset, 120,
                25);
        fetchButton.setBounds(b.width - 2 * inset - 120, 60, 120, 25);
        queryAggregate.setBounds(inset, inset, b.width - 2 * inset - 150, 80);
        tableAggregate.setBounds(new Rectangle(inset,
                inset + topHeight,
                b.width - 2 * inset,
                b.height - 2 * inset - topHeight));
    }
}
