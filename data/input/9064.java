public class TransformXSLT extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_XSLT;
   static final String XSLTSpecNS              = "http:
   static final String defaultXSLTSpecNSprefix = "xslt";
   static final String XSLTSTYLESHEET          = "stylesheet";
   private static Class xClass = null;
   static {
      try {
         xClass = Class.forName("javax.xml.XMLConstants");
      } catch (Exception e) {}
   }
   static java.util.logging.Logger log =
      java.util.logging.Logger.getLogger(
         TransformXSLT.class.getName());
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform
        (XMLSignatureInput input, Transform _transformObject)
           throws IOException,
                  TransformationException {
        return enginePerformTransform(input, null, _transformObject);
   }
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input,OutputStream baos, Transform _transformObject)
    throws IOException,
           TransformationException {
      if (xClass == null) {
         Object exArgs[] = { "SECURE_PROCESSING_FEATURE not supported" };
         throw new TransformationException("generic.EmptyMessage", exArgs);
      }
      try {
         Element transformElement = _transformObject.getElement();
         Element _xsltElement =
            XMLUtils.selectNode(transformElement.getFirstChild(),
                                                XSLTSpecNS,"stylesheet", 0);
         if (_xsltElement == null) {
            Object exArgs[] = { "xslt:stylesheet", "Transform" };
            throw new TransformationException("xml.WrongContent", exArgs);
         }
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Class c = tFactory.getClass();
         Method m = c.getMethod("setFeature", new Class[] {String.class, boolean.class});
         m.invoke(tFactory, new Object[] {"http:
         Source xmlSource =
            new StreamSource(new ByteArrayInputStream(input.getBytes()));
         Source stylesheet;
         {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(_xsltElement);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
            stylesheet =
               new StreamSource(new ByteArrayInputStream(os.toByteArray()));
         }
         Transformer transformer = tFactory.newTransformer(stylesheet);
         try {
            transformer.setOutputProperty
               ("{http:
         } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, "Unable to set Xalan line-separator property: "
               + e.getMessage());
         }
         if (baos==null) {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            StreamResult outputTarget = new StreamResult(baos1);
            transformer.transform(xmlSource, outputTarget);
            return new XMLSignatureInput(baos1.toByteArray());
         }
         StreamResult outputTarget = new StreamResult(baos);
         transformer.transform(xmlSource, outputTarget);
         XMLSignatureInput output=new XMLSignatureInput((byte[])null);
         output.setOutputStream(baos);
         return output;
      } catch (XMLSecurityException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      } catch (TransformerConfigurationException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      } catch (TransformerException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      } catch (NoSuchMethodException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      } catch (IllegalAccessException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      } catch (java.lang.reflect.InvocationTargetException ex) {
         Object exArgs[] = { ex.getMessage() };
         throw new TransformationException("generic.EmptyMessage", exArgs, ex);
      }
   }
}
