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
package saker.java.compiler.util15.impl;

import javax.lang.model.element.TypeElement;

import saker.build.thirdparty.saker.rmi.connection.MethodTransferProperties;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.rmi.io.writer.WrapperRMIObjectWriteHandler;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;

public class Java15LanguageUtils {
	private Java15LanguageUtils() {
		throw new UnsupportedOperationException();
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(TypeElement.class, "getPermittedSubclasses"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
	}
}
