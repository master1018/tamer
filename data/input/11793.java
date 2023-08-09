final class ByteElementHandler extends StringElementHandler {
    @Override
    public Object getValue(String argument) {
        return Byte.decode(argument);
    }
}
