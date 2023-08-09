class DontCareFieldPosition extends FieldPosition {
    static final FieldPosition INSTANCE = new DontCareFieldPosition();
    private final Format.FieldDelegate noDelegate = new Format.FieldDelegate() {
        public void formatted(Format.Field attr, Object value, int start,
                              int end, StringBuffer buffer) {
        }
        public void formatted(int fieldID, Format.Field attr, Object value,
                              int start, int end, StringBuffer buffer) {
        }
    };
    private DontCareFieldPosition() {
        super(0);
    }
    Format.FieldDelegate getFieldDelegate() {
        return noDelegate;
    }
}
