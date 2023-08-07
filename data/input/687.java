public class EditorTarefas extends AbsolutePanel {
    private Projeto selectedProject;
    private RemoteTaskModel model;
    private HorizontalSplitPanel pnlMain = new HorizontalSplitPanel();
    private ETPMenu emnuMenu = new ETPMenu();
    private TarefaForm frmNovaTarefa;
    private VisualizacaoTarefas viewCorrente;
    private TarefaAdminServiceAsync tarefaService = GWT.create(TarefaAdminService.class);
    public EditorTarefas() {
        this.addStyleName("etp-EditorTarefas");
        this.pnlMain.setSplitPosition("12%");
        this.pnlMain.add(this.emnuMenu);
        this.add(this.pnlMain);
        this.setupMenu();
        this.model = new RemoteTaskModel(this.tarefaService);
        this.frmNovaTarefa = new TarefaForm(this.model);
        this.frmNovaTarefa.addCadastroHandler(new ActFormTarefaCadastro());
        this.setDefaultView();
    }
    protected void setupMenu() {
        this.emnuMenu.setHeight("100%");
        this.emnuMenu.setWidth("100%");
        this.emnuMenu.addMenuItem("Novo", new ActNovaTarefa());
        this.emnuMenu.addMenuItem("WBS", new ActShowWBS());
    }
    public TaskModel getModel() {
        return model;
    }
    public void novaTarefa(Tarefa t) {
        if (this.selectedProject == null) {
            Etp.logger.show("Nenhum projeto selecionado.", LoggerType.ERROR);
            return;
        }
        t.setProjeto(this.selectedProject.getCod());
        Etp.logger.show("Cadastrando nova tarefa.", LoggerType.WAITING);
        this.model.cadastraTarefa(t, new AsyncCallback<Tarefa>() {
            @Override
            public void onFailure(Throwable caught) {
                Etp.logger.show(caught.getMessage(), LoggerType.ERROR);
            }
            @Override
            public void onSuccess(Tarefa result) {
                Etp.logger.show("Tarefa cadastrada com sucesso.", LoggerType.SUCCESS);
            }
        });
    }
    public void editarTarefa(Tarefa t) {
    }
    public void loadTarefas() {
        this.viewCorrente.clear();
        this.model.loadTasks(new AsyncCallback<List<Tarefa>>() {
            @Override
            public void onFailure(Throwable caught) {
                Etp.logger.show(caught.getMessage(), LoggerType.ERROR);
            }
            @Override
            public void onSuccess(List<Tarefa> result) {
            }
        });
    }
    public Projeto getSelectedProject() {
        return selectedProject;
    }
    public void setSelectedProject(Projeto selectedProject) {
        this.selectedProject = selectedProject;
        this.model.setProjeto(selectedProject);
        this.model.loadTasks(new AsyncCallback<List<Tarefa>>() {
            @Override
            public void onFailure(Throwable caught) {
                Etp.logger.show(caught.getMessage(), LoggerType.ERROR);
            }
            @Override
            public void onSuccess(List<Tarefa> result) {
            }
        });
    }
    public VisualizacaoTarefas getViewCorrente() {
        return viewCorrente;
    }
    public void setViewCorrente(VisualizacaoTarefas viewCorrente) {
        if (this.viewCorrente != null) {
            this.pnlMain.remove(viewCorrente);
        }
        this.viewCorrente = viewCorrente;
        this.pnlMain.add(this.viewCorrente);
    }
    public void setDefaultView() {
        this.setViewCorrente(new TaskBoxes(this.model));
    }
    private class ActFormTarefaCadastro implements FormTarefaHandler {
        @Override
        public void cadastrarTarefa(Tarefa t) {
            EditorTarefas.this.novaTarefa(t);
            Etp.logger.setVisible(false);
        }
        @Override
        public void editarTarefa(Tarefa t) {
            Etp.logger.setVisible(false);
        }
    }
    protected class CallbackAddTarefa implements AsyncCallback<Tarefa> {
        @Override
        public void onFailure(Throwable caught) {
            Etp.logger.show(caught.getMessage(), LoggerType.ERROR);
        }
        @Override
        public void onSuccess(Tarefa result) {
            Etp.logger.show("Tarefa cadastrada com sucesso.", LoggerType.SUCCESS);
        }
    }
    protected class ActNovaTarefa implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            if (EditorTarefas.this.selectedProject == null) {
                Etp.logger.show("Nenhum projeto selecionado.", LoggerType.ERROR);
                return;
            }
            frmNovaTarefa.setModal(true);
            frmNovaTarefa.novo();
            frmNovaTarefa.center();
        }
    }
    protected class ActShowWBS implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
        }
    }
}
