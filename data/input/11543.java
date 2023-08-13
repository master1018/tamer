class IntTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "int";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeInt(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readInt()";
    }
}
