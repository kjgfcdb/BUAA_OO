class Floor {
	private static int maxFloor = 20;
	private static int minFloor = 1;
	private int[] upLantern;
	private int[] downLantern;
	Floor() {
		upLantern = new int[21];
		downLantern = new int[21];
	}
	public synchronized void turnOn(int i,Direction direct) {
		if (direct==Direction.UP) upLantern[i] = 1;
		else downLantern[i] = 1;
	}
	public synchronized void turnOff(int i,Direction direct) {
	    if (direct==Direction.UP) upLantern[i] = 0;
	    else downLantern[i] = 0;
    }
    public synchronized boolean isOn(int i,Direction direction) {
	    if (direction==Direction.UP) return upLantern[i]==1;
	    else return downLantern[i]==1;
    }
	public static int getMaxFloor() {
		return maxFloor;
	}
	public static int getMinFloor() {
		return minFloor;
	}
}
