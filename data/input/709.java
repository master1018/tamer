public class BookDaoJpa extends BaseDaoJpa<Book> implements BookDao {
    private int maxRecords = 250;
    public BookDaoJpa() {
        super(Book.class, "Book");
    }
    public List<Book> loadByFilter(BookSearchRequest searchRequest) {
        String queryStr = "select b from Book as b where lower(title) like lower(:title) and isbn like :isbn ";
        Query query = getEntityManager().createQuery(queryStr).setParameter("title", '%' + searchRequest.getBookTitle() + '%').setParameter("isbn", '%' + searchRequest.getBookIsbn() + '%').setMaxResults(maxRecords);
        return query.getResultList();
    }
    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }
}
