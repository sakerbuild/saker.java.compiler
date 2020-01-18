package test;

public record TestRecord(int i) {
}

record WithConstructor(long j) {
	WithConstructor(int i){
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

//compilation failure, canonical constructor must be public
//record NonPublicEmptyRecord() {
//	NonPublicEmptyRecord() {}
//}