public class CommandPresenterTest {
    private interface CommandDisplay extends HasClickHandlers, IsWidget {
    }
    protected ClickHandler clickHandler;
    @Mock
    private Command command;
    @Mock
    private CommandDisplay display;
    private CommandPresenter presenter;
    @Test
    public void callExecuteOnClick() {
        clickHandler.onClick(new ClickEvent() {
        });
        verify(command, times(1)).execute();
    }
    @Test
    public void initialState() {
        verify(display).addClickHandler(any(ClickHandler.class));
    }
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(display.addClickHandler(any(ClickHandler.class))).thenAnswer(new Answer<HandlerRegistration>() {
            @Override
            public HandlerRegistration answer(InvocationOnMock invocation) {
                clickHandler = (ClickHandler) invocation.getArguments()[0];
                return null;
            }
        });
        presenter = new CommandPresenter(display, command);
        presenter.init();
    }
}
