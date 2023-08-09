public class ExpectedInvocationAndResult implements Serializable {
    private static final long serialVersionUID = -1951159588262854559L;
    ExpectedInvocation expectedInvocation;
    Result result;
    public ExpectedInvocationAndResult(ExpectedInvocation expectedInvocation,
            Result result) {
        this.expectedInvocation = expectedInvocation;
        this.result = result;
    }
    public ExpectedInvocation getExpectedInvocation() {
        return expectedInvocation;
    }
    public Result getResult() {
        return result;
    }
}