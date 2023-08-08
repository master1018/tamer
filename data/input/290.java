public class BotChannel extends Channel {
    private Bot bot;
    private static Logger log = Logger.getLogger(BotChannel.class);
    public BotChannel(String chanName, Bot bot) {
        super(chanName);
        this.bot = bot;
    }
    @Override
    public void setMode(Mode mode) {
        super.setMode(mode);
        log.debug("Mode: " + mode);
    }
    @Override
    public String toString() {
        return "BoticelliChannel";
    }
}
