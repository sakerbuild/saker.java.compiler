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
package saker.java.compiler.api.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Describes the {@link Element} and {@link TypeMirror} functionality provided by the incremental Java compiler.
 * <p>
 * The {@link Types} and {@link Elements} instances can be downcasted to {@link SakerElementsTypes} during annotation
 * processing.
 * 
 * @see ProcessingEnvironment#getElementUtils()
 * @see ProcessingEnvironment#getTypeUtils()
 */
public interface SakerElementsTypes extends Elements, Types {
}
