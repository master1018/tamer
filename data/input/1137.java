@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class HttpDataReaderServiceTest extends AbstractBaseServiceTest {
    @Autowired
    @Qualifier("httpDataReaderService")
    private HttpDataReaderService httpDataReaderService;
    @Test
    public void testInit() throws ParseException {
        this.httpDataReaderService.setWeatherStation(WeatherStations.INOORDBR35_BOXMEER);
        try {
            httpDataReaderService.init();
            String line = "";
            do {
                line = httpDataReaderService.getNextLine();
                System.out.println(line);
            } while (line != null);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
    @Test
    public void testGetCurrentData() {
        System.out.println("========================================");
        this.httpDataReaderService.setWeatherStation(WeatherStations.INOORDBR35_BOXMEER);
        this.printWeatherStationData(httpDataReaderService.getWeatherStation(), httpDataReaderService.getCurrentData());
        this.httpDataReaderService.setWeatherStation(WeatherStations.INORDRHE72_DORTMUND);
        this.printWeatherStationData(httpDataReaderService.getWeatherStation(), httpDataReaderService.getCurrentData());
        this.httpDataReaderService.setWeatherStation(WeatherStations.INRWKLEV2_KLEVE);
        this.printWeatherStationData(httpDataReaderService.getWeatherStation(), httpDataReaderService.getCurrentData());
        this.httpDataReaderService.setWeatherStation(WeatherStations.IDRENTHE48_COEVORDEN);
        this.printWeatherStationData(httpDataReaderService.getWeatherStation(), httpDataReaderService.getCurrentData());
        this.httpDataReaderService.setWeatherStation(WeatherStations.IZEELAND13_GOES);
        this.printWeatherStationData(httpDataReaderService.getWeatherStation(), httpDataReaderService.getCurrentData());
    }
    @Test
    public void testGetDataByDate() throws Exception {
        Date weatherDate = new DateTime(2011, 8, 15, 0, 0, 0).toDate();
        this.httpDataReaderService.setWeatherStation(WeatherStations.INOORDBR35_BOXMEER);
        this.httpDataReaderService.setHttpProxy(new HttpProxy());
        this.httpDataReaderService.setWeatherDate(weatherDate);
        DataSet minTemp = this.httpDataReaderService.minTemperature();
        this.printWeatherStationData(this.httpDataReaderService.getWeatherStation(), minTemp);
        DataSet maxTemp = this.httpDataReaderService.maxTemperature();
        this.printWeatherStationData(this.httpDataReaderService.getWeatherStation(), maxTemp);
    }
    @Test
    public void testGraphSpanMonth() throws Exception {
        this.httpDataReaderService.setWeatherStation(WeatherStations.INOORDBR35_BOXMEER);
        this.httpDataReaderService.setHttpProxy(new HttpProxy());
        this.httpDataReaderService.setDataGraphSpan(DataGraphSpan.MONTH);
        this.httpDataReaderService.setWeatherDate(new DateTime(2012, 2, 1, 0, 0, 0).toDate());
        List<DataSet> monthly = this.httpDataReaderService.readDataSets();
        for (DataSet dataSet : monthly) {
            System.out.println(dataSet.getDateTime() + "\t" + dataSet.getTemperatureHigh() + "\t" + dataSet.getTemperatureLow());
        }
    }
}
