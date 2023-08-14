public class test {
    public void generateBaseMethods(Writer w) throws IOException {
        super.generateBaseMethods(w);
        w.write("\n \n");
        w.write("\n" + "  public Integer get" + suffix + "Index()\n" + "      throws AccessPoemException {\n" + "    readLock();\n" + "    return get" + suffix + "_unsafe();\n" + "  }\n" + "\n");
        w.write("\n \n");
        w.write("  public void set" + suffix + "Index(Integer raw)\n" + "      throws AccessPoemException {\n" + "    " + tableAccessorMethod + "().get" + suffix + "Column()." + "getType().assertValidRaw(raw);\n" + "    writeLock();\n" + "    set" + suffix + "_unsafe(raw);\n" + "  }\n" + "\n");
        w.write("\n \n");
        w.write("  public " + type + " get" + suffix + "()\n" + "      throws AccessPoemException {\n" + "    Integer index = get" + suffix + "Index();\n" + "    return index == null ? null :\n" + "        DisplayLevel.forIndex(index.intValue());\n" + "  }\n" + "\n");
        w.write("\n \n");
        w.write("  public void set" + suffix + "(" + type + " cooked)\n" + "      throws AccessPoemException {\n" + "    set" + suffix + "Index(cooked == null ? null : cooked.index);\n" + "  }\n");
    }
}
