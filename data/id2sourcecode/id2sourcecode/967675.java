    public void fill(String src, String dest, String user) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        FileOutputStream writer = new FileOutputStream(dest);
        PdfStamper stamper = new PdfStamper(reader, writer);
        stamper.setEncryption(true, "", "Gu7ruc*YAWaStEbr", 0);
        AcroFields fields = stamper.getAcroFields();
        Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
        font.setSize((float) 20.2);
        BaseFont baseFont = font.getBaseFont();
        fields.setFieldProperty("Sch1531Line21", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line21", "textfont", baseFont, null);
        fields.setField("Sch1531Line21", this.get_Sch1531Line21());
        fields.setFieldProperty("Sch1531Line2b", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line2b", "textfont", baseFont, null);
        fields.setField("Sch1531Line2b", this.get_Sch1531Line2b());
        fields.setFieldProperty("Sch1531Line17", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line17", "textfont", baseFont, null);
        fields.setField("Sch1531Line17", this.get_Sch1531Line17());
        fields.setFieldProperty("Sch1531Line10", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line10", "textfont", baseFont, null);
        fields.setField("Sch1531Line10", this.get_Sch1531Line10());
        fields.setFieldProperty("Sch1531Line13b", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line13b", "textfont", baseFont, null);
        fields.setField("Sch1531Line13b", this.get_Sch1531Line13b());
        fields.setFieldProperty("Sch1531Line22", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line22", "textfont", baseFont, null);
        fields.setField("Sch1531Line22", this.get_Sch1531Line22());
        fields.setFieldProperty("Sch1531Line5a", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line5a", "textfont", baseFont, null);
        fields.setField("Sch1531Line5a", this.get_Sch1531Line5a());
        fields.setFieldProperty("Sch1531Line3", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line3", "textfont", baseFont, null);
        fields.setField("Sch1531Line3", this.get_Sch1531Line3());
        fields.setFieldProperty("Sch1531Line19", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line19", "textfont", baseFont, null);
        fields.setField("Sch1531Line19", this.get_Sch1531Line19());
        fields.setFieldProperty("Sch1531Line12", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line12", "textfont", baseFont, null);
        fields.setField("Sch1531Line12", this.get_Sch1531Line12());
        fields.setFieldProperty("Sch1531Line4", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line4", "textfont", baseFont, null);
        fields.setField("Sch1531Line4", this.get_Sch1531Line4());
        fields.setFieldProperty("Sch1531Line2a", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line2a", "textfont", baseFont, null);
        fields.setField("Sch1531Line2a", this.get_Sch1531Line2a());
        fields.setFieldProperty("Sch1531Line13a", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line13a", "textfont", baseFont, null);
        fields.setField("Sch1531Line13a", this.get_Sch1531Line13a());
        fields.setFieldProperty("Sch1531Line18", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line18", "textfont", baseFont, null);
        fields.setField("Sch1531Line18", this.get_Sch1531Line18());
        fields.setFieldProperty("Sch1531Line1", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line1", "textfont", baseFont, null);
        fields.setField("Sch1531Line1", this.get_Sch1531Line1());
        fields.setFieldProperty("Sch1531Line5b", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line5b", "textfont", baseFont, null);
        fields.setField("Sch1531Line5b", this.get_Sch1531Line5b());
        fields.setFieldProperty("Sch1531Line16", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line16", "textfont", baseFont, null);
        fields.setField("Sch1531Line16", this.get_Sch1531Line16());
        fields.setFieldProperty("Sch1531Line5e", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line5e", "textfont", baseFont, null);
        fields.setField("Sch1531Line5e", this.get_Sch1531Line5e());
        fields.setFieldProperty("Sch1531Line9", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line9", "textfont", baseFont, null);
        fields.setField("Sch1531Line9", this.get_Sch1531Line9());
        fields.setFieldProperty("Sch1531Line13c", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line13c", "textfont", baseFont, null);
        fields.setField("Sch1531Line13c", this.get_Sch1531Line13c());
        fields.setFieldProperty("Sch1531Line11", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line11", "textfont", baseFont, null);
        fields.setField("Sch1531Line11", this.get_Sch1531Line11());
        fields.setFieldProperty("Sch1531Line6", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line6", "textfont", baseFont, null);
        fields.setField("Sch1531Line6", this.get_Sch1531Line6());
        fields.setFieldProperty("Sch1531Line15", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line15", "textfont", baseFont, null);
        fields.setField("Sch1531Line15", this.get_Sch1531Line15());
        fields.setFieldProperty("Sch1531Line8", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line8", "textfont", baseFont, null);
        fields.setField("Sch1531Line8", this.get_Sch1531Line8());
        fields.setFieldProperty("Sch1531Line5d", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line5d", "textfont", baseFont, null);
        fields.setField("Sch1531Line5d", this.get_Sch1531Line5d());
        fields.setFieldProperty("Sch1531Line20", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line20", "textfont", baseFont, null);
        fields.setField("Sch1531Line20", this.get_Sch1531Line20());
        fields.setFieldProperty("Sch1531Line14", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line14", "textfont", baseFont, null);
        fields.setField("Sch1531Line14", this.get_Sch1531Line14());
        fields.setFieldProperty("Sch1531Line7", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line7", "textfont", baseFont, null);
        fields.setField("Sch1531Line7", this.get_Sch1531Line7());
        fields.setFieldProperty("Sch1531Line5c", "textsize", new Float(20.2), null);
        fields.setFieldProperty("Sch1531Line5c", "textfont", baseFont, null);
        fields.setField("Sch1531Line5c", this.get_Sch1531Line5c());
        stamper.setFormFlattening(true);
        stamper.setFullCompression();
        for (int i = 0; i < reader.getNumberOfPages() + 1; i++) {
            PdfContentByte overContent = stamper.getOverContent(i);
            if (overContent != null) {
                overContent.beginText();
                font = FontFactory.getFont(FontFactory.TIMES_ITALIC);
                font.setColor(BaseColor.BLUE);
                baseFont = font.getBaseFont();
                overContent.setColorFill(BaseColor.BLUE);
                overContent.setFontAndSize(baseFont, 24);
                overContent.showTextAligned(Element.ALIGN_RIGHT | Element.ALIGN_TOP, "Electronically filed via Modernized eFile", 20, 175, 90);
                overContent.endText();
                overContent.beginText();
                font = FontFactory.getFont(FontFactory.TIMES);
                font.setColor(BaseColor.RED);
                baseFont = font.getBaseFont();
                overContent.setColorFill(BaseColor.RED);
                overContent.setFontAndSize(baseFont, 8);
                overContent.showTextAligned(Element.ALIGN_CENTER | Element.ALIGN_BOTTOM, "Retrieved by " + user + " on " + new Date().toString(), 220, 3, 0);
                overContent.endText();
            }
        }
        stamper.close();
        reader.close();
    }