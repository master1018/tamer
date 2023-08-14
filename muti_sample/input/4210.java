final class CharElementHandler extends StringElementHandler {
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("code")) { 
            int code = Integer.decode(value);
            for (char ch : Character.toChars(code)) {
                addCharacter(ch);
            }
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    public Object getValue(String argument) {
        if (argument.length() != 1) {
            throw new IllegalArgumentException("Wrong characters count");
        }
        return Character.valueOf(argument.charAt(0));
    }
}
