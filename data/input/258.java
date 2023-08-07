public class BgMaxTk15Record extends BgFooter {
    public static final String TC = "15";
    private static Pattern linePattern = Pattern.compile(TC + "(\\d{35})(\\d{8})(\\d{5})(\\d{18})(\\w{3})(\\d{8}).{1}.*");
    private String recipientBankAccountNumber;
    private Date paymentDate;
    private String depositSerialNumber;
    private Currency currency;
    public BgMaxTk15Record() {
        super(TC);
    }
    @Override
    public BgRecord fromRecordString(String line) throws BgParseException {
        Matcher m = linePattern.matcher(line);
        if (m.matches()) {
            recipientBankAccountNumber = BgUtil.trimLeadingZeros(m.group(1));
            try {
                paymentDate = BgUtil.parseExtDateString(m.group(2));
            } catch (ParseException pe) {
                throw new BgParseException("Payment date: " + m.group(2) + " is not valid", line);
            }
            depositSerialNumber = BgUtil.trimLeadingZeros(m.group(3));
            amount = BgUtil.parseAmountStr(m.group(4));
            currency = Currency.getInstance(m.group(5));
            count = Integer.valueOf(m.group(6));
            return this;
        } else {
            throw new BgParseException("Deposit record doesn`t match expected pattern", line);
        }
    }
    @Override
    public String toRecordString() {
        return "";
    }
    public String getRecipientBankAccountNumber() {
        return recipientBankAccountNumber;
    }
    public Date getPaymentDate() {
        return paymentDate;
    }
    public String getDepositSerialNumber() {
        return depositSerialNumber;
    }
    public Currency getCurrency() {
        return currency;
    }
}
