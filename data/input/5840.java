public class Manifest extends SignatureElementProxy {
  static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(Manifest.class.getName());
   List _references;
   Element[] _referencesEl;
   private boolean verificationResults[] = null;
   HashMap _resolverProperties = null;
   List _perManifestResolvers = null;
   public Manifest(Document doc) {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      this._references = new ArrayList();
   }
   public Manifest(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
      this._referencesEl = XMLUtils.selectDsNodes(this._constructionElement.getFirstChild(),
         Constants._TAG_REFERENCE);
      int le = this._referencesEl.length;
      {
         if (le == 0) {
            Object exArgs[] = { Constants._TAG_REFERENCE,
                                Constants._TAG_MANIFEST };
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                                   I18n.translate("xml.WrongContent", exArgs));
         }
      }
      this._references = new ArrayList(le);
      for (int i = 0; i < le; i++) {
         this._references.add(null);
      }
   }
   public void addDocument(
           String BaseURI, String referenceURI, Transforms transforms, String digestURI, String ReferenceId, String ReferenceType)
              throws XMLSignatureException {
         Reference ref = new Reference(this._doc, BaseURI, referenceURI, this,
                                       transforms, digestURI);
         if (ReferenceId != null) {
            ref.setId(ReferenceId);
         }
         if (ReferenceType != null) {
            ref.setType(ReferenceType);
         }
         this._references.add(ref);
         this._constructionElement.appendChild(ref.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void generateDigestValues()
           throws XMLSignatureException, ReferenceNotInitializedException {
         for (int i = 0; i < this.getLength(); i++) {
            Reference currentRef = (Reference) this._references.get(i);
            currentRef.generateDigestValue();
         }
   }
   public int getLength() {
      return this._references.size();
   }
   public Reference item(int i) throws XMLSecurityException {
         if (this._references.get(i) == null) {
            Reference ref = new Reference(_referencesEl[i], this._baseURI, this);
            this._references.set(i, ref);
         }
         return (Reference) this._references.get(i);
   }
   public void setId(String Id) {
      if (Id != null) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ID, Id);
         IdResolver.registerElementById(this._constructionElement, Id);
      }
   }
   public String getId() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ID);
   }
   public boolean verifyReferences()
           throws MissingResourceFailureException, XMLSecurityException {
      return this.verifyReferences(false);
   }
   public boolean verifyReferences(boolean followManifests)
           throws MissingResourceFailureException, XMLSecurityException {
      if (_referencesEl==null) {
        this._referencesEl =
            XMLUtils.selectDsNodes(this._constructionElement.getFirstChild(),
                         Constants._TAG_REFERENCE);
      }
          if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "verify " +_referencesEl.length + " References");
        log.log(java.util.logging.Level.FINE, "I am " + (followManifests
                           ? ""
                           : "not") + " requested to follow nested Manifests");
      }
      boolean verify = true;
      if (_referencesEl.length==0) {
         throw new XMLSecurityException("empty");
      }
      this.verificationResults =
         new boolean[_referencesEl.length];
      for (int i =
              0; i < this._referencesEl.length; i++) {
         Reference currentRef =
            new Reference(_referencesEl[i], this._baseURI, this);
         this._references.set(i, currentRef);
         try {
            boolean currentRefVerified = currentRef.verify();
            this.setVerificationResult(i, currentRefVerified);
            if (!currentRefVerified) {
               verify = false;
            }
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "The Reference has Type " + currentRef.getType());
            if (verify && followManifests
                    && currentRef.typeIsReferenceToManifest()) {
               log.log(java.util.logging.Level.FINE, "We have to follow a nested Manifest");
                try {
                  XMLSignatureInput signedManifestNodes =
                    currentRef.dereferenceURIandPerformTransforms(null);
                  Set nl = signedManifestNodes.getNodeSet();
                  Manifest referencedManifest = null;
                  Iterator nlIterator = nl.iterator();
                  findManifest: while (nlIterator.hasNext()) {
                     Node n = (Node) nlIterator.next();
                     if ((n.getNodeType() == Node.ELEMENT_NODE) && ((Element) n)
                             .getNamespaceURI()
                             .equals(Constants.SignatureSpecNS) && ((Element) n)
                             .getLocalName().equals(Constants._TAG_MANIFEST)) {
                        try {
                           referencedManifest =
                              new Manifest((Element) n,
                                           signedManifestNodes.getSourceURI());
                           break findManifest;
                        } catch (XMLSecurityException ex) {
                        }
                     }
                  }
                  if (referencedManifest == null) {
                     throw new MissingResourceFailureException("empty",
                                                               currentRef);
                  }
                  referencedManifest._perManifestResolvers =
                     this._perManifestResolvers;
                  referencedManifest._resolverProperties =
                     this._resolverProperties;
                  boolean referencedManifestValid =
                     referencedManifest.verifyReferences(followManifests);
                  if (!referencedManifestValid) {
                     verify = false;
                     log.log(java.util.logging.Level.WARNING, "The nested Manifest was invalid (bad)");
                  } else {
                     log.log(java.util.logging.Level.FINE, "The nested Manifest was valid (good)");
                  }
               } catch (IOException ex) {
                  throw new ReferenceNotInitializedException("empty", ex);
               } catch (ParserConfigurationException ex) {
                  throw new ReferenceNotInitializedException("empty", ex);
               } catch (SAXException ex) {
                  throw new ReferenceNotInitializedException("empty", ex);
               }
            }
         } catch (ReferenceNotInitializedException ex) {
            Object exArgs[] = { currentRef.getURI() };
            throw new MissingResourceFailureException(
               "signature.Verification.Reference.NoInput", exArgs, ex,
               currentRef);
         }
      }
      return verify;
   }
   private void setVerificationResult(int index, boolean verify)
   {
      if (this.verificationResults == null) {
         this.verificationResults = new boolean[this.getLength()];
      }
      this.verificationResults[index] = verify;
   }
   public boolean getVerificationResult(int index) throws XMLSecurityException {
      if ((index < 0) || (index > this.getLength() - 1)) {
         Object exArgs[] = { Integer.toString(index),
                             Integer.toString(this.getLength()) };
         Exception e =
            new IndexOutOfBoundsException(I18n
               .translate("signature.Verification.IndexOutOfBounds", exArgs));
         throw new XMLSecurityException("generic.EmptyMessage", e);
      }
      if (this.verificationResults == null) {
         try {
            this.verifyReferences();
         } catch (Exception ex) {
            throw new XMLSecurityException("generic.EmptyMessage", ex);
         }
      }
      return this.verificationResults[index];
   }
   public void addResourceResolver(ResourceResolver resolver) {
      if (resolver == null) {
          return;
      }
      if (_perManifestResolvers==null)
          _perManifestResolvers = new ArrayList();
      this._perManifestResolvers.add(resolver);
   }
   public void addResourceResolver(ResourceResolverSpi resolverSpi) {
      if (resolverSpi == null) {
          return;
      }
      if (_perManifestResolvers==null)
                  _perManifestResolvers = new ArrayList();
      this._perManifestResolvers.add(new ResourceResolver(resolverSpi));
   }
   public void setResolverProperty(String key, String value) {
           if (_resolverProperties==null) {
                   _resolverProperties=new HashMap(10);
           }
      this._resolverProperties.put(key, value);
   }
   public String getResolverProperty(String key) {
      return (String) this._resolverProperties.get(key);
   }
   public byte[] getSignedContentItem(int i) throws XMLSignatureException {
      try {
         return this.getReferencedContentAfterTransformsItem(i).getBytes();
      } catch (IOException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (CanonicalizationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidCanonicalizerException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public XMLSignatureInput getReferencedContentBeforeTransformsItem(int i)
           throws XMLSecurityException {
      return this.item(i).getContentsBeforeTransformation();
   }
   public XMLSignatureInput getReferencedContentAfterTransformsItem(int i)
           throws XMLSecurityException {
      return this.item(i).getContentsAfterTransformation();
   }
   public int getSignedContentLength() {
      return this.getLength();
   }
   public String getBaseLocalName() {
      return Constants._TAG_MANIFEST;
   }
}
