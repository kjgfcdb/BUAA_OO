import java.text.DecimalFormat;

class Req{
    private String type;
    private int dstFloor;
    private int elvId;
    private long time;
    private double dbTime;
    private Direction direction;
    Req(String type,String medium,String last,long time) {
        this.time = time;
        this.dbTime = time/1000.0;
        this.type = type;
        if (type.equals("FR")) {
            this.dstFloor = Integer.parseInt(medium);
            this.direction = last.equals("UP")? Direction.UP:Direction.DOWN;
        } else {//"ER"
            this.elvId = Integer.parseInt(medium.substring(1));
            this.dstFloor = Integer.parseInt(last);
            this.direction = Direction.STILL;//"ER"统统设置为STILL
        }
    }
    public int getDstFloor() {return dstFloor;}
    public int getElvId() {return elvId;}
    public Direction getDirection() {return direction;}
    public String getType() {return type;}
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.0");
        if (type.equals("FR")) {
            return "FR, "+dstFloor+", "+direction+", "+df.format(dbTime);
        }
        return "ER, #"+elvId+", "+dstFloor+", "+df.format(dbTime);
    }
    public long getTime() {return time;}
}
