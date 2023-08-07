public class SwapListerner extends BaseAction {
    private static final long serialVersionUID = 8931619990440158065L;
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private CheckCardDao checkCardDao;
    private String cardNo;
    private String readerNo;
    private String time;
    private Timestamp checkTime;
    public String getCardNo() {
        return cardNo;
    }
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    public String getReaderNo() {
        return readerNo;
    }
    public void setReaderNo(String readerNo) {
        this.readerNo = readerNo;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        if (StringUtils.isNotBlank(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
            try {
                checkTime = new Timestamp(sdf.parse(time).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.time = time;
    }
    public void setCheckCardDao(CheckCardDao checkCardDao) {
        this.checkCardDao = checkCardDao;
    }
    @Override
    public String execute() throws Exception {
        if (StringUtils.isNotBlank(cardNo) && StringUtils.isNotBlank(readerNo)) {
            if (checkTime == null) {
                checkTime = DateUtil.getCurrentTimestamp();
            }
            CheckCard cc = new CheckCard();
            cc.setCardNo(cardNo);
            cc.setReader(readerNo);
            cc.setCreatedDate(checkTime);
        }
        return super.execute();
    }
}
