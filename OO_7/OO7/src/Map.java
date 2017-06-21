import java.io.*;
import java.util.Vector;

class Map {
    private String data = null;
    private Vector<Integer>[] edges = new Vector[TaxiSys.SIZE2];
    int[][] numMap = new int[TaxiSys.SIZE][TaxiSys.SIZE];
    Map(String filename) {
        for (int i=0;i<TaxiSys.SIZE2;i++)
            edges[i] = new Vector<>();
        if (!init(filename)) {//输入错误，立即退出
            System.out.println("Wrong input!");
            System.exit(0);
        }
    }
    Vector<Integer>[] getEdges() {
        return edges;
    }
    private boolean init(String filename) {
        BufferedReader bufferReader;
        StringBuilder sb = new StringBuilder();
        if(!new File(filename).exists()){
            System.out.println("地图文件不存在,程序退出");
            System.exit(1);
            return false;
        }
        try {
            InputStream inputStream = new FileInputStream(new File(filename));
            bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineCnt = 0;
            while ((line = bufferReader.readLine()) != null) {
                String _line = line.replaceAll("[ \t]","");
                if (_line.equals("")) continue;//允许空行
                if (_line.length()!=TaxiSys.SIZE) return false;//长度不对报错
                String[] strArray = _line.split("");
                for (int i=0;i<_line.length();i++) {//非法字符报错
                    if (_line.charAt(i)!='0' && _line.charAt(i)!='1' &&
                            _line.charAt(i)!='2'&&_line.charAt(i)!='3')
                        return false;
                    this.numMap[lineCnt][i] = Integer.parseInt(strArray[i]);
                }
                sb.append(_line);
                lineCnt++;
            }
            if (lineCnt!=TaxiSys.SIZE) return false;//行数不对报错
            bufferReader.close();
        } catch (Exception e) {
            return false;
        }
        data = sb.toString();
        for (int site = 0; site<data.length();site++) {
            char c = data.charAt(site);
            if (c=='1' || c=='3') {
                edges[site].add(site+1);
                edges[site+1].add(site);
            }
            if (c=='2' || c=='3') {
                edges[site].add(site+TaxiSys.SIZE);
                edges[site+TaxiSys.SIZE].add(site);
            }
        }
        return true;
    }

}
