/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class CompilationUnitResolutionScope implements ResolutionScope {

	protected ImportScope importScope;
	protected NavigableMap<String, ResolutionScope> declaredTypes;

	public CompilationUnitResolutionScope(ImportScope importScope) {
		this.importScope = importScope;
		this.declaredTypes = new TreeMap<>();
	}

	public CompilationUnitResolutionScope(ImportScope importScope,
			NavigableMap<String, ResolutionScope> declaredTypes) {
		this.importScope = importScope;
		this.declaredTypes = declaredTypes;
	}

	public void addDeclaredType(String name, ResolutionScope scope) {
		declaredTypes.put(name, scope);
	}

	public ImportScope getImportScope() {
		return importScope;
	}

	@Override
	public ResolutionScope getEnclosingScope() {
		return null;
	}

	@Override
	public Element asElement(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	public TypeElement asType(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	public ExecutableElement asExecutable(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	public TypeElement resolveType(SakerElementsTypes elemtypes, String simplename) {
		ResolutionScope declared = declaredTypes.get(simplename);
		if (declared != null) {
			return declared.asType(elemtypes);
		}
		Set<? extends ImportDeclaration> importdecls = importScope.getImportDeclarations();
		for (ImportDeclaration id : importdecls) {
			if (id.isWildcard()) {
				continue;
			}
			String resolved = id.resolveType(simplename);
			if (resolved != null) {
				TypeElement te = elemtypes.getTypeElement(resolved);
				if (te != null) {
					return te;
				}
			}
		}

		String packname = importScope.getPackageName();
		if (packname != null) {
			String packid = packname + "." + simplename;
			TypeElement type = elemtypes.getTypeElement(packid);
			if (type != null) {
				return type;
			}
		}
		for (ImportDeclaration id : importdecls) {
			if (!id.isWildcard()) {
				continue;
			}
			String resolved = id.resolveType(simplename);
			if (resolved != null) {
				TypeElement te = elemtypes.getTypeElement(resolved);
				if (te != null) {
					return te;
				}
			}
		}

		String langid = "java.lang." + simplename;
		TypeElement langtype = elemtypes.getTypeElement(langid);
		if (langtype != null) {
			return langtype;
		}
		return null;
	}

	@Override
	public VariableElement resolveVariable(SakerElementsTypes elemtypes, String simplename) {
		Set<? extends ImportDeclaration> importdecls = importScope.getImportDeclarations();
		for (ImportDeclaration id : importdecls) {
			if (!id.isWildcard()) {
				continue;
			}
			String resolved = id.resolveMember(simplename);
			if (resolved != null) {
				TypeElement te = elemtypes.getTypeElement(resolved);
				if (te != null) {
					for (Element member : te.getEnclosedElements()) {
						if (member.getKind().isField()) {
							if (member.getSimpleName().contentEquals(simplename)) {
								return (VariableElement) member;
							}
						}
					}
				}
			}
		}
		for (ImportDeclaration id : importdecls) {
			if (id.isWildcard()) {
				continue;
			}
			String resolved = id.resolveMember(simplename);
			if (resolved != null) {
				TypeElement te = elemtypes.getTypeElement(resolved);
				if (te != null) {
					for (Element member : te.getEnclosedElements()) {
						if (member.getKind().isField()) {
							if (member.getSimpleName().contentEquals(simplename)) {
								return (VariableElement) member;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters) {
		//always raw, as no intermediate template parameters can be provided in a qualified name
		//imports need to be directy through the declaring class, cannot be through a subclass

		QualifiedNameIterator it = new QualifiedNameIterator(qualifiedname);
		String first = it.next();
		TypeElement firsttype = resolveType(elemtypes, first);
		if (firsttype != null) {
			if (!it.hasNext()) {
				//simple name
				return IncrementalElementsTypes.createRawTypeElementSignature(firsttype, typeparameters);
			}
			while (it.hasNext()) {
				String namepart = it.next();
				TypeElement resolved = IncrementalElementsTypes.findTypeInHierarchy(firsttype, namepart);
				if (resolved != null) {
					firsttype = resolved;
					continue;
				}
				//not found
				return null;
			}

			//create raw signature as no parameters were given for internal name parts
			return IncrementalElementsTypes.createRawTypeElementSignature(firsttype, typeparameters);
		}
		//no imported type found with first name part
		//check if qualified name is a full path name
		TypeElement qtype = elemtypes.getTypeElement(qualifiedname);
		if (qtype != null) {
			return CanonicalTypeSignatureImpl.create(qtype.getQualifiedName().toString(), typeparameters);
		}
		//no fully qualified name found, return null
		return null;
	}

	@Override
	public String toString() {
		return "Compilation unit scope: " + importScope.toString();
	}

}
