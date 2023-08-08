public class StatusServerListener extends SocketAction implements IStatusServerListener {
    private static final boolean CLASSDEBUG = false;
    public StatusServerListener(LoggerCollection loggerCol, ConfigurationCollection config, String name, ServerSocket servSocket, StatisticModel statsModel) throws NetworkException, WrongConfigurationException {
        super(loggerCol, config, name, servSocket, statsModel);
        timer.pause();
    }
    @Override
    public Socket getClientSocket() {
        return super.clientSocket;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public SocketActionTimer getTimer() {
        return timer;
    }
    @Override
    public void statusServerFired(Report report) {
        try {
            timer.goOn();
            send(report.toXMLString(), true);
            timer.pause();
        } catch (NetworkException ex) {
            loggerCol.logException(CLASSDEBUG, this.getClass().getName(), Level.FATAL, ex);
        }
    }
    @Override
    public void offline() {
        offline = true;
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                loggerCol.logException(CLASSDEBUG, this.getClass().getName(), Level.FATAL, ex);
            }
        }
        if (out != null) {
            out.close();
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                loggerCol.logException(CLASSDEBUG, this.getClass().getName(), Level.FATAL, ex);
            }
        }
    }
}
