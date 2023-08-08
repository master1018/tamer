public abstract class BaseCmsAcquisition implements Serializable {
    public static String REF = "CmsAcquisition";
    public static String PROP_USER = "user";
    public static String PROP_LINK_START = "linkStart";
    public static String PROP_DESCRIPTION_END = "descriptionEnd";
    public static String PROP_CHANNEL = "channel";
    public static String PROP_DYNAMIC_START = "dynamicStart";
    public static String PROP_CONTENT_START = "contentStart";
    public static String PROP_TYPE = "type";
    public static String PROP_PAGINATION_END = "paginationEnd";
    public static String PROP_LINKSET_START = "linksetStart";
    public static String PROP_DYNAMIC_ADDR = "dynamicAddr";
    public static String PROP_LINKSET_END = "linksetEnd";
    public static String PROP_KEYWORDS_END = "keywordsEnd";
    public static String PROP_CURR_NUM = "currNum";
    public static String PROP_QUEUE = "queue";
    public static String PROP_LINK_END = "linkEnd";
    public static String PROP_START_TIME = "startTime";
    public static String PROP_PAGINATION_START = "paginationStart";
    public static String PROP_SITE = "site";
    public static String PROP_TOTAL_ITEM = "totalItem";
    public static String PROP_CURR_ITEM = "currItem";
    public static String PROP_NAME = "name";
    public static String PROP_STATUS = "status";
    public static String PROP_PAUSE_TIME = "pauseTime";
    public static String PROP_TITLE_START = "titleStart";
    public static String PROP_TITLE_END = "titleEnd";
    public static String PROP_CONTENT_END = "contentEnd";
    public static String PROP_PAGE_ENCODING = "pageEncoding";
    public static String PROP_ID = "id";
    public static String PROP_PLAN_LIST = "planList";
    public static String PROP_END_TIME = "endTime";
    public static String PROP_KEYWORDS_START = "keywordsStart";
    public static String PROP_DESCRIPTION_START = "descriptionStart";
    public static String PROP_DYNAMIC_END = "dynamicEnd";
    public BaseCmsAcquisition() {
        initialize();
    }
    public BaseCmsAcquisition(java.lang.Integer id) {
        this.setId(id);
        initialize();
    }
    public BaseCmsAcquisition(java.lang.Integer id, com.jeecms.cms.entity.main.CmsUser user, com.jeecms.cms.entity.main.ContentType type, com.jeecms.cms.entity.main.CmsSite site, com.jeecms.cms.entity.main.Channel channel, java.lang.String name, java.lang.Integer status, java.lang.Integer currNum, java.lang.Integer currItem, java.lang.Integer totalItem, java.lang.Integer pauseTime, java.lang.String pageEncoding, java.lang.Integer queue) {
        this.setId(id);
        this.setUser(user);
        this.setType(type);
        this.setSite(site);
        this.setChannel(channel);
        this.setName(name);
        this.setStatus(status);
        this.setCurrNum(currNum);
        this.setCurrItem(currItem);
        this.setTotalItem(totalItem);
        this.setPauseTime(pauseTime);
        this.setPageEncoding(pageEncoding);
        this.setQueue(queue);
        initialize();
    }
    protected void initialize() {
    }
    private int hashCode = Integer.MIN_VALUE;
    private java.lang.Integer id;
    private java.lang.String name;
    private java.util.Date startTime;
    private java.util.Date endTime;
    private java.lang.Integer status;
    private java.lang.Integer currNum;
    private java.lang.Integer currItem;
    private java.lang.Integer totalItem;
    private java.lang.Integer pauseTime;
    private java.lang.String pageEncoding;
    private java.lang.String planList;
    private java.lang.String dynamicAddr;
    private java.lang.Integer dynamicStart;
    private java.lang.Integer dynamicEnd;
    private java.lang.String linksetStart;
    private java.lang.String linksetEnd;
    private java.lang.String linkStart;
    private java.lang.String linkEnd;
    private java.lang.String titleStart;
    private java.lang.String titleEnd;
    private java.lang.String keywordsStart;
    private java.lang.String keywordsEnd;
    private java.lang.String descriptionStart;
    private java.lang.String descriptionEnd;
    private java.lang.String contentStart;
    private java.lang.String contentEnd;
    private java.lang.String paginationStart;
    private java.lang.String paginationEnd;
    private java.lang.Integer queue;
    private com.jeecms.cms.entity.main.CmsUser user;
    private com.jeecms.cms.entity.main.ContentType type;
    private com.jeecms.cms.entity.main.CmsSite site;
    private com.jeecms.cms.entity.main.Channel channel;
    public java.lang.Integer getId() {
        return id;
    }
    public void setId(java.lang.Integer id) {
        this.id = id;
        this.hashCode = Integer.MIN_VALUE;
    }
    public java.lang.String getName() {
        return name;
    }
    public void setName(java.lang.String name) {
        this.name = name;
    }
    public java.util.Date getStartTime() {
        return startTime;
    }
    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }
    public java.util.Date getEndTime() {
        return endTime;
    }
    public void setEndTime(java.util.Date endTime) {
        this.endTime = endTime;
    }
    public java.lang.Integer getStatus() {
        return status;
    }
    public void setStatus(java.lang.Integer status) {
        this.status = status;
    }
    public java.lang.Integer getCurrNum() {
        return currNum;
    }
    public void setCurrNum(java.lang.Integer currNum) {
        this.currNum = currNum;
    }
    public java.lang.Integer getCurrItem() {
        return currItem;
    }
    public void setCurrItem(java.lang.Integer currItem) {
        this.currItem = currItem;
    }
    public java.lang.Integer getTotalItem() {
        return totalItem;
    }
    public void setTotalItem(java.lang.Integer totalItem) {
        this.totalItem = totalItem;
    }
    public java.lang.Integer getPauseTime() {
        return pauseTime;
    }
    public void setPauseTime(java.lang.Integer pauseTime) {
        this.pauseTime = pauseTime;
    }
    public java.lang.String getPageEncoding() {
        return pageEncoding;
    }
    public void setPageEncoding(java.lang.String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }
    public java.lang.String getPlanList() {
        return planList;
    }
    public void setPlanList(java.lang.String planList) {
        this.planList = planList;
    }
    public java.lang.String getDynamicAddr() {
        return dynamicAddr;
    }
    public void setDynamicAddr(java.lang.String dynamicAddr) {
        this.dynamicAddr = dynamicAddr;
    }
    public java.lang.Integer getDynamicStart() {
        return dynamicStart;
    }
    public void setDynamicStart(java.lang.Integer dynamicStart) {
        this.dynamicStart = dynamicStart;
    }
    public java.lang.Integer getDynamicEnd() {
        return dynamicEnd;
    }
    public void setDynamicEnd(java.lang.Integer dynamicEnd) {
        this.dynamicEnd = dynamicEnd;
    }
    public java.lang.String getLinksetStart() {
        return linksetStart;
    }
    public void setLinksetStart(java.lang.String linksetStart) {
        this.linksetStart = linksetStart;
    }
    public java.lang.String getLinksetEnd() {
        return linksetEnd;
    }
    public void setLinksetEnd(java.lang.String linksetEnd) {
        this.linksetEnd = linksetEnd;
    }
    public java.lang.String getLinkStart() {
        return linkStart;
    }
    public void setLinkStart(java.lang.String linkStart) {
        this.linkStart = linkStart;
    }
    public java.lang.String getLinkEnd() {
        return linkEnd;
    }
    public void setLinkEnd(java.lang.String linkEnd) {
        this.linkEnd = linkEnd;
    }
    public java.lang.String getTitleStart() {
        return titleStart;
    }
    public void setTitleStart(java.lang.String titleStart) {
        this.titleStart = titleStart;
    }
    public java.lang.String getTitleEnd() {
        return titleEnd;
    }
    public void setTitleEnd(java.lang.String titleEnd) {
        this.titleEnd = titleEnd;
    }
    public java.lang.String getKeywordsStart() {
        return keywordsStart;
    }
    public void setKeywordsStart(java.lang.String keywordsStart) {
        this.keywordsStart = keywordsStart;
    }
    public java.lang.String getKeywordsEnd() {
        return keywordsEnd;
    }
    public void setKeywordsEnd(java.lang.String keywordsEnd) {
        this.keywordsEnd = keywordsEnd;
    }
    public java.lang.String getDescriptionStart() {
        return descriptionStart;
    }
    public void setDescriptionStart(java.lang.String descriptionStart) {
        this.descriptionStart = descriptionStart;
    }
    public java.lang.String getDescriptionEnd() {
        return descriptionEnd;
    }
    public void setDescriptionEnd(java.lang.String descriptionEnd) {
        this.descriptionEnd = descriptionEnd;
    }
    public java.lang.String getContentStart() {
        return contentStart;
    }
    public void setContentStart(java.lang.String contentStart) {
        this.contentStart = contentStart;
    }
    public java.lang.String getContentEnd() {
        return contentEnd;
    }
    public void setContentEnd(java.lang.String contentEnd) {
        this.contentEnd = contentEnd;
    }
    public java.lang.String getPaginationStart() {
        return paginationStart;
    }
    public void setPaginationStart(java.lang.String paginationStart) {
        this.paginationStart = paginationStart;
    }
    public java.lang.String getPaginationEnd() {
        return paginationEnd;
    }
    public void setPaginationEnd(java.lang.String paginationEnd) {
        this.paginationEnd = paginationEnd;
    }
    public java.lang.Integer getQueue() {
        return queue;
    }
    public void setQueue(java.lang.Integer queue) {
        this.queue = queue;
    }
    public com.jeecms.cms.entity.main.CmsUser getUser() {
        return user;
    }
    public void setUser(com.jeecms.cms.entity.main.CmsUser user) {
        this.user = user;
    }
    public com.jeecms.cms.entity.main.ContentType getType() {
        return type;
    }
    public void setType(com.jeecms.cms.entity.main.ContentType type) {
        this.type = type;
    }
    public com.jeecms.cms.entity.main.CmsSite getSite() {
        return site;
    }
    public void setSite(com.jeecms.cms.entity.main.CmsSite site) {
        this.site = site;
    }
    public com.jeecms.cms.entity.main.Channel getChannel() {
        return channel;
    }
    public void setChannel(com.jeecms.cms.entity.main.Channel channel) {
        this.channel = channel;
    }
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof com.jeecms.cms.entity.assist.CmsAcquisition)) return false; else {
            com.jeecms.cms.entity.assist.CmsAcquisition cmsAcquisition = (com.jeecms.cms.entity.assist.CmsAcquisition) obj;
            if (null == this.getId() || null == cmsAcquisition.getId()) return false; else return (this.getId().equals(cmsAcquisition.getId()));
        }
    }
    public int hashCode() {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode(); else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }
    public String toString() {
        return super.toString();
    }
}
