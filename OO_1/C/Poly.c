#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int coef[10000];
int degree[10000];
int ind;
int isIllegal(char *s) {//检测是否存在非法字符
	int flag = 0, i = 0;
	for (i = 0; i < strlen(s); i++) {
		if (!((s[i]>='0' && s[i]<='9') || s[i]=='+' ||\
			s[i]=='-' || s[i]==',' || s[i]=='{' || s[i]=='}' ||\
			s[i]=='(' || s[i]==')' )) {
			flag = 1;
			break;
		}
	}
	return flag;
}
void insertPoly(int PreOp,int coe,int deg) {//插入多项式中
	int i = 0;
	for (i = 0; i < ind; i++) {
		if (degree[i] == deg) {
			coef[i] = PreOp == 2 ? coef[i] - coe : coef[i] + coe;
			return;
		}
	}
	if (i == ind) {//当前多项式中不存在该次数，故向其中添加
		degree[ind] = deg;
		coef[ind] = PreOp == 2 ? -coe : coe;
		ind++;
	}
}
int getItem(char *s, int left, int right, int *coe, int *deg) {//得到一个形如(..,..)中的项中的两个数字
	int _coe = 0, _deg = 0;
	int i = 0, period = -1, PreOp = 0;
	for (i = left + 1; i < right - 1; i++) {//找到逗号位置
		if (s[i] == ',') {
			period = i;
			break;
		}
	}
	if (period == -1) return 1;//不存在逗号，报错
	for (i = left + 1; i < period; i++) {
		if ((s[i] == '+' || s[i] == '-') && PreOp == 0) //只允许出现一次加减符号
			PreOp = s[i] == '+' ? 1 : 2;
		else if (s[i] >= '0'&&s[i] <= '9') { //循环计数
			if (period - i > 6) return 1;
			_coe = _coe * 10 + s[i] - '0';
		}
		else return 1;//非法字符，报错
	}
	*coe = PreOp == 2 ? -_coe : _coe;
	PreOp = 0;
	for (i = period + 1; i < right; i++) {
		if ((s[i] == '+' || s[i] == '-') && PreOp == 0) {
			PreOp = s[i] == '+' ? 1 : 2;
		}
		else if (s[i] >= '0'&&s[i] <= '9') {
			if (right - i > 6) return 1;
			_deg = _deg * 10 + s[i] - '0';
		}
		else return 1;
	}
	*deg = PreOp == 2 ? -_deg : _deg;
	if (*deg >= 1000000 || *deg < 0) return 1;//检测c,n范围
	if (*coe >= 1000000 || *coe <= -1000000) return 1;
	return 0;
}
int checkString(char *s) {//检测字符串是否为标准输入格式，同时写入结果中
	int flag = 0, i = 0;
	int PreOp = 0;//多项式之间的操作符,0表示最初状态,1表示加法,2表示减法,3表示一个多项式{}结束
	int coe = 0, deg = 0, period = 0;//period=1表示最近使用过逗号，下次不可继续使用
	int big = 0, small = 0;
	while (i < strlen(s)) {
		if (s[i] == '{' && PreOp < 3) {//多项式内
			big++;
			small = 0;
			if (big > 20) return 1;//超过20个多项式，报错
			while (i < strlen(s)) {
				i++;
				if (s[i] == '(') {//单项内
					small++;
					if (small > 50) return 1;//超过50个数对，报错
					period = 0;
					int j = i;
					while (s[i] != ')' && i < strlen(s)) i++;
					if (s[i] != ')') return 1;
					if (getItem(s, j, i, &coe, &deg)) return 1;
				}
				else if (s[i] == ','&&period == 0) {//单项外
					insertPoly(PreOp,coe,deg);
					period = 1;
				}
				else if (s[i] == '}'&&period == 0) {//结束多项式
					insertPoly(PreOp, coe, deg);
					PreOp = 3;//多项式结束
					break;
				}
				else return 1; //非法
			}
		}
		else if (s[i] == '+' || s[i] == '-') {//多项式外
			if (PreOp < 3 && PreOp != 0) return 1;//如果之前的操作符不为0，那么现在的操作符是多余的
			PreOp = s[i] == '+' ? 1 : 2;
		}
		else return 1; //非法
		i++;
	}
	flag = (PreOp == 3 || PreOp == 0) ? 0 : 1;
	return flag;
}
void slowSort() {//排序
	int i = 0, temp = 0, j = 0;
	for (j=ind-1;j>0;j--) {
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
}
int main() {
	int i = 0, j = 0;
	char c = 0;
	char *input = (char *)malloc(1000000);
	memset(input, 0, sizeof(input));
	while (1) {
		scanf("%c", &c);
		if (c == '\n') break;
		if (c != ' ') input[i++] = c;
	}
	input[i] = 0;
	memset(coef, 0, sizeof(coef));
	memset(degree, 0, sizeof(coef));
	if (isIllegal(input)) {
		printf("Illegal symbol(s)!\n");
		return 0;
	}
	if (checkString(input)) {
		printf("Invalid input!\n");
	}
	else {
		slowSort();
		printf("{");
		for (i = 0; i < ind-1; i++) {
			if (coef[i]!=0) printf("(%d,%d),", coef[i], degree[i]);
		}
		if (ind >= 1 && coef[i] != 0) printf("(%d,%d)", coef[i], degree[i]);
		printf("}");
	}
	return 0;
}
