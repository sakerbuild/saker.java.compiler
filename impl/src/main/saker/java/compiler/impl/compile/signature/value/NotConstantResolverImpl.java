package saker.java.compiler.impl.compile.signature.value;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.lang.model.element.Element;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public class NotConstantResolverImpl implements NotConstantResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private String info;

	public NotConstantResolverImpl() {
	}

	public NotConstantResolverImpl(Object info) {
		this.info = info.toString();
	}

	public NotConstantResolverImpl(String info) {
		this.info = info;
	}

	@Override
	public Object resolve(SakerElementsTypes elemtypes, Element resolutionelement) {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(info);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		info = in.readUTF();
	}

	@Override
	public String toString() {
		return "<notconst>" + info + "</notconst";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
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
		NotConstantResolverImpl other = (NotConstantResolverImpl) obj;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		return true;
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		return other instanceof NotConstantResolverImpl;
	}

}
