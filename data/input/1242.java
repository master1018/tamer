public class GetKostLijstWithFormAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
        final ActionErrors errors = new ActionErrors();
        String forward = "failure";
        List<Cost> result = null;
        BalansForm balansForm = (BalansForm) form;
        if (balansForm.getBalansSoort() == null && StringUtils.isEmpty(balansForm.getSearchTerm())) {
            Periode periode = DateHelper.getPeriodPreviousYear();
            balansForm.setBalansSoort("alles");
            balansForm.setBeginDatum(DateHelper.getDate(periode.getBeginDatum()));
            balansForm.setEindDatum(DateHelper.getDate(periode.getEindDatum()));
        }
        try {
            User user = (User) request.getSession().getAttribute("user");
            String userId = Long.toString(user.getId());
            BoekDao boekDao = new BoekDao();
            if (StringUtils.isNotEmpty(balansForm.getSearchTerm())) {
                result = boekDao.searchCosts(balansForm.getSearchTerm(), userId);
            } else {
                result = boekDao.getKostLijst(balansForm.getBeginDatum(), balansForm.getEindDatum(), balansForm.getBalansSoort(), userId);
            }
            request.setAttribute("kostLijst", result);
            String balansSoort = balansForm.getBalansSoort();
            if (balansSoort != null) {
                if (balansSoort.equals("btwBalans")) {
                    Balans balans = BalanceCalculator.calculateBtwBalance(result, false);
                    request.setAttribute("btwOut", balans.getTotaleKosten());
                    request.setAttribute("btwIn", balans.getTotaleBaten());
                    request.setAttribute("balans", (balans.getTotaleBaten().subtract(balans.getTotaleKosten()).add(balans.getCorrection())));
                    request.setAttribute("brutoOmzet", balans.getBrutoOmzet());
                    request.setAttribute("nettoOmzet", balans.getNettoOmzet());
                    request.setAttribute("btwCorrection", balans.getCorrection());
                } else if (balansSoort.equals("rekeningBalans")) {
                    BigDecimal actualBalance = BalanceCalculator.getActualAccountBalance(balansForm.getBeginDatum(), balansForm.getEindDatum(), user.getId());
                    if (actualBalance != null) {
                        request.setAttribute("actualBalance", actualBalance);
                    } else {
                        request.setAttribute("actualBalance", "kan nog niet berekend worden");
                    }
                    Liquiditeit liquiditeit = BalanceCalculator.calculateAccountBalance(result);
                    request.setAttribute("balans", liquiditeit.getRekeningBalans());
                    request.setAttribute("sparen", liquiditeit.getSpaarBalans());
                    request.setAttribute("private", liquiditeit.getPriveBalans());
                    List<Cost> result2 = boekDao.getKostLijst(balansForm.getBeginDatum(), balansForm.getEindDatum(), "btwBalans", userId);
                    Balans balans = BalanceCalculator.calculateBtwBalance(result2, true);
                    BigDecimal totalPaidInvoices = BalanceCalculator.calculateTotalPaidInvoices(result);
                    request.setAttribute("brutoOmzet", balans.getBrutoOmzet().add(totalPaidInvoices));
                    List<Cost> result3 = boekDao.getKostLijst(balansForm.getBeginDatum(), balansForm.getEindDatum(), "tax", userId);
                    BigDecimal taxBalance = BalanceCalculator.calculateTaxBalance(result3).getTotaleKosten();
                    request.setAttribute("taxBalans", taxBalance);
                    List<Cost> result4 = boekDao.getCostListCurrentAccount(balansForm.getBeginDatum(), balansForm.getEindDatum(), userId);
                    BigDecimal costBalance = BalanceCalculator.calculateCostBalanceCurrentAccount(result4, true).getTotaleKosten();
                    request.setAttribute("costBalance", costBalance);
                    BigDecimal doubleCheck = balans.getBrutoOmzet().add(totalPaidInvoices).subtract(taxBalance).subtract(costBalance).subtract(liquiditeit.getSpaarBalans().subtract(liquiditeit.getPriveBalans()));
                    request.setAttribute("doubleCheck", doubleCheck);
                } else if (balansSoort.equals("kostenBalans")) {
                    Balans balans = BalanceCalculator.calculatCostBalance(result);
                    request.setAttribute("kosten", balans.getTotaleKosten());
                    request.setAttribute("baten", balans.getTotaleBaten());
                    List<Cost> result4 = boekDao.getCostListCurrentAccount(balansForm.getBeginDatum(), balansForm.getEindDatum(), userId);
                    Balans balanceCurrentAccount = BalanceCalculator.calculateCostBalanceCurrentAccount(result4, false);
                    request.setAttribute("costCurrentAccount", balanceCurrentAccount.getTotaleKosten());
                } else if (balansSoort.equals("reiskostenBalans")) {
                    Reiskosten travelCostBalance = BalanceCalculator.calculatTravelCostBalance(result);
                    request.setAttribute("kostenOv", travelCostBalance.getOvKosten());
                    request.setAttribute("kostenAutoMetBtw", travelCostBalance.getAutoKostenMetBtw());
                    request.setAttribute("kostenAutoZonderBtw", travelCostBalance.getAutoKostenZonderBtw());
                    request.setAttribute("vatCorrection", travelCostBalance.getVatCorrection());
                    BigDecimal verschil = (travelCostBalance.getAutoKostenMetBtw().subtract(travelCostBalance.getAutoKostenZonderBtw()).subtract(travelCostBalance.getVatCorrection()));
                    request.setAttribute("verschil", verschil);
                } else if (balansSoort.equals("private")) {
                    BigDecimal monthlyExpenses = BalanceCalculator.calculatMonthlyPrivateExpenses(result);
                    request.setAttribute("monthlyExpenses", monthlyExpenses);
                }
            }
            String action = (String) request.getParameter("action");
            if (action == null) {
                forward = "success";
            } else {
                if (action.equals("Fiscaal overzicht")) {
                    FiscalOverview overview = (FiscalOverview) request.getSession().getAttribute("overview");
                    if (overview == null) {
                        Locale locale = (Locale) request.getSession().getAttribute(Globals.LOCALE_KEY);
                        overview = FiscalOverviewHelper.createFiscalOverview(balansForm.getBeginDatum(), balansForm.getEindDatum(), result, user.getId(), locale);
                        request.getSession().setAttribute("overview", overview);
                    }
                    forward = "fiscaal";
                } else {
                    forward = "success";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ActionMessage message = null;
            if (e.getMessage().startsWith("error")) {
                message = new ActionMessage(e.getMessage());
            } else {
                message = new ActionMessage("errors.detail", e.getMessage());
            }
            errors.add(ActionErrors.GLOBAL_MESSAGE, message);
            addErrors(request, errors);
            saveErrors(request, errors);
            return mapping.findForward("error");
        }
        return mapping.findForward(forward);
    }
}
