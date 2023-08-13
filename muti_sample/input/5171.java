final class FloatElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Float.valueOf(argument);
    }
}
