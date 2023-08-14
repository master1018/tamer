public class RFC2253Parser {
   static boolean _TOXML = true;
   public static String rfc2253toXMLdsig(String dn) {
      _TOXML = true;
      String normalized = normalize(dn);
      return rfctoXML(normalized);
   }
   public static String xmldsigtoRFC2253(String dn) {
      _TOXML = false;
      String normalized = normalize(dn);
      return xmltoRFC(normalized);
   }
   public static String normalize(String dn) {
      if ((dn == null) || dn.equals("")) {
         return "";
      }
      try {
         String _DN = semicolonToComma(dn);
         StringBuffer sb = new StringBuffer();
         int i = 0;
         int l = 0;
         int k;
         for (int j = 0; (k = _DN.indexOf(",", j)) >= 0; j = k + 1) {
            l += countQuotes(_DN, j, k);
            if ((k > 0) && (_DN.charAt(k - 1) != '\\') && (l % 2) != 1) {
               sb.append(parseRDN(_DN.substring(i, k).trim()) + ",");
               i = k + 1;
               l = 0;
            }
         }
         sb.append(parseRDN(trim(_DN.substring(i))));
         return sb.toString();
      } catch (IOException ex) {
         return dn;
      }
   }
   static String parseRDN(String str) throws IOException {
      StringBuffer sb = new StringBuffer();
      int i = 0;
      int l = 0;
      int k;
      for (int j = 0; (k = str.indexOf("+", j)) >= 0; j = k + 1) {
         l += countQuotes(str, j, k);
         if ((k > 0) && (str.charAt(k - 1) != '\\') && (l % 2) != 1) {
            sb.append(parseATAV(trim(str.substring(i, k))) + "+");
            i = k + 1;
            l = 0;
         }
      }
      sb.append(parseATAV(trim(str.substring(i))));
      return sb.toString();
   }
   static String parseATAV(String str) throws IOException {
      int i = str.indexOf("=");
      if ((i == -1) || ((i > 0) && (str.charAt(i - 1) == '\\'))) {
         return str;
      }
      String attrType = normalizeAT(str.substring(0, i));
      String attrValue = null;
      if (attrType.charAt(0) >= '0' && attrType.charAt(0) <= '9') {
          attrValue = str.substring(i + 1);
      } else {
          attrValue = normalizeV(str.substring(i + 1));
      }
      return attrType + "=" + attrValue;
   }
   static String normalizeAT(String str) {
      String at = str.toUpperCase().trim();
      if (at.startsWith("OID")) {
         at = at.substring(3);
      }
      return at;
   }
   static String normalizeV(String str) throws IOException {
      String value = trim(str);
      if (value.startsWith("\"")) {
         StringBuffer sb = new StringBuffer();
         StringReader sr = new StringReader(value.substring(1,
                              value.length() - 1));
         int i = 0;
         char c;
         for (; (i = sr.read()) > -1; ) {
            c = (char) i;
            if ((c == ',') || (c == '=') || (c == '+') || (c == '<')
                    || (c == '>') || (c == '#') || (c == ';')) {
               sb.append('\\');
            }
            sb.append(c);
         }
         value = trim(sb.toString());
      }
      if (_TOXML == true) {
         if (value.startsWith("#")) {
            value = '\\' + value;
         }
      } else {
         if (value.startsWith("\\#")) {
            value = value.substring(1);
         }
      }
      return value;
   }
   static String rfctoXML(String string) {
      try {
         String s = changeLess32toXML(string);
         return changeWStoXML(s);
      } catch (Exception e) {
         return string;
      }
   }
   static String xmltoRFC(String string) {
      try {
         String s = changeLess32toRFC(string);
         return changeWStoRFC(s);
      } catch (Exception e) {
         return string;
      }
   }
   static String changeLess32toRFC(String string) throws IOException {
      StringBuffer sb = new StringBuffer();
      StringReader sr = new StringReader(string);
      int i = 0;
      char c;
      for (; (i = sr.read()) > -1; ) {
         c = (char) i;
         if (c == '\\') {
            sb.append(c);
            char c1 = (char) sr.read();
            char c2 = (char) sr.read();
            if ((((c1 >= 48) && (c1 <= 57)) || ((c1 >= 65) && (c1 <= 70)) || ((c1 >= 97) && (c1 <= 102)))
                    && (((c2 >= 48) && (c2 <= 57))
                        || ((c2 >= 65) && (c2 <= 70))
                        || ((c2 >= 97) && (c2 <= 102)))) {
               char ch = (char) Byte.parseByte("" + c1 + c2, 16);
               sb.append(ch);
            } else {
               sb.append(c1);
               sb.append(c2);
            }
         } else {
            sb.append(c);
         }
      }
      return sb.toString();
   }
   static String changeLess32toXML(String string) throws IOException {
      StringBuffer sb = new StringBuffer();
      StringReader sr = new StringReader(string);
      int i = 0;
      for (; (i = sr.read()) > -1; ) {
         if (i < 32) {
            sb.append('\\');
            sb.append(Integer.toHexString(i));
         } else {
            sb.append((char) i);
         }
      }
      return sb.toString();
   }
   static String changeWStoXML(String string) throws IOException {
      StringBuffer sb = new StringBuffer();
      StringReader sr = new StringReader(string);
      int i = 0;
      char c;
      for (; (i = sr.read()) > -1; ) {
         c = (char) i;
         if (c == '\\') {
            char c1 = (char) sr.read();
            if (c1 == ' ') {
               sb.append('\\');
               String s = "20";
               sb.append(s);
            } else {
               sb.append('\\');
               sb.append(c1);
            }
         } else {
            sb.append(c);
         }
      }
      return sb.toString();
   }
   static String changeWStoRFC(String string) {
      StringBuffer sb = new StringBuffer();
      int i = 0;
      int k;
      for (int j = 0; (k = string.indexOf("\\20", j)) >= 0; j = k + 3) {
         sb.append(trim(string.substring(i, k)) + "\\ ");
         i = k + 3;
      }
      sb.append(string.substring(i));
      return sb.toString();
   }
   static String semicolonToComma(String str) {
      return removeWSandReplace(str, ";", ",");
   }
   static String removeWhiteSpace(String str, String symbol) {
      return removeWSandReplace(str, symbol, symbol);
   }
   static String removeWSandReplace(String str, String symbol, String replace) {
      StringBuffer sb = new StringBuffer();
      int i = 0;
      int l = 0;
      int k;
      for (int j = 0; (k = str.indexOf(symbol, j)) >= 0; j = k + 1) {
         l += countQuotes(str, j, k);
         if ((k > 0) && (str.charAt(k - 1) != '\\') && (l % 2) != 1) {
            sb.append(trim(str.substring(i, k)) + replace);
            i = k + 1;
            l = 0;
         }
      }
      sb.append(trim(str.substring(i)));
      return sb.toString();
   }
   private static int countQuotes(String s, int i, int j) {
      int k = 0;
      for (int l = i; l < j; l++) {
         if (s.charAt(l) == '"') {
            k++;
         }
      }
      return k;
   }
   static String trim(String str) {
      String trimed = str.trim();
      int i = str.indexOf(trimed) + trimed.length();
      if ((str.length() > i) && trimed.endsWith("\\")
              &&!trimed.endsWith("\\\\")) {
         if (str.charAt(i) == ' ') {
            trimed = trimed + " ";
         }
      }
      return trimed;
   }
   public static void main(String[] args) throws Exception {
      testToXML("CN=\"Steve, Kille\",  O=Isode Limited, C=GB");
      testToXML("CN=Steve Kille    ,   O=Isode Limited,C=GB");
      testToXML("\\ OU=Sales+CN=J. Smith,O=Widget Inc.,C=US\\ \\ ");
      testToXML("CN=L. Eagle,O=Sue\\, Grabbit and Runn,C=GB");
      testToXML("CN=Before\\0DAfter,O=Test,C=GB");
      testToXML("CN=\"L. Eagle,O=Sue, = + < > # ;Grabbit and Runn\",C=GB");
      testToXML("1.3.6.1.4.1.1466.0=#04024869,O=Test,C=GB");
      {
         StringBuffer sb = new StringBuffer();
         sb.append('L');
         sb.append('u');
         sb.append('\uc48d');
         sb.append('i');
         sb.append('\uc487');
         String test7 = "SN=" + sb.toString();
         testToXML(test7);
      }
      testToRFC("CN=\"Steve, Kille\",  O=Isode Limited, C=GB");
      testToRFC("CN=Steve Kille    ,   O=Isode Limited,C=GB");
      testToRFC("\\20OU=Sales+CN=J. Smith,O=Widget Inc.,C=US\\20\\20 ");
      testToRFC("CN=L. Eagle,O=Sue\\, Grabbit and Runn,C=GB");
      testToRFC("CN=Before\\12After,O=Test,C=GB");
      testToRFC("CN=\"L. Eagle,O=Sue, = + < > # ;Grabbit and Runn\",C=GB");
      testToRFC("1.3.6.1.4.1.1466.0=\\#04024869,O=Test,C=GB");
      {
         StringBuffer sb = new StringBuffer();
         sb.append('L');
         sb.append('u');
         sb.append('\uc48d');
         sb.append('i');
         sb.append('\uc487');
         String test7 = "SN=" + sb.toString();
         testToRFC(test7);
      }
   }
   static int counter = 0;
   static void testToXML(String st) {
      System.out.println("start " + counter++ + ": " + st);
      System.out.println("         " + rfc2253toXMLdsig(st));
      System.out.println("");
   }
   static void testToRFC(String st) {
      System.out.println("start " + counter++ + ": " + st);
      System.out.println("         " + xmldsigtoRFC2253(st));
      System.out.println("");
   }
}
