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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.info.CompilationInfoBase;

public class IncrementalCompilationInfo extends CompilationInfoBase {
	private static final long serialVersionUID = 1L;

	private List<String> options;
	private String sourceVersion;
	private String targetVersion;
	private String jreVersion;

	public IncrementalCompilationInfo() {
	}

	@Override
	public void setOptions(String sourceversion, String targetversion, List<String> options, String jreVersion) {
		this.sourceVersion = sourceversion;
		this.targetVersion = targetversion;
		this.options = options;
		this.jreVersion = jreVersion;
	}

	@Override
	public String getSourceVersion() {
		return sourceVersion;
	}

	@Override
	public String getTargetVersion() {
		return targetVersion;
	}

	@Override
	public List<String> getOptions() {
		return options;
	}

	@Override
	public String getJreVersion() {
		return jreVersion;
	}

	@Override
	public String toString() {
		return "IncrementalCompilationInfo [" + (sourceFiles != null ? "sourceFiles=" + sourceFiles + ", " : "")
				+ (classFiles != null ? "classFiles=" + classFiles : "") + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		SerialUtils.writeExternalCollection(out, options);

		out.writeObject(sourceVersion);
		out.writeObject(targetVersion);
		out.writeObject(jreVersion);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		options = SerialUtils.readExternalImmutableList(in);

		sourceVersion = (String) in.readObject();
		targetVersion = (String) in.readObject();
		jreVersion = (String) in.readObject();
	}

}
