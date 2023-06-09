package com.wideplay.warp.widgets.rendering.control;

import com.wideplay.warp.widgets.MvelEvaluator;
import com.wideplay.warp.widgets.Renderable;
import com.wideplay.warp.widgets.Respond;
import com.wideplay.warp.widgets.RespondersForTesting;
import com.wideplay.warp.widgets.compiler.EvaluatorCompiler;
import com.wideplay.warp.widgets.compiler.ExpressionCompileException;
import com.wideplay.warp.widgets.compiler.MvelEvaluatorCompiler;
import com.wideplay.warp.widgets.routing.PageBook;
import static org.easymock.EasyMock.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class EmbedWidgetTest {

    private static final String PAGES_FOR_EMBEDDING = "pagesForEmbedding";

    private static final String PAGES_FOR_EMBEDDING_BROKEN = "pagesForEmbeddingBroken";

    private static final String PAGES_FOR_EMBEDDING_BROKEN_EXCEPTION = "pagesForEmbeddingBrokenThrowing";

    private static final String HELLO_FROM_INCLUDE = "helloFromEmbed";

    @DataProvider(name = PAGES_FOR_EMBEDDING)
    public Object[][] getPages() {
        return new Object[][] { { "MyEmbeddedPage", "aString", "message=pass" }, { "YourPage", "anotherString", "message=pass" }, { "YourPage2", "aaaa", "message='aaaa'" }, { "YourPage3", "aaaa", "message=\"aaaa\"" }, { "YourPage4", "bbbb", "message=\"aaaa\", message='bb' + 'bb'" }, { "YourPage5", "cccc", "message=\"aaaa\", message='bb' + 'bb', message = getPass()" } };
    }

    @DataProvider(name = EmbedWidgetTest.PAGES_FOR_EMBEDDING_BROKEN)
    public Object[][] getPagesBroken() {
        return new Object[][] { { "YourPage", "anotherString", "  " }, { "YourPage", "anotherString", "" }, { "YourPage", "anotherString", ",," }, { "YourPage2", "aaaa", "message='aa'" }, { "YourPage2", "aaaa", "message='aa'," }, { "YourPage3", "aaaa", "message=\"aaa\"" }, { "YourPage4", "bbbbb", "message=\"aaaa\", message='bb' + 'bb'" }, { "YourPage4", "bbbbb", "message=\"aaaa\", message='bb' + 'bb',," } };
    }

    @DataProvider(name = EmbedWidgetTest.PAGES_FOR_EMBEDDING_BROKEN_EXCEPTION)
    public Object[][] getPagesBrokenThrowing() {
        return new Object[][] { { "MyEmbeddedPage", "aString", "=pass" }, { "YourPage", "anotherString", "message=" }, { "YourPage", "anotherString", "message=pass=pass" } };
    }

    public static class MyParentPage {

        private String pass;

        MyParentPage(String pass) {
            this.pass = pass;
        }

        public String getPass() {
            return pass;
        }
    }

    public static class MyEmbeddedPage {

        private boolean set;

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
            assert null != message;
            assert !"".equals(message.trim());
            set = true;
        }

        public boolean isSet() {
            return set;
        }
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING)
    public final void pageEmbeddingSupressesNormalWidgetChain(String pageName, final String passOn, final String expression) throws ExpressionCompileException {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond respond = RespondersForTesting.newRespond();
        final Renderable widget = createMock(Renderable.class);
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(widget);
        widget.render(eq(myEmbeddedPage), isA(Respond.class));
        replay(pageBook, page, widget);
        final MvelEvaluator evaluator = new MvelEvaluator();
        final WidgetChain widgetChain = new ProceedingWidgetChain();
        final WidgetChain targetWidgetChain = new ProceedingWidgetChain();
        targetWidgetChain.addWidget(new XmlWidget(new TerminalWidgetChain(), "p", createMock(EvaluatorCompiler.class), Collections.EMPTY_MAP));
        widgetChain.addWidget(new ShowIfWidget(targetWidgetChain, "true", evaluator));
        final EmbedWidget embedWidget = new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, evaluator, pageBook, pageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), respond);
        assert myEmbeddedPage.isSet() : "variable not passed on to embedded page";
        assert passOn.equals(myEmbeddedPage.getMessage()) : "variable not set on embedded page";
        final String resp = respond.toString();
        assert "".equals(resp) : "widget not embedded correctly : " + resp;
        verify(pageBook, page, widget);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING)
    public final void pageEmbeddingChainsToEmbeddedWidget(String pageName, final String passOn, final String expression) throws ExpressionCompileException {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond respond = RespondersForTesting.newRespond();
        final MvelEvaluator evaluator = new MvelEvaluator();
        final WidgetChain widget = new ProceedingWidgetChain();
        final WidgetChain targetWidgetChain = new ProceedingWidgetChain();
        targetWidgetChain.addWidget(new XmlWidget(new TerminalWidgetChain(), "p", new MvelEvaluatorCompiler(Object.class), new LinkedHashMap<String, String>() {

            {
                put("class", "pretty");
                put("id", "a-p-tag");
            }
        }));
        widget.addWidget(new ShowIfWidget(targetWidgetChain, "true", evaluator));
        Renderable bodyWrapper = new XmlWidget(widget, "body", createMock(EvaluatorCompiler.class), Collections.<String, String>emptyMap());
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(bodyWrapper);
        replay(pageBook, page);
        final EmbedWidget embedWidget = new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, evaluator, pageBook, pageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), respond);
        assert myEmbeddedPage.isSet() : "variable not passed on to embedded page";
        assert passOn.equals(myEmbeddedPage.getMessage()) : "variable not set on embedded page";
        final String resp = respond.toString();
        assert "<p class=\"pretty\" id=\"a-p-tag\"/>".equals(resp) : "widget not embedded correctly : " + resp;
        verify(pageBook, page);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING)
    public final void pageEmbeddingChainsToEmbeddedWidgetWithArgs(String targetPageName, final String passOn, final String expression) throws ExpressionCompileException {
        targetPageName = targetPageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond respond = RespondersForTesting.newRespond();
        final MvelEvaluator evaluator = new MvelEvaluator();
        final WidgetChain widget = new ProceedingWidgetChain();
        final WidgetChain targetWidgetChain = new ProceedingWidgetChain();
        targetWidgetChain.addWidget(new XmlWidget(new ProceedingWidgetChain().addWidget(new IncludeWidget(new TerminalWidgetChain(), "'me'", evaluator)), "p", new MvelEvaluatorCompiler(Object.class), new LinkedHashMap<String, String>() {

            {
                put("class", "pretty");
                put("id", "a-p-tag");
            }
        }));
        widget.addWidget(new ShowIfWidget(targetWidgetChain, "true", evaluator));
        Renderable bodyWrapper = new XmlWidget(widget, "body", createMock(EvaluatorCompiler.class), Collections.<String, String>emptyMap());
        expect(pageBook.forName(targetPageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(bodyWrapper);
        replay(pageBook, page);
        final String includeExpr = "me";
        Map<String, ArgumentWidget> inners = new HashMap<String, ArgumentWidget>();
        inners.put(includeExpr, new ArgumentWidget(new ProceedingWidgetChain().addWidget(new TextWidget(HELLO_FROM_INCLUDE, new MvelEvaluatorCompiler(Object.class))), includeExpr, evaluator));
        final EmbedWidget embedWidget = new EmbedWidget(inners, expression, evaluator, pageBook, targetPageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), respond);
        assert myEmbeddedPage.isSet() : "variable not passed on to embedded page";
        assert passOn.equals(myEmbeddedPage.getMessage()) : "variable not set on embedded page";
        final String resp = respond.toString();
        assert String.format("<p class=\"pretty\" id=\"a-p-tag\">%s</p>", HELLO_FROM_INCLUDE).equals(resp) : "widget not embedded correctly : " + resp;
        verify(pageBook, page);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING)
    public final void pageEmbeddingChainsToEmbeddedWidgetBehavior(String pageName, final String passOn, final String expression) throws ExpressionCompileException {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond respond = RespondersForTesting.newRespond();
        final MvelEvaluator evaluator = new MvelEvaluator();
        final ProceedingWidgetChain widget = new ProceedingWidgetChain();
        final WidgetChain targetWidgetChain = new ProceedingWidgetChain();
        targetWidgetChain.addWidget(new XmlWidget(new TerminalWidgetChain(), "p", new MvelEvaluatorCompiler(Object.class), new LinkedHashMap<String, String>() {

            {
                put("class", "pretty");
                put("id", "a-p-tag");
            }
        }));
        widget.addWidget(new ShowIfWidget(targetWidgetChain, "false", evaluator));
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(widget);
        replay(pageBook, page);
        final EmbedWidget embedWidget = new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, evaluator, pageBook, pageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), respond);
        assert myEmbeddedPage.isSet() : "variable not passed on to embedded page";
        assert passOn.equals(myEmbeddedPage.getMessage()) : "variable not set on embedded page";
        final String resp = respond.toString();
        assert "".equals(resp) : "widget not embedded correctly : " + resp;
        verify(pageBook, page);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING)
    public final void pageEmbeddingAndBinding(String pageName, final String passOn, final String expression) {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond mockRespond = createNiceMock(Respond.class);
        final Renderable widget = createMock(Renderable.class);
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(widget);
        widget.render(eq(myEmbeddedPage), isA(Respond.class));
        replay(pageBook, page, mockRespond, widget);
        final EmbedWidget embedWidget = new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, new MvelEvaluator(), pageBook, pageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), mockRespond);
        assert myEmbeddedPage.isSet() : "variable not passed on to embedded page";
        assert passOn.equals(myEmbeddedPage.getMessage()) : "variable not set on embedded page";
        verify(pageBook, page, mockRespond, widget);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING_BROKEN_EXCEPTION, expectedExceptions = IllegalArgumentException.class)
    public final void failedPageEmbeddingThrowing(String pageName, final String passOn, final String expression) {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond mockRespond = createMock(Respond.class);
        final Renderable widget = createMock(Renderable.class);
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(widget);
        widget.render(myEmbeddedPage, mockRespond);
        replay(pageBook, page, mockRespond, widget);
        new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, new MvelEvaluator(), pageBook, pageName).render(new MyParentPage(passOn), mockRespond);
        verify(pageBook, page, mockRespond, widget);
    }

    @Test(dataProvider = PAGES_FOR_EMBEDDING_BROKEN)
    public final void failedPageEmbedding(String pageName, final String passOn, final String expression) {
        pageName = pageName.toLowerCase();
        final PageBook pageBook = createMock(PageBook.class);
        final PageBook.Page page = createMock(PageBook.Page.class);
        final Respond mockRespond = createNiceMock(Respond.class);
        final Renderable widget = createMock(Renderable.class);
        expect(pageBook.forName(pageName)).andReturn(page);
        final MyEmbeddedPage myEmbeddedPage = new MyEmbeddedPage();
        expect(page.instantiate()).andReturn(myEmbeddedPage);
        expect(page.widget()).andReturn(widget);
        widget.render(eq(myEmbeddedPage), isA(Respond.class));
        replay(pageBook, page, mockRespond, widget);
        final EmbedWidget embedWidget = new EmbedWidget(Collections.<String, ArgumentWidget>emptyMap(), expression, new MvelEvaluator(), pageBook, pageName);
        embedWidget.setFactory(new EmbeddedRespondFactory(RespondersForTesting.newRespond()));
        embedWidget.render(new MyParentPage(passOn), mockRespond);
        assert !passOn.equals(myEmbeddedPage.getMessage()) : "variable somehow set on embedded page";
        verify(pageBook, page, mockRespond, widget);
    }
}
