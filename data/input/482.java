public class NewConnectionListener implements EventListener {
    MainController mainCont;
    String connectionName;
    public NewConnectionListener(MainController mainCont, String connectionName) {
        this.mainCont = mainCont;
        this.connectionName = connectionName;
    }
    @Override
    public void onEvent(Event arg0) throws Exception {
        mainCont.onNewConnection(connectionName);
    }
}
