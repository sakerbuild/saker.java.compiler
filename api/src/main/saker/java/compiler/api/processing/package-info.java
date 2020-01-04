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
/**
 * Contains classes that are used by the incremental Java compiler during annotation processing.
 * <p>
 * The interfaces can be downcasted to their API types when appropriate. (E.g. {@link ProcessingEnvironment} that a
 * {@link Processor} receives may be downcasted to {@link SakerProcessingEnvironment}.) When doing so, make sure to use
 * <code>instanceof</code> to ensure that your processor doesn't cause a cast exception when being run in a different
 * build environment.
 */
package saker.java.compiler.api.processing;

import javax.annotation.processing.Processor;
import javax.annotation.processing.ProcessingEnvironment;