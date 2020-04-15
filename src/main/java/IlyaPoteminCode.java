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