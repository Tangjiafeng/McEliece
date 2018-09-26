package com.ecnu.entity.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;

import com.ecnu.entity.Alphas;
import com.ecnu.entity.GaloisField;
import com.ecnu.entity.GoppaCode;

public class GoppaCodeTest {
	
	@Test
	public void checker() {
	}

	@Test
	public void binaryGoppaCode() {
		int m = 4;
		int t = 2;
		int n = 12;
		GaloisField GF = new GaloisField(m);
		
		int[] irrePolyn = {1, 1, 0, 0, 1};
		GF.setIrrePolyn(irrePolyn);
		GF.generate();
		Alphas.GF = GF;
		GoppaCode.GF = GF;
		
		// ��ʼ�� g ����ʽ
		Alphas[] gPolyn = new Alphas[t + 1];
		gPolyn[0] = GaloisField.elements.get(1);
		gPolyn[1] = GaloisField.elements.get(8);
		gPolyn[2] = GaloisField.elements.get(1);
		
		GoppaCode gc = new GoppaCode(t, gPolyn);
		
		// ��ʼ�������Ӽ�
		Alphas[] alphaList = new Alphas[n];
		for (int i = 0; i < alphaList.length; i++) {
			alphaList[i] = GaloisField.elements.get(i + 3);
		}
		gc.setL(alphaList);
		
		// ���㲢���У�����
		Alphas[][] H = gc.ConstructH(t, n);
		gc.setH(H);
		int[][] binaryH = gc.getMatrix(gc.getH(), t, m, n);
		System.out.println("У����� H, shape(" + m * t + "*" + n + "):");
		GoppaCode.printMatrix(binaryH);
		
		//���㲢������ɾ���
		int[][] tempH = gc.getMatrix(gc.getH(), t, m, n);
		int[][] G = gc.constructG(tempH, t, m, n); 
		gc.setG(G);
		System.out.println("���ɾ��� G, shape(" + (n - m * t) +"*" + n + "):");
		GoppaCode.printMatrix(G);
		
		// ����
		int[] message = {1, 0, 1, 1};
		int[] Y = gc.encode(message, t, m, n);
		System.out.println("received message: " + Arrays.toString(message));
		System.out.println("Encoded messages: " + Arrays.toString(Y));
		int[] ciphertext = Arrays.copyOf(Y, Y.length);
		ciphertext[3] ^= 1;
		ciphertext[5] ^= 1;
		System.out.println("error ciphertext: " + Arrays.toString(ciphertext));
		
		// ����
		t = (int)Math.pow(t, 2);
		Alphas[] gPolynSquare = new Alphas[t + 1];
		gPolynSquare[0] = GaloisField.elements.get(1);
		gPolynSquare[1] = GaloisField.elements.get(0);
		gPolynSquare[2] = GaloisField.elements.get(15);
		gPolynSquare[3] = GaloisField.elements.get(0);
		gPolynSquare[4] = GaloisField.elements.get(1);
		gc.setgPoly(gPolynSquare);
		Alphas[][] HSquare = gc.ConstructH(t, n);
		gc.setH(HSquare);
		int[] plaintext = gc.decode(ciphertext);
		System.out.println("correctplaintext: " + Arrays.toString(plaintext));
		Assert.assertArrayEquals(message, plaintext);
	}
	
	@Test
	public void enhancedBinaryGoppaCode() {
		int m = 8;
		int t = 9;
		int n = 128;
		GaloisField GF = new GaloisField(m);
		
		int[] irrePolyn = {1, 1, 1, 1, 1, 0, 1, 0, 1};// 1+x+x^2+x^3+x^4+x^6+x^8
		GF.setIrrePolyn(irrePolyn);
		GF.generate();
		Alphas.GF = GF;
		GoppaCode.GF = GF;
		
		// ��ʼ�� g ����ʽ
		Alphas[] gPolyn = new Alphas[t + 1];// 1+x+x^4+x^5+x^6+x^8+x^9
		gPolyn[0] = GaloisField.elements.get(1);
		gPolyn[1] = GaloisField.elements.get(1);
		gPolyn[2] = GaloisField.elements.get(0);
		gPolyn[3] = GaloisField.elements.get(0);
		gPolyn[4] = GaloisField.elements.get(1);
		gPolyn[5] = GaloisField.elements.get(1);
		gPolyn[6] = GaloisField.elements.get(1);
		gPolyn[7] = GaloisField.elements.get(0);
		gPolyn[8] = GaloisField.elements.get(1);
		gPolyn[9] = GaloisField.elements.get(1);
		
		GoppaCode gc = new GoppaCode(t, gPolyn);
		
		// ��ʼ�������Ӽ�
		Alphas[] alphaList = new Alphas[n];
		for (int i = 0; i < alphaList.length; i++) {
			alphaList[i] = GaloisField.elements.get(i + 4);// ��^3 ~ ��^130
		}
		gc.setL(alphaList);
		
		// ���㲢���У�����
		Alphas[][] H = gc.ConstructH(t, n);
		gc.setH(H);
		int[][] binaryH = gc.getMatrix(gc.getH(), t, m, n);
		System.out.println("У����� H, shape(" + m * t + "*" + n + "):");
		GoppaCode.printMatrix(binaryH);
		
		//���㲢������ɾ���
		int[][] tempH = gc.getMatrix(gc.getH(), t, m, n);
		int[][] G = gc.constructG(tempH, t, m, n); 
		gc.setG(G);
		System.out.println("���ɾ��� G, shape(" + (n - m * t) +"*" + n + "):");
		GoppaCode.printMatrix(G);
		
		// ����
		Random r = new Random();
		int[] message = new int[gc.getG().length];
		// �������������Ϣ
		for (int i = 0; i < message.length; i++) {
			message[i] = r.nextInt(2);
		}
		System.out.println("������Ϣ: " + Arrays.toString(message));// ������Ϣ
		int[] Y = gc.encode(message, t, m, n);
		System.out.println("��������: " + Arrays.toString(Y));// ��������
		int[] ciphertext = Arrays.copyOf(Y, Y.length);
		// �����Ӳ������
		for (int i = 0; i < t; i++) {
			ciphertext[r.nextInt(128)] ^= 1;
		}
		System.out.println("�������: " + Arrays.toString(ciphertext));// �������
		// ����
		int[] plaintext = gc.decode(ciphertext);
		System.out.println("��������: " + Arrays.toString(plaintext));// ��������
		Assert.assertArrayEquals(message, plaintext);
	}
	
	@Test
	public void Mceliece() {
		int m = 8;
		int t = 9;
		int n = 128;
		GaloisField GF = new GaloisField(m);
		
		int[] irrePolyn = {1, 1, 1, 1, 1, 0, 1, 0, 1};// 1+x+x^2+x^3+x^4+x^6+x^8
		GF.setIrrePolyn(irrePolyn);
		GF.generate();
		Alphas.GF = GF;
		GoppaCode.GF = GF;
		
		// ��ʼ�� g ����ʽ
		Alphas[] gPolyn = new Alphas[t + 1];// 1+x+x^4+x^5+x^6+x^8+x^9
		gPolyn[0] = GaloisField.elements.get(1);
		gPolyn[1] = GaloisField.elements.get(1);
		gPolyn[2] = GaloisField.elements.get(0);
		gPolyn[3] = GaloisField.elements.get(0);
		gPolyn[4] = GaloisField.elements.get(1);
		gPolyn[5] = GaloisField.elements.get(1);
		gPolyn[6] = GaloisField.elements.get(1);
		gPolyn[7] = GaloisField.elements.get(0);
		gPolyn[8] = GaloisField.elements.get(1);
		gPolyn[9] = GaloisField.elements.get(1);
		
		GoppaCode gc = new GoppaCode(t, gPolyn);
		
		// У�鸨���Ӽ��е�Ԫ�ش��벻��Լ����ʽ��ֵ��Ϊ��
//		for(int i = 0; i < GaloisField.elements.size(); i++) {
//			Alphas res = gc.getgPoly()[0];
//			for(int j = 1; j < gc.getgPoly().length; j ++) {
//				res = res.add(gc.getgPoly()[j].multiply(GaloisField.elements.get(i).pows(j)));
//			}
//			if(res.getPows() == -1) {
//				System.out.println(i + ": " + res.getPows());
//				break;
//			}
//		}
		
		// ��ʼ�������Ӽ�Ԫ��
		Alphas[] alphaList = new Alphas[n];
		for (int i = 0; i < alphaList.length; i++) {
			alphaList[i] = GaloisField.elements.get(i + 4);// ��^3 ~ ��^130
		}
		gc.setL(alphaList);
		// ���㲢���У�����
		Alphas[][] H = gc.ConstructH(t, n);
		gc.setH(H);
		int[][] binaryH = gc.getMatrix(gc.getH(), t, m, n);
		System.out.println("У����� H, shape(" + m * t + "*" + n + "):");
		GoppaCode.printMatrix(binaryH);
		// ���㲢������ɾ���
		int[][] tempH = gc.getMatrix(gc.getH(), t, m, n);
		int[][] G = gc.constructG(tempH, t, m, n); 
		gc.setG(G);
		System.out.println("���ɾ��� G, shape(" + (n - m * t) +"*" + n + "):");
		GoppaCode.printMatrix(G);
		
		// 1�������������� S
		// 1.1�����쵥λ����
		int[][] identityToS = new int[n - m * t][n - m * t];
		for (int i = 0; i < identityToS.length; i ++){
           	identityToS[i][i] = 1;
        }
		// 1.2������������
		Random r = new Random();
		List<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < identityToS.length; i ++) {
			for (int j = 0; j < identityToS.length; j ++) {
				arr.add(r.nextInt(2));
			}
			
			for (int index = 0; index < identityToS.length; index ++) {
				if(arr.get(index) != 0 && i != index) {
					for (int j = 0; j < identityToS.length; j ++) {
						identityToS[index][j] ^= identityToS[i][j];
					}
				}
			}
			arr.clear();
		}
		Matrix test = DenseMatrix.Factory.importFromArray(identityToS);
		System.out.println("������� S��");
		for (int i = 0; i < identityToS.length; i++) {
			System.out.println(Arrays.toString(identityToS[i]));
		}
		// 2�������û����� P
		// 1.1�����쵥λ����
		int[][] identityToP = new int[n][n];
		for (int i = 0; i < identityToP.length; i ++){
           	identityToP[i][i] = 1;
        }
		// 1.2�������û�����
		for (int i = 0; i < n; i++) {
			gc.exchangeRows(identityToP, r.nextInt(128), r.nextInt(128));
		}
		System.out.println("�û����� P��");
		for (int i = 0; i < identityToP.length; i++) {
			System.out.println(Arrays.toString(identityToP[i]));
		}
		
		int[] message = new int[n - m * t];
		// �������������Ϣ
		for (int i = 0; i < message.length; i++) {
			message[i] = r.nextInt(2);
		}
		System.out.println("");
		System.out.println("������Ϣ: " + Arrays.toString(message));// ������Ϣ
		System.out.println("**************************************************************");
		System.out.println("****************          ENCRYPTION          ****************");
		System.out.println("**************************************************************");
		int[] encodedMessage = gc.encode(message, identityToS, identityToP, t, m, n);
		System.out.println("��������: " + Arrays.toString(encodedMessage));// ��������
		int[] y = Arrays.copyOf(encodedMessage, encodedMessage.length);
		// �����Ӳ������
		for (int i = 0; i < t; i++) {
			int position = r.nextInt(128);
			y[position] ^= 1;
		}
		System.out.println("�������: " + Arrays.toString(y));// �������
		System.out.println("**************************************************************");
		System.out.println("****************          DECRYPTION          ****************");
		System.out.println("**************************************************************");
		// y * P ^ -1
		int[] cStar = gc.multiMatrix(y, identityToP, true);
		// ����
		int[] plaintext = gc.decode(cStar, identityToS);
		System.out.println("��������: " + Arrays.toString(plaintext));// ��������
		Assert.assertArrayEquals(message, plaintext);
	}
	//*******************************
}
