import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Metric/Stat is a value computed based on current game state
 */
interface IlyaPoteminMetric<T> {
    T update(IlyaPoteminHistory history);

    T get();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface IlyaPoteminInjectedMetric {
    String value();
}

abstract class IlyaPoteminAbstractMetric<T> implements IlyaPoteminMetric<T> {
    protected T value;
    protected int historyDepth;

    protected IlyaPoteminAbstractMetric(int historyDepth) {
        this.historyDepth = historyDepth;
    }

    protected abstract T doUpdate(List<IlyaPoteminRoundRecord> recordList);

    public final T update(IlyaPoteminHistory history) {
        value = Optional.ofNullable(history.getLastActions(historyDepth))
                .map(this::doUpdate)
                .orElseThrow(NullPointerException::new);
        return value;
    }

    public final T get() {
        return value;
    }

    @Override
    public String toString() {
        return "Metric(value=" + value + ")";
    }
}


class IlyaPoteminMetricFactory {

    private IlyaPoteminLogger logger = IlyaPoteminLogger.getLogger("MetricFactory");
    private static IlyaPoteminMetricFactory instance;

    public static IlyaPoteminMetricFactory getDefault() {
        if (instance == null) {
            instance = new IlyaPoteminMetricFactory();
        }
        return instance;
    }

    private String factoryName = "metricFactory@" + hashCode();
    private List<Map.Entry<String, IlyaPoteminMetric<?>>> metricList;
    private Map<String, IlyaPoteminMetric<?>> metricMap;

    public IlyaPoteminMetricFactory() {
        metricMap = new HashMap<>();
        metricList = new ArrayList<>();
    }

    public <T> void set(String name, IlyaPoteminMetric<T> metric) {
        metricMap.put(name, metric);
        if (!metricList.contains(new HashMap.SimpleEntry<String, IlyaPoteminMetric<?>>(name, metric))) {
            metricList.add(new HashMap.SimpleEntry<>(name, metric));
        }
    }

    @SuppressWarnings(value = {"unchecked"})
    public <T> IlyaPoteminMetric<T> get(String name) {
        return (IlyaPoteminMetric<T>) metricMap.getOrDefault(name, null);
    }

    @SuppressWarnings(value = {"unchecked"})
    public <T> IlyaPoteminMetric<T> getOrCreate(String name, Supplier<IlyaPoteminMetric<T>> supplier) {
        return Optional.ofNullable((IlyaPoteminMetric<T>) get(name))
                .orElseGet(supplier);
    }

    public <T> IlyaPoteminMetricFactory with(String name, Supplier<IlyaPoteminMetric<T>> metricSupplier) {
        if (metricMap.getOrDefault(name, null) == null) {
            set(name, Objects.requireNonNull(metricSupplier.get()));
        }
        return this;
    }

    public void forEach(BiConsumer<String, IlyaPoteminMetric<?>> consumer) {
        for (Map.Entry<String, IlyaPoteminMetric<?>> entry : metricList) {
            String name = entry.getKey();
            IlyaPoteminMetric<?> metric = entry.getValue();
            consumer.accept(name, metric);
            logger.info("" + factoryName + ":" + name + " = " + metric.get());
        }
    }
}

