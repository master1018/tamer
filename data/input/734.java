public class SVGAElementBridge extends SVGGElementBridge {
    public SVGAElementBridge() {
    }
    public String getLocalName() {
        return SVG_A_TAG;
    }
    public Bridge getInstance() {
        return new SVGAElementBridge();
    }
    public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
        super.buildGraphicsNode(ctx, e, node);
        if (ctx.isInteractive()) {
            EventTarget target = (EventTarget) e;
            EventListener l = new AnchorListener(ctx.getUserAgent());
            target.addEventListener(SVG_EVENT_CLICK, l, false);
            ctx.storeEventListener(target, SVG_EVENT_CLICK, l, false);
            l = new CursorMouseOverListener(ctx.getUserAgent());
            target.addEventListener(SVG_EVENT_MOUSEOVER, l, false);
            ctx.storeEventListener(target, SVG_EVENT_MOUSEOVER, l, false);
            l = new CursorMouseOutListener(ctx.getUserAgent());
            target.addEventListener(SVG_EVENT_MOUSEOUT, l, false);
            ctx.storeEventListener(target, SVG_EVENT_MOUSEOUT, l, false);
        }
    }
    public boolean isComposite() {
        return true;
    }
    public static class AnchorListener implements EventListener {
        protected UserAgent userAgent;
        public AnchorListener(UserAgent ua) {
            userAgent = ua;
        }
        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt)) return;
            SVGAElement elt = (SVGAElement) evt.getCurrentTarget();
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            userAgent.setSVGCursor(cursor);
            userAgent.openLink(elt);
            evt.stopPropagation();
        }
    }
    public static class CursorMouseOverListener implements EventListener {
        protected UserAgent userAgent;
        public CursorMouseOverListener(UserAgent ua) {
            userAgent = ua;
        }
        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt)) return;
            Element target = (Element) evt.getTarget();
            if (CSSUtilities.isAutoCursor(target)) {
                userAgent.setSVGCursor(CursorManager.ANCHOR_CURSOR);
            }
            SVGAElement elt = (SVGAElement) evt.getCurrentTarget();
            if (elt != null) {
                String href = XLinkSupport.getXLinkHref(elt);
                userAgent.displayMessage(href);
            }
        }
    }
    public static class CursorMouseOutListener implements EventListener {
        protected UserAgent userAgent;
        public CursorMouseOutListener(UserAgent ua) {
            userAgent = ua;
        }
        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt)) return;
            SVGAElement elt = (SVGAElement) evt.getCurrentTarget();
            if (elt != null) {
                userAgent.displayMessage("");
            }
        }
    }
}
