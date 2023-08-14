class UntaggedValueTypeNode extends ValueTypeNode {
    String docType() {
        return "untagged-value";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeUntaggedValue(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readUntaggedValue()";
    }
}
