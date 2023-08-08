public class XSLTTransformer {
	public void transform() {
		try {
			TransformerFactory  tFactory =  TransformerFactory.newInstance();
			Source xslSource = new StreamSource( "src/xml/applicantprofileTransform.xsl" );
			Transformer transformer = tFactory.newTransformer( xslSource );
			transformer.transform( new StreamSource( "src/xml/resume.xml" ),new StreamResult( new FileOutputStream( "src/xml/userProfile.xml" )));
		}catch(TransformerFactoryConfigurationError | FileNotFoundException | TransformerException ex) {}
	}
}
