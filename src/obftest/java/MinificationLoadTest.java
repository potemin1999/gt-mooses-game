import com.company.Player;
import org.junit.Test;

public class MinificationLoadTest {

    @Test
    public void loadMinifiedPlayer() {
        System.out.println("Loading started");
        long millisStart = System.currentTimeMillis();
        Player player = new IlyaPoteminCodeMinified();
        long millisEnd = System.currentTimeMillis();
        System.out.println("Millis load time = "+(millisEnd - millisStart));
        player.reset();
        player.move(0,1,2,3);
        player.move(1,1,2,3);
    }
}
