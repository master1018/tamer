public class ExportNode {
    public static void main(String[] args) throws
                                            BackingStoreException, IOException {
            Preferences N1 = Preferences.userRoot().node("ExportNodeTest1");
            N1.put("ExportNodeTestName1","ExportNodeTestValue1");
            Preferences N2 = N1.node("ExportNodeTest2");
            N2.put("ExportNodeTestName2","ExportNodeTestValue2");
            ByteArrayOutputStream exportStream = new ByteArrayOutputStream();
            N2.exportNode(exportStream);
            N1.removeNode();
            if (((exportStream.toString()).lastIndexOf("ExportNodeTestName2")== -1) ||
               ((exportStream.toString()).lastIndexOf("ExportNodeTestName1")!= -1)) {
            }
   }
}
