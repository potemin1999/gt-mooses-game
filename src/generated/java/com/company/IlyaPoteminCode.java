/* MERGED FROM MULTIPLE SOURCE FILES */
package com.company;
/* SUMMARIZED IMPORTS */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* CODE */

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/IlyaPoteminCode.java */
/**
 * Player implementation
 * Uses all available information given to method  int move(int,int,int,int)
 */
public class IlyaPoteminCode implements com.company.Player {

    private IlyaPoteminHistory history;
    private IlyaPoteminStrategy playerStrategy;
    private IlyaPoteminGameStateSimulation simulation;

    public IlyaPoteminCode() {
        reset();
    }

    public void reset() {
        history = new IlyaPoteminArrayHistory();
        simulation = new IlyaPoteminGameStateSimulationImpl();
        playerStrategy = new IlyaPoteminMooseStrategy(simulation, 1000);
    }

    public IlyaPoteminField move(IlyaPoteminField lastOpponentMove) {
        if (lastOpponentMove != null) {
            history.writeLastOpponentMove(lastOpponentMove);
            simulation.simulateLastRound(history.getLastAction());
        }
        IlyaPoteminField moveToField = playerStrategy.move(history, lastOpponentMove);
        history.writeCurrentPlayerMove(moveToField);
        return moveToField;
    }

    public int move(int opponentLastMoveInt, int xA, int xB, int xC) {
        IlyaPoteminField lastOpponentMove = IlyaPoteminField.ofValue(opponentLastMoveInt);
        IlyaPoteminField moveToField = move(lastOpponentMove);
        return moveToField.getValue();
    }

    public String getEmail() {
        return "i.potemin@innopolis.ru";
    }

    public String toString() {
        return "Player(hash=" + this.hashCode() + ", strategy=" + playerStrategy.toString() + ")";
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/IlyaPoteminField.java */

/**
 * Field represents territorial region
 * Removes dependency on integer field values of base class
 */
enum IlyaPoteminField {
    A(1),
    B(2),
    C(3);

    private int value;

    IlyaPoteminField(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static IlyaPoteminField ofValue(int value) {
        switch (value) {
            case 1:
                return A;
            case 2:
                return B;
            case 3:
                return C;
            default:
                return null;
        }
    }
}

/**
 * FieldState represents current state of field in GameStateSimulation
 * used to store vegetation, which used to compute player payoffs, which used in MooseStrategy, whoa
 */
class IlyaPoteminFieldState {
    private IlyaPoteminField field;
    private long vegetationStep;
    private double vegetationAmount;
    private Function<Long, Double> vegetationAmountFunc;

    public IlyaPoteminFieldState(IlyaPoteminField field, Function<Long, Double> vegetationAmountFunc) {
        this.vegetationAmountFunc = vegetationAmountFunc;
        this.field = field;
        setVegetationStep(1);
    }

    public long getVegetationStep() {
        return this.vegetationStep;
    }

    public double getVegetationAmount() {
        return this.vegetationAmount;
    }

    public void setVegetationStep(long vegetationStep) {
        if (vegetationStep < 0) {
            vegetationStep = 0;
        }
        this.vegetationStep = vegetationStep;
        this.vegetationAmount = vegetationAmountFunc.apply(vegetationStep);
    }

    public IlyaPoteminField getField() {
        return this.field;
    }

    public void incrementVegetationStep() {
        setVegetationStep(this.vegetationStep + 1);
    }

    public void decrementVegetationStep() {
        setVegetationStep(this.vegetationStep - 1);
    }

    @Override
    public String toString() {
        return "FieldState(" +
                "field=" + field +
                ", vegetationStep=" + vegetationStep +
                ", vegetationAmount=" + vegetationAmount +
                ')';
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/IlyaPoteminHistory.java */

/**
 * Round actions holds both players actions for round,
 * as well as their payoffs for already hold rounds
 */
class IlyaPoteminRoundRecord {
    int round;
    IlyaPoteminField playerMove;
    IlyaPoteminField opponentMove;
    boolean isBattle;
    double computedPlayerPayoff;
    double computedOpponentPayoff;

    public double getComputedPlayerPayoff() {
        return computedPlayerPayoff;
    }
}

/**
 * History is an interface to access last players' actions
 */
interface IlyaPoteminHistory {
    void writeLastOpponentMove(IlyaPoteminField field);

    void writeCurrentPlayerMove(IlyaPoteminField field);

    List<IlyaPoteminRoundRecord> getLastActions(int depth);

    default IlyaPoteminRoundRecord getLastAction() {
        List<IlyaPoteminRoundRecord> actions = getLastActions(1);
        if (actions.size() == 0) {
            return null;
        } else {
            return actions.get(0);
        }
    }
}

class IlyaPoteminArrayHistory implements IlyaPoteminHistory {

    private List<IlyaPoteminRoundRecord> historyList = new LinkedList<>();
    private int roundCounter = 1;

    @Override
    public void writeLastOpponentMove(IlyaPoteminField field) {
        IlyaPoteminRoundRecord previousRecord = historyList.get(historyList.size() - 1);
        previousRecord.opponentMove = field;
        previousRecord.isBattle = (previousRecord.playerMove == previousRecord.opponentMove);
    }

    @Override
    public void writeCurrentPlayerMove(IlyaPoteminField field) {
        IlyaPoteminRoundRecord currentRecord = new IlyaPoteminRoundRecord();
        currentRecord.round = roundCounter++;
        currentRecord.opponentMove = null;
        currentRecord.playerMove = field;
        historyList.add(currentRecord);
    }

    @Override
    public List<IlyaPoteminRoundRecord> getLastActions(int depth) {
        int currentSize = historyList.size();
        return historyList.subList(Math.max(0, currentSize - depth), currentSize);
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/IlyaPoteminMetric.java */

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


/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/IlyaPoteminStrategy.java */
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


/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/Log.java */
class IlyaPoteminLogger {

    public static final boolean IS_LOGGING_ENABLED = false;

    public static IlyaPoteminLogger getLogger(String name) {
        return new IlyaPoteminLogger(name);
    }

    private String name;

    public IlyaPoteminLogger(String name) {
        this.name = name;
    }

    public void info(String msg) {
        if (IS_LOGGING_ENABLED) {
            System.out.printf("[%20.20s] %s\n", this.name, msg);
        }
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/Metrics.java */

class IlyaPoteminPayoffSumMetric extends IlyaPoteminAbstractMetric<Double> {
    public static final String NAME = "payoff_sum_metric";

    public IlyaPoteminPayoffSumMetric(int depth) {
        super(depth);
    }

    @Override
    protected Double doUpdate(List<IlyaPoteminRoundRecord> recordList) {
        return recordList.stream()
                .map(IlyaPoteminRoundRecord::getComputedPlayerPayoff)
                .reduce(Double::sum)
                .orElse(0.0);
    }
}

class IlyaPoteminFightPercentMetric extends IlyaPoteminAbstractMetric<Double> {
    public static final String NAME = "fight_percent_metric";

    public IlyaPoteminFightPercentMetric(int depth) {
        super(depth);
    }

    @Override
    protected Double doUpdate(List<IlyaPoteminRoundRecord> recordList) {
        long totalRoundCount = recordList.size();
        long hitRoundCount = recordList.stream()
                .filter(record -> record.isBattle)
                .count();
        return ((double) hitRoundCount) / totalRoundCount;
    }
}

class IlyaPoteminOpponentFieldDistributionMetric extends IlyaPoteminAbstractMetric<Double> {
    public static final String NAME = "opponent_field_distribution_metric";
    private IlyaPoteminField target;

    public IlyaPoteminOpponentFieldDistributionMetric(IlyaPoteminField field, int depth) {
        super(depth);
        this.target = field;
    }

    @Override
    protected Double doUpdate(List<IlyaPoteminRoundRecord> recordList) {
        long all = recordList.size();
        long fielded = recordList.stream()
                .filter(record -> record.opponentMove == target)
                .count();
        return ((double) fielded) / all;
    }
}

class IlyaPoteminMostOpponentSelectedFieldMetric extends IlyaPoteminAbstractMetric<IlyaPoteminField> {
    public static final String NAME = "most_opponent_selected_metric";

    private IlyaPoteminMetric<Double> aMetric;
    private IlyaPoteminMetric<Double> bMetric;
    private IlyaPoteminMetric<Double> cMetric;

    public IlyaPoteminMostOpponentSelectedFieldMetric(int historyDepth, IlyaPoteminMetric<Double> aMetric,
                                                      IlyaPoteminMetric<Double> bMetric, IlyaPoteminMetric<Double> cMetric) {
        super(historyDepth);
        this.aMetric = Objects.requireNonNull(aMetric);
        this.bMetric = Objects.requireNonNull(bMetric);
        this.cMetric = Objects.requireNonNull(cMetric);
    }

    @Override
    protected IlyaPoteminField doUpdate(List<IlyaPoteminRoundRecord> recordList) {
        if (aMetric.get() > bMetric.get()) {      // a > b
            if (aMetric.get() > cMetric.get()) { // a > b and a > c
                return IlyaPoteminField.A;
            } else { // a > b and c > a
                return IlyaPoteminField.C;
            }
        } else { // b > a
            if (bMetric.get() > cMetric.get()) { // b > a and b > c
                return IlyaPoteminField.B;
            } else { // b > a and c > b
                return IlyaPoteminField.C;
            }
        }
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/Simulation.java */

/**
 * GameStateSimulation provides simulated game state based on previous player and opponent actions
 */
interface IlyaPoteminGameStateSimulation {
    double vegetationAmountFunction(long vegetationStep);

    void simulateLastRound(IlyaPoteminRoundRecord lastRoundRecord);

    Collection<IlyaPoteminFieldState> getFieldStates();

    void dumpFieldStates();
}

class IlyaPoteminGameStateSimulationImpl implements IlyaPoteminGameStateSimulation {

    private IlyaPoteminLogger logger = IlyaPoteminLogger.getLogger("GameStateSimulation");
    private Map<IlyaPoteminField, IlyaPoteminFieldState> fieldStateMap;
    private double vegetationAmount0;

    public IlyaPoteminGameStateSimulationImpl() {
        fieldStateMap = new HashMap<>(8);
        vegetationAmount0 = vegetationAmountFunction(0);
        for (IlyaPoteminField f : IlyaPoteminField.values()) {
            fieldStateMap.put(f, new IlyaPoteminFieldState(f, this::vegetationAmountFunction));
        }
    }

    public double vegetationAmountFunction(long vegetationStep) {
        return (10.0 * Math.exp(vegetationStep)) / (1.0 + Math.exp(vegetationStep));
    }

    public void simulateLastRound(IlyaPoteminRoundRecord lastRoundRecord) {
        logger.info("Applying round " + lastRoundRecord.round + " to simulation");
        Objects.requireNonNull(lastRoundRecord);
        if (lastRoundRecord.isBattle) {
            IlyaPoteminField battleGround = lastRoundRecord.playerMove;
            IlyaPoteminFieldState fieldState = fieldStateMap.get(battleGround);
            fieldState.decrementVegetationStep();
        } else {
            IlyaPoteminFieldState playerFieldState = fieldStateMap.get(lastRoundRecord.playerMove);
            lastRoundRecord.computedPlayerPayoff = playerFieldState.getVegetationAmount() - vegetationAmount0;
            playerFieldState.decrementVegetationStep();

            IlyaPoteminFieldState opponentFieldState = fieldStateMap.get(lastRoundRecord.opponentMove);
            lastRoundRecord.computedOpponentPayoff = opponentFieldState.getVegetationAmount() - vegetationAmount0;
            opponentFieldState.decrementVegetationStep();
        }
        fieldStateMap.entrySet().stream()
                .filter(entry -> entry.getKey() != lastRoundRecord.playerMove)
                .filter(entry -> entry.getKey() != lastRoundRecord.opponentMove)
                .forEach(entry -> entry.getValue().incrementVegetationStep());
        dumpFieldStates();
    }

    public Collection<IlyaPoteminFieldState> getFieldStates() {
        return fieldStateMap.values();
    }

    public void dumpFieldStates() {
        fieldStateMap.entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .forEach((entry) -> {
                    logger.info("Field " + entry.getKey().name() +
                            " : vegetationStep=" + entry.getValue().getVegetationStep() +
                            ", vegetationAmount=" + entry.getValue().getVegetationAmount());
                });
    }
}

/* FROM https://github.com/potemin1999/gt-mooses-game/blob/master/src/main/java/Strategies.java */

/**
 * Default base strategy
 * This strategy is directly called by IlyaPoteminCode a.k.a. Player implementation
 * Is utilizes most profitable strategy taking into account all opponent actions in past
 */
class IlyaPoteminMooseStrategy extends IlyaPoteminAbstractStrategy {

    private IlyaPoteminLogger logger = IlyaPoteminLogger.getLogger("MooseStrategy");
    private IlyaPoteminGameStateSimulation gameState;
    private IlyaPoteminStrategy mostPayoffStrategy;

    @IlyaPoteminInjectedMetric(IlyaPoteminFightPercentMetric.NAME)
    protected IlyaPoteminMetric<Double> fightPercentMetric;

    public IlyaPoteminMooseStrategy(IlyaPoteminGameStateSimulation gameState, int metricDepth) {
        super(metricDepth);
        this.gameState = gameState;
        mostPayoffStrategy = new IlyaPoteminMostPayoffStrategy(this, gameState);
    }

    public IlyaPoteminMooseStrategy(IlyaPoteminAbstractStrategy parent) {
        super(parent);
    }

    @Override
    public IlyaPoteminField move(IlyaPoteminHistory history, IlyaPoteminField opponentLastMove) {
        if (opponentLastMove == null) {
            return mostPayoffStrategy.move(history, null);
        }
        super.updateMetrics(history);
        Collection<IlyaPoteminFieldState> fieldStates = gameState.getFieldStates();
        List<IlyaPoteminFieldState> nonZeroStepFieldStates = fieldStates.stream()
                .filter(state -> state.getVegetationStep() > 0)
                .collect(Collectors.toList());
        if (nonZeroStepFieldStates.size() == 1) {
            // there is only one field with non-zero step and non-zero available payoff
            // if opponent is not stupid enough, it will this move and get an advantage in payoffs
            // we have one choice - select it and either win or neglect both payoffs\
            logger.info("Using no other choice strategy");
            return nonZeroStepFieldStates.get(0).getField();
        }
        // there is not only one field with non-zero step and non-zero available payoff
        return mostPayoffStrategy.move(history, opponentLastMove);
    }

    public String toString() {
        String gameStateStr = gameState.getFieldStates().stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        return "MooseStrategy(" +
                "gameState=" + gameStateStr +
                ", fightPercentMetric=" + fightPercentMetric.toString() +
                ")";
    }
}

/**
 * This strategy selects fields which give player most payoff
 * It uses GameStateSimulation to fetch current state of fields based on round actions
 */
class IlyaPoteminMostPayoffStrategy extends IlyaPoteminAbstractStrategy {

    private IlyaPoteminGameStateSimulation gameState;
    private IlyaPoteminRandomStrategy randomStrategy;

    private IlyaPoteminMostPayoffStrategy(int metricDepth) {
        super(metricDepth);
    }

    protected IlyaPoteminMostPayoffStrategy(IlyaPoteminAbstractStrategy parent, IlyaPoteminGameStateSimulation gameState) {
        super(parent);
        this.gameState = gameState;
        randomStrategy = new IlyaPoteminRandomStrategy(this);
    }

    @Override
    public IlyaPoteminField move(IlyaPoteminHistory history, IlyaPoteminField opponentLastMove) {
        long maxFieldVegetationStep = gameState.getFieldStates().stream()
                .max(Comparator.comparing(IlyaPoteminFieldState::getVegetationAmount))
                .orElseThrow().getVegetationStep();
        List<IlyaPoteminField> maxPayoffFields = gameState.getFieldStates().stream()
                .filter(state -> state.getVegetationStep() == maxFieldVegetationStep)
                .map(IlyaPoteminFieldState::getField)
                .collect(Collectors.toList());
        return randomStrategy.move(maxPayoffFields);
    }
}

/**
 * RandomStrategy - selects random field
 */
class IlyaPoteminRandomStrategy extends IlyaPoteminAbstractStrategy {
    private Random randomEngine = new Random();

    public IlyaPoteminRandomStrategy(int metricDepth) {
        super(metricDepth);
    }

    public IlyaPoteminRandomStrategy(IlyaPoteminAbstractStrategy parent) {
        super(parent);
    }

    @Override
    public IlyaPoteminField move(IlyaPoteminHistory history, IlyaPoteminField opponentLastMove) {
        return IlyaPoteminField.ofValue(randomEngine.nextInt(3) + 1);
    }

    public IlyaPoteminField move(List<IlyaPoteminField> fromList) {
        return fromList.get(randomEngine.nextInt(fromList.size()));
    }
}
