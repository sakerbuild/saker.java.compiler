package test;

import java.util.List;

import testing.saker.tests.tasks.javac.compatibility.*;

class BaseClass {
}

interface BaseItf {
}

class ExcClass extends Throwable {
	private static final long serialVersionUID = 1L;
}

@MyInheritableAnnot
class InheritedBaseClass extends BaseClass {
}

@MyInheritableAnnot
class InheritedClass {
}

class SubInheritedBaseClass extends InheritedBaseClass {
}

class SubSubInheritedBaseClass extends SubInheritedBaseClass {
}

class SubInheritedClass extends InheritedClass {
}

class SubSubInheritedClass extends SubInheritedClass {
}

@MyInheritableAnnot
class InheritedSubSubInheritedClass extends SubInheritedClass {
}

@MyInheritableAnnot
interface InheritableItf {
}

interface SubInheritableItf extends InheritableItf {
}

class SubInheritableItfClass implements SubInheritableItf {
}

class InheritableItfClass implements InheritableItf {
}

@MyAnyAnnotation
class SimpleAnnotatedClass {
}

@MyAnyAnnotation(1)
class FullyAnnotatedClass<@MyAnyAnnotation(2) T, @MyAnyAnnotation(3) X extends @MyAnyAnnotation(4) T, @MyAnyAnnotation(5) Y extends @MyAnyAnnotation(6) SimpleAnnotatedClass>
		extends @MyAnyAnnotation(7) BaseClass implements @MyAnyAnnotation(8) BaseItf {
	@MyAnyAnnotation(9)
	public void func(@MyAnyAnnotation(11) BaseItf p) throws @MyAnyAnnotation(10) ExcClass {
	}

	@MyAnyAnnotation(13)
	public List<@MyAnyAnnotation(12) BaseItf> l;

	//XXX compatibility testing of the wildcard fails, as javac doesn't keep the annotation on the wildcard.
//	public List<@MyAnyAnnotation(13) ? extends @MyAnyAnnotation(14) BaseClass> wl;

	@MyAnyAnnotation(15)
	public <@MyAnyAnnotation(16) CT> FullyAnnotatedClass() {
	}

	@MyAnyAnnotation(20)
	public FullyAnnotatedClass<@MyAnyAnnotation(17) Number, @MyAnyAnnotation(18) Integer, @MyAnyAnnotation(19) SimpleAnnotatedClass> fac;
}

@MyRepeatableAnnot(1)
class SingleRepeatable {
}

@MyRepeatableAnnot(2)
@MyRepeatableAnnot(3)
@MyRepeatableAnnot(4)
class MultiRepeatable {
}

@MyRepeatableAnnotContainer(@MyRepeatableAnnot(5))
class SingleRepeatableContainer {
}

@MyRepeatableAnnotContainer({ @MyRepeatableAnnot(6) })
class SingleRepeatableArrayContainer {
}

@MyRepeatableAnnotContainer({ @MyRepeatableAnnot(7), @MyRepeatableAnnot(8), @MyRepeatableAnnot(9) })
class MultiRepeatableContainer {
}

@MyRepeatableAnnotContainer({ @MyRepeatableAnnot(10), @MyRepeatableAnnot(11), @MyRepeatableAnnot(12) })
@MyRepeatableAnnot(13)
//cannot have more than one outside of the contained annotation
class MixedRepeatableArrayContainer {
}

@MyRepeatableAnnotContainer(@MyRepeatableAnnot(14))
@MyRepeatableAnnot(15)
//cannot have more than one outside of the contained annotation
class MixedRepeatableContainer {
}
