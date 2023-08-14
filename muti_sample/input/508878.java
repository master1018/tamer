public class LargeDexTests extends DexTestsCommon{
    @Test
    public void testManyFields() throws IOException{
        String CLASS_NAME = "L0";
        int NR_OF_FIELDS = 10000;
        StringBuilder b = new StringBuilder();
        b.append("public class ").append(CLASS_NAME).append("{\n");
        for (int i = 0; i < NR_OF_FIELDS; i++) {
            b.append("\tpublic int f").append(i).append(";\n");
        }
        b.append("}\n");
        JavaSource source = new JavaSource(CLASS_NAME, b.toString());
        DexFile dexFile = javaToDexUtil.getFrom(source);
        assertEquals(1, dexFile.getDefinedClasses().size());
        DexClass clazz = dexFile.getDefinedClasses().get(0);
        assertEquals("LL0;", clazz.getName());
        assertPublic(clazz);
        assertEquals(NR_OF_FIELDS, clazz.getFields().size());
    }
}
