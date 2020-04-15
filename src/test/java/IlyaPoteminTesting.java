import com.company.Player;

public class IlyaPoteminTesting {

    /**
     * Actually, this was JUnit test, but was no guaranteed compatibility with the testing system
     */
    public void tournament1() {
        IlyaPoteminLogger.IS_LOGGING_ENABLED = true;
        Player player1 = new IlyaPoteminCode();
        Player player2 = new IlyaPoteminCode();
        int player1move = 0;
        int player2move = 0;
        for (int i = 0; i < 100; i++) {
            System.out.println("Round " + (i + 1)+" start");
            int player1moveNew = player1.move(player2move, 1, 2, 3);
            int player2moveNew = player2.move(player1move, 1, 2, 3);
            System.out.println("Round "+(i + 1)+" finished ->  PL1 = " + player1moveNew
                    + ", PL2 = " + player2moveNew
                    + (player1moveNew == player2moveNew ? "(collision)" : ""));
            System.out.println("PL1 = "+player1.toString());
            System.out.println("PL2 = "+player2.toString());
            System.out.println();
            player1move = player1moveNew;
            player2move = player2moveNew;
        }
        System.out.println("Rounds done");
    }

    public static void main(String[] args) {
        new IlyaPoteminTesting().tournament1();
    }
}
