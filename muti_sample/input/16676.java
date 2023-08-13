class RepeatNode extends AbstractTypeNode {
    Node member = null;
    void constrain(Context ctx) {
        super.constrain(ctx);
        if (components.size() != 1) {
            error("Repeat must have exactly one member, use Group for more");
        }
        member = components.get(0);
        if (!(member instanceof TypeNode)) {
            error("Repeat member must be type specifier");
        }
    }
    void document(PrintWriter writer) {
        docRowStart(writer);
        writer.println("<td colspan=" +
                       (maxStructIndent - structIndent) + ">");
        writer.println("int<td><i>" + name + "</i><td>" +
                       comment() + "&nbsp;");
        docRowStart(writer);
        writer.println("<td colspan=" +
                       (maxStructIndent - structIndent + 2) + ">");
        writer.println("Repeated <i>" + name + "</i> times:");
        ++structIndent;
        member.document(writer);
        --structIndent;
    }
    String docType() {
        return "-BOGUS-"; 
    }
    String javaType() {
        return member.javaType() + "[]";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel, "\"\"");
        indent(writer, depth);
        writer.println("ps.writeInt(" + writeLabel + ".length);");
        indent(writer, depth);
        writer.println("for (int i = 0; i < " + writeLabel + ".length; i++) {;");
        ((TypeNode)member).genJavaWrite(writer, depth+1, writeLabel + "[i]");
        indent(writer, depth);
        writer.println("}");
    }
    String javaRead() {
        error("Internal - Should not call RepeatNode.javaRead()");
        return "";
    }
    public void genJavaRead(PrintWriter writer, int depth,
                            String readLabel) {
        genJavaDebugRead(writer, depth, readLabel, "\"\"");
        String cntLbl = readLabel + "Count";
        indent(writer, depth);
        writer.println("int " + cntLbl + " = ps.readInt();");
        indent(writer, depth);
        writer.println(readLabel + " = new " + member.javaType() +
                       "[" + cntLbl + "];");
        indent(writer, depth);
        writer.println("for (int i = 0; i < " + cntLbl + "; i++) {;");
        member.genJavaRead(writer, depth+1, readLabel + "[i]");
        indent(writer, depth);
        writer.println("}");
    }
}
