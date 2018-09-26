package com.ecnu.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 多项式四则运算工具类
 * 数组存储多项式系数，比如 [σ1,σ2,σ3,σ4] 表示：σ1+σ2*x+σ3*x^2+σ4*x^3
 * @author A
 *
 */
public class PolynomialUtil {
	private int[] dividend;
    private int[] divisor;
    private int[] result;
    private int[] remainder;

    public PolynomialUtil (int[] dividend,int[] divisor){
        this.dividend=dividend;
        this.divisor=divisor;
    }
    
    public static int[] addPolyn(int[] a,int[] b){
        int[] heigher=a.length>b.length?a:b;
        int[] lower=a.length>b.length?b:a;
        int[] res=new int[heigher.length];
        for(int i=0;i<heigher.length;i++){
            if(i<lower.length){
                res[i]=heigher[i]+lower[i];
            }
            else{
                res[i]=heigher[i];
            }
        }
        return res;
    }

    public static int[] subPolyn(int[] a,int[] b){
        int[] heigher=a.length>=b.length?a:b;
        int[] lower=a.length>=b.length?b:a;
        int[] res=new int[heigher.length];
        if(heigher==a){
            for(int i=0;i<heigher.length;i++){
                if(i<lower.length){
                    res[i]=heigher[i]-lower[i];
                }
                else{
                    res[i]=heigher[i];
                }
            }
        }else{
            for(int i=0;i<heigher.length;i++){
                if(i<lower.length){
                    res[i]=lower[i]-heigher[i];
                }
                else{
                    res[i]=-heigher[i];
                }
            }
        }
        int k=res.length-1;
        for(;res[k]==0.0&&k>0;k--);
        int[] newres=new int[k+1];
        for(int m=0;m<newres.length;m++){
            newres[m]=res[m];
        }
        return newres;
    }

    public static int[] mulPolyn(int[] a,int[] b){
        int[] res=new int[a.length+b.length-1];
        for(int i=0;i<a.length;i++){
            for(int j=0;j<b.length;j++){
                    res[i+j]+=a[i]*b[j];
            }
        }
        return res;
    }
    /*
     * 扩域多项式乘法
     */
    public static Alphas[] mulPolyn(Alphas[] a, Alphas[] b){
    	Alphas[] res = new Alphas[a.length + b.length - 1];
    	
        for(int i = 0; i < res.length; i++){
        	res[i] = new Alphas(-1);
            for(int j = 0; j <= i && j < a.length; j++){
            	if(!(i - j < b.length)) {
            		continue;
            	}
                res[i] = res[i].add(a[j].multiply(b[i - j]));
            }
        }
        return res;
    }
    /*
     * 定位多项式解法
     */
    public static Alphas[] analysisByString(GaloisField GF, GoppaCode gc, int r, Alphas[] s) {
    	// 1，字符串表示出 σ 多项式与 s 多项式的乘积 rows 多项式
    	StringBuilder[] rows = new StringBuilder[r + s.length];
    	List<Integer> currentRowsMaxPows = new ArrayList<>();
    	for(int i = 0; i < rows.length; i ++) {
    		 rows[i] = new StringBuilder("");
    		 for(int j = 0; j <= i && j < r + 1; j++){
    			 if(!(i - j < s.length)) {
             		continue;
             	 }
    			 if(s[i - j].getPows() != -1) {
    				 if(j == r) {
        				 rows[i].append("α" + s[i - j].getPows() + "+");
        			 } else {
        				 rows[i].append("σ" + j + "*" + "α" + s[i - j].getPows() + "+");
        			 }
    			 }
    		 }
    		 if(rows[i].length() != 0) {
    			 currentRowsMaxPows.add(i);
    			 rows[i].deleteCharAt(rows[i].length() - 1);// 删除最后的计算符号
    		 }
    	}
    	
    	// 2，字符串表示出 w 多项式，二元 goppa 码下 w 多项式是 σ 多项式求导的结果
    	List<StringBuilder> w = new ArrayList<StringBuilder>();
    	for (int i = 0; i < r; i++) {
			w.add(new StringBuilder(""));
			int index = i + 1;
			if(index % 2 != 0) {
				if(index == r) {
					w.get(i).append("α0");
					break;
				}
				w.get(i).append("σ" + index);
			}
		}
    	// 3，1 中的结果对 g 多项式取模
    	String highestCoefficient = null;
    	int target = rows.length - 1;
    	if(currentRowsMaxPows.size() > 0 && currentRowsMaxPows.get(currentRowsMaxPows.size() - 1) >= gc.getgPoly()[gc.getgPoly().length - 1].getPows()) {
    		int difPows = currentRowsMaxPows.get(currentRowsMaxPows.size() - 1) - gc.getgPoly().length + 1;
        	while(difPows >= 0) {
        		highestCoefficient = rows[currentRowsMaxPows.get(currentRowsMaxPows.size() - 1)].toString();
        		String[] strs = highestCoefficient.split("\\+");
        		for (int i = difPows; i < target; i++) {
        			if(gc.getgPoly()[i - difPows].getPows() != -1) {
        				if(gc.getgPoly()[i - difPows].getPows() == 0) {
        					for(String str : strs) {
        						if("".equals(rows[i].toString())) {
        							rows[i].append(str);
        							continue;
        						}
        						rows[i].append("+" + str);  
        					}
        				} else {
        					for(String str : strs) {
        						rows[i].append("+" + str + "*" + "α" + gc.getgPoly()[i - difPows].getPows());
        					}
        				}
        			}
        		}
        		currentRowsMaxPows.remove(currentRowsMaxPows.size() - 1);
        		rows[target].delete(0, rows[target].length());
        		target --;
        		difPows = currentRowsMaxPows.get(currentRowsMaxPows.size() - 1) - gc.getgPoly().length + 1;
        	}
    	}
    	
    	// 4，字符串表示方程组
    	for (int i = 0; i < rows.length; i++) {
    		if(i >= w.size()) {
				w.add(new StringBuilder(""));
			}
    		if("".equals(rows[i].toString())) {
    			continue;
    		}
    		String[] tempRows = rows[i].toString().split("\\+");
    		for(String str: tempRows) {
    			if(!("".equals(str)) && (!str.contains("σ"))) {
					if("".equals(w.get(i).toString())) {
						w.get(i).append(str);
					} else {
						w.get(i).append("+" + str);
					}
    				int index = rows[i].indexOf("+" + str + "+");
    				if(index < 0) {
    					index = rows[i].lastIndexOf("+" + str);
    				}
    				rows[i].delete(index, index + str.length() + 1);
    			}
    		}
		}
    	for (int i = 0; i < w.size(); i++) {
    		if("".equals(w.get(i).toString())) {
    			continue;
    		}
    		String[] strs = w.get(i).toString().split("\\+");
    		for(String str : strs) {
    			int index = str.indexOf("σ");
    			if(index != -1) {
    				w.get(i).delete(index, str.length() + 1);
    				if("".equals(rows[i].toString())) {
    					rows[i].append(str);
    				} else {
    					rows[i].append("+" + str);
    				}
    				
    			}
    		}
		}
    	// 5，构造系数矩阵
    	Alphas[][] A = new Alphas[r][r];
    	
    	StringBuilder[][] sigma = new StringBuilder[r][r];
    	for (int i = 0; i < sigma.length; i++) {
			for (int j = 0; j < sigma[i].length; j++) {
				sigma[i][j] = new StringBuilder("");
			}
		}
    	for (int i = 0; i < r; i++) {
			List<String> strs = new ArrayList<String>();
			for(String str : rows[i].toString().split("\\+")) {
				if(!("".equals(str))) {
					strs.add(str);
				}
			}
			if(strs.size() > 0) {
				for (int j = 0; j < sigma[i].length; j++) {
					for (int k = 0; k < strs.size();) {
						String str = strs.get(k);
						if(!("".equals(str)) && str.contains("σ" + j)) {
							strs.remove(str);
							str = str.replace("σ" + j, "");
							if(str.indexOf("*") == 0) {
								str = str.substring(str.indexOf("*") + 1, str.length());
							}
							if("".equals(str)) {
								 str = "α" + 0;
							}
							sigma[i][j].append(str + "+");
						} else {
							k ++;
						}
					}
					if(sigma[i][j].length() > 0) sigma[i][j].deleteCharAt(sigma[i][j].length() - 1);
				}
			}
		}
    	// 6，系数矩阵单位化
    	Alphas[] b = new Alphas[r];
    	for (int i = 0; i < A.length; i++) {
    		b[i] = stringToValue(w.get(i).toString(), GF.N - 1);
			for (int j = 0; j < A[i].length; j++) {
				A[i][j] = stringToValue(sigma[i][j].toString(), GF.N - 1);
			}
		}
    	
    	for (int i = 0; i < A.length; i++) {
    		for(int k = i; k < r; k++) {
				if(A[k][i].getPows() == -1) {
	    			continue;
	    		}
	    		exchangeRows(A, i, k);
	    		exchangeColumn(b, i, k);
	    		break;
			}
    		int pows1 = A[i][i].getPows();
			int mod = GF.N - 1 - pows1;
			Alphas temp1 = new Alphas(mod);
			for (int j = i; j < r; j++) {
				A[i][j] = A[i][j].multiply(temp1);
			}
			b[i] = b[i].multiply(temp1);
			int next = i + 1;
			while(next < r) {
				int pows2 = A[next][i].getPows();
				Alphas temp2 = new Alphas(pows2);
				if(pows2 != -1) {
					for (int j = i; j < r; j++) {
						A[next][j] = A[i][j].multiply(temp2).add(A[next][j]);
					}
					b[next] = b[i].multiply(temp2).add(b[next]);
				}				
				next ++;
			}
		}
    	for (int i = r - 1; i >= 0; i--) {
    		for (int j = i; j >= 0; j--) {
    			if(j - 1 >= 0) {
    				int pows = A[j - 1][i].getPows();
    				if(pows != -1) {
    					Alphas temp = new Alphas(pows);
    					A[j - 1][i] = temp.add(A[j - 1][i]);
    					b[j - 1] = temp.multiply(b[i]).add(b[j - 1]);
    				}
    			} else {
    				break;
    			}
			}
    	}
    	return Arrays.copyOf(b, r);
    }
    /*
     * 字符串化简求值
     */
    public static Alphas stringToValue(String str, int n) {
		Alphas res = new Alphas(-1);
		String[] addItems = str.split("\\+");
		for(String addItem : addItems) {
			if("".equals(addItem)) {
				return res;
			}
			String[] mulItems = addItem.split("\\*");
			int pows = 0;
			for(String mulItem: mulItems) {
				mulItem = mulItem.replace("α", "");
				if(!("".equals(mulItem))) {
					pows += Integer.parseInt(mulItem);
				}
								
			}
			pows %= n;
			res = res.add(new Alphas(pows));
		}
		return res;
	}
    
    public static void exchangeRows(Alphas[][] matrix, int i, int j) {
		if(i == j) return;
		Alphas[] temp = matrix[i];
		matrix[i] = matrix[j];
		matrix[j] = temp;
	}
    
    public static void exchangeColumn(Alphas[] matrix, int i, int j) {
		if(i == j) return;
		Alphas temp = matrix[i];
		matrix[i] = matrix[j];
		matrix[j] = temp;
	}
    
    public void devPolyn(){
        if(dividend.length<divisor.length){
            result=new int[]{0};
            remainder=dividend;
        }else{
            int[] a=dividend;
            int[] b=divisor;
            int[] res=new int[]{0};
            while(a.length>=b.length){
                int num=a.length-b.length;
                int[] temp=new int[num+1];
                for(int i=0;i<num;i++){
                    temp[i]=0;
                }
                temp[num]=a[a.length-1]/b[b.length-1];
                res=addPolyn(temp, res);
                int[] c=mulPolyn(b, temp);
                a=subPolyn(a, c);
            }
            remainder=a;
            result=res;
        }
    }
    public int[] getResult() {
        if(result==null){
            devPolyn();
        }
        return result;
    }
    public int[] getRemainder() {
        if(remainder==null){
            devPolyn();
        }
        return remainder;
    }
    
    
}
