public class TransformBase64Decode extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_BASE64_DECODE;
   protected String engineGetURI() {
      return TransformBase64Decode.implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform
        (XMLSignatureInput input, Transform _transformObject)
           throws IOException, CanonicalizationException,
                  TransformationException {
        return enginePerformTransform(input, null, _transformObject);
   }
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input,
            OutputStream os, Transform _transformObject)
    throws IOException, CanonicalizationException,
           TransformationException {
         try {
      if (input.isElement()) {
         Node el=input.getSubNode();
         if (input.getSubNode().getNodeType()==Node.TEXT_NODE) {
            el=el.getParentNode();
         }
         StringBuffer sb=new StringBuffer();
         traverseElement((Element)el,sb);
         if (os==null) {
                byte[] decodedBytes = Base64.decode(sb.toString());
                return new XMLSignatureInput(decodedBytes);
         }
                Base64.decode(sb.toString(),os);
            XMLSignatureInput output=new XMLSignatureInput((byte[])null);
            output.setOutputStream(os);
            return output;
      }
      if (input.isOctetStream() || input.isNodeSet()) {
        if (os==null) {
            byte[] base64Bytes = input.getBytes();
            byte[] decodedBytes = Base64.decode(base64Bytes);
            return new XMLSignatureInput(decodedBytes);
         }
        if (input.isByteArray() || input.isNodeSet()) {
               Base64.decode(input.getBytes(),os);
        } else {
            Base64.decode(new BufferedInputStream(input.getOctetStreamReal())
                    ,os);
        }
            XMLSignatureInput output=new XMLSignatureInput((byte[])null);
            output.setOutputStream(os);
            return output;
      }
         try {
            Document doc =
               DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                  input.getOctetStream());
            Element rootNode = doc.getDocumentElement();
            StringBuffer sb = new StringBuffer();
            traverseElement(rootNode,sb);
            byte[] decodedBytes = Base64.decode(sb.toString());
            return new XMLSignatureInput(decodedBytes);
                  } catch (ParserConfigurationException e) {
                          throw new TransformationException("c14n.Canonicalizer.Exception",e);
                  } catch (SAXException e) {
                          throw new TransformationException("SAX exception", e);
                  }
        } catch (Base64DecodingException e) {
        throw new TransformationException("Base64Decoding", e);
        }
   }
   void traverseElement(org.w3c.dom.Element node,StringBuffer sb) {
            Node sibling=node.getFirstChild();
        while (sibling!=null) {
                switch (sibling.getNodeType()) {
                        case Node.ELEMENT_NODE:
                    traverseElement((Element)sibling,sb);
                    break;
               case Node.TEXT_NODE:
                    sb.append(((Text)sibling).getData());
            }
            sibling=sibling.getNextSibling();
        }
   }
}
