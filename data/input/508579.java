public class SkbPool {
    private static SkbPool mInstance = null;
    private Vector<SkbTemplate> mSkbTemplates = new Vector<SkbTemplate>();
    private Vector<SoftKeyboard> mSoftKeyboards = new Vector<SoftKeyboard>();
    private SkbPool() {
    }
    public static SkbPool getInstance() {
        if (null == mInstance) mInstance = new SkbPool();
        return mInstance;
    }
    public void resetCachedSkb() {
        mSoftKeyboards.clear();
    }
    public SkbTemplate getSkbTemplate(int skbTemplateId, Context context) {
        for (int i = 0; i < mSkbTemplates.size(); i++) {
            SkbTemplate t = mSkbTemplates.elementAt(i);
            if (t.getSkbTemplateId() == skbTemplateId) {
                return t;
            }
        }
        if (null != context) {
            XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
            SkbTemplate t = xkbl.loadSkbTemplate(skbTemplateId);
            if (null != t) {
                mSkbTemplates.add(t);
                return t;
            }
        }
        return null;
    }
    public SoftKeyboard getSoftKeyboard(int skbCacheId, int skbXmlId,
            int skbWidth, int skbHeight, Context context) {
        for (int i = 0; i < mSoftKeyboards.size(); i++) {
            SoftKeyboard skb = mSoftKeyboards.elementAt(i);
            if (skb.getCacheId() == skbCacheId && skb.getSkbXmlId() == skbXmlId) {
                skb.setSkbCoreSize(skbWidth, skbHeight);
                skb.setNewlyLoadedFlag(false);
                return skb;
            }
        }
        if (null != context) {
            XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
            SoftKeyboard skb = xkbl.loadKeyboard(skbXmlId, skbWidth, skbHeight);
            if (skb != null) {
                if (skb.getCacheFlag()) {
                    skb.setCacheId(skbCacheId);
                    mSoftKeyboards.add(skb);
                }
            }
            return skb;
        }
        return null;
    }
}
