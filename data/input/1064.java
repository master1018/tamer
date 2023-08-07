public class ViewHelpController extends StandardForm {
    private final Logger log = Logger.getLogger(ViewHelpController.class);
    @Autowired
    public ViewHelpController(DaoUtility daoUtility, FacebookPortal fbPortal, BusinessRules businessRules, UserTransientDataAccessor userTransientDataAccessor) {
        super(daoUtility, fbPortal, businessRules, userTransientDataAccessor);
    }
    @RequestMapping(method = RequestMethod.GET)
    public String handleGetRequest(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        return processRequest(model, session, request, response);
    }
    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        return processRequest(model, session, request, response);
    }
    private String processRequest(ModelMap model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Player player = getLoggedInPlayer(request, response);
        setLastUrlRequested(session, player, request.getRequestURI());
        return "help";
    }
}
