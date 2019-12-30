package saker.java.compiler.impl.compile.handler.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class DiagnosticLocation implements Externalizable {
	private static final long serialVersionUID = 1L;

	public static final DiagnosticLocation EMPTY_INSTANCE = new DiagnosticLocation();

	private SakerPath path;
	/**
	 * Zero based index.
	 */
	private int lineNumber = -1;
	/**
	 * Inclusive zero based index.
	 */
	private int linePositionStart = -1;
	/**
	 * Exclusive zero based index.
	 */
	private int linePositionEnd = -1;

	/**
	 * For {@link Externalizable}.
	 */
	public DiagnosticLocation() {
	}

	private DiagnosticLocation(SakerPath path) {
		this.path = path;
	}

	public static DiagnosticLocation create(SakerPath path) {
		if (path == null) {
			return EMPTY_INSTANCE;
		}
		return new DiagnosticLocation(path);
	}

	public static DiagnosticLocation create(SakerPath path, int lineNumber, int linePositionStart,
			int linePositionEnd) {
		if (path == null) {
			return EMPTY_INSTANCE;
		}
		DiagnosticLocation result = new DiagnosticLocation();
		result.path = path;
		if (path != null) {
			result.lineNumber = lineNumber < 0 ? -1 : lineNumber;
			if (linePositionStart >= 0 && linePositionEnd >= linePositionStart) {
				result.linePositionStart = linePositionStart;
				result.linePositionEnd = linePositionEnd;
			}
		}
		return result;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getLinePositionEnd() {
		return linePositionEnd;
	}

	public int getLinePositionStart() {
		return linePositionStart;
	}

	public SakerPath getPath() {
		return path;
	}

	public static int compare(DiagnosticLocation l, DiagnosticLocation r) {
		if (l == null) {
			if (r == null) {
				return 0;
			}
			return -1;
		}
		if (r == null) {
			return 1;
		}
		int pcmp = ObjectUtils.compareNullsFirst(l.path, r.path);
		if (pcmp != 0) {
			return pcmp;
		}
		int lcmp = Integer.compare(l.lineNumber, r.lineNumber);
		if (lcmp != 0) {
			return lcmp;
		}
		lcmp = Integer.compare(l.linePositionStart, r.linePositionStart);
		if (lcmp != 0) {
			return lcmp;
		}
		lcmp = Integer.compare(l.linePositionEnd, r.linePositionEnd);
		if (lcmp != 0) {
			return lcmp;
		}
		return 0;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeInt(lineNumber);
		out.writeInt(linePositionStart);
		out.writeInt(linePositionEnd);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		lineNumber = in.readInt();
		linePositionStart = in.readInt();
		linePositionEnd = in.readInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lineNumber;
		result = prime * result + linePositionEnd;
		result = prime * result + linePositionStart;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		DiagnosticLocation other = (DiagnosticLocation) obj;
		if (lineNumber != other.lineNumber)
			return false;
		if (linePositionEnd != other.linePositionEnd)
			return false;
		if (linePositionStart != other.linePositionStart)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (path == null) {
			return "";
		}
		if (lineNumber < 0) {
			return path.toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append(":" + lineNumber);
		if (linePositionStart >= 0) {
			sb.append(':');
			sb.append(linePositionStart);
			sb.append('-');
			sb.append(linePositionEnd);
		}
		return sb.toString();
	}
}
