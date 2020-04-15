/**
 * Strategy is a player strategy
 * It expected to be created at each new round/player swap
 * Otherwise it is not guaranteed that previous actions history will not affect current round
 */
interface IlyaPoteminStrategy {
    IlyaPoteminField move(IlyaPoteminHistory history, IlyaPoteminField opponentLastMove);
}

abstract class IlyaPoteminAbstractStrategy implements IlyaPoteminStrategy {

    int metricDepth = 10;
    IlyaPoteminMetricFactory metricFactory;

    private IlyaPoteminAbstractStrategy() {
        throw new RuntimeException("Should not be called");
    }

    protected IlyaPoteminAbstractStrategy(int metricDepth) {
        metricFactory = new IlyaPoteminMetricFactory();
        metricFactory
                .with(IlyaPoteminPayoffSumMetric.NAME,
                        () -> new IlyaPoteminPayoffSumMetric(metricDepth))
                .with(IlyaPoteminFightPercentMetric.NAME,
                        () -> new IlyaPoteminFightPercentMetric(metricDepth))
                .with(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".A",
                        () -> new IlyaPoteminOpponentFieldDistributionMetric(IlyaPoteminField.A, metricDepth))
                .with(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".B",
                        () -> new IlyaPoteminOpponentFieldDistributionMetric(IlyaPoteminField.B, metricDepth))
                .with(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".C",
                        () -> new IlyaPoteminOpponentFieldDistributionMetric(IlyaPoteminField.C, metricDepth))
                .with(IlyaPoteminMostOpponentSelectedFieldMetric.NAME,
                        () -> new IlyaPoteminMostOpponentSelectedFieldMetric(metricDepth,
                                metricFactory.get(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".A"),
                                metricFactory.get(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".B"),
                                metricFactory.get(IlyaPoteminOpponentFieldDistributionMetric.NAME + ".C")));
        injectMetrics();
    }

    protected IlyaPoteminAbstractStrategy(IlyaPoteminAbstractStrategy parent) {
        metricFactory = parent.metricFactory;
        metricDepth = parent.metricDepth;
        injectMetrics();
    }

    private void injectMetric(java.lang.reflect.Field field) {
        String name = field.getAnnotation(IlyaPoteminInjectedMetric.class).value();
        try {
            field.set(this, metricFactory.get(name));
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void injectMetrics() {
        for (java.lang.reflect.Field f : getClass().getDeclaredFields()) {
            if (f.getType() == IlyaPoteminMetric.class) {
                if (f.isAnnotationPresent(IlyaPoteminInjectedMetric.class)) {
                    injectMetric(f);
                }
            }
        }
    }

    protected <T> void setMetric(String name, IlyaPoteminMetric<T> metric) {
        metricFactory.set(name, metric);
    }

    protected IlyaPoteminMetric<Double> getDoubleMetric(String name) {
        return metricFactory.get(name);
    }

    protected void updateMetrics(IlyaPoteminHistory history) {
        metricFactory.forEach((name, metric) -> metric.update(history));
    }

}

