public class test {
    private void addOriginSigsRels(String signatureZipEntryName, ZipOutputStream zipOutputStream) throws ParserConfigurationException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document originSignRelsDocument = documentBuilder.newDocument();
        Element relationshipsElement = originSignRelsDocument.createElementNS("http:
        relationshipsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns", "http:
        originSignRelsDocument.appendChild(relationshipsElement);
        Element relationshipElement = originSignRelsDocument.createElementNS("http:
        String relationshipId = "rel-" + UUID.randomUUID().toString();
        relationshipElement.setAttribute("Id", relationshipId);
        relationshipElement.setAttribute("Type", "http:
        String target = FilenameUtils.getName(signatureZipEntryName);
        LOG.debug("target: " + target);
        relationshipElement.setAttribute("Target", target);
        relationshipsElement.appendChild(relationshipElement);
        zipOutputStream.putNextEntry(new ZipEntry("_xmlsignatures/_rels/origin.sigs.rels"));
        writeDocumentNoClosing(originSignRelsDocument, zipOutputStream, false);
    }
}
