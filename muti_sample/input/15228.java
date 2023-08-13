final class LongElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Long.decode(argument);
    }
}
