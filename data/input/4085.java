public final class Init {
  static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(Init.class.getName());
   private static boolean _alreadyInitialized = false;
   public static final String CONF_NS="http:
   public static final boolean isInitialized() {
      return Init._alreadyInitialized;
   }
   public synchronized static void init() {
      if (_alreadyInitialized) {
        return;
      }
      long XX_configure_i18n_end=0;
      long XX_configure_reg_c14n_start=0;
      long XX_configure_reg_c14n_end=0;
      long XX_configure_reg_jcemapper_end=0;
      long XX_configure_reg_keyInfo_start=0;
      long XX_configure_reg_keyResolver_end=0;
      long XX_configure_reg_prefixes_start=0;
      long XX_configure_reg_resourceresolver_start=0;
      long XX_configure_reg_sigalgos_end=0;
      long XX_configure_reg_transforms_end=0;
      long XX_configure_reg_keyInfo_end=0;
      long XX_configure_reg_keyResolver_start=0;
         _alreadyInitialized = true;
         try {
            long XX_init_start = System.currentTimeMillis();
            long XX_prng_start = System.currentTimeMillis();
            long XX_prng_end = System.currentTimeMillis();
            long XX_parsing_start = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream is = (InputStream) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return getClass().getResourceAsStream
                            ("resource/config.xml");
                    }
                });
            Document doc = db.parse(is);
            long XX_parsing_end = System.currentTimeMillis();
            long XX_configure_i18n_start = 0;
            {
                XX_configure_reg_keyInfo_start = System.currentTimeMillis();
               try {
                  KeyInfo.init();
               } catch (Exception e) {
                  e.printStackTrace();
                  throw e;
               }
               XX_configure_reg_keyInfo_end = System.currentTimeMillis();
            }
                        long XX_configure_reg_transforms_start=0;
                        long XX_configure_reg_jcemapper_start=0;
                        long XX_configure_reg_sigalgos_start=0;
                        long XX_configure_reg_resourceresolver_end=0;
                        long XX_configure_reg_prefixes_end=0;
            Node config=doc.getFirstChild();
            for (;config!=null;config=config.getNextSibling()) {
                if ("Configuration".equals(config.getLocalName())) {
                        break;
                }
            }
                        for (Node el=config.getFirstChild();el!=null;el=el.getNextSibling()) {
                if (!(el instanceof Element)) {
                        continue;
                }
                String tag=el.getLocalName();
            if (tag.equals("CanonicalizationMethods")){
                XX_configure_reg_c14n_start = System.currentTimeMillis();
               Canonicalizer.init();
               Element[] list=XMLUtils.selectNodes(el.getFirstChild(),CONF_NS,"CanonicalizationMethod");
               for (int i = 0; i < list.length; i++) {
                  String URI = list[i].getAttributeNS(null,
                                  "URI");
                  String JAVACLASS =
                     list[i].getAttributeNS(null,
                        "JAVACLASS");
                  try {
                      Class.forName(JAVACLASS);
                      if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Canonicalizer.register(" + URI + ", "
                            + JAVACLASS + ")");
                     Canonicalizer.register(URI, JAVACLASS);
                  } catch (ClassNotFoundException e) {
                     Object exArgs[] = { URI, JAVACLASS };
                     log.log(java.util.logging.Level.SEVERE, I18n.translate("algorithm.classDoesNotExist",
                                              exArgs));
                  }
               }
               XX_configure_reg_c14n_end = System.currentTimeMillis();
            }
            if (tag.equals("TransformAlgorithms")){
               XX_configure_reg_transforms_start = System.currentTimeMillis();
               Transform.init();
               Element[] tranElem = XMLUtils.selectNodes(el.getFirstChild(),CONF_NS,"TransformAlgorithm");
               for (int i = 0; i < tranElem.length; i++) {
                  String URI = tranElem[i].getAttributeNS(null,
                                  "URI");
                  String JAVACLASS =
                     tranElem[i].getAttributeNS(null,
                        "JAVACLASS");
                  try {
                     Class.forName(JAVACLASS);
                     if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Transform.register(" + URI + ", " + JAVACLASS + ")");
                     Transform.register(URI, JAVACLASS);
                  } catch (ClassNotFoundException e) {
                     Object exArgs[] = { URI, JAVACLASS };
                     log.log(java.util.logging.Level.SEVERE, I18n.translate("algorithm.classDoesNotExist",
                                              exArgs));
                  } catch (NoClassDefFoundError ex) {
                                          log.log(java.util.logging.Level.WARNING, "Not able to found dependecies for algorithm, I'm keep working.");
                  }
               }
               XX_configure_reg_transforms_end = System.currentTimeMillis();
            }
            if ("JCEAlgorithmMappings".equals(tag)){
               XX_configure_reg_jcemapper_start = System.currentTimeMillis();
               JCEMapper.init((Element)el);
               XX_configure_reg_jcemapper_end = System.currentTimeMillis();
            }
            if (tag.equals("SignatureAlgorithms")){
               XX_configure_reg_sigalgos_start = System.currentTimeMillis();
               SignatureAlgorithm.providerInit();
               Element[] sigElems = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS,
                  "SignatureAlgorithm");
               for (int i = 0; i < sigElems.length; i++) {
                  String URI = sigElems[i].getAttributeNS(null,
                                  "URI");
                  String JAVACLASS =
                    sigElems[i].getAttributeNS(null,
                        "JAVACLASS");
                  try {
                      Class.forName(JAVACLASS);
                      if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "SignatureAlgorithm.register(" + URI + ", " + JAVACLASS + ")");
                     SignatureAlgorithm.register(URI, JAVACLASS);
                  } catch (ClassNotFoundException e) {
                     Object exArgs[] = { URI, JAVACLASS };
                     log.log(java.util.logging.Level.SEVERE, I18n.translate("algorithm.classDoesNotExist",
                                              exArgs));
                  }
               }
               XX_configure_reg_sigalgos_end = System.currentTimeMillis();
            }
            if (tag.equals("ResourceResolvers")){
               XX_configure_reg_resourceresolver_start = System.currentTimeMillis();
               ResourceResolver.init();
               Element[]resolverElem = XMLUtils.selectNodes(el.getFirstChild(),CONF_NS,
                  "Resolver");
               for (int i = 0; i < resolverElem.length; i++) {
                  String JAVACLASS =
                      resolverElem[i].getAttributeNS(null,
                        "JAVACLASS");
                  String Description =
                     resolverElem[i].getAttributeNS(null,
                        "DESCRIPTION");
                  if ((Description != null) && (Description.length() > 0)) {
                    if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Register Resolver: " + JAVACLASS + ": " + Description);
                  } else {
                    if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Register Resolver: " + JAVACLASS + ": For unknown purposes");
                  }
                                  try {
                                          ResourceResolver.register(JAVACLASS);
                                  } catch (Throwable e) {
                                          log.log(java.util.logging.Level.WARNING, "Cannot register:"+JAVACLASS+" perhaps some needed jars are not installed",e);
                                  }
                  XX_configure_reg_resourceresolver_end =
                    System.currentTimeMillis();
               }
            }
            if (tag.equals("KeyResolver")){
               XX_configure_reg_keyResolver_start =System.currentTimeMillis();
               KeyResolver.init();
               Element[] resolverElem = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS,"Resolver");
               for (int i = 0; i < resolverElem.length; i++) {
                  String JAVACLASS =
                     resolverElem[i].getAttributeNS(null,
                        "JAVACLASS");
                  String Description =
                     resolverElem[i].getAttributeNS(null,
                        "DESCRIPTION");
                  if ((Description != null) && (Description.length() > 0)) {
                    if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Register Resolver: " + JAVACLASS + ": " + Description);
                  } else {
                    if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Register Resolver: " + JAVACLASS + ": For unknown purposes");
                  }
                  KeyResolver.register(JAVACLASS);
               }
               XX_configure_reg_keyResolver_end = System.currentTimeMillis();
            }
            if (tag.equals("PrefixMappings")){
                XX_configure_reg_prefixes_start = System.currentTimeMillis();
                if (log.isLoggable(java.util.logging.Level.FINE))
                    log.log(java.util.logging.Level.FINE, "Now I try to bind prefixes:");
               Element[] nl = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS,"PrefixMapping");
               for (int i = 0; i < nl.length; i++) {
                  String namespace = nl[i].getAttributeNS(null,
                                        "namespace");
                  String prefix = nl[i].getAttributeNS(null,
                                     "prefix");
                  if (log.isLoggable(java.util.logging.Level.FINE))
                      log.log(java.util.logging.Level.FINE, "Now I try to bind " + prefix + " to " + namespace);
                  com.sun.org.apache.xml.internal.security.utils.ElementProxy
                     .setDefaultPrefix(namespace, prefix);
               }
               XX_configure_reg_prefixes_end = System.currentTimeMillis();
            }
            }
            long XX_init_end = System.currentTimeMillis();
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "XX_init                             " + ((int)(XX_init_end - XX_init_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_prng                           " + ((int)(XX_prng_end - XX_prng_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_parsing                        " + ((int)(XX_parsing_end - XX_parsing_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_i18n                 " + ((int)(XX_configure_i18n_end- XX_configure_i18n_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_c14n             " + ((int)(XX_configure_reg_c14n_end- XX_configure_reg_c14n_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_jcemapper        " + ((int)(XX_configure_reg_jcemapper_end- XX_configure_reg_jcemapper_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_keyInfo          " + ((int)(XX_configure_reg_keyInfo_end- XX_configure_reg_keyInfo_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_keyResolver      " + ((int)(XX_configure_reg_keyResolver_end- XX_configure_reg_keyResolver_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_prefixes         " + ((int)(XX_configure_reg_prefixes_end- XX_configure_reg_prefixes_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_resourceresolver " + ((int)(XX_configure_reg_resourceresolver_end- XX_configure_reg_resourceresolver_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_sigalgos         " + ((int)(XX_configure_reg_sigalgos_end- XX_configure_reg_sigalgos_start)) + " ms");
                log.log(java.util.logging.Level.FINE, "  XX_configure_reg_transforms       " + ((int)(XX_configure_reg_transforms_end- XX_configure_reg_transforms_start)) + " ms");
            }
         } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Bad: ", e);
            e.printStackTrace();
         }
   }
}
