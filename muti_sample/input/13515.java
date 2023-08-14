final class ShortElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Short.decode(argument);
    }
}
