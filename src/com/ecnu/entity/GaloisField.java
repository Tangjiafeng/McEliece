package com.ecnu.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ������ 
 * TODO: �޸�Ϊ����ģʽ
 * @author A
 *
 */
public class GaloisField {
	public int q;
	public int m;
	public int N;
	private int[] irrePolyn;
	public static List<Alphas> elements = new ArrayList<Alphas>();
	
	public GaloisField(int q, int m) {
		this.q = q;
		this.m = m;
		N = (int) Math.pow(q, m);
	}
	
	public GaloisField(int m) {
		this.q = 2;
		this.m = m;
		N = (int) Math.pow(q, m);
	}
	/*
	 * ������ʽת��Ϊ alpha ��ʽ
	 */
	public Alphas getAlpha(int[] alp) {
		for(int i = 0; i < elements.size(); i ++) {
			if(Arrays.equals(this.getArray(elements.get(i)), alp)) {
				return elements.get(i);
			}
		}
		return null;
	}
	/*
	 * alpha ת��Ϊ������ʽ
	 */
	public int[] getArray(Alphas alpha) {
		if(alpha.getPows() == -1) {
			int[] res = new int[this.m];
			for (int i = 0; i < res.length; i++) {
				res[i] = 0;
			}
			return res;
		}
		int len = this.m;
		int pows = alpha.getPows(); 
		int[] res = new int[len];
		// ��ʼ������Ԫ��Ϊ��
		for(int i = 0; i < len; i ++) {
			res[i] = 0;
		}
		if(pows < len) {
			res[pows] = 1;
			return res;
		} else if(pows == len) {
			res = Arrays.copyOfRange(this.irrePolyn, 0, this.irrePolyn.length - 1);
			return res;
		} else {
			res = Arrays.copyOfRange(this.irrePolyn, 0, this.irrePolyn.length - 1);
			int[] resCopy = Arrays.copyOf(res, res.length);
			for(int i = 0; i < pows - len; i ++) {
				if(res[len - 1] != 1) {
					for(int j = len - 1; j > 0; j --) {
						res[j] = res[j - 1];
					}
					res[0] = 0;
					continue;
				} else {
					for(int j = len - 1; j > 0; j --) {
						res[j] = res[j - 1];
					}
					res[0] = 0;
					for(int k = 0; k < len; k ++) {
						res[k] = res[k] ^ resCopy[k];
					}
					continue;
				}				
			}
			return res;
		}
	}
	/*
	 * ���������Ա�ԭԪ����ʽ��ʾ���������ϵ�Ԫ�ز������� elements ��
	 */
	public void generate() {
		elements.add(new Alphas(-1));// ��Ԫ
		for(int i = 0; i < N - 1; i ++) {
			elements.add(new Alphas(i));// 1��alpha��alpha^2��������alpha^(N-2)
		}
	}
	/*
	 * ŷ������㷨��ax + by = gcb(a, b)
	 * ���� y�� �� b ����Ԫ
	 * src:	https://blog.csdn.net/zs064811/article/details/55000930
	 */
	public static int[] extend_gcd(int a, int b){
		int ans;
		int[] result = new int[3];
	    if(b == 0)
	    {
	    	result[0] = a;
	    	result[1] = 1;
	    	result[2] = 0;
	        return result;
	    }
	    int[] temp = extend_gcd(b, a%b);
	    ans = temp[0];
	    result[0] = ans;
	    result[1] = temp[2];
    	result[2] = temp[1]-(a/b)*temp[2];
	    return result;
	}
	
	public int[] getIrrePolyn() {
		return irrePolyn;
	}
	
	public void setIrrePolyn(int[] irrePolyn) {
		this.irrePolyn = irrePolyn;
	}
}
