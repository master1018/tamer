public class ExportSubtree {
   public static void main(String[] args) throws Exception {
      try
      {
          ByteArrayInputStream bais = new ByteArrayInputStream(importPrefs.getBytes("utf-8"));
          Preferences.importPreferences(bais);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          Preferences.userRoot().node("testExportSubtree").exportSubtree(baos);
          Preferences.userRoot().node("testExportSubtree").removeNode();
          if (!expectedResult.equals(baos.toString())) {
              throw new IOException("exportSubtree does not output expected result");
          }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
   }
   static String ls = System.getProperty("line.separator");
   static String importPrefs =
       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<!DOCTYPE preferences SYSTEM \"http:
        + "<preferences EXTERNAL_XML_VERSION=\"1.0\">"
        + "  <root type=\"user\">"
        + "    <map>"
        + "      <entry key=\"key1\" value=\"value1\"/>"
        + "    </map>"
        + "    <node name=\"testExportSubtree\">"
        + "      <map>"
        + "        <entry key=\"key2\" value=\"value2\"/>"
        + "      </map>"
        + "      <node name=\"test\">"
        + "        <map>"
        + "          <entry key=\"key3\" value=\"value3\"/>"
        + "        </map>"
        + "      </node>"
        + "    </node>"
        + "  </root>"
        + "</preferences>";
   static String expectedResult =
       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + ls    +  "<!DOCTYPE preferences SYSTEM \"http:
        + ls    +  "<preferences EXTERNAL_XML_VERSION=\"1.0\">"
        + ls    +  "  <root type=\"user\">"
        + ls    +  "    <map/>"
        + ls    +  "    <node name=\"testExportSubtree\">"
        + ls    +  "      <map>"
        + ls    +  "        <entry key=\"key2\" value=\"value2\"/>"
        + ls    +  "      </map>"
        + ls    +  "      <node name=\"test\">"
        + ls    +  "        <map>"
        + ls    +  "          <entry key=\"key3\" value=\"value3\"/>"
        + ls    +  "        </map>"
        + ls    +  "      </node>"
        + ls    +  "    </node>"
        + ls    +  "  </root>"
        + ls    +  "</preferences>"     + ls;
}
