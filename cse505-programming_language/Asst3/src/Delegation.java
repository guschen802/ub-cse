public class Delegation {
	public static void main(String args[]) {
		C111 c111 = new C111();
		System.out.println(c111.m111());

		C112 c112 = new C112();
		System.out.println(c112.m112());
		
		D111 d111 = new D111();
		System.out.println(d111.m111());

		D112 d112 = new D112();
		System.out.println(d112.m112());
	}
}

 class C1 {
	int a1 = 1;

	public int m1() {
		return a1 + p1(100) + q1(100);
	}

	public int p1(int m) {
		return m;
	}
	
	public int q1(int m) {
		return m;
	}
}

 class C11 extends C1 {
	int a11 = 11;

	public int m11() {
		return m1() + q1(200);
	}

	public int p1(int m) {
		return m * a1;
	}

	public int q1(int m) {
		return m + a11;
	}
}

class C111 extends C11 {
	int a111 = 111;

	public int m111() {
		return m1() + m11() + a111;
	}
	
	public int p1(int m) {
		return m * a1 * a11;
	}
	
}

class C112 extends C11 {
	int a112 = 112;

	public int m112() {
		return m1() + m11() + a112;
	}

	public int p1(int m) {
		return m * a1 * a11 * a112;
		
	}
}


// -------SIMULATING CLASS INHERITANCE BY DELEGATION ---------

interface I1 {
	int m1();
	int p1(int m);
	int q1(int m);
	int getA1();
	 
}

interface I11 extends I1 {
	int m11();
	int getA11();
}

interface I111 extends I11 {
	int m111();
	int getA111();
}

interface I112 extends I11 {
	int m112();
	int getA112();
}

class D1 implements I1 {
	int a1 = 1;
	I1 i1x;//lower layer interface
	
	public D1(){
		i1x = this;
	}
	
	public D1(I1 i){
		i1x = i;
	}
	
	public int m1() {
		return a1 + i1x.p1(100) + i1x.q1(100);
	}

	public int p1(int m) {
		return m;
	}
	
	public int q1(int m) {
		return m;
	} 
	
	public int getA1() {
		return a1;
	} 	
}

class D11 implements I11 {
	int a11 = 11;
	I1 i1;//upper layer interface
	I11 i11x;//lower layer interface
	
	public D11(){
		i1 = new D1(this);
		i11x = this;
	}
	public D11(I11 i){
		i1 = new D1(i);
		i11x = i;
	}
	
	//og
	public int m11() {
		return i11x.m1() + i11x.q1(200);
	}

	public int p1(int m) {
		return m * i11x.getA1();
	}

	public int q1(int m) {
		return m + a11;
	}
	
	public int getA11() {
		return a11;
	}
	//end og
	public int m1() {
		return i1.m1();
	}

	public int getA1() {
		return i1.getA1();
	}
}

class D111 implements I111 { 
	int a111 = 111;
	I11 i11;//upper layer interface
	I111 i111x;//lower layer interface
	public D111(){
		i11 = new D11(this);
		i111x = this;
	}
	
	public D111(I111 i){
		i11 = new D11(i);
		i111x = i;
	}
	//og
	public int m111() {
		return i111x.m1() + i111x.m11() + a111;
	}
	
	public int p1(int m) {
		return m * i111x.getA1() * i111x.getA11();
	}
	
	public int getA111() {
		return a111;
	}
	//end og
	public int m1() {
		return i11.m1();
	}
	
	public int m11() {
		return i11.m11();
	}
	
	public int q1(int m) {
		return i11.q1(m);
	}
	
	public int getA1() {
		return i11.getA1();
	}
	
	public int getA11() {
		return i11.getA11();
	}
}

class D112 implements I112 {
	int a112 = 112;
	I11 i11;//upper layer interface
	I112 i112x;//lower layer interface
	
	public D112(){
		i11 = new D11(this);
		i112x = this;
	}
	public D112(I112 i){
		i11 = new D11(i);
		i112x = i;
	}
	//og
	
	public int m112() {
		return i112x.m1() + i112x.m11() + a112;
	}

	public int p1(int m) {
		return m * i112x.getA1() * i112x.getA11() * a112;
		
	}
	
	public int getA112() {
		return a112;
	}
	//end og
	public int m1() {
		return i11.m1();
	}
	
	public int m11() {
		return i11.m11();
	}
	
	public int q1(int m) {
		return i11.q1(m);
	}
	
	public int getA1() {
		return i11.getA1();
	}
	
	public int getA11() {
		return i11.getA11();
	}
	
	
}
