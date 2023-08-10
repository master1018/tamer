public class GetAccountFundsCommand implements BFCommand {
    private GetAccountFundsResp resp;
    public void execute(BFExchangeService exchangeWebService, BFGlobalService globalWebService, String sessionToken) {
        APIRequestHeader requestHeader = new APIRequestHeader();
        requestHeader.setSessionToken(sessionToken);
        GetAccountFundsReq req = new GetAccountFundsReq();
        req.setHeader(requestHeader);
        resp = exchangeWebService.getAccountFunds(req);
    }
    public String getErrorCode() {
        return resp.getHeader().getErrorCode().name();
    }
    public String getSessionToken() {
        return resp.getHeader().getSessionToken();
    }
    public GetAccountFundsResp getAccountFunds() {
        if (resp.getErrorCode().equals(GetAccountFundsErrorEnum.OK)) {
            return resp;
        } else {
            throw new BetFairException("getAccountFunds: " + resp.getErrorCode().name());
        }
    }
}
