package saker.java.compiler.impl.compile.handler.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;

public class SimpleDiagnosticLocationReference implements DiagnosticLocationReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private DiagnosticLocation location;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleDiagnosticLocationReference() {
	}

	public SimpleDiagnosticLocationReference(DiagnosticLocation location) {
		this.location = location;
	}

	@Override
	public SakerPath getPath() {
		return location.getPath();
	}

	@Override
	public DiagnosticLocation getLocation(DiagnosticPositionTable table) {
		return location;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(location);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		location = (DiagnosticLocation) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
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
		SimpleDiagnosticLocationReference other = (SimpleDiagnosticLocationReference) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + location + "]";
	}
}
