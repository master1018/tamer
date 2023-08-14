public class AbstractGet extends AbstractBase {
    public DoubledExtendOkay getExtended() {
        return new DoubledExtendOkay();
    }
}
abstract class AbstractBase extends BaseOkay {
    public abstract DoubledExtendOkay getExtended();
}
