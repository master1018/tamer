public class DBMenuItem extends MapHandlerMenuItem implements GUIConstants {
    private static final long serialVersionUID = 4376634902865428743L;
    protected WindowSupport ws;
    protected DBQueryPanel dbPanel;
    protected float lat;
    protected float lon;
    protected boolean popup = false;
    private DBMenuItemAction action = null;
    public DBMenuItem() {
        super("Distance-Based");
        setAction(getMenuItemAction("Distance-Based"));
        dbPanel = new DBQueryPanel(getMenuItemAction());
    }
    public void setClickCoordinates(LatLonPoint point) {
        popup = true;
        lat = point.getLatitude();
        lon = point.getLongitude();
    }
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof MapHandler) {
            ((MapHandler) someObj).add(this);
            ((MapHandler) someObj).add(dbPanel);
        }
        dbPanel.findAndInit(someObj);
    }
    public Action getMenuItemAction(String text) {
        if (action == null) {
            action = new DBMenuItemAction(text);
        }
        return action;
    }
    public Action getMenuItemAction() {
        return getMenuItemAction(null);
    }
    class DBMenuItemAction extends AbstractAction {
        private static final long serialVersionUID = 663976833476682713L;
        public DBMenuItemAction(String text) {
            super(text);
        }
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if ((command.equals(OK)) && (ws != null)) {
                dbPanel.sendQueryParameters();
                ws.killWindow();
            } else if ((command.equals(CANCEL)) && (ws != null)) {
                ws.killWindow();
            } else {
                if (ws == null) {
                    ws = new WindowSupport(dbPanel, "Distance Query");
                }
                MapHandler mh = getMapHandler();
                Frame frame = null;
                if (mh != null) {
                    frame = (Frame) mh.get(java.awt.Frame.class);
                }
                ws.displayInWindow(frame);
                if (popup) {
                    dbPanel.updateDialogValues(lat, lon);
                } else {
                    dbPanel.updateDialogValues();
                }
            }
            popup = false;
        }
    }
    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        dbPanel.findAndUndo(someObj);
    }
}
