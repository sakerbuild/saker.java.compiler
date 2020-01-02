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
package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.impl.JavaTaskUtils;

public class SimpleAnnotationProcessorReferenceOption implements JavaAnnotationProcessor, Externalizable {
	private static final long serialVersionUID = 1L;

	private ProcessorConfiguration configuration;
	private Map<String, String> options;
	private Collection<String> suppressWarnings;

	private Boolean aggregating;
	private Boolean consistent;
	private Boolean alwaysRun;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleAnnotationProcessorReferenceOption() {
	}

	public SimpleAnnotationProcessorReferenceOption(ProcessorConfiguration configuration, Map<String, String> options,
			Collection<String> suppressWarnings) {
		Objects.requireNonNull(configuration, "configuration");
		this.configuration = configuration;
		this.options = options == null ? Collections.emptyMap() : ImmutableUtils.makeImmutableLinkedHashMap(options);
		this.suppressWarnings = suppressWarnings == null
				? JavaTaskUtils.emptyImmutableIgnoreCaseNullableStringCollection()
				: JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(suppressWarnings);
	}

	public SimpleAnnotationProcessorReferenceOption(ProcessorConfiguration configuration) {
		this(configuration, Collections.emptyNavigableMap(), Collections.emptySet());
	}

	public SimpleAnnotationProcessorReferenceOption(JavaAnnotationProcessor copy) {
		this(new SimpleProcessorConfiguration(
				Objects.requireNonNull(copy, "annotation processor reference").getProcessor()), copy.getOptions(),
				copy.getSuppressWarnings());
		this.aggregating = copy.getAggregating();
		this.consistent = copy.getConsistent();
		this.alwaysRun = copy.getAlwaysRun();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(configuration);
		out.writeObject(aggregating);
		out.writeObject(consistent);
		out.writeObject(alwaysRun);
		SerialUtils.writeExternalMap(out, options);
		SerialUtils.writeExternalCollection(out, suppressWarnings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		configuration = (ProcessorConfiguration) in.readObject();
		aggregating = (Boolean) in.readObject();
		consistent = (Boolean) in.readObject();
		alwaysRun = (Boolean) in.readObject();
		options = SerialUtils.readExternalMap(new LinkedHashMap<>(), in);
		suppressWarnings = SerialUtils.readExternalImmutableNavigableSet(in,
				StringUtils::compareStringsNullFirstIgnoreCase);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggregating == null) ? 0 : aggregating.hashCode());
		result = prime * result + ((alwaysRun == null) ? 0 : alwaysRun.hashCode());
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((consistent == null) ? 0 : consistent.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((suppressWarnings == null) ? 0 : suppressWarnings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleAnnotationProcessorReferenceOption other = (SimpleAnnotationProcessorReferenceOption) obj;
		if (aggregating == null) {
			if (other.aggregating != null)
				return false;
		} else if (!aggregating.equals(other.aggregating))
			return false;
		if (alwaysRun == null) {
			if (other.alwaysRun != null)
				return false;
		} else if (!alwaysRun.equals(other.alwaysRun))
			return false;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (consistent == null) {
			if (other.consistent != null)
				return false;
		} else if (!consistent.equals(other.consistent))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (suppressWarnings == null) {
			if (other.suppressWarnings != null)
				return false;
		} else if (!suppressWarnings.equals(other.suppressWarnings))
			return false;
		return true;
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
