package ${package}.ui.presenter;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.event.ProcessClear;
import br.gov.frameworkdemoiselle.event.ProcessDelete;
import br.gov.frameworkdemoiselle.event.ProcessItemSelection;
import br.gov.frameworkdemoiselle.event.BeforeNavigateToView;
import br.gov.frameworkdemoiselle.event.ProcessSave;
import br.gov.frameworkdemoiselle.template.AbstractPresenter;
import br.gov.frameworkdemoiselle.stereotype.Presenter;
import ${package}.business.BookmarkBC;
import ${package}.business.CategoryBC;
import ${package}.domain.Bookmark;
import ${package}.domain.Category;
import ${package}.ui.view.BookmarkView;

@Presenter
public class BookmarkPresenter extends AbstractPresenter<BookmarkView> {

	private static final long serialVersionUID = 1L;

	@Inject
	private BookmarkBC bookmarkBC;

	@Inject
	private CategoryBC categoryBC;

	public void processSaveCategory(@Observes @ProcessSave Category category) {
		getView().setCategories(categoryBC.findAll());
	}

	public void processSave(@Observes @ProcessSave Bookmark bookmark) {
		if (bookmark.getId() != null) {
			bookmarkBC.update(bookmark);
		} else {
			bookmarkBC.insert(bookmark);
		}
		getView().clear();
		getView().setDeleteButtonEnabled(false);
		getView().setList(bookmarkBC.findAll());
	}

	public void processItemSelection(@Observes @ProcessItemSelection Bookmark bookmark) {
		getView().setBean(bookmark);
		getView().setDeleteButtonEnabled(true);
	}

	public void processDelete(@Observes @ProcessDelete Bookmark bookmark) {
		bookmarkBC.delete(bookmark.getId());
		getView().setDeleteButtonEnabled(false);
		getView().setList(bookmarkBC.findAll());
	}

	public void beforeNavigate(@Observes @BeforeNavigateToView BookmarkView view) {
		getView().setCategories(categoryBC.findAll());
		getView().setBean(new Bookmark());
		getView().setList(bookmarkBC.findAll());
	}

	public void processFormClear(@Observes @ProcessClear Bookmark bookmark) {
		getView().clear();
		getView().setDeleteButtonEnabled(false);
	}

}
