public class BenchInfo {
    Benchmark benchmark;
    String name;
    long time;
    float weight;
    String[] args;
    BenchInfo(Benchmark benchmark, String name, float weight, String[] args) {
        this.benchmark = benchmark;
        this.name = name;
        this.weight = weight;
        this.args = args;
        this.time = -1;
    }
    void runBenchmark() throws Exception {
        time = benchmark.run(args);
    }
    public Benchmark getBenchmark() {
        return benchmark;
    }
    public String getName() {
        return name;
    }
    public long getTime() {
        return time;
    }
    public float getWeight() {
        return weight;
    }
}
