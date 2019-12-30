package saker.java.compiler.util9.impl.model;

import java.util.EnumMap;
import java.util.Map;

import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.ModuleElement.UsesDirective;

import saker.build.thirdparty.saker.util.function.TriFunction;

public class KindBasedDirectiveVisitor9 {
	@SuppressWarnings("rawtypes")
	private static final Map<DirectiveKind, TriFunction<DirectiveVisitor, ? extends Directive, Object, Object>> KIND_CALL_MAPS = getKindBasedDirectiveVisitorMap();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<DirectiveKind, TriFunction<DirectiveVisitor, ? extends Directive, Object, Object>> getKindBasedDirectiveVisitorMap() {
		EnumMap<DirectiveKind, TriFunction<DirectiveVisitor, ? extends Directive, Object, Object>> result = new EnumMap<>(
				DirectiveKind.class);
		result.put(DirectiveKind.EXPORTS,
				(TriFunction<DirectiveVisitor, ExportsDirective, Object, Object>) DirectiveVisitor::visitExports);
		result.put(DirectiveKind.OPENS,
				(TriFunction<DirectiveVisitor, OpensDirective, Object, Object>) DirectiveVisitor::visitOpens);
		result.put(DirectiveKind.PROVIDES,
				(TriFunction<DirectiveVisitor, ProvidesDirective, Object, Object>) DirectiveVisitor::visitProvides);
		result.put(DirectiveKind.REQUIRES,
				(TriFunction<DirectiveVisitor, RequiresDirective, Object, Object>) DirectiveVisitor::visitRequires);
		result.put(DirectiveKind.USES,
				(TriFunction<DirectiveVisitor, UsesDirective, Object, Object>) DirectiveVisitor::visitUses);

		return result;
	}

	private KindBasedDirectiveVisitor9() {
		throw new UnsupportedOperationException();
	}

	public static <R, P> R visit(Directive Directive, DirectiveVisitor<R, P> visitor, P p) {
		return visit(Directive.getKind(), Directive, visitor, p);
	}

	@SuppressWarnings("unchecked")
	public static <R, P> R visit(DirectiveKind kind, Directive Directive, DirectiveVisitor<R, P> visitor, P p) {
		@SuppressWarnings("rawtypes")
		TriFunction got = KIND_CALL_MAPS.get(kind);
		if (got == null) {
			return visitor.visitUnknown(Directive, p);
		}
		return (R) got.apply(visitor, Directive, p);
	}
}
