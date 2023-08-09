final class IntElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Integer.decode(argument);
    }
}
