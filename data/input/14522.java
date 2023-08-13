final class DoubleElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Double.valueOf(argument);
    }
}
