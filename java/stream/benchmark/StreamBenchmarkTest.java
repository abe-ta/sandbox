package sandbox;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<BigDecimal> data02;

    @Setup
    public void setup() {
        data = new ArrayList<BigDecimal>();
        data02 = new ArrayList<BigDecimal>();
        for (long i = setSize; i-- > 0; ) {
            data.add(new BigDecimal(i));
            data02.add(new BigDecimal(i));
        }
    }

    @Benchmark
    public void singleList_streamBigDecimalToLongSetBh(Blackhole bh) {
        bh.consume(data.stream().map(BigDecimal::longValue).collect(Collectors.toSet()));
    }

    @Benchmark
    public void singleList_foreachBigDecimalToLongSetBh(Blackhole bh) {
        Set<Long> s = new HashSet<>();
        for (BigDecimal d : data) {
            s.add(d.longValue());
        }
        bh.consume(s);
    }

    @Benchmark
    public void singleList_simpleForBigDecimalToLongSetBh(Blackhole bh) {
        Set<Long> s = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            s.add(data.get(i).longValue());
        }
        bh.consume(s);
    }

    @Benchmark
    public void multiList_streamBigDecimalToLongSet(Blackhole bh) {
        bh.consume(
            Stream.of(data, data02)
                .flatMap(Collection::stream)
                .map(BigDecimal::longValue).collect(Collectors.toSet())
        );
    }

    @Benchmark
    public void multiList_foreachBigDecimalToLongSetBh(Blackhole bh) {
        Set<Long> s = new HashSet<>();
        for (BigDecimal d : data) {
            s.add(d.longValue());
        }
        for (BigDecimal d : data02) {
            s.add(d.longValue());
        }
        bh.consume(s);
    }

    @Benchmark
    public void multiList_simpleForBigDecimalToLongSetBh(Blackhole bh) {
        Set<Long> s = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            s.add(data.get(i).longValue());
        }
        for (int i = 0; i < data02.size(); i++) {
            s.add(data02.get(i).longValue());
        }
        bh.consume(s);
    }

    public static void main(String... args) throws RunnerException {
        Options opts = new OptionsBuilder()
            .include(".*")
            .warmupIterations(1)
            .measurementIterations(3)
            .jvmArgs("-Xms2g", "-Xmx2g")
            .shouldDoGC(true)
            .forks(1)
            .build();

        new Runner(opts).run();
    }
}
