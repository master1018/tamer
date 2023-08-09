final class FalseElementHandler extends NullElementHandler {
    @Override
    public Object getValue() {
        return Boolean.FALSE;
    }
}
