public class ConnectionDialogue extends WindowPane {
    private final MainController controller;
    private final Column column = new Column();
    private SelectField databaseType;
    private TextField host;
    private LiveTextField port;
    private TextField database;
    private TextField userName;
    private PasswordField password;
    public ConnectionDialogue(final MainController controller) {
        super();
        this.controller = controller;
        setMaximizable(false);
        setMinimizable(false);
        initComponents();
        add(column);
    }
    private void initComponents() {
        Grid grid = new Grid();
        grid.add(Utilities.createLabel(getClass().getName(), "databaseType", "Title.Label"));
        grid.add(createDatabaseType());
        grid.add(Utilities.createLabel(getClass().getName(), "host", "Title.Label"));
        grid.add(Utilities.createTextField(getClass().getName(), "host", this));
        grid.add(Utilities.createLabel(getClass().getName(), "port", "Title.Label"));
        grid.add(createPort());
        grid.add(Utilities.createLabel(getClass().getName(), "database", "Title.Label"));
        grid.add(Utilities.createTextField(getClass().getName(), "database", this));
        grid.add(Utilities.createLabel(getClass().getName(), "userName", "Title.Label"));
        grid.add(Utilities.createTextField(getClass().getName(), "userName", this));
        grid.add(Utilities.createLabel(getClass().getName(), "password", "Title.Label"));
        grid.add(Utilities.createTextField(getClass().getName(), "password", new ConnectListener(controller), this));
        column.add(grid);
        column.add(createButtons());
    }
    protected Component createDatabaseType() {
        String[] values = Configuration.getString(this, "databaseTypes").split(",");
        for (int i = 0; i < values.length; ++i) {
            values[i] = values[i].trim();
        }
        databaseType = new SelectField(values);
        databaseType.setWidth(Extent.getInstance(Dimensions.getInt(this, "databaseType.width")));
        databaseType.addActionListener(new DatabaseTypeListener());
        return databaseType;
    }
    protected Component createPort() {
        port = new LiveTextField();
        port.setRegexp("[1-9][0-9]*");
        return port;
    }
    protected Component createButtons() {
        Row row = new Row();
        row.add(Utilities.createButton(getClass().getName(), "connect", "Accept.Button", new ConnectListener(controller)));
        row.add(Utilities.createButton(getClass().getName(), "cancel", "Cancel.Button", new CancelListener()));
        row.add(createSave());
        return row;
    }
    protected Component createSave() {
        PopUp popup = new PopUp();
        popup.setTarget(Utilities.createLabel(getClass().getName(), "save", "Save.Label"));
        popup.setPopUp(new SaveConnectionComponent(controller));
        return popup;
    }
    protected String getDatabaseType() {
        return (String) databaseType.getSelectedItem();
    }
    protected void setDatabaseType(final String value) {
        databaseType.setSelectedItem(value);
    }
    protected String getHost() {
        return host.getText();
    }
    protected void setHost(final String value) {
        host.setText(value);
    }
    protected int getPort() {
        return ((port.getText().length() == 0) ? 0 : Integer.parseInt(port.getText()));
    }
    protected void setPort(final int value) {
        port.setText(String.valueOf(value));
    }
    protected String getDatabase() {
        return database.getText();
    }
    protected void setDatabase(final String value) {
        database.setText(value);
    }
    protected String getUserName() {
        return userName.getText();
    }
    protected void setUserName(final String value) {
        userName.setText(value);
    }
    protected String getPassword() {
        return password.getText();
    }
    protected void setPassword(final String value) {
        password.setText(value);
    }
    protected class DatabaseTypeListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            port.setText(Configuration.getString(ConnectionDialogue.this, databaseType.getSelectedItem().toString() + ".port"));
        }
    }
    protected class CancelListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            ConnectionDialogue.this.userClose();
        }
    }
}
