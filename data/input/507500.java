 final class SpecialAccess {
    static  final LangAccess LANG;
    static {
        try {
            Runnable.class.getMethod("run", (Class[]) null);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
        LANG = EnumSet.LANG_BOOTSTRAP;
        if (LANG == null) {
            throw new AssertionError();
        }
    }
}
