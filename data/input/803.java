public class AbstractOnBalanceVolume extends AbstractIndicator {
    public AbstractOnBalanceVolume() {
        super("OBV", "Description");
    }
    public Indicator newInstance() {
        return new OnBalanceVolume();
    }
}
