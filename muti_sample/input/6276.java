public class CommentsInXml {
    public static void main(String[] argv) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(new String(
            "<!DOCTYPE preferences SYSTEM                          " +
            "\"http:
            "<preferences EXTERNAL_XML_VERSION=\"1.0\">            " +
            "  <root type=\"user\">                                " +
            "    <map>                                             " +
            "    </map>                                            " +
            "    <node name=\"hlrAgent\"> <!-- HLR Agent -->       " +
            "      <map>                                           " +
            "        <entry key=\"agentName\" value=\"HLRAgent\" />" +
            "      </map>                                          " +
            "    </node>                                           " +
            "  </root>                                             " +
            "</preferences>                                        "
        ).getBytes());
        Preferences ur = Preferences.userRoot();
        ur.importPreferences(new ByteArrayInputStream(bos.toByteArray()));
        ur.node("hlrAgent").removeNode(); 
    }
}
