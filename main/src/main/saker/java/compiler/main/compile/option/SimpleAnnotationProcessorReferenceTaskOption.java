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
package saker.java.compiler.main.compile.option;

import java.io.Externalizable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.options.SimpleAnnotationProcessorReferenceOption;
import saker.java.compiler.impl.options.SimpleProcessorConfiguration;

class SimpleAnnotationProcessorReferenceTaskOption implements AnnotationProcessorReferenceTaskOption {
	private ProcessorConfiguration configuration;
	private Map<String, String> options;
	private Collection<String> suppressWarnings;

	private Boolean aggregating;
	private Boolean consistent;
	private Boolean alwaysRun;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleAnnotationProcessorReferenceTaskOption() {
	}

	public SimpleAnnotationProcessorReferenceTaskOption(ProcessorConfiguration configuration,
			Map<String, String> options, Collection<String> suppressWarnings) {
		Objects.requireNonNull(configuration, "configuration");
		this.configuration = configuration;
		this.options = options;
		this.suppressWarnings = suppressWarnings;
	}

	public SimpleAnnotationProcessorReferenceTaskOption(ProcessorConfiguration configuration) {
		this(configuration, Collections.emptyNavigableMap(), Collections.emptySet());
	}

	public SimpleAnnotationProcessorReferenceTaskOption(AnnotationProcessorReferenceTaskOption copy) {
		this(new SimpleProcessorConfiguration(
				Objects.requireNonNull(copy, "annotation processor reference").getProcessor()),
				ImmutableUtils.makeImmutableLinkedHashMap(copy.getOptions()),
				JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(copy.getSuppressWarnings()));
		this.aggregating = copy.getAggregating();
		this.consistent = copy.getConsistent();
		this.alwaysRun = copy.getAlwaysRun();
	}

	@Override
	public JavaAnnotationProcessor toJavaAnnotationProcessor(TaskContext taskcontext) {
		SimpleAnnotationProcessorReferenceOption result = new SimpleAnnotationProcessorReferenceOption(configuration,
				options, suppressWarnings);
		result.setAggregating(aggregating);
		result.setConsistent(consistent);
		result.setAlwaysRun(alwaysRun);
		return result;
	}

	@Override
	public AnnotationProcessorReferenceTaskOption clone() {
		return this;
	}

	@Override
	public ProcessorConfiguration getProcessor() {
		return configuration;
	}

	@Override
	public Map<String, String> getOptions() {
		return options;
	}

	@Override
	public Collection<String> getSuppressWarnings() {
		return suppressWarnings;
	}

	@Override
	public boolean getAlwaysRun() {
		return alwaysRun == null ? configuration.getAlwaysRun() : alwaysRun;
	}

	@Override
	public boolean getConsistent() {
		return consistent == null ? configuration.getConsistent() : consistent;
	}

	@Override
	public boolean getAggregating() {
		return aggregating == null ? configuration.getAggregating() : aggregating;
	}

	public void setAlwaysRun(Boolean alwaysRun) {
		this.alwaysRun = alwaysRun;
	}

	public void setAggregating(Boolean aggregating) {
		this.aggregating = aggregating;
	}

	public void setConsistent(Boolean consistent) {
		this.consistent = consistent;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (configuration != null ? "configuration=" + configuration + ", " : "")
				+ (options != null ? "options=" + options + ", " : "")
				+ (suppressWarnings != null ? "suppressWarnings=" + suppressWarnings + ", " : "")
				+ (aggregating != null ? "aggregating=" + aggregating + ", " : "")
				+ (consistent != null ? "consistent=" + consistent + ", " : "")
				+ (alwaysRun != null ? "alwaysRun=" + alwaysRun : "") + "]";
	}

}
