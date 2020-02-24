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
package saker.java.compiler.api.processor;

import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;

/**
 * Context interface provided to {@link ProcessorCreator}s when the annotation processors are being instantiated.
 * <p>
 * The context provides access to some features of the build environment in order for processor creators to be able to
 * perform operations related to creating the processor instances.
 * <p>
 * The interface doesn't provide any dependency management related functionality. If a processor creator needs to report
 * dependencies then it should do that when it is being constructed.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ProcessorCreationContext {
	/**
	 * Gets the build environment.
	 * <p>
	 * The callers should only use the {@link SakerEnvironment#getEnvironmentPropertyCurrentValue(EnvironmentProperty)}
	 * if they already reported a dependency to it when the processor creator was created.
	 * 
	 * @return The environment.
	 */
	public SakerEnvironment getEnvironment();
}
