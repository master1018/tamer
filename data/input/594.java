public class test {
    @Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.firstName", trim = true, message = "You must enter a first name."), @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.lastName", trim = true, message = "You must enter a last name."), @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.company", trim = true, message = "You must enter a company.") }, requiredFields = { @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "user.email", message = "You must enter an email address.") }, emails = { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "user.email", message = "You must enter a valid email address.") }, stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "user.username", message = "The user name must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "user.firstName", message = "The first name must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "user.lastName", message = "The last name must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "user.company", message = "The company name must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "user.email", message = "The email address must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25", fieldName = "user.phone", message = "The phone number must be shorter than ${maxLength} characters."), @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25", fieldName = "user.fax", message = "The fax number must be shorter than ${maxLength} characters.") })
    public String saveForOpenId() {
        if (session.get("OpenIdUserObject") == null) {
            addActionError(getText("class.SignupAction.error.no_openid_token_found"));
        }
        final User openIdUser = (User) session.get("OpenIdUserObject");
        user.setUsername(openIdUser.getUsername());
        user.setUserAuthenticationType(UserAuthenticationType.OPEN_ID);
        final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(ServletActionContext.getRequest().getRemoteHost(), recaptcha_challenge_field, recaptcha_response_field);
        if (!reCaptchaResponse.isValid()) {
            addActionError(super.getText("class.SignupAction.error.not.a.good.captcha"));
            return INPUT;
        }
        user.setPassword(stringDigester.digest(PasswordGenerator.generatePassword()));
        try {
            userService.addUser(user, Boolean.TRUE);
        } catch (DuplicateUserException e) {
            LOGGER.warn(e.getMessage());
            addFieldError("username", getText("class._ALL.error.duplicateEmail"));
            return INPUT;
        }
        addActionMessage(getText("class.SignupAction.success"));
        return SUCCESS;
    }
}
