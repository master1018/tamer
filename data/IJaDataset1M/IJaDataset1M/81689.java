package org.roller.presentation.website.actions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.weblog.actions.WeblogEntryPageModel;
import org.roller.presentation.weblog.formbeans.WeblogEntryFormEx;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.RebuildUserIndexOperation;

/**
 * Allows user to perform Website maintenence operations such as flushing
 * the website page cache or re-indexing the website search index.
 * 
 * @struts.action path="/maintenance" name="maintenanceForm" scope="request" parameter="method"
 * 
 * @struts.action-forward name="maintenance.page" path="/website/Maintenance.jsp"
 */
public class MaintenanceAction extends DispatchAction {

    private static Log mLogger = LogFactory.getFactory().getInstance(MaintenanceAction.class);

    protected ActionForward unspecified(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        return mapping.findForward("maintenance.page");
    }

    /**
     * Respond to user's request to rebuild search index.
     */
    public ActionForward index(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.isUserAuthorizedToEdit()) {
                UserData ud = rreq.getUser();
                IndexManager manager = RollerContext.getRollerContext(RollerContext.getServletContext()).getIndexManager();
                manager.scheduleIndexOperation(new RebuildUserIndexOperation(rreq.getWebsite()));
                ActionMessages messages = new ActionMessages();
                messages.add(null, new ActionMessage("maintenance.message.indexed"));
                saveMessages(request, messages);
            }
        } catch (RollerException re) {
            mLogger.error("Unexpected exception", re.getRootCause());
            throw new ServletException(re);
        } catch (Exception e) {
            mLogger.error("Unexpected exception", e);
            throw new ServletException(e);
        }
        return mapping.findForward("maintenance.page");
    }

    /**
     * Respond to request to flush a user's page cache.
     */
    public ActionForward flushCache(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.isUserAuthorizedToEdit()) {
                UserData user = rreq.getUser();
                PageCache.removeFromCache(request, user);
                ActionMessages messages = new ActionMessages();
                messages.add(null, new ActionMessage("maintenance.message.flushed"));
                saveMessages(request, messages);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        return mapping.findForward("maintenance.page");
    }
}
