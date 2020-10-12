package test;

public abstract sealed class Shape
	permits Circle, Rectangle, Square {}
final class Circle    extends Shape {}
final class Rectangle extends Shape {}
non-sealed class Square    extends Shape {}