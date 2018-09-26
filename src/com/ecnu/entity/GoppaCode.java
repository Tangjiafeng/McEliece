package com.ecnu.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;

public class GoppaCode {
	private int t;
	private Alphas[] gPoly = null;
	private Alphas[] L = null;
	private Alphas[][] H = null;
	private int[][] G =null;
	public static GaloisField GF = null;
	
	public GoppaCode(int _t, Alphas[] g) {
		this.t = _t;
		this.gPoly = g;
	}
	
	// 构造校验矩阵
	public Alphas[][] ConstructH(int t, int n) {
		Alphas[][] H = new Alphas[t][n];
		Alphas[] h = new Alphas[n];
		for (int i = 0; i < n; i++) {
			h[i] = this.getgPoly()[0];
			for (int j = 1; j <= t; j++) {
				 h[i] = h[i].add(this.getgPoly()[j].multiply(this.getL()[i].pows(j)));
			}
			h[i] = h[i].reverse();
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = t; j > 0; j--) {
				int temp = t;
				H[j - 1][i] = h[i].multiply(this.getgPoly()[j]);
				while(temp - j > 0) {
					H[j - 1][i] = H[j - 1][i].add(this.getgPoly()[temp].multiply(this.getL()[i].pows(temp - j)).multiply(h[i]));
					temp --;
				}
			}
		}
		return H;
	}
	// 构造生成矩阵
	public int[][] constructG(int[][] H, int t, int m, int n) {
		// step 1: 化简为上三角矩阵
		int k = n - m * t;
		for (int i = n - m * t; i < n; i++) {
			int temp = i - (n - m * t);
			for (int j = temp; j < t * m; j++) {
				if(H[j][i] != 1) {
					continue;
				}
				this.exchangeRows(H, temp, j);
				break;
			}
			
			for (int j = i - (n - m * t) + 1; j < t * m; j++) {
				if(H[j][i] == 1) {
					this.addRows(H, temp, j);
				}
			}
		}
		// step 2: 化简为单位矩阵
		for (int i = n - 1; i >= n - m * t; i --) {
			int temp = i - (n - m * t);
			for (int j = temp - 1; j >= 0; j --) {
				if(H[j][i] == 1) {
					this.addRows(H, temp, j);
				}
			}
		}
		// step 3: G 矩阵
		int[][] G = new int[k][n];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < n; j++) {
				if(j < k) {
					if(i == j) G[i][j] = 1; 
				} else {
					G[i][j] = H[j - k][i];
				}
			}
		}
		// step 4: G 矩阵随机化
		Random r = new Random();
		List<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < G.length; i++) {
			for (int j = 0; j < G.length; j++) {
				arr.add(r.nextInt(2));
			}
			
			for (int index = 0; index < G.length; index++) {
				if(arr.get(index) != 0 && i != index) {
					for (int j = 0; j < G[index].length; j++) {
						G[i][j] ^= G[index][j];
					}
				}
			}
			
			arr.clear();
		}
		return G;
	}
	
	// 编码
	public int[] encode(int[] message, int t, int m, int n) {
		Matrix denseM = DenseMatrix.Factory.importFromArray(message);
		Matrix denseG = DenseMatrix.Factory.importFromArray(this.G);
		Matrix denseY = denseM.mtimes(denseG);
		int[][] Y = denseY.toIntArray();
		for(int i = 0;i < Y.length;i++){
			for(int j = 0;j < Y[i].length;j++){
				Y[i][j] = Y[i][j] % GF.q;
			}
		}
		return Y[0];
	}
	public int[] encode(int[] message, int[][] S, int[][] P, int t, int m, int n) {
		Matrix denseM = DenseMatrix.Factory.importFromArray(message);
		Matrix denseS = DenseMatrix.Factory.importFromArray(S);
		Matrix denseG = DenseMatrix.Factory.importFromArray(this.G);
		Matrix denseP = DenseMatrix.Factory.importFromArray(P);
		Matrix GStar = denseS.mtimes(denseG).mtimes(denseP);
		Matrix denseY = denseM.mtimes(GStar);
		int[][] Y = denseY.toIntArray();
		for(int i = 0;i < Y.length;i++){
			for(int j = 0;j < Y[i].length;j++){
				Y[i][j] = Y[i][j] % GF.q;
			}
		}
		return Y[0];
	}
	
	// 解码
	public int[] decode(int[] ciphertext) {
		// step 1：计算伴随多项式 s
		Alphas[] s = new Alphas[this.getgPoly().length - 1];
		for (int i = 0; i < s.length; i++) {
			s[i] = new Alphas(-1);// 默认初始化为零元
			for (int j = 0; j < this.getL().length; j++) {
				if(ciphertext[j] != 0) {
					s[i] = s[i].add(this.getH()[i][j]);
				}
			}
		}
		// step 2： 求解错误定位多项式 σ(z)
		Alphas[] sigma = PolynomialUtil.analysisByString(GF, this, t, s);// 纠错能力
		// step 3：确定差错向量的差错位置
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < L.length; i++) {
			if(cacl(sigma, L[i]).getPows() == -1) {
				positions.add(i);
			}
		}
		// step 4：密文恢复
		for (int i = 0; i < positions.size(); i++) {
			ciphertext[positions.get(i)] ^= 1;
		}
		// step 5: 明文恢复
		this.finalOperation(this.getG(), ciphertext);
		return Arrays.copyOf(ciphertext, G.length);
	}
	public int[] decode(int[] ciphertext, int[][] S) {
		// step 1：计算伴随多项式 s
		Alphas[] s = new Alphas[this.getgPoly().length - 1];
		for (int i = 0; i < s.length; i++) {
			s[i] = new Alphas(-1);// 默认初始化为零元
			for (int j = 0; j < this.getL().length; j++) {
				if(ciphertext[j] != 0) {
					s[i] = s[i].add(this.getH()[i][j]);
				}
			}
		}
		// step 2： 求解错误定位多项式 σ(z)
		Alphas[] sigma = PolynomialUtil.analysisByString(GF, this, t, s);// 纠错能力
		// step 3：确定差错向量的差错位置
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < L.length; i++) {
			if(cacl(sigma, L[i]).getPows() == -1) {
				positions.add(i);
			}
		}
		// step 4：密文恢复
		for (int i = 0; i < positions.size(); i++) {
			ciphertext[positions.get(i)] ^= 1;
		}
		// step 5: 明文恢复
		this.finalOperation(this.getG(), ciphertext);
		// step 6: S ^ -1
		int[][] sInv = new int[S.length][S.length];
		for (int i = 0; i < sInv.length; i ++){
			sInv[i][i] = 1;
        }
		// step 1: 化简为上三角矩阵
		for (int i = 0; i < S.length; i ++) {// colume
			for (int j = i; j < S.length; j ++) {// row
				if(S[j][i] != 1) {
					continue;
				}
				this.exchangeRows(S, i, j);
				this.exchangeRows(sInv, i, j);
				break;
			}
			
			for (int j = i + 1; j < S.length; j ++) {// row
				if(S[j][i] == 1) {
					this.addRows(S, i, j);
					this.addRows(sInv, i, j);
				}
			}
		}
		// step 2: 化简为单位矩阵
		for (int i = S.length - 1; i >= 0; i --) {// column
			for (int j = i; j > 0; j --) {// row
				if(S[j - 1][i] == 1) {
					this.addRows(S, i, j - 1);
					this.addRows(sInv, i, j - 1);
				}
			}
		}
		return this.multiMatrix(Arrays.copyOf(ciphertext, G.length), sInv, false);
	}
	// 明文消息
	public void finalOperation(int[][] G, int[] ciphertext) {
		Matrix denseG = DenseMatrix.Factory.importFromArray(this.G);
		Matrix denseGT = denseG.transpose();
		int[][] GT = denseGT.toIntArray();
		for (int i = 0; i < G.length; i++) {
			for(int k = i; k < GT.length; k++) {
				if(GT[k][i] == 0) {
	    			continue;
	    		}
	    		exchangeRows(GT, i, k);
	    		exchangeRows(ciphertext, i, k);
	    		break;
			}
			int k = i + 1;
			while(k < GT.length) {
				if(GT[k][i] != 0) {
					for(int j = i; j < GT[i].length; j ++) {
						GT[k][j] ^= GT[i][j];
					}
					ciphertext[k] ^= ciphertext[i];
				}
				k ++;
			}
		}
		for(int i = G.length - 1; i >= 0; i --) {
			for(int j = i; j > 0; j --) {
				if(GT[j - 1][i] != 0) {
					GT[j - 1][i] = 0;
					ciphertext[j - 1] ^= ciphertext[i];
				}
			}
		}
	}
	// 交换矩阵的第 i 行与第 j 行
	public void exchangeRows(int[][] matrix, int i, int j) {
		if(i == j) return;
		int[] temp = matrix[i];
		matrix[i] = matrix[j];
		matrix[j] = temp;
	}
	// 交换数组的第 i 位与第 j 位
	public void exchangeRows(int[] row, int i, int j) {
		if(i == j) return;
		int temp = row[i];
		row[i] = row[j];
		row[j] = temp;
	}
	// 将矩阵第 i 行加到 第 j 行
	public void addRows(int[][] matrix, int i, int j) {
		if(matrix[i].length == matrix[j].length) {
			for (int k = 0; k < matrix[j].length; k++) {
				matrix[j][k] = matrix[i][k] ^ matrix[j][k];
			}
		}
	}
	//获取有限域元素填充的矩阵
	public int[][] getMatrix(Alphas[][] H, int t, int m, int n) {
		int[][] binary = new int[t * m][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < t; j++) {
				for (int k = 0; k < m; k++) {
					binary[j * m + k][i] = GF.getArray(H[j][i])[k];
				}
			}
		}
		return binary;
	}
	// 计算向量与(逆)矩阵的乘积
	public int[] multiMatrix(int[] values, int[][] matrix, boolean inv) {
		Matrix denseM = null;
		if(inv) {
			denseM = DenseMatrix.Factory.importFromArray(matrix).inv();
		} else {
			denseM = DenseMatrix.Factory.importFromArray(matrix);
		}
		Matrix denseV = DenseMatrix.Factory.importFromArray(values);
		Matrix denseRes = denseV.mtimes(denseM);
		int[][] res = denseRes.toIntArray();
		for(int i = 0;i < res.length;i++){
			for(int j = 0;j < res[i].length;j++){
				res[i][j] = res[i][j] % GF.q;
			}
		}
		return res[0];
	}
	// 计算多项式结果
	public static Alphas cacl(Alphas[] poly, Alphas alpha) {
		Alphas res = new Alphas(-1);
		Alphas factor = new Alphas();
		int pow = 0;
		for (int i = 0; i < poly.length + 1; i++) {
			pow = alpha.getPows() * i;
			factor.setPows(pow);
			if(i < poly.length) {
				res = res.add(poly[i].multiply(factor));
			} else {
				res =res.add(factor);
			}
		}
		return res;
	}
	// 输出矩阵元素
 	public static void printMatrix(int[][] matrix) {
 		for (int[] is : matrix) {
 			System.out.print("   ");
 			for (int i : is) {
 				System.out.print(i + " ");
 			}
 			System.out.println();
 		}
 		System.out.println();
 	}
 	// 输出矩阵元素
 	public static void printMatrix(int[] array) {
 		System.out.print("   ");
 		for (int i : array) {
 			System.out.print(i + " ");
 		}
 		System.out.println();
 	}
	
	public Alphas[][] getH() {
		return H;
	}

	public void setH(Alphas[][] h) {
		H = h;
	}

	public int[][] getG() {
		return G;
	}

	public void setG(int[][] g) {
		G = g;
	}
	public int getT() {
		return t;
	}
	public void setT(int t) {
		this.t = t;
	}
	public Alphas[] getgPoly() {
		return gPoly;
	}
	public void setgPoly(Alphas[] gPoly) {
		this.gPoly = gPoly;
	}
	public Alphas[] getL() {
		return L;
	}
	public void setL(Alphas[] l) {
		L = l;
	}
}
