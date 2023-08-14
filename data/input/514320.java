public class DexExternalizerTest {
    @Test
    public void testExternalizer() throws IOException{
        DexToSigConverter converter = new DexToSigConverter();
        IApi api = converter.convertApi("Dex Tests", DexUtil.getDexFiles(new HashSet<String>(Arrays.asList(new String[]{"resources/javaCore.dex"}))), Visibility.PRIVATE);
        System.setProperty("sun.io.serialization.extendedDebugInfo", "true");
        IApiExternalizer externalizer = new BinaryApi();
        externalizer.externalizeApi("dex-spec", api);
    }
}
