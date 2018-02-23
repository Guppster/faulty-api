/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.softlab.goodinges.fpgrowth;

import java.io.FileNotFoundException;

/**
 *
 * @author Kamran
 */
public class Main {

	static int threshold = 10000;
	static String file = "census.dat";

	public static void main(String[] args) throws FileNotFoundException {
		long start = System.currentTimeMillis();
		// new FPGrowth(new File(file), threshold);
		System.out.println((System.currentTimeMillis() - start));
	}
}
