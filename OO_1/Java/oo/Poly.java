package oo;
import java.util.Scanner;

public class Poly {
	public static void main(String[] args) {
		//读入字符串
		Scanner input = new Scanner(System.in);
		String raw_input = null;
		
		try {
			raw_input = input.nextLine();
			raw_input = raw_input.replaceAll(" ", "");
		} catch(Exception e) {
			System.out.println("Invalid input!");
			System.exit(0);
		}
		
		//实例化多项式类
		Polynomial p = new Polynomial(raw_input);
		//检测非法字符
		if (p.isIllegal()) {
			System.out.println("Illegal symbol(s)!");
		} else if (p.checkString()) {//检测表达式是否规范
			System.out.println("Invalid input!");
		} else {//输出
			p.Output();
		}
		input.close();
	}
}
class Polynomial{
	int [] coef = new int[10000];//系数与指数数组
	int [] degree = new int[10000];
	int [] power = new int[10000];//power数组用来检测一个多项式类数对的指数是否重复
	int index = 0,powerLen = 0;//powerLen表示power数组的长度
	String expression = null;
	public void Output() {
		//排序
		int i = 0, temp = 0, j = 0;
		for (j = index - 1; j > 0; j--) {
			for (i = 0; i < j; i++) {
				if (degree[i] > degree[i + 1]) {
					temp = degree[i];
					degree[i] = degree[i + 1];
					degree[i + 1] = temp;
					temp = coef[i];
					coef[i] = coef[i + 1];
					coef[i + 1] = temp;
				}
			}
		}
		//输出，忽略系数为0的项
		System.out.print("{");
		for (i = 0; i < index - 1; i++) {
			if (coef[i] != 0)
				System.out.printf("(%d,%d),", coef[i], degree[i]);
		}
		if (index >= 1 && coef[i] != 0)
			System.out.printf("(%d,%d)", coef[i], degree[i]);
		System.out.println("}");
	}
	public Polynomial(String e) {
		//构造函数
		expression  = e;
	}
	public boolean checkString() {
		int i=0,coe = 0,deg = 0;
		int PreOp = 0,period = 0,big = 0,small = 0;//PreOp表示之前的操作符，0表示初始状态,1,2分别表示加减法,3表示一个多项式的结束
		                                           //Period是逗号的标志，big,small用于判断多项式个数与多项式中数对的个数是否规范
		try {
			while (i<expression.length()) {
				if (expression.charAt(i)=='{' && PreOp<3) {//多项式中的"{"必须在初始状态或者出现过"+"或"-"之后才能出现
					big++;
					small = 0;
					powerLen = 0;
					period = 2;//period=2表示进入一个多项式内
					if (big>20) return true;
					while (i<expression.length()) {
						i++;
						if (expression.charAt(i)=='(' && period!=0) {//多项式中的"("必须在"{"或","出现之后才能出现
							small++;
							if (small>50) return true;
							period = 0;//period=0表示进入一个数对内
							int j = i;
							while (i<expression.length() && expression.charAt(i)!=')') i++;
							if (expression.charAt(i)!=')') return true;
							Item Ite = new Item();
							if (getItem(j, i, Ite)) return true;
							coe = Ite.coe;
							deg = Ite.deg;
							for (int k=0;k<powerLen;k++) {
								if (power[k]==deg) return true;
							}
							power[powerLen++] = deg;
						} else if (expression.charAt(i)==',' && period==0) {//多项式内数对之间的","必须在出现过数对之后才能出现
							insertPoly(PreOp, coe, deg);
							period = 1;//period=1表示最近出现过逗号
						} else if (expression.charAt(i)=='}' && period!=1) {//多项式结尾的"}"必须在前方无","的情况下才能出现
							insertPoly(PreOp, coe, deg);
							PreOp = 3;
							break;
						} else return true;
					}
				} else if (expression.charAt(i)=='+' || expression.charAt(i)=='-') {
					if (PreOp<3 && PreOp!=0) return true;//操作符必须在初始状态或者一个多项式结束之后才可出现
					PreOp = expression.charAt(i)=='+'? 1:2;
				} else return true;
				i++;
			}
		} catch (Throwable th) {
			return true;
		}
		boolean flag = (PreOp==3 || PreOp==0)? false:true;
		return flag;
	}
	
	public boolean isIllegal() {
		try {
			for (int i=0;i<expression.length();i++) {
				char temp = expression.charAt(i);
				if (!((temp>='0' && temp<='9') || temp=='+'
						|| temp=='-' || temp==',' || temp=='{'
						|| temp=='}' || temp=='(' || temp==')')) {
					return true;
				}
			}
		} catch (Throwable th) {
			return true;
		}
		return false;
	}
	
	public void insertPoly(int PreOp,int coe,int deg) {
		int i = 0;
		for (i=0;i<index;i++) {
			if (degree[i]==deg) {
				coef[i] = PreOp==2? coef[i]-coe:coef[i]+coe;
				return;
			}
		}
		if (i==index) {
			degree[index] = deg;
			coef[index] = PreOp==2? -coe:coe;
			index++;
		}
	}
	public boolean getItem(int left,int right,Item Ite) {
		int _coe = 0,_deg = 0;
		int i = 0,period = -1,PreOp = 0;
		int noNum = 1;
		try {
			for (i=left+1;i<right-1;i++) {
				if (expression.charAt(i)==',') {
					period = i; 
					break;
				}
			}
			if (period==-1) return true;//必须要有逗号
			for (i=left+1;i<period;i++) {
				char temp = expression.charAt(i);
				if ((temp=='+' || temp=='-') && PreOp==0) {
					PreOp = temp=='+'? 1:2;
				} else if (temp>='0' && temp<='9') {
					if (period-i>6) return true;//判断数据长度
					if (PreOp==0) PreOp = 1;//锁住符号位，禁止其再次出现
					_coe = _coe*10+temp-'0';
					noNum = 0;//必须要有数字
				} else return true;
			}
			if (noNum==1) return true;
			Ite.coe = PreOp==2? -_coe:_coe;
			PreOp = 0;
			noNum = 1;
			for (i = period + 1; i < right; i++) {
				char temp = expression.charAt(i);
				if ((temp == '+' || temp == '-') && PreOp == 0) {
					PreOp = temp == '+' ? 1 : 2;
				}
				else if (temp >= '0'&&temp <= '9') {
					if (right - i > 6) return true;
					if (PreOp==0) PreOp = 1;//锁住符号位，禁止其再次出现
					_deg = _deg * 10 + temp - '0';
					noNum = 0;//必须要有数字
				}
				else return true;
			}
			if (noNum==1) return true;
		} catch (Throwable th) {
			return true;
		}
		Ite.deg = PreOp == 2 ? -_deg : _deg;
		if (Ite.deg >= 1000000 || Ite.deg < 0) return true;//判断数据大小
		if (Ite.coe >= 1000000 || Ite.coe <= -1000000) return true;
		return false;
	}
}

class Item{//数对类，存放两个数
	int coe,deg;
}