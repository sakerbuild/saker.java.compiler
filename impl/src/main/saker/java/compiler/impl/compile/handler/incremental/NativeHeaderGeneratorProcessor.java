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
package saker.java.compiler.impl.compile.handler.incremental;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.handler.NativeHeaderSakerFile;
import saker.java.compiler.impl.compile.signature.jni.NativeConstantSignature;
import saker.java.compiler.impl.compile.signature.jni.NativeMethodSignature;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;

public class NativeHeaderGeneratorProcessor implements Processor {
	private Filer filer;
	private Elements elements;
	private Types types;

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return ImmutableUtils.singletonSet("*");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		filer = processingEnv.getFiler();
		elements = processingEnv.getElementUtils();
		types = processingEnv.getTypeUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element rootelem : roundEnv.getRootElements()) {
			ElementKind tkind = rootelem.getKind();
			switch (tkind) {
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					TypeElement telem = (TypeElement) rootelem;
					Map<String, Integer> methodfreqs = new TreeMap<>();
					List<VariableElement> nativefields = new ArrayList<>();
					Collection<ExecutableElement> nativemethods = new ArrayList<>();
					for (Element enclosed : rootelem.getEnclosedElements()) {
						switch (enclosed.getKind()) {
							case FIELD: {
								for (AnnotationMirror am : enclosed.getAnnotationMirrors()) {
									DeclaredType at = am.getAnnotationType();
									TypeElement attypeelem = (TypeElement) at.asElement();
									if (attypeelem.getQualifiedName().contentEquals("java.lang.annotation.Native")) {
										VariableElement ve = (VariableElement) enclosed;
										if (ve.getConstantValue() != null) {
											nativefields.add(ve);
										}
										break;
									}
								}
								break;
							}
							case METHOD: {
								if (enclosed.getModifiers().contains(Modifier.NATIVE)) {
									methodfreqs.compute(enclosed.getSimpleName().toString(),
											(k, v) -> v == null ? 1 : (v + 1));
									nativemethods.add((ExecutableElement) enclosed);
								}
								break;
							}
							default: {
								break;
							}
						}
					}
					if (!nativefields.isEmpty() || !nativemethods.isEmpty()) {
						Collection<NativeSignature> sigs = new ArrayList<>();
						String classbinaryname = elements.getBinaryName(telem).toString();
						for (VariableElement ve : nativefields) {
							sigs.add(new NativeConstantSignature(ve.getSimpleName().toString(), ve.getConstantValue(),
									elements.getDocComment(ve), classbinaryname));
						}
						for (ExecutableElement ee : nativemethods) {
							sigs.add(new NativeMethodSignature(classbinaryname, ee, types, elements,
									methodfreqs.get(ee.getSimpleName().toString()) > 1, elements.getDocComment(ee)));
						}
						String fname = classbinaryname.replace('.', '_').replace('$', '_') + ".h";
						try {
							FileObject headerfileobj = filer.createResource(StandardLocation.NATIVE_HEADER_OUTPUT, "",
									fname, telem);
							try (OutputStream os = headerfileobj.openOutputStream()) {
								NativeHeaderSakerFile.generateHeader(sigs, classbinaryname, os);
							}
						} catch (IOException e) {
							throw new UncheckedIOException("Failed to create header file for: " + classbinaryname, e);
						}
					}
					break;
				}
				default: {
					break;
				}
			}

		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
