public class SubprojectAction extends ActionSupport implements GenericActionInterface {
    private static final long serialVersionUID = -8802591889117560853L;
    private int id;
    private List<Subproject> subprojects;
    private List<Factory> factories;
    private List<Project> projects;
    private Subproject subproject;
    public int getId() {
        return id;
    }
    public List<Subproject> getSubprojects() {
        return subprojects;
    }
    public List<Factory> getFactories() {
        return factories;
    }
    public List<Project> getProjects() {
        return projects;
    }
    public Subproject getSubproject() {
        return subproject;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setSubprojects(List<Subproject> subprojects) {
        this.subprojects = subprojects;
    }
    public void setFactories(List<Factory> factories) {
        this.factories = factories;
    }
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
    public void setSubproject(Subproject subproject) {
        this.subproject = subproject;
    }
    @Override
    public String execute() {
        subprojects = SubprojectManager.getAllSubprojects();
        return SUCCESS;
    }
    public void validateDoShowForm() {
        try {
            id = Utilities.checkValidId(ServletActionContext.getRequest().getParameter("id"));
            if (!SubprojectManager.checkSubprojectExists(id)) {
                addActionError(getText("error.subproject.id"));
            }
        } catch (NullIdParameterException e) {
        } catch (NotValidIdParameterException e) {
            addActionError(getText("error.subproject.id"));
        }
        if (hasActionErrors()) {
            subprojects = SubprojectManager.getAllSubprojects();
        }
    }
    public String showForm() throws Exception {
        projects = ProjectManager.getAllProjects();
        factories = FactoryManager.getAllFactories();
        if (id > 0) {
            subproject = SubprojectManager.getSubproject(id);
        }
        return SUCCESS;
    }
    public void validateDoSave() {
        if (subproject != null) {
            if (subproject.getProject() != null) {
                try {
                    subproject.setProject(ProjectManager.getProject(subproject.getProject().getId()));
                } catch (ProjectNotFoundException e) {
                    addFieldError("error.project_required", getText("error.subproject.project.required"));
                }
            } else {
                addFieldError("error.project_required", getText("error.subproject.project.required"));
            }
            if (subproject.getFactory() != null) {
                try {
                    subproject.setFactory(FactoryManager.getFactory(subproject.getFactory().getId()));
                } catch (FactoryNotFoundException e) {
                    addFieldError("error.factory_required", getText("error.subproject.factory.required"));
                }
            } else {
                addFieldError("error.factory_required", getText("error.subproject.factory.required"));
            }
            if (Utilities.isEmptyString(subproject.getName())) {
                addFieldError("error.subproject.name", getText("error.subproject.name"));
            }
        } else {
            addActionError(getText("error.general"));
        }
        if (hasActionErrors() || hasErrors() || hasFieldErrors()) {
            projects = ProjectManager.getAllProjects();
            factories = FactoryManager.getAllFactories();
        }
        if (hasFieldErrors()) {
            addFieldError("error.required_fields", getText("error.required_fields"));
        }
    }
    public String save() {
        SubprojectManager.saveSubproject(subproject);
        addActionMessage(getText("message.subproject.added_successfully"));
        return SUCCESS;
    }
    public void validateDoEdit() {
        if (subproject != null) {
            try {
                Utilities.checkValidId(subproject.getId());
                SubprojectManager.getSubproject(subproject.getId());
                if (subproject.getProject() != null) {
                    subproject.setProject(ProjectManager.getProject(subproject.getProject().getId()));
                } else {
                    addFieldError("error.project_required", getText("error.subproject.project.required"));
                }
                if (subproject.getFactory() != null) {
                    subproject.setFactory(FactoryManager.getFactory(subproject.getFactory().getId()));
                } else {
                    addFieldError("error.factory_required", getText("error.subproject.factory.required"));
                }
            } catch (FactoryNotFoundException e) {
                addFieldError("error.factory_required", getText("error.subproject.factory.required"));
            } catch (ProjectNotFoundException e) {
                addFieldError("error.project_required", getText("error.subproject.project.required"));
            } catch (NotValidIdParameterException e) {
                addActionError(getText("error.subproject.id"));
            } catch (SubprojectNotFoundException e) {
                addActionError(getText("error.subproject.id"));
            }
            if (Utilities.isEmptyString(subproject.getName())) {
                addFieldError("error.subproject.name", getText("error.subproject.name"));
            }
        } else {
            addActionError(getText("error.general"));
        }
        if (hasActionErrors() || hasErrors() || hasFieldErrors()) {
            projects = ProjectManager.getAllProjects();
            factories = FactoryManager.getAllFactories();
        }
        if (hasFieldErrors()) {
            addFieldError("error.required_fields", getText("error.required_fields"));
        }
    }
    public String edit() {
        SubprojectManager.saveSubproject(subproject);
        addActionMessage(getText("message.subproject.updated_successfully"));
        return SUCCESS;
    }
    public void validateDoDelete() {
        checkSubprojectExists();
    }
    public String delete() {
        SubprojectManager.removeSubproject(id);
        addActionMessage(getText("message.subproject.deleted_successfully"));
        return SUCCESS;
    }
    public void validateDoGet() {
        checkSubprojectExists();
    }
    public String get() throws SubprojectNotFoundException {
        subproject = SubprojectManager.getSubproject(id);
        return SUCCESS;
    }
    public void validateDoUpdateMeasures() {
        checkSubprojectExists();
    }
    public String updateMeasures() {
        String result = ERROR;
        try {
            SubprojectManager.updateMeasures(id, subproject);
            result = SUCCESS;
            addActionMessage(getText("message.subproject.measures_updated_successfully"));
        } catch (SubprojectNotFoundException e) {
            addActionError(getText("error.subproject.id"));
        } catch (SecurityException e) {
            addActionError(getText("exception.security"));
        } catch (IllegalArgumentException e) {
            addActionError(getText("exception.illegal_argument"));
        } catch (NoSuchMethodException e) {
            addActionError(getText("exception.no_such_method"));
        } catch (IllegalAccessException e) {
            addActionError(getText("exception.illegal_access"));
        } catch (InvocationTargetException e) {
            addActionError(getText("exception.invocation_target"));
        } catch (Exception e) {
            addActionError(getText("exception.generic"));
        }
        return result;
    }
    private void checkSubprojectExists() {
        try {
            id = Utilities.checkValidId(ServletActionContext.getRequest().getParameter("id"));
            if (!SubprojectManager.checkSubprojectExists(id)) {
                addActionError(getText("error.subproject.id"));
            }
        } catch (NullIdParameterException e1) {
            addActionError(getText("error.subproject.id"));
        } catch (NotValidIdParameterException e1) {
            addActionError(getText("error.subproject.id"));
        }
        if (hasActionErrors()) {
            subprojects = SubprojectManager.getAllSubprojects();
        }
    }
}
