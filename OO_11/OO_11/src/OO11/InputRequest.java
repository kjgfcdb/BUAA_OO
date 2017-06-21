package OO11;

import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InputRequest {
    /**
     * @Overview:输入请求类，拥有处理输入的功能。
     */
    private RequestQueue customers;
    private MyPrintWriter myPrintWriter;
    private Map map;
    private TaxiGUI gui;

    InputRequest(RequestQueue customerRequest,Map map,TaxiGUI taxiGUI) {
        /*
         * @REQUIRES:
         *      customerRequest!=null;
         *      map!=null;
         *      taxiGUI!=null;
         * @MODIFIES:
         *      \this.customers;
         *      \this.myPrintWriter;
         *      \this.map;
         *      \this.gui;
         * @EFFECTS:
         *      \this.customers==customerRequest;
         *      \this.myPrintWriter==new MyPrintWriter("Log.txt");
         *      \this.map == map;
         *      \this.gui == taxiGUI;
         */
        this.customers = customerRequest;
        this.myPrintWriter = new MyPrintWriter("Log.txt");
        this.map = map;
        this.gui = taxiGUI;
    }

    public boolean repOK() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == customers!=null && myPrintWriter!=null && map!=null && gui!=null;
         */
        return  customers!=null &&
                myPrintWriter!=null &&
                map!=null &&
                gui!=null;
    }

    void parseInput(Taxi[] taxis) {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.customers;
         *      \this.map;
         * @EFFECTS:
         *      Process the input and put the request into the customerRequest, or
         *      open/close the path according to the input.
         *      The whole system ends when the input is "end";
         */
        Scanner input = new Scanner(System.in);
        Pattern userRequest = Pattern.compile("^\\[CR,\\(\\+?\\d+,\\+?\\d+\\),\\(\\+?\\d+,\\+?\\d+\\)\\]$");
        Pattern closePath = Pattern.compile("^\\[CP,\\(\\+?\\d+,\\+?\\d+\\),\\(\\+?\\d+,\\+?\\d+\\)\\]$");
        Pattern openPath = Pattern.compile("^\\[OP,\\(\\+?\\d+,\\+?\\d+\\),\\(\\+?\\d+,\\+?\\d+\\)\\]$");
        Pattern extract = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        try {
            while (true) {
                String line = input.nextLine().trim();
                if (line.equals("end")) break;//退出系统
                String[] lines = line.replaceAll(" +", "").split(";", -1);//分成若干单条请求
                if (lines.length > 10) {
                    System.out.println("You can input at most 10 requests in a line!");
                    continue;
                }
                long curTime = gv.getTime();
                for (String _line : lines) {
                    Matcher grp = extract.matcher(_line);
                    int[] position = new int[2];
                    int index = 0;
                    boolean CR = userRequest.matcher(_line).find();
                    boolean CP = closePath.matcher(_line).find();
                    boolean OP = openPath.matcher(_line).find();
                    if ((!CR) && (!CP) && (!OP)) {
                        System.out.println("无效输入 " + _line);
                        continue;//如果不能匹配,则跳过
                    }
                    while (grp.find()) {//能匹配，开始抽取字符串
                        String[] coordinate = grp.group().split(",");
                        try {
                            int x = Integer.parseInt(coordinate[0]);
                            int y = Integer.parseInt(coordinate[1]);
                            if (x >= 1 && x <= TaxiSys.SIZE && y >= 1 && y <= TaxiSys.SIZE) {
                                position[index++] = TaxiSys.SIZE * (x - 1) + y - 1;
                            }
                        } catch (Exception e) {
                            System.out.println("Wrong input!");
                        }
                    }
                    if (index == 2) {
                        //避免起始点与目标点重合
                        if (position[0] != position[1]) {
                            if (CR) {//用户请求
                                Customer ct = new Customer(position[0], position[1], curTime, myPrintWriter);
                                customers.offer(ct);
                            } else if (CP) {//关闭路径
                                map.closePath(position[0],position[1],taxis);
                            } else {//OP打开路径
                                map.openPath(position[0],position[1],taxis);
                            }
                        } else {
                            System.out.println("指令重复或者起始点重复！" + _line);
                        }
                    } else {
                        System.out.println("无效输入 " + _line);
                    }
                }
            }
            input.close();
            myPrintWriter.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("System exploded!");
            System.exit(0);
        }
    }
}
