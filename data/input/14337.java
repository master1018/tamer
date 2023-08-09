final class ClassElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return getOwner().findClass(argument);
    }
}
