import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InputRequest {
    private RequestQueue customers;
    private TaxiGUI taxiGUI;
    private Set<Customer> custSet;//去重
    InputRequest(RequestQueue customerRequest, TaxiGUI gui) {
        this.customers = customerRequest;
        this.taxiGUI = gui;
        this.custSet = new HashSet<>();
    }
    void parseInput() {
        Scanner input = new Scanner(System.in);
        Pattern p = Pattern.compile("^\\[CR,\\(\\+?\\d+,\\+?\\d+\\),\\(\\+?\\d+,\\+?\\d+\\)\\]$");
        Pattern extract = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        try {
            while (true) {
                String line = input.nextLine().trim();
                if (line.equals("end")) break;//退出系统
                String[] lines = line.replaceAll(" +","").split(";",-1);//分成若干单条请求
                if (lines.length>10) {
                    System.out.println("You can input at most 10 requests in a line!");
                    continue;
                }
                long curTime = gv.getTime();
                for (String _line:lines) {
                    Matcher m = p.matcher(_line);//支持含空格
                    if (!m.find()) {
                        System.out.println("无效输入 "+_line);
                        continue;//如果不能匹配,则跳过
                    }
                    Matcher grp = extract.matcher(_line);
                    int[] position = new int[2];
                    int index = 0;
                    while(grp.find()) {//能匹配，开始抽取字符串
                        String[] coordinate = grp.group().split(",");
                        try {
                            int x = Integer.parseInt(coordinate[0]);
                            int y = Integer.parseInt(coordinate[1]);
                            if (x>=1 && x<=TaxiSys.SIZE && y>=1 && y<=TaxiSys.SIZE) {
                                position[index++] =TaxiSys.SIZE*(x-1)+y-1;
                            }
                        } catch (Exception e) {
                            System.out.println("Wrong input!");
                        }
                    }
                    if (index==2) {
                        //避免起始点与目标点重合
                        Customer ct = new  Customer(position[0],position[1],curTime);
                        if (position[0]!=position[1] && !custSet.contains(ct)) {
                            customers.offer(ct);
                            custSet.add(ct);
                            taxiGUI.RequestTaxi(new Point(position[0] / TaxiSys.SIZE, position[0] % TaxiSys.SIZE),
                                    new Point(position[1] / TaxiSys.SIZE, position[1] % TaxiSys.SIZE));
                        } else {
                            System.out.println("指令重复或者起始点重复！"+_line);
                        }
                    } else {
                        System.out.println("无效输入 "+_line);
                    }
                }
            }
            System.exit(0);
        }catch (Exception e) {
            System.out.println("System exploded!");
            System.exit(0);
        }
    }
}
