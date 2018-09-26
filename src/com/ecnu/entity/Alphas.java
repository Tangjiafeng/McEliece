package com.ecnu.entity;

public class Alphas {
	public static GaloisField GF = null;
	private int pows;
	
	public Alphas(int pows) {
		this.pows = pows;
	}
	
	public Alphas() {}
	
	/*
	 * 加法
	 */
	public Alphas add(Alphas alpha) {
		int[] a = GF.getArray(this);
		int[] b = GF.getArray(alpha);
		for (int i = 0; i < a.length; i++) {
			a[i] = a[i] ^ b[i];
		}
		Alphas alp = GF.getAlpha(a);
		return alp;
	}
	/*
	 * 乘法
	 */
	public Alphas multiply(Alphas alpha) {
		Alphas res = new Alphas();
		if(this.getPows() == -1 || alpha.getPows() == -1) {// ZERO
			res.setPows(-1);
			return res;
		}
		res.setPows((this.getPows() + alpha.getPows()) % (GF.N - 1));
		return res;
	}
	/*
	 * 逆元
	 */
	public Alphas reverse() {
		Alphas res = new Alphas();
		res.setPows((0 - this.getPows() + GF.N - 1) % (GF.N - 1));
		return res;
	}
	/*
	 * 幂次
	 */
	public Alphas pows(int n) {
		Alphas res = new Alphas();
		if(this.getPows() == -1) {// ZERO
			res.setPows(-1);
			return res;
		}
		int pows = this.getPows() * n % (GF.N - 1);
		res.setPows(pows);
		return res;
	}
	
	public int getPows() {
		return pows;
	}
	public void setPows(int pows) {
		this.pows = pows;
	}
}
