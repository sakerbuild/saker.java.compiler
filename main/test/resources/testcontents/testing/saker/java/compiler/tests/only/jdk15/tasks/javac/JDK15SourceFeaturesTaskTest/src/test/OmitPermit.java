package test;

public abstract sealed class OmitPermit {}
final class OP1 extends OmitPermit {}
final class OP2 extends OmitPermit {}
final class OP3 extends test.OmitPermit {}

final class SomeOther {
	final class OPInner extends test.OmitPermit {}	
}