import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
