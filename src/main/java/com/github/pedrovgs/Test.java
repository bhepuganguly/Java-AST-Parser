package com.github.pedrovgs;
public class Test {
	int j = 1, k =5;

	public static void main(String[] args) {
		int i = 0, m =9, a;
		while( i<m ) {
			i++;
		}
		a = i+m;
		Template.instrum("10", "ExpressionStatement",
				"com.github.pedrovgs.Test.main.a", a,
				"com.github.pedrovgs.Test.main.i", i,
				"com.github.pedrovgs.Test.main.m", m);
		new Test().abc();
		System.out.println("Local Variable Declarations inside main() : "+ i +" , "+ m+" , "+a);
	}

	public void abc() {
		if (j == k/5) {
			int g = 45;
			System.out.println("Local Variable Declarations inside abc() : " + g);
		}
		j = j*k;
		Template.instrum("20", "ExpressionStatement",
				"com.github.pedrovgs.Test.abc.j", j,
				"com.github.pedrovgs.Test.abc.j", j,
				"com.github.pedrovgs.Test.abc.k", k);
		System.out.println("Global Variable Declarations inside abc() :  "+ j + " , " + k);
	}
}