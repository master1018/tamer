package cz.softinel.retra.worklog.web;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.springframework.web.servlet.ModelAndView;
import cz.softinel.retra.activity.Activity;
import cz.softinel.retra.activity.blo.ActivityLogic;
import cz.softinel.retra.component.blo.ComponentLogic;
import cz.softinel.retra.core.utils.convertor.LongConvertor;
import cz.softinel.retra.core.utils.helper.DateHelper;
import cz.softinel.retra.core.utils.helper.HolidaysHelper;
import cz.softinel.retra.employee.Employee;
import cz.softinel.retra.employee.blo.EmployeeLogic;
import cz.softinel.retra.invoice.InvoiceHelper;
import cz.softinel.retra.project.Project;
import cz.softinel.retra.project.blo.ProjectLogic;
import cz.softinel.retra.spring.web.DispatchController;
import cz.softinel.retra.worklog.Worklog;
import cz.softinel.retra.worklog.WorklogViewOverview;
import cz.softinel.retra.worklog.blo.WorklogLogic;
import cz.softinel.retra.worklog.dao.WorklogFilter;
import cz.softinel.uaf.filter.Filter;
import cz.softinel.uaf.filter.FilterHelper;
import cz.softinel.uaf.lovs.Lov;
import cz.softinel.uaf.lovs.LovsFactory;
import cz.softinel.uaf.messages.Message;
import cz.softinel.uaf.spring.web.controller.Model;
import cz.softinel.uaf.spring.web.controller.RequestContext;
import cz.softinel.uaf.util.grouping.GroupedItem;
import cz.softinel.uaf.util.grouping.GroupingHelper;
import cz.softinel.uaf.util.grouping.GroupingKey;
import cz.softinel.uaf.util.grouping.GroupingMap;

/**
 * Controller for worklogs.
 * 
 * @version $Revision: 1.29 $ $Date: 2007-12-03 18:42:29 $
 * @author Radek Pinc, Petr Sígl
 */
public class WorklogController extends DispatchController {

    private WorklogLogic worklogLogic;

    private EmployeeLogic employeeLogic;

    private ProjectLogic projectLogic;

    private ComponentLogic componentLogic;

    private ActivityLogic activityLogic;

    /**
	 * @return the worklogLogic
	 */
    public WorklogLogic getWorklogLogic() {
        return worklogLogic;
    }

    /**
	 * @param worklogLogic the worklogLogic to set
	 */
    public void setWorklogLogic(WorklogLogic worklogLogic) {
        this.worklogLogic = worklogLogic;
    }

    /**
	 * @return the employeeLogic
	 */
    public EmployeeLogic getEmployeeLogic() {
        return employeeLogic;
    }

    /**
	 * @param employeeLogic the employeeLogic to set
	 */
    public void setEmployeeLogic(EmployeeLogic employeeLogic) {
        this.employeeLogic = employeeLogic;
    }

    /**
	 * @return the activityLogic
	 */
    public ActivityLogic getActivityLogic() {
        return activityLogic;
    }

    /**
	 * @param activityLogic the activityLogic to set
	 */
    public void setActivityLogic(ActivityLogic activityLogic) {
        this.activityLogic = activityLogic;
    }

    /**
	 * @return the projectLogic
	 */
    public ProjectLogic getProjectLogic() {
        return projectLogic;
    }

    /**
	 * @param projectLogic the projectLogic to set
	 */
    public void setProjectLogic(ProjectLogic projectLogic) {
        this.projectLogic = projectLogic;
    }

    /**
	 * @return the componentLogic
	 */
    public ComponentLogic getComponentLogic() {
        return componentLogic;
    }

    /**
	 * @param componentLogic the componentLogic to set
	 */
    public void setComponentLogic(ComponentLogic componentLogic) {
        this.componentLogic = componentLogic;
    }

    @Override
    public Filter getFilter(Model model) {
        Filter filter = (Filter) model.get(WorklogFilter.class.toString());
        if (filter == null) {
            filter = new WorklogFilter();
            model.set(WorklogFilter.class.toString(), filter);
        }
        return filter;
    }

    /**
	 * Show worklog for user.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogOverview(Model model, RequestContext requestContext) {
        Filter filter = getFilter(model);
        Long employeePk = FilterHelper.getFieldAsLong(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, filter);
        String fromStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_FROM, filter);
        String toStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_TO, filter);
        if (employeePk == null) {
            employeePk = getSecurityLogic().getLoggedEmployee().getPk();
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, employeePk, filter);
        }
        if (fromStr == null) {
            Date from = DateHelper.getWeekBeforeDateStartOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_FROM, from, true, filter);
        }
        if (toStr == null) {
            Date to = DateHelper.getTodayDateEndOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_TO, to, true, filter);
        }
        if (requestContext.getParameter("filterPreviousMonth") != null) {
            Date current = DateHelper.add(new Date(), Calendar.MONTH, -1);
            Date from = DateHelper.getStartOfMonth(current);
            Date to = DateHelper.getEndOfMonth(current);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_FROM, from, true, filter);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_TO, to, true, filter);
        }
        if (requestContext.getParameter("filterCurrentMonth") != null) {
            Date current = new Date();
            Date from = DateHelper.getStartOfMonth(current);
            Date to = DateHelper.getEndOfMonth(current);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_FROM, from, true, filter);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_TO, to, true, filter);
        }
        List<Worklog> worklogs = worklogLogic.findByFilter(filter);
        GroupingMap grouppedWorklog = GroupingHelper.group(worklogs, "hours", new String[] { "project", "activity" });
        prepareActivities(model, grouppedWorklog.getProjection(1).getKeyList());
        prepareProjects(model, grouppedWorklog.getProjection(0).getKeyList());
        prepareMainProjects(model);
        prepareEmployees(model);
        prepareEmployee(model, employeePk);
        prepareInvoiceRelations(model);
        GroupingMap worklogGroupped = GroupingHelper.group(worklogs, "hours", new String[] { "week", "date", "project.code", "activity.code" });
        Date from = FilterHelper.getFieldAsDate(WorklogFilter.WORKLOG_FILTER_FROM, filter);
        Date to = FilterHelper.getFieldAsDate(WorklogFilter.WORKLOG_FILTER_TO, filter);
        if (from != null && to != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(from);
            while (to.compareTo(calendar.getTime()) >= 0) {
                Date date = calendar.getTime();
                worklogGroupped.add(new GroupingKey(new Object[] { DateHelper.getWeekCode(date), date, null, null }), new GroupedItem(0));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        model.set("worklogs", worklogs);
        model.set("worklogGroupped", worklogGroupped);
        return createModelAndView(model, getSuccessView());
    }

    /**
	 * Show worklog for user.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogList(Model model, RequestContext requestContext) {
        Filter filter = getFilter(model);
        Long employeePk = FilterHelper.getFieldAsLong(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, filter);
        String fromStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_FROM, filter);
        String toStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_TO, filter);
        if (employeePk == null) {
            employeePk = getSecurityLogic().getLoggedEmployee().getPk();
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, employeePk, filter);
        }
        if (fromStr == null) {
            Date from = DateHelper.getDayBeforeDateStartOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_FROM, from, true, filter);
        }
        if (toStr == null) {
            Date to = DateHelper.getDayAfterDateEndOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_TO, to, true, filter);
        }
        prepareActivities(model, getShowHistoryData(requestContext));
        prepareProjects(model, getShowHistoryData(requestContext));
        prepareEmployees(model);
        prepareEmployee(model, employeePk);
        prepareInvoiceRelations(model);
        List<Worklog> worklogs = worklogLogic.findByFilter(filter);
        model.set("worklogs", worklogs);
        return createModelAndView(model, getSuccessView());
    }

    /**
	 * Show worklog overview for project.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogProjectOverview(Model model, RequestContext requestContext) {
        Filter filter = getFilter(model);
        String fromStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_FROM, filter);
        String toStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_TO, filter);
        if (fromStr == null) {
            Date from = DateHelper.getDayBeforeDateStartOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_FROM, from, true, filter);
        }
        if (toStr == null) {
            Date to = DateHelper.getDayAfterDateEndOfDay(new Date());
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_TO, to, true, filter);
        }
        List<Worklog> worklogs = worklogLogic.findByFilter(filter);
        GroupingMap grouppedWorklog = GroupingHelper.group(worklogs, "hours", new String[] { "project", "activity" });
        prepareActivities(model, grouppedWorklog.getProjection(1).getKeyList());
        prepareProjects(model, grouppedWorklog.getProjection(0).getKeyList());
        prepareInvoiceRelations(model);
        GroupingMap groupped = GroupingHelper.group(worklogs, "hours", new String[] { "employee.user.contactInfo.displayName", "activity.codeAndName", "project.codeAndName" });
        GroupingMap worklogGroupped = GroupingHelper.group(worklogs, "hours", new String[] { "employee.user.contactInfo.displayName", "activity.code", "project.code" });
        model.set("worklogs", worklogs);
        model.set("worklogByEmployee", groupped.getProjection(0));
        model.set("worklogByAcrivity", groupped.getProjection(1));
        model.set("worklogByProject", groupped.getProjection(2));
        model.set("worklogGroupped", worklogGroupped);
        model.set("worklogGrouppedAcrivity", worklogGroupped.getProjection(1));
        return createModelAndView(model, getSuccessView());
    }

    /**
	 * Show daily worklog for user.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogDaily(Model model, RequestContext requestContext) {
        Filter filter = getFilter(model);
        Long employeePk = FilterHelper.getFieldAsLong(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, filter);
        String dateStr = FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_DATE, filter);
        if (employeePk == null) {
            employeePk = getSecurityLogic().getLoggedEmployee().getPk();
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, employeePk, filter);
        }
        Date date = null;
        if (dateStr == null) {
            date = new Date();
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_DATE, date, true, filter);
        } else {
            date = FilterHelper.getFieldAsDate(WorklogFilter.WORKLOG_FILTER_DATE, filter);
            if (date == null) {
                requestContext.addInfo(new Message("worklogFilterForm.date.bad.format"));
                date = new Date();
                FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_DATE, date, true, filter);
            }
        }
        prepareEmployees(model);
        prepareEmployee(model, employeePk);
        List<Worklog> worklogs = worklogLogic.findByFilter(filter);
        GroupingMap grouppedWorklog = GroupingHelper.group(worklogs, "hours", new String[] { "project", "activity" });
        model.set("worklogs", worklogs);
        model.set("grouppedWorklog", grouppedWorklog);
        prepareNextDay(model, filter);
        preparePreviousDay(model, filter);
        prepareDayOfWeek(model, date);
        return createModelAndView(model, getSuccessView());
    }

    /**
	 * Show worklog for user.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogOverviewList(Model model, RequestContext requestContext) {
        Filter filter = getFilter(model);
        Long employeePk = FilterHelper.getFieldAsLong(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, filter);
        Integer year = FilterHelper.getFieldAsInteger(WorklogFilter.WORKLOG_FILTER_YEAR, filter);
        Integer month = FilterHelper.getFieldAsInteger(WorklogFilter.WORKLOG_FILTER_MONTH, filter);
        if (employeePk == null) {
            employeePk = getSecurityLogic().getLoggedEmployee().getPk();
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_EMPLOYEE, employeePk, filter);
        }
        if (year == null) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_YEAR, year, filter);
        }
        if (month == null) {
            Calendar calendar = Calendar.getInstance();
            month = calendar.get(Calendar.MONTH);
            FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_MONTH, month, filter);
        }
        prepareYears(model);
        prepareMonths(model);
        prepareEmployees(model);
        prepareEmployee(model, employeePk);
        prepareInvoiceRelations(model);
        prepareNext(model, filter);
        preparePrevious(model, filter);
        List<WorklogViewOverview> worklogs = worklogLogic.findWorklogOverviewByFilter(filter);
        model.set("worklogs", worklogs);
        return createModelAndView(model, getSuccessView());
    }

    /**
	 * Delete given worklog for user.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogDelete(Model model, RequestContext requestContext) {
        String worklogPkStr = requestContext.getParameter("worklogPk");
        Long worklogPk = LongConvertor.getLongFromString(worklogPkStr);
        String view = getSuccessView();
        if (worklogPk != null) {
            worklogLogic.remove(worklogPk);
            if (requestContext.getErrors().size() > 0) {
                view = getErrorView();
            } else {
                requestContext.addRedirectIgnoreInfo(new Message("worklog.deleted"));
            }
            requestContext.convertMessagesToRedirectIgnoring();
        } else {
            view = getErrorView();
        }
        return createModelAndView(model, view);
    }

    /**
	 * View given worklog.
	 * 
	 * @param model
	 * @param requestContext
	 * @return
	 */
    public ModelAndView worklogView(Model model, RequestContext requestContext) {
        String worklogPkStr = requestContext.getParameter("worklogPk");
        Long worklogPk = LongConvertor.getLongFromString(worklogPkStr);
        String view = getSuccessView();
        if (worklogPk != null) {
            Worklog worklog = worklogLogic.get(worklogPk);
            model.put("worklog", worklog);
        } else {
            view = getErrorView();
        }
        return createModelAndView(model, view);
    }

    protected void prepareWeeks(Model model) {
        List<DateHelper.Week> weeks = DateHelper.getWeekList(12);
        model.set("weeks", weeks);
    }

    protected void prepareActivities(Model model) {
        prepareActivities(model, false);
    }

    protected void prepareActivities(Model model, boolean showAll) {
        List<Activity> activities = null;
        if (showAll) {
            activities = activityLogic.findAllActivities();
        } else {
            activities = activityLogic.findAllNotDeletedActivities();
        }
        prepareActivities(model, activities);
    }

    protected void prepareActivities(Model model, List activities) {
        model.put("activities", activities);
    }

    protected void prepareProjects(Model model) {
        prepareProjects(model, false);
    }

    protected void prepareProjects(Model model, boolean showAll) {
        List<Project> projects = null;
        if (showAll) {
            projects = projectLogic.findAllProjects();
        } else {
            projects = projectLogic.findAllNotDeletedProjects();
        }
        prepareProjects(model, projects);
    }

    protected void prepareProjects(Model model, List projects) {
        model.put("projects", projects);
    }

    protected void prepareMainProjects(Model model) {
        List<Project> projects = projectLogic.findAllProjects();
        for (Iterator<Project> i = projects.iterator(); i.hasNext(); ) {
            Project project = i.next();
            if (project.getParent() != null) {
                i.remove();
            }
        }
        model.set("mainProjects", projects);
    }

    protected void prepareEmployees(Model model) {
        List<Employee> employees = employeeLogic.getAllEmployees();
        model.set("employees", employees);
    }

    protected void prepareEmployee(Model model, Long employeePk) {
        Employee employee = new Employee();
        employee.setPk(employeePk);
        employeeLogic.load(employee);
        model.set("employee", employee);
    }

    protected void prepareInvoiceRelations(Model model) {
        model.set("invoiceRelations", InvoiceHelper.INVOICE_RELATIONS);
    }

    protected void prepareMonths(Model model) {
        LovsFactory factory = LovsFactory.getInstance();
        Lov lov = factory.getLov("months", getApplicationContext());
        model.set("months", lov.getFields());
    }

    protected void prepareYears(Model model) {
        LovsFactory factory = LovsFactory.getInstance();
        Lov lov = factory.getLov("years", getApplicationContext());
        model.set("years", lov.getFields());
    }

    protected void prepareNext(Model model, Filter filter) {
        Filter newFilter = prepareNewFilter(filter, 1);
        super.prepareNext(model, newFilter);
    }

    protected void preparePrevious(Model model, Filter filter) {
        Filter newFilter = prepareNewFilter(filter, -1);
        super.preparePrevious(model, newFilter);
    }

    private Filter prepareNewFilter(Filter filter, int addMonth) {
        Filter newFilter = new WorklogFilter();
        for (String fieldName : filter.getAllFields()) {
            newFilter.setFieldValue(fieldName, filter.getFieldValue(fieldName));
        }
        Integer year = FilterHelper.getFieldAsInteger(WorklogFilter.WORKLOG_FILTER_YEAR, filter);
        Integer month = FilterHelper.getFieldAsInteger(WorklogFilter.WORKLOG_FILTER_MONTH, filter);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        calendar.add(Calendar.MONTH, addMonth);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_YEAR, year, newFilter);
        FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_MONTH, month, newFilter);
        return newFilter;
    }

    private void prepareNextDay(Model model, Filter filter) {
        Filter newFilter = prepareNewFilterDay(filter, 1);
        super.prepareNext(model, newFilter);
        model.set("nextDate", FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_DATE, newFilter));
    }

    private void preparePreviousDay(Model model, Filter filter) {
        Filter newFilter = prepareNewFilterDay(filter, -1);
        super.preparePrevious(model, newFilter);
        model.set("previousDate", FilterHelper.getFieldAsString(WorklogFilter.WORKLOG_FILTER_DATE, newFilter));
    }

    private Filter prepareNewFilterDay(Filter filter, int addDay) {
        Filter newFilter = new WorklogFilter();
        for (String fieldName : filter.getAllFields()) {
            newFilter.setFieldValue(fieldName, filter.getFieldValue(fieldName));
        }
        Date currentDate = FilterHelper.getFieldAsDate(WorklogFilter.WORKLOG_FILTER_DATE, filter);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, addDay);
        FilterHelper.setField(WorklogFilter.WORKLOG_FILTER_DATE, calendar.getTime(), true, newFilter);
        return newFilter;
    }

    private void prepareDayOfWeek(Model model, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        model.set("dayOfWeek", dayOfWeek);
        String dayOfWeekCss = "scheduleDefault";
        String titleKey = "day.normal";
        if (HolidaysHelper.isPublicHoliday(date) || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            dayOfWeekCss = "scheduleWeekend";
            titleKey = "day.free";
        }
        model.set("dayOfWeekCss", dayOfWeekCss);
        model.set("dayOfWeekTitle", titleKey);
    }
}
