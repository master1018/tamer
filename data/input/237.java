public class ConfigureLogMessage extends SyncMessage {
    static Logger logger = Logger.getLogger(ConfigureLogMessage.class);
    public ConfigureLogMessage() {
    }
    @Override
    public AsyncMessage process() {
        SimpleResponseMessage response = new SimpleResponseMessage();
        try {
            LogManager.getInstance().configure();
            response.setSuccess(true);
        } catch (OrkException e) {
            response.setSuccess(false);
            response.setComment(e.getMessage());
        }
        return response;
    }
    public void define(OrkSerializer serializer) throws OrkException {
    }
    public String getOrkClassName() {
        return "configurelog";
    }
    public void validate() {
    }
}
