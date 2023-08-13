abstract class AbstractTypeNode extends AbstractNamedNode
                                implements TypeNode {
    abstract String docType();
    public abstract void genJavaWrite(PrintWriter writer, int depth,
                                      String writeLabel);
    abstract String javaRead();
    void document(PrintWriter writer) {
        docRowStart(writer);
        writer.println("<td colspan=" +
                       (maxStructIndent - structIndent) + ">");
        writer.println(docType() + "<td><i>" + name() +
                       "</i><td>" + comment() + "&nbsp;");
    }
    String javaType() {
        return docType(); 
    }
    public void genJavaRead(PrintWriter writer, int depth,
                            String readLabel) {
        indent(writer, depth);
        writer.print(readLabel);
        writer.print(" = ");
        writer.print(javaRead());
        writer.println(";");
        genJavaDebugRead(writer, depth, readLabel, debugValue(readLabel));
    }
    public void genJavaDeclaration(PrintWriter writer, int depth) {
        writer.println();
        genJavaComment(writer, depth);
        indent(writer, depth);
        writer.print("final ");
        writer.print(javaType());
        writer.print(" " + name);
        writer.println(";");
    }
    public String javaParam() {
        return javaType() + " " + name;
    }
}
