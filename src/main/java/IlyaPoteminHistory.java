import java.util.LinkedList;
import java.util.List;

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
