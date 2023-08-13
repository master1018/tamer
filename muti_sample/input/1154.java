class StringTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "string";
    }
    String javaType() {
        return "String";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeString(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readString()";
    }
}
