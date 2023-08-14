public class TypeScriptOutputListener implements OutputListener {
    private TypeScript script;
    private boolean appendNewline;
    public TypeScriptOutputListener(TypeScript script) {
        this(script, false);
    }
    public TypeScriptOutputListener(TypeScript script, boolean appendNewline) {
        this.script = script;
        this.appendNewline = appendNewline;
    }
    @Override
    public void putString(String s) {
        script.append(s);
        if (appendNewline) {
            script.newline();
    }
    }
}
