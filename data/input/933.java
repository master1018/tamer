public class ComponentInfoForm extends ValidatorForm {
    public static final long serialVersionUID = 1;
    private String environmentDescription = "";
    private String functionDescription = "";
    private String isPublic = "Y";
    private String admins = "";
    private String devlopers = "";
    private String developmentPlatforms = "";
    private String developmentLanguages = "";
    private String authors = "";
    private String alias = "";
    private String name = "";
    private String version = "1.0";
    private String status = "Unconfirmed";
    private String serialno = "";
    private String confirmedDate = "N/A";
    private String confirmedBy = "N/A";
    private String createdDate = "";
    private String createdBy = "";
    private String updatedDate = "";
    private String updatedBy = "";
    private String extractedFunction = "";
    private FormFile file = null;
    private String projsn = "";
    public String getConfirmedBy() {
        return confirmedBy;
    }
    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
    public String getConfirmedDate() {
        return confirmedDate;
    }
    public void setConfirmedDate(String confirmedDate) {
        this.confirmedDate = confirmedDate;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public String getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    public String getUpdatedBy() {
        return updatedBy;
    }
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    public String getUpdatedDate() {
        return updatedDate;
    }
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
    public String getAdmins() {
        return admins;
    }
    public void setAdmins(String admins) {
        this.admins = admins;
    }
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public String getAuthors() {
        return authors;
    }
    public void setAuthors(String authors) {
        this.authors = authors;
    }
    public String getDevelopmentLanguages() {
        return developmentLanguages;
    }
    public void setDevelopmentLanguages(String developmentLanguages) {
        this.developmentLanguages = developmentLanguages;
    }
    public String getDevelopmentPlatforms() {
        return developmentPlatforms;
    }
    public void setDevelopmentPlatforms(String developmentPlatforms) {
        this.developmentPlatforms = developmentPlatforms;
    }
    public String getDevlopers() {
        return devlopers;
    }
    public void setDevlopers(String devlopers) {
        this.devlopers = devlopers;
    }
    public String getEnvironmentDescription() {
        return environmentDescription;
    }
    public void setEnvironmentDescription(String environmentDescription) {
        this.environmentDescription = environmentDescription;
    }
    public String getFunctionDescription() {
        return functionDescription;
    }
    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }
    public String getIsPublic() {
        return isPublic;
    }
    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getSerialno() {
        if (this.serialno.equals("")) {
            this.serialno = GenerateSerialUtil.getComsn();
        }
        return this.serialno;
    }
    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }
    public String getExtractedFunction() {
        if (this.functionDescription == null) {
            return extractedFunction;
        }
        if (this.functionDescription.length() <= 50) this.extractedFunction = this.functionDescription; else {
            this.extractedFunction = this.functionDescription.substring(0, 50);
            this.extractedFunction += "...";
        }
        return extractedFunction;
    }
    public FormFile getFile() {
        return file;
    }
    public void setFile(FormFile file) {
        this.file = file;
    }
    public String getProjsn() {
        return projsn;
    }
    public void setProjsn(String projsn) {
        this.projsn = projsn;
    }
}
