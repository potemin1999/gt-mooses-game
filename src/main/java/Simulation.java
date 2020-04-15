import java.util.*;

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
