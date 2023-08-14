public class DeprecatedTaglet extends BaseTaglet{
    public DeprecatedTaglet() {
        name = "deprecated";
    }
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) {
        return writer.deprecatedTagOutput(holder);
    }
}
