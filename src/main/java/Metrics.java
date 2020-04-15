import java.util.List;
import java.util.Objects;

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
