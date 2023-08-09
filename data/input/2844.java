public class Reference extends SignatureElementProxy {
   private static boolean useC14N11 =
      AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
         public Boolean run() {
            return Boolean.getBoolean
               ("com.sun.org.apache.xml.internal.security.useC14N11");
         }
      });
   public final static boolean CacheSignedNodes = false;
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(Reference.class.getName());
   public static final String OBJECT_URI = Constants.SignatureSpecNS
                                           + Constants._TAG_OBJECT;
   public static final String MANIFEST_URI = Constants.SignatureSpecNS
                                             + Constants._TAG_MANIFEST;
   Manifest _manifest = null;
   XMLSignatureInput _transformsOutput;
private Transforms transforms;
private Element digestMethodElem;
private Element digestValueElement;
   protected Reference(Document doc, String BaseURI, String ReferenceURI, Manifest manifest, Transforms transforms, String messageDigestAlgorithm)
           throws XMLSignatureException {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      this._baseURI = BaseURI;
      this._manifest = manifest;
      this.setURI(ReferenceURI);
      if (transforms != null) {
          this.transforms=transforms;
         this._constructionElement.appendChild(transforms.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
      }
      {
         MessageDigestAlgorithm mda =
            MessageDigestAlgorithm.getInstance(this._doc,
                                               messageDigestAlgorithm);
         digestMethodElem=mda.getElement();
         this._constructionElement.appendChild(digestMethodElem);
         XMLUtils.addReturnToElement(this._constructionElement);
      }
      {
         digestValueElement =
            XMLUtils.createElementInSignatureSpace(this._doc,
                                                   Constants._TAG_DIGESTVALUE);
         this._constructionElement.appendChild(digestValueElement);
         XMLUtils.addReturnToElement(this._constructionElement);
      }
   }
   protected Reference(Element element, String BaseURI, Manifest manifest)
           throws XMLSecurityException {
      super(element, BaseURI);
      this._baseURI=BaseURI;
      Element el=XMLUtils.getNextElement(element.getFirstChild());
      if (Constants._TAG_TRANSFORMS.equals(el.getLocalName()) &&
                  Constants.SignatureSpecNS.equals(el.getNamespaceURI())) {
          transforms = new Transforms(el,this._baseURI);
          el=XMLUtils.getNextElement(el.getNextSibling());
      }
      digestMethodElem = el;
      digestValueElement =XMLUtils.getNextElement(digestMethodElem.getNextSibling());;
      this._manifest = manifest;
   }
   public MessageDigestAlgorithm getMessageDigestAlgorithm()
           throws XMLSignatureException {
      if (digestMethodElem == null) {
         return null;
      }
      String uri = digestMethodElem.getAttributeNS(null,
         Constants._ATT_ALGORITHM);
          if (uri == null) {
                  return null;
          }
      return MessageDigestAlgorithm.getInstance(this._doc, uri);
   }
   public void setURI(String URI) {
      if ( URI != null) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_URI,
                                                  URI);
      }
   }
   public String getURI() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_URI);
   }
   public void setId(String Id) {
      if ( Id != null ) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ID, Id);
         IdResolver.registerElementById(this._constructionElement, Id);
      }
   }
   public String getId() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ID);
   }
   public void setType(String Type) {
      if (Type != null) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_TYPE,
                                                  Type);
      }
   }
   public String getType() {
      return this._constructionElement.getAttributeNS(null,
              Constants._ATT_TYPE);
   }
   public boolean typeIsReferenceToObject() {
      if (Reference.OBJECT_URI.equals(this.getType())) {
         return true;
      }
      return false;
   }
   public boolean typeIsReferenceToManifest() {
      if (Reference.MANIFEST_URI.equals(this.getType())) {
         return true;
      }
      return false;
   }
   private void setDigestValueElement(byte[] digestValue)
   {
         Node n=digestValueElement.getFirstChild();
         while (n!=null) {
               digestValueElement.removeChild(n);
               n = n.getNextSibling();
         }
         String base64codedValue = Base64.encode(digestValue);
         Text t = this._doc.createTextNode(base64codedValue);
         digestValueElement.appendChild(t);
   }
   public void generateDigestValue()
           throws XMLSignatureException, ReferenceNotInitializedException {
      this.setDigestValueElement(this.calculateDigest(false));
   }
   public XMLSignatureInput getContentsBeforeTransformation()
           throws ReferenceNotInitializedException {
      try {
         Attr URIAttr = this._constructionElement.getAttributeNodeNS(null,
            Constants._ATT_URI);
         String URI;
         if (URIAttr == null) {
            URI = null;
         } else {
            URI = URIAttr.getNodeValue();
         }
         ResourceResolver resolver = ResourceResolver.getInstance(URIAttr,
            this._baseURI, this._manifest._perManifestResolvers);
         if (resolver == null) {
            Object exArgs[] = { URI };
            throw new ReferenceNotInitializedException(
               "signature.Verification.Reference.NoInput", exArgs);
         }
         resolver.addProperties(this._manifest._resolverProperties);
         XMLSignatureInput input = resolver.resolve(URIAttr, this._baseURI);
         return input;
      }  catch (ResourceResolverException ex) {
         throw new ReferenceNotInitializedException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new ReferenceNotInitializedException("empty", ex);
      }
   }
   public XMLSignatureInput getTransformsInput() throws ReferenceNotInitializedException
        {
                XMLSignatureInput input=getContentsBeforeTransformation();
                XMLSignatureInput result;
                try {
                        result = new XMLSignatureInput(input.getBytes());
                } catch (CanonicalizationException ex) {
                         throw new ReferenceNotInitializedException("empty", ex);
                } catch (IOException ex) {
                         throw new ReferenceNotInitializedException("empty", ex);
                }
                result.setSourceURI(input.getSourceURI());
                return result;
   }
   private XMLSignatureInput getContentsAfterTransformation(XMLSignatureInput input, OutputStream os)
           throws XMLSignatureException {
      try {
         Transforms transforms = this.getTransforms();
         XMLSignatureInput output = null;
         if (transforms != null) {
            output = transforms.performTransforms(input,os);
            this._transformsOutput = output;
         } else {
            output = input;
         }
         return output;
      } catch (ResourceResolverException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (CanonicalizationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidCanonicalizerException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (TransformationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public XMLSignatureInput getContentsAfterTransformation()
           throws XMLSignatureException {
      XMLSignatureInput input = this.getContentsBeforeTransformation();
      return this.getContentsAfterTransformation(input, null);
   }
   public XMLSignatureInput getNodesetBeforeFirstCanonicalization()
           throws XMLSignatureException {
      try {
         XMLSignatureInput input = this.getContentsBeforeTransformation();
         XMLSignatureInput output = input;
         Transforms transforms = this.getTransforms();
         if (transforms != null) {
            doTransforms: for (int i = 0; i < transforms.getLength(); i++) {
               Transform t = transforms.item(i);
               String URI = t.getURI();
               if (URI.equals(Transforms
                       .TRANSFORM_C14N_EXCL_OMIT_COMMENTS) || URI
                          .equals(Transforms
                             .TRANSFORM_C14N_EXCL_WITH_COMMENTS) || URI
                                .equals(Transforms
                                   .TRANSFORM_C14N_OMIT_COMMENTS) || URI
                                      .equals(Transforms
                                         .TRANSFORM_C14N_WITH_COMMENTS)) {
                  break doTransforms;
               }
               output = t.performTransform(output, null);
            }
            output.setSourceURI(input.getSourceURI());
         }
         return output;
      } catch (IOException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (ResourceResolverException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (CanonicalizationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidCanonicalizerException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (TransformationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public String getHTMLRepresentation() throws XMLSignatureException {
      try {
         XMLSignatureInput nodes = this.getNodesetBeforeFirstCanonicalization();
         Set inclusiveNamespaces = new HashSet();
         {
            Transforms transforms = this.getTransforms();
            Transform c14nTransform = null;
            if (transforms != null) {
               doTransforms: for (int i = 0; i < transforms.getLength(); i++) {
                  Transform t = transforms.item(i);
                  String URI = t.getURI();
                  if (URI.equals(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS)
                          || URI.equals(
                             Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS)) {
                     c14nTransform = t;
                     break doTransforms;
                  }
               }
            }
            if (c14nTransform != null) {
               if (c14nTransform
                       .length(InclusiveNamespaces
                          .ExclusiveCanonicalizationNamespace, InclusiveNamespaces
                          ._TAG_EC_INCLUSIVENAMESPACES) == 1) {
                  InclusiveNamespaces in = new InclusiveNamespaces(
                        XMLUtils.selectNode(
                        c14nTransform.getElement().getFirstChild(),
                                                InclusiveNamespaces.ExclusiveCanonicalizationNamespace,
                        InclusiveNamespaces._TAG_EC_INCLUSIVENAMESPACES,0), this.getBaseURI());
                  inclusiveNamespaces = InclusiveNamespaces.prefixStr2Set(
                     in.getInclusiveNamespaces());
               }
            }
         }
         return nodes.getHTMLRepresentation(inclusiveNamespaces);
      } catch (TransformationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidTransformException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public XMLSignatureInput getTransformsOutput() {
      return this._transformsOutput;
   }
   protected XMLSignatureInput dereferenceURIandPerformTransforms(OutputStream os)
           throws XMLSignatureException {
      try {
         XMLSignatureInput input = this.getContentsBeforeTransformation();
         XMLSignatureInput output = this.getContentsAfterTransformation(input, os);
         if (!Reference.CacheSignedNodes) {
            this._transformsOutput = output;
         }
         return output;
      } catch (XMLSecurityException ex) {
         throw new ReferenceNotInitializedException("empty", ex);
      }
   }
   public Transforms getTransforms()
           throws XMLSignatureException, InvalidTransformException,
                  TransformationException, XMLSecurityException {
      return transforms;
   }
   public byte[] getReferencedBytes()
           throws ReferenceNotInitializedException, XMLSignatureException {
    try {
        XMLSignatureInput output=this.dereferenceURIandPerformTransforms(null);
        byte[] signedBytes = output.getBytes();
        return signedBytes;
     } catch (IOException ex) {
        throw new ReferenceNotInitializedException("empty", ex);
     } catch (CanonicalizationException ex) {
        throw new ReferenceNotInitializedException("empty", ex);
     }
   }
   private byte[] calculateDigest(boolean validating)
           throws ReferenceNotInitializedException, XMLSignatureException {
      try {
         MessageDigestAlgorithm mda = this.getMessageDigestAlgorithm();
         mda.reset();
         DigesterOutputStream diOs=new DigesterOutputStream(mda);
         OutputStream os=new UnsyncBufferedOutputStream(diOs);
         XMLSignatureInput output=this.dereferenceURIandPerformTransforms(os);
         if (this.useC14N11 && !validating &&
             !output.isOutputStreamSet() && !output.isOctetStream()) {
             if (transforms == null) {
                 transforms = new Transforms(this._doc);
                 this._constructionElement.insertBefore
                     (transforms.getElement(), digestMethodElem);
             }
             transforms.addTransform(Transforms.TRANSFORM_C14N11_OMIT_COMMENTS);
             output.updateOutputStream(os, true);
         } else {
             output.updateOutputStream(os);
         }
         os.flush();
         return diOs.getDigestValue();
      } catch (XMLSecurityException ex) {
         throw new ReferenceNotInitializedException("empty", ex);
      } catch (IOException ex) {
         throw new ReferenceNotInitializedException("empty", ex);
      }
   }
   public byte[] getDigestValue() throws Base64DecodingException, XMLSecurityException {
      if (digestValueElement == null) {
                  Object[] exArgs ={ Constants._TAG_DIGESTVALUE,
                                                         Constants.SignatureSpecNS };
                  throw new XMLSecurityException(
                                        "signature.Verification.NoSignatureElement",
                                        exArgs);
          }
      byte[] elemDig = Base64.decode(digestValueElement);
      return elemDig;
   }
   public boolean verify()
           throws ReferenceNotInitializedException, XMLSecurityException {
      byte[] elemDig = this.getDigestValue();
      byte[] calcDig = this.calculateDigest(true);
      boolean equal = MessageDigestAlgorithm.isEqual(elemDig, calcDig);
      if (!equal) {
         log.log(java.util.logging.Level.WARNING, "Verification failed for URI \"" + this.getURI() + "\"");
         log.log(java.util.logging.Level.WARNING, "Expected Digest: " + Base64.encode(elemDig));
         log.log(java.util.logging.Level.WARNING, "Actual Digest: " + Base64.encode(calcDig));
      } else {
         log.log(java.util.logging.Level.INFO, "Verification successful for URI \"" + this.getURI() + "\"");
      }
      return equal;
   }
   public String getBaseLocalName() {
      return Constants._TAG_REFERENCE;
   }
}
