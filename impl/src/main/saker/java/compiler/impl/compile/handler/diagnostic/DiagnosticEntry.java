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
package saker.java.compiler.impl.compile.handler.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.info.GeneratedFileOrigin;

public class DiagnosticEntry implements Externalizable {
	private static final long serialVersionUID = 1L;

	private Kind kind;

	private DiagnosticLocationReference locationReference;

	private String message;
	private GeneratedFileOrigin origin;
	private String warningType;

	/**
	 * For {@link Externalizable}.
	 */
	public DiagnosticEntry() {
	}

	public DiagnosticEntry(Kind kind, String message, String warningtype) {
		this.kind = kind;
		this.message = message;
		this.warningType = warningtype;
	}

	public DiagnosticEntry(Kind kind, String message, GeneratedFileOrigin origin, String warningtype) {
		this.kind = kind;
		this.message = message;
		this.origin = origin;
		this.warningType = warningtype;
	}

	public DiagnosticEntry(Kind kind, DiagnosticLocationReference locationReference, String message,
			GeneratedFileOrigin origin, String warningType) {
		this.kind = kind;
		this.locationReference = locationReference;
		this.message = message;
		this.origin = origin;
		this.warningType = warningType;
	}

	public DiagnosticEntry(Diagnostic<? extends JavaFileObject> diagnostic, SakerPath path, String warningtype) {
		this.kind = diagnostic.getKind();
		this.message = diagnostic.getMessage(null);
		this.warningType = warningtype;

		//-1 as it begins at 1
		int lineNumber = (int) diagnostic.getLineNumber() - 1;
		//diagnostic.getColumnNumber() returns invalid number, as it accounts for tabs. It counts them as 8 width, so it is unusable.
		int lineposstart = -1;
		int lineposend = -1;

		JavaFileObject srcfile = diagnostic.getSource();
		if (srcfile instanceof JavaCompilerJavaFileObject) {
			int[] lineindices = ((JavaCompilerJavaFileObject) srcfile).getLineIndexMap();
			if (lineindices != null) {
				int startpos = (int) diagnostic.getStartPosition();
				if (startpos != Diagnostic.NOPOS) {
					int endpos = (int) diagnostic.getEndPosition();
					int lineidx = StringUtils.getLineIndex(lineindices, startpos);
					int linepositionidx = StringUtils.getLinePositionIndex(lineindices, lineidx, startpos);
					lineposstart = linepositionidx;
					lineposend = (lineposstart + (endpos - startpos));
				}
			}
		}
		this.locationReference = new SimpleDiagnosticLocationReference(
				DiagnosticLocation.create(path, lineNumber, lineposstart, lineposend));
	}

	public DiagnosticLocationReference getLocationReference() {
		return locationReference;
	}

	public Kind getKind() {
		return kind;
	}

	public String getMessage() {
		return message;
	}

	public String getWarningType() {
		return warningType;
	}

	public GeneratedFileOrigin getOrigin() {
		return origin;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(message);
		out.writeObject(locationReference);

		out.writeObject(kind);

		out.writeObject(origin);

		out.writeObject(warningType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		message = (String) in.readObject();
		locationReference = (DiagnosticLocationReference) in.readObject();

		kind = (Kind) in.readObject();

		origin = (GeneratedFileOrigin) in.readObject();

		warningType = (String) in.readObject();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (kind != null ? "kind=" + kind + ", " : "")
				+ (locationReference != null ? "locationReference=" + locationReference + ", " : "")
				+ (message != null ? "message=" + message + ", " : "")
				+ (origin != null ? "origin=" + origin + ", " : "")
				+ (warningType != null ? "warningType=" + warningType : "") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((locationReference == null) ? 0 : locationReference.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((warningType == null) ? 0 : warningType.hashCode());
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
		DiagnosticEntry other = (DiagnosticEntry) obj;
		if (kind != other.kind)
			return false;
		if (locationReference == null) {
			if (other.locationReference != null)
				return false;
		} else if (!locationReference.equals(other.locationReference))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		if (warningType == null) {
			if (other.warningType != null)
				return false;
		} else if (!warningType.equals(other.warningType))
			return false;
		return true;
	}

	public static int compareContents(DiagnosticEntry l, DiagnosticEntry r) {
		int kcmp = l.kind.compareTo(r.kind);
		if (kcmp != 0) {
			return kcmp;
		}
		int mcmp = ObjectUtils.compareNullsFirst(l.message, r.message);
		if (mcmp != 0) {
			return mcmp;
		}
		int wcmp = ObjectUtils.compareNullsFirst(l.warningType, r.warningType);
		if (wcmp != 0) {
			return wcmp;
		}
		return 0;
	}

}