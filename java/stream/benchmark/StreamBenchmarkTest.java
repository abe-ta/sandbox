package sandbox;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Collection stream と foreach のパフォーマンス比較
 * JMH を使用。
 * 参考：
 * https://bufferings.hatenablog.com/entry/2018/10/14/232631
 * https://stackoverflow.com/questions/57423942/why-is-the-java-stream-foreach-method-faster-than-other-loops-in-certain-situati
 * <p>
 * maven dependency:
 *
 * <dependency>
 * <groupId>org.openjdk.jmh</groupId>
 * <artifactId>jmh-core</artifactId>
 * <version>1.19</version>
 * <scope>test</scope>
 * </dependency>
 * <dependency>
 * <groupId>org.openjdk.jmh</groupId>
 * <artifactId>jmh-generator-annprocess</artifactId>
 * <version>1.19</version>
 * <scope>test</scope>
 * </dependency>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class StreamBenchmarkTest {

    @Param({"100", "1000", "10000", "100000"})
    public int setSize;

    public List<BigDecimal> data;

    @Setup
    public void setup() {
        data = new ArrayList<BigDecimal>();
        for (long i = setSize; i-- > 0; ) {
            data.add(new BigDecimal(i));
        }
    }

    @Benchmark
    public void streamBigDecimalToLongSetBh(Blackhole bh) {
        data.stream().map(BigDecimal::longValue).forEach(bh::consume);
    }

    @Benchmark
    public Set<Long> streamBigDecimalToLongSet() {
        return data.stream().map(BigDecimal::longValue).collect(Collectors.toSet());
    }


    @Benchmark
    public void foreachBigDecimalToLongSetBh(Blackhole bh) {
        for (BigDecimal d : data) {
            bh.consume(d.longValue());
        }
    }

    @Benchmark
    public Set<Long> foreachBigDecimalToLongSet() {
        Set<Long> s = new HashSet<>();
        for (BigDecimal d : data) {
            s.add(d.longValue());
        }
        return s;
    }

    @Benchmark
    public void simpleForBigDecimalToLongSetBh(Blackhole bh) {
        for (int i = 0; i < setSize; i++) {
            bh.consume(data.get(i).longValue());
        }
    }

    @Benchmark
    public Set<Long> simpleForBigDecimalToLongSet() {
        Set<Long> s = new HashSet<>();
        for (int i = 0; i < setSize; i++) {
            s.add(data.get(i).longValue());
        }
        return s;
    }

    public static void main(String... args) throws RunnerException {
        Options opts = new OptionsBuilder()
            .include(".*")
            .warmupIterations(1)
            .measurementIterations(5)
            .jvmArgs("-Xms2g", "-Xmx2g")
            .shouldDoGC(true)
            .forks(1)
            .build();

        new Runner(opts).run();
    }
}
