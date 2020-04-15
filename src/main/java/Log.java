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
