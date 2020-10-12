package test;

public abstract sealed class OmitPermit {}
final class OP1 extends OmitPermit {}

final class OP3 extends test.OmitPermit {}

class SomeOther {
	final class OPInner extends test.OmitPermit {}
	final class OPInner2 extends OmitPermit {}
	
	//the following is NOT implicitly added to the permit list
	class OmitPermit {}
	final class OPInner3 extends OmitPermit {}
}

class SubSomeOther extends SomeOther {
	//the following is NOT implicitly added to the permit list
	final class OPInner4 extends OmitPermit {}	
}

final class OP2 extends OmitPermit {}

sealed class PermitTemplated permits MyTemplated {}
final class MyTemplated<T> extends PermitTemplated {}

sealed interface OmitItf {}

sealed interface Itf1 extends OmitItf {}
final class SubItf implements Itf1 {}