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
package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.NavigableSet;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;

public class GeneratedFileOrigin implements Externalizable {
	private static final long serialVersionUID = 1L;

	protected NavigableMap<SakerPath, ? extends ClassHoldingFileData> originatingFiles;
	protected ProcessorDetails originatingProcessorDetails;

	/**
	 * For {@link Externalizable}.
	 */
	public GeneratedFileOrigin() {
	}

	public GeneratedFileOrigin(ProcessorDetails originatingProcessorDetails) {
		this.originatingProcessorDetails = originatingProcessorDetails;
		this.originatingFiles = Collections.emptyNavigableMap();
	}

	public GeneratedFileOrigin(NavigableMap<SakerPath, ? extends ClassHoldingFileData> originatingFiles,
			ProcessorDetails originatingProcessorDetails) {
		this.originatingFiles = originatingFiles;
		this.originatingProcessorDetails = originatingProcessorDetails;
	}

	public Collection<? extends ClassHoldingFileData> getOriginatingFileDatas() {
		return originatingFiles.values();
	}

	public NavigableMap<SakerPath, ? extends ClassHoldingFileData> getOriginatingFiles() {
		return originatingFiles;
	}

	public NavigableSet<SakerPath> getOriginatingFilePaths() {
		return originatingFiles.navigableKeySet();
	}

	public ProcessorDetails getProcessorDetails() {
		return originatingProcessorDetails;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, originatingFiles);
		out.writeObject(originatingProcessorDetails);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		originatingFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		originatingProcessorDetails = (ProcessorDetails) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((originatingFiles == null) ? 0 : originatingFiles.hashCode());
		result = prime * result + ((originatingProcessorDetails == null) ? 0 : originatingProcessorDetails.hashCode());
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
		GeneratedFileOrigin other = (GeneratedFileOrigin) obj;
		if (originatingFiles == null) {
			if (other.originatingFiles != null)
				return false;
		} else if (!originatingFiles.equals(other.originatingFiles))
			return false;
		if (originatingProcessorDetails == null) {
			if (other.originatingProcessorDetails != null)
				return false;
		} else if (!originatingProcessorDetails.equals(other.originatingProcessorDetails))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return originatingProcessorDetails.toString() + (originatingFiles.isEmpty() ? ""
				: "(" + StringUtils.toStringJoin(", ", originatingFiles.keySet()) + ")");
	}

}
