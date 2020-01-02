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
package saker.java.compiler.api.compile;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.options.SimpleAnnotationProcessorReferenceOption;

final class JavaAnnotationProcessorBuilderImpl implements JavaAnnotationProcessorBuilder {
	protected ProcessorConfiguration configuration;
	protected Map<String, String> options;
	protected Collection<String> suppressWarnings;

	protected Boolean aggregating;
	protected Boolean consistent;
	protected Boolean alwaysRun;

	public JavaAnnotationProcessorBuilderImpl() {
	}

	@Override
	public void setProcessor(ProcessorConfiguration configuration) {
		Objects.requireNonNull(configuration, "processor configuration");
		this.configuration = configuration;
	}

	@Override
	public void setOptions(Map<String, String> options) {
		this.options = ImmutableUtils.makeImmutableLinkedHashMap(options);
	}

	@Override
	public void setSuppressWarnings(Collection<String> suppressWarnings) {
		this.suppressWarnings = JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(suppressWarnings);
	}

	@Override
	public void setAggregating(Boolean aggregating) {
		this.aggregating = aggregating;
	}

	@Override
	public void setConsistent(Boolean consistent) {
		this.consistent = consistent;
	}

	@Override
	public void setAlwaysRun(Boolean alwaysRun) {
		this.alwaysRun = alwaysRun;
	}

	@Override
	public JavaAnnotationProcessor build() {
		if (configuration == null) {
			throw new IllegalStateException("Processor configuration was not set.");
		}
		SimpleAnnotationProcessorReferenceOption opt = new SimpleAnnotationProcessorReferenceOption(configuration,
				options, suppressWarnings);
		opt.setAggregating(aggregating);
		opt.setConsistent(consistent);
		opt.setAlwaysRun(alwaysRun);
		return opt;
	}

}
