public class FuncHereContext extends XPathContext {
   private FuncHereContext() {}
   public FuncHereContext(Node owner) {
      super(owner);
   }
   public FuncHereContext(Node owner, XPathContext xpathContext) {
      super(owner);
      try {
         super.m_dtmManager = xpathContext.getDTMManager();
      } catch (IllegalAccessError iae) {
         throw new IllegalAccessError(I18n.translate("endorsed.jdk1.4.0")
                                      + " Original message was \""
                                      + iae.getMessage() + "\"");
      }
   }
   public FuncHereContext(Node owner, CachedXPathAPI previouslyUsed) {
      super(owner);
      try {
         super.m_dtmManager = previouslyUsed.getXPathContext().getDTMManager();
      } catch (IllegalAccessError iae) {
         throw new IllegalAccessError(I18n.translate("endorsed.jdk1.4.0")
                                      + " Original message was \""
                                      + iae.getMessage() + "\"");
      }
   }
   public FuncHereContext(Node owner, DTMManager dtmManager) {
      super(owner);
      try {
         super.m_dtmManager = dtmManager;
      } catch (IllegalAccessError iae) {
         throw new IllegalAccessError(I18n.translate("endorsed.jdk1.4.0")
                                      + " Original message was \""
                                      + iae.getMessage() + "\"");
      }
   }
}
