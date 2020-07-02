package test;

public record TestRecord(int i) {
	//don't declare constructor
}

record WithConstructor(long j) {
	WithConstructor(int i){
		this((long)i);
	}
}
record WithPublicConstructor(long j) {
	public WithPublicConstructor(int i){
		this((long)i);
	}
}
record WithProtectedConstructor(long j) {
	protected WithProtectedConstructor(int i){
		this((long)i);
	}
}

record MultiConstructor(float f) {
	protected MultiConstructor(int i) {
		this(0f);
	}
	public MultiConstructor(float f) {
		this.f = f;
	}
	private MultiConstructor(double i) {
		this(0f);
	}
}
record EmptyRecord() {
}
record PublicEmptyRecord() {
	public PublicEmptyRecord() {
	}
}

record ObjField(Main m) {
}
record ObjFieldConstructor(Main m) {
	public ObjFieldConstructor(Main m) {
		this.m = m;
	}
}
record ObjFieldQualifiedConstructor(Main qm) {
	public ObjFieldQualifiedConstructor(test.Main qm) {
		this.qm = qm;
	}
}

record Methods(int i) {
	public void method() {
	}
	
	public int methodI() {
		return this.i;
	}
	public int i() {
		return this.i * 2;
	}
}

record CustomHashcode(int i) {
	public int hashCode() {
		return 3;
	}
}
record CustomToString(int i) {
	public String toString() {
		return "abc_" + i;
	}
}

record CustomEquals1(int i) {
	public boolean equals(Object other) {
		return false;
	}
}
record CustomEquals2(int i) {
	public boolean equals(java.lang.Object other) {
		return false;
	}
}

record EqualsHashcode1(int i) {
	public boolean equals(java.lang.Object other) {
		return false;
	}
	public int hashCode() {
		return 3;
	}
}
record EqualsHashcode2(int i) {
	public int hashCode() {
		return 3;
	}
	public boolean equals(java.lang.Object other) {
		return false;
	}
}

//compilation failure, canonical constructor must be public
//record NonPublicEmptyRecord() {
//	NonPublicEmptyRecord() {}
//}