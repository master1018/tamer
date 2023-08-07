public class TarefaJpaDAO extends GenericJpaDAO<Tarefa> implements TarefaDAO {
    public TarefaJpaDAO() {
        super(Tarefa.class);
    }
}
