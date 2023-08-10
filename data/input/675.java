public class SecurityContextSessionIntegrationPhaseListener implements PhaseListener {
    private Log logger = LogFactory.getLog(getClass());
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
            if (currentRequestIsPortletRequest(event)) {
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("Current request is HttpServletRequest, phase listener won't be executed.");
        }
    }
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW) || event.getPhaseId().equals(PhaseId.INVOKE_APPLICATION)) {
            if (currentRequestIsPortletRequest(event)) {
                try {
                    SecurityContext securityContext = obtainSecurityContextFromCurrentPortletRequest(event);
                    if (securityContext == null) {
                        securityContext = obtainSecurityContextFromSessionMap(event);
                    } else {
                        updateSecurityContextInSessionMap(event, securityContext);
                    }
                    if (securityContext != null) {
                        logger.debug("SecurityContext found :" + securityContext);
                        SecurityContextHolder.setContext(securityContext);
                    } else {
                        logger.debug("SecurityContext not found.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.debug("Current request is HttpServletRequest, phase listener won't be executed.");
            }
        }
    }
    private void updateSecurityContextInSessionMap(PhaseEvent event, SecurityContext securityContext) {
        logger.debug("Updating SecurityContext in session map");
        event.getFacesContext().getExternalContext().getSessionMap().put(org.acegisecurity.context.HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY, securityContext);
    }
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
    private boolean currentRequestIsPortletRequest(PhaseEvent event) {
        return (event.getFacesContext().getExternalContext().getRequest() instanceof PortletRequest);
    }
    private SecurityContext obtainSecurityContextFromSessionMap(PhaseEvent event) {
        SecurityContext securityContext = (SecurityContext) event.getFacesContext().getExternalContext().getSessionMap().get(org.acegisecurity.context.HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
        logger.debug("SecurityContext" + (securityContext == null ? "not" : "") + "found in session map");
        return securityContext;
    }
    private SecurityContext obtainSecurityContextFromCurrentPortletRequest(PhaseEvent event) throws ClassNotFoundException, IOException {
        logger.debug("Current request is of type PortletRequest");
        PortletRequest pReq = (PortletRequest) event.getFacesContext().getExternalContext().getRequest();
        Object securityContextObj = pReq.getAttribute(org.acegisecurity.context.HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
        logger.debug("SecurityContext " + ((securityContextObj == null) ? "not" : "") + " found in current request");
        SecurityContext securityContext = null;
        if (securityContextObj != null) {
            securityContext = (SecurityContext) AcegiPortletUtils.serializeDeserialize(securityContextObj);
        }
        return securityContext;
    }
}
