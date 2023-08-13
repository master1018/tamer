public class JTop extends JPanel {
    private static final long serialVersionUID = -1499762160973870696L;
    private MBeanServerConnection server;
    private ThreadMXBean tmbean;
    private MyTableModel tmodel;
    public JTop() {
        super(new GridLayout(1,0));
        tmodel = new MyTableModel();
        JTable table = new JTable(tmodel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.setDefaultRenderer(Double.class, new DoubleRenderer());
        table.setIntercellSpacing(new Dimension(6,3));
        table.setRowHeight(table.getRowHeight() + 4);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }
    public void setMBeanServerConnection(MBeanServerConnection mbs) {
        this.server = mbs;
        try {
            this.tmbean = newPlatformMXBeanProxy(server,
                                                 THREAD_MXBEAN_NAME,
                                                 ThreadMXBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!tmbean.isThreadCpuTimeSupported()) {
            System.err.println("This VM does not support thread CPU time monitoring");
        } else {
            tmbean.setThreadCpuTimeEnabled(true);
        }
    }
    class MyTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -7877310288576779514L;
        private String[] columnNames = {"ThreadName",
                                        "CPU(sec)",
                                        "State"};
        private List<Map.Entry<Long, ThreadInfo>> threadList =
            Collections.emptyList();
        public MyTableModel() {
        }
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        @Override
        public int getRowCount() {
            return threadList.size();
        }
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<Long, ThreadInfo> me = threadList.get(row);
            switch (col) {
                case 0 :
                    return me.getValue().getThreadName();
                case 1 :
                    long ns = me.getKey().longValue();
                    double sec = ns / 1000000000;
                    return new Double(sec);
                case 2 :
                    return me.getValue().getThreadState();
                default:
                    return null;
            }
        }
        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        void setThreadList(List<Map.Entry<Long, ThreadInfo>> list) {
            threadList = list;
        }
    }
    private List<Map.Entry<Long, ThreadInfo>> getThreadList() {
        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids);
        SortedMap<Long, ThreadInfo> map = new TreeMap<Long, ThreadInfo>();
        for (int i = 0; i < tids.length; i++) {
            long cpuTime = tmbean.getThreadCpuTime(tids[i]);
            if (cpuTime != -1 && tinfos[i] != null) {
                map.put(new Long(cpuTime), tinfos[i]);
            }
        }
        Set<Map.Entry<Long, ThreadInfo>> set = map.entrySet();
        List<Map.Entry<Long, ThreadInfo>> list =
            new ArrayList<Map.Entry<Long, ThreadInfo>>(set);
        Collections.reverse(list);
        return list;
    }
    class DoubleRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1704639497162584382L;
        NumberFormat formatter;
        public DoubleRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
        @Override
        public void setValue(Object value) {
            if (formatter==null) {
                formatter = NumberFormat.getInstance();
                formatter.setMinimumFractionDigits(4);
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
    class Worker extends SwingWorker<List<Map.Entry<Long, ThreadInfo>>,Object> {
        private MyTableModel tmodel;
        Worker(MyTableModel tmodel) {
            this.tmodel = tmodel;
        }
        @Override
        public List<Map.Entry<Long, ThreadInfo>> doInBackground() {
            return getThreadList();
        }
        @Override
        protected void done() {
            try {
                tmodel.setThreadList(get());
                tmodel.fireTableDataChanged();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }
    public SwingWorker<?,?> newSwingWorker() {
        return new Worker(tmodel);
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            usage();
        }
        String[] arg2 = args[0].split(":");
        if (arg2.length != 2) {
            usage();
        }
        String hostname = arg2[0];
        int port = -1;
        try {
            port = Integer.parseInt(arg2[1]);
        } catch (NumberFormatException x) {
            usage();
        }
        if (port < 0) {
            usage();
        }
        final JTop jtop = new JTop();
        MBeanServerConnection server = connect(hostname, port);
        jtop.setMBeanServerConnection(server);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                jtop.newSwingWorker().execute();
            }
        };
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI(jtop);
            }
        });
        Timer timer = new Timer("JTop Sampling thread");
        timer.schedule(timerTask, 0, 2000);
    }
    private static MBeanServerConnection connect(String hostname, int port) {
        String urlPath = "/jndi/rmi:
        MBeanServerConnection server = null;
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            server = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
        return server;
    }
    private static void usage() {
        System.out.println("Usage: java JTop <hostname>:<port>");
        System.exit(1);
    }
    private static void createAndShowGUI(JPanel jtop) {
        JFrame frame = new JFrame("JTop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent contentPane = (JComponent) frame.getContentPane();
        contentPane.add(jtop, BorderLayout.CENTER);
        contentPane.setOpaque(true); 
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setVisible(true);
    }
}
