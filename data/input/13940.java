public class TypeScriptWriter extends Writer {
    TypeScript script;
    public TypeScriptWriter(TypeScript script) {
        this.script = script;
    }
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        script.append(String.valueOf(cbuf, off, len));
    }
    @Override
    public void flush() {
        script.flush();
    }
    @Override
    public void close() {
        script.flush();
    }
}
