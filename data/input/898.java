public class ParallelPortCommunicator {
    private final transient Log log = LogFactory.getLog(this.getClass());
    public void listPorts() {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
            if (this.log.isDebugEnabled()) {
                this.log.debug(id.getPortType() + "," + id.getName() + "," + id.getCurrentOwner());
            }
        }
    }
}
