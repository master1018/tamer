public class StkLauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_LAUNCH_APP);
        startService(new Intent(this, StkAppService.class).putExtras(args));
        finish();
    }
}
