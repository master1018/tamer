public class XmlStreamResultHandler implements ResultHandler {
    public void handle(ResultSet rs, Result res) throws SQLException, XBoundException {
        XmlStreamResult result = (XmlStreamResult) res;
        OutputFormat of = new OutputFormat();
        Writer writer = result.getWriter();
        OutputStream outputstream = result.getOutputStream();
        AttributesImpl attrs = null;
        String namespaceURI = result.getNamespaceURI();
        String namespacePrefix = result.getNamespacePrefix();
        Locale locale = result.getLocale();
        of.setIndenting(true);
        of.setIndent(5);
        XMLSerializer ser = new XMLSerializer();
        ser.setOutputFormat(of);
        if (writer != null) ser.setOutputCharStream(writer); else ser.setOutputByteStream(outputstream);
        ser.setNamespaces(true);
        try {
            ser.startDocument();
            ser.startPrefixMapping(namespacePrefix, namespaceURI);
            attrs = new AttributesImpl();
            attrs.addAttribute(namespaceURI, "locale", namespacePrefix + ":locale", "", locale.toString());
            ser.startElement(namespaceURI, "rowset", namespacePrefix + ":rowset", attrs);
            int rowNum = 0;
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                rowNum++;
                attrs = new AttributesImpl();
                attrs.addAttribute(namespaceURI, "num", namespacePrefix + ":num", "", "" + rowNum);
                ser.startElement(namespaceURI, "row", namespacePrefix + ":row", attrs);
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    attrs = new AttributesImpl();
                    attrs.addAttribute(namespaceURI, "name", namespacePrefix + ":name", "", rsmd.getColumnName(i));
                    attrs.addAttribute(namespaceURI, "type", namespacePrefix + ":type", "", "" + rsmd.getColumnType(i));
                    ser.startElement(namespaceURI, "column", namespacePrefix + ":column", attrs);
                    Object value = rs.getObject(i);
                    String strValue = null;
                    if (value != null) {
                        switch(rsmd.getColumnType(i)) {
                            case Types.DATE:
                                strValue = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale).format(value);
                                break;
                            case Types.DECIMAL:
                            case Types.NUMERIC:
                                strValue = DecimalFormat.getInstance(locale).format(value);
                                break;
                            default:
                                strValue = value.toString();
                        }
                        ser.characters(strValue.toCharArray(), 0, strValue.length());
                    }
                    ser.endElement(namespaceURI, "column", namespacePrefix + ":column");
                }
                ser.endElement(namespaceURI, "row", namespacePrefix + ":row");
            }
            ser.endElement(namespaceURI, "rowset", namespacePrefix + ":rowset");
            ser.endDocument();
        } catch (SAXException e) {
            throw new XBoundException(e);
        }
    }
}
