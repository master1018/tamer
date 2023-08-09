public abstract class XErrorHandler {
    public abstract int handleError(long display, XErrorEvent err);
    public static class XBaseErrorHandler extends XErrorHandler {
        @Override
        public int handleError(long display, XErrorEvent err) {
            return XToolkit.SAVED_ERROR_HANDLER(display, err);
        }
    }
    public static class IgnoreBadWindowHandler extends XBaseErrorHandler {
        @Override
        public int handleError(long display, XErrorEvent err) {
            if (err.get_error_code() == XConstants.BadWindow) {
                return 0;
            }
            return super.handleError(display, err);
        }
        private static IgnoreBadWindowHandler theInstance = new IgnoreBadWindowHandler();
        public static IgnoreBadWindowHandler getInstance() {
            return theInstance;
        }
    }
    public static class VerifyChangePropertyHandler extends XBaseErrorHandler {
        @Override
        public int handleError(long display, XErrorEvent err) {
            if (err.get_request_code() == XProtocolConstants.X_ChangeProperty) {
                return 0;
            }
            return super.handleError(display, err);
        }
        private static VerifyChangePropertyHandler theInstance = new VerifyChangePropertyHandler();
        public static VerifyChangePropertyHandler getInstance() {
            return theInstance;
        }
    }
}
