import java.util.function.Function;

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
