final class BooleanElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(argument)) {
            return Boolean.TRUE;
        }
        if (Boolean.FALSE.toString().equalsIgnoreCase(argument)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Unsupported boolean argument: " + argument);
    }
}
