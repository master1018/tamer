    public void generateBaseMethods(Writer w) throws IOException {
        super.generateBaseMethods(w);
        String targetTableAccessorMethod = "get" + type + "Table";
        String targetSuffix = type;
        String db = "get" + table.dsd.databaseTablesClass + "()";
        w.write("\n /**\n" + "  * Retrieves the Table Row Object ID. \n" + "  *\n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer read access rights \n" + "  * @return the TROID as an <code>Integer</code> \n" + "  */\n");
        w.write("\n" + "  public Integer get" + suffix + "Troid()\n" + "      throws AccessPoemException {\n" + "    readLock();\n" + "    return get" + suffix + "_unsafe();\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Sets the Table Row Object ID. \n" + "  * \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @param raw  a Table Row Object Id \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer write access rights\n" + "  */\n");
        w.write("  public void set" + suffix + "Troid(Integer raw)\n" + "      throws AccessPoemException {\n" + "    set" + suffix + "(" + "raw == null ? null : \n" + "        " + targetCast() + db + "." + targetTableAccessorMethod + "()." + "get" + targetSuffix + "Object(raw));\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Retrieves the <code>" + suffix + "</code> object reffered to.\n" + "  *  \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer read access rights \n" + "  * @throws NoSuchRowPoemException  \n" + "  *         if the <Persistent</code> has yet " + "to be allocated a TROID \n" + "  * @return the <code>" + suffix + "</code> as a <code>" + type + "</code> \n" + "  */\n");
        w.write("  public " + type + " get" + suffix + "()\n" + "      throws AccessPoemException, NoSuchRowPoemException {\n" + "    Integer troid = get" + suffix + "Troid();\n" + "    return troid == null ? null :\n" + "        " + targetCast() + db + "." + targetTableAccessorMethod + "()." + "get" + targetSuffix + "Object(troid);\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Set the " + suffix + ".\n" + "  * \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @param cooked  a validated <code>" + type + "</code>\n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer write access rights \n" + "  */\n");
        w.write("  public void set" + suffix + "(" + type + " cooked)\n" + "      throws AccessPoemException {\n" + "    _" + tableAccessorMethod + "().\n" + "      get" + suffix + "Column().\n" + "        getType().assertValidCooked(cooked);\n" + "    writeLock();\n" + "    if (cooked == null)\n" + "      set" + suffix + "_unsafe(null);\n" + "    else {\n" + "      cooked.existenceLock();\n" + "      set" + suffix + "_unsafe(cooked.troid());\n" + "    }\n" + "  }\n");
    }