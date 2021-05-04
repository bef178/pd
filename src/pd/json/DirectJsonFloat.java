package pd.json;

class DirectJsonFloat implements IJsonFloat {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private double value;

    public DirectJsonFloat() {
        this(0);
    }

    public DirectJsonFloat(double value) {
        set(value);
    }

    @Override
    public void set(double value) {
        this.value = value;
    }

    @Override
    public float float32() {
        return (float) value;
    }

    @Override
    public double float64() {
        return value;
    }
}
