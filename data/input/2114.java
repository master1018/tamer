public class CodeTaglet extends LiteralTaglet {
        private static final String NAME = "code";
        public static void register(Map<String, Taglet> map) {
                map.remove(NAME);
                map.put(NAME, new CodeTaglet());
        }
        public String getName() {
                return NAME;
        }
        public String toString(Tag tag) {
                return "<code>" + super.toString(tag) + "</code>";
        }
}
