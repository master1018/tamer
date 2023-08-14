public final class Test6256805 extends AbstractTest {
    public static void main(String[] args) {
        new Test6256805().test(true);
    }
    protected CharacterBean getObject() {
        CharacterBean bean = new CharacterBean();
        bean.setString("\u0000\ud800\udc00\uFFFF");
        return bean;
    }
    protected CharacterBean getAnotherObject() {
        CharacterBean bean = new CharacterBean();
        bean.setPrimitive('\uD800');
        bean.setChar(Character.valueOf('\u001F'));
        return bean;
    }
    public static final class CharacterBean {
        private char primitive;
        private Character character;
        private String string;
        public char getPrimitive() {
            return this.primitive;
        }
        public void setPrimitive( char primitive ) {
            this.primitive = primitive;
        }
        public Character getChar() {
            return this.character;
        }
        public void setChar( Character character ) {
            this.character = character;
        }
        public String getString() {
            return this.string;
        }
        public void setString( String string ) {
            this.string = string;
        }
    }
}
