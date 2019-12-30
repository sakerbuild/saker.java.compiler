package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;

public class ModuleSignatureImpl extends AnnotatedSignatureImpl implements ModuleSignature {
	private static final long serialVersionUID = 1L;

	public static final ModuleSignature UNNAMED = new ModuleSignatureImpl(Collections.emptyList(), "", false,
			Collections.emptyList(), null);

	private String name;
	private boolean open;
	private List<? extends DirectiveSignature> directives = Collections.emptyList();
	private String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public ModuleSignatureImpl() {
	}

	public ModuleSignatureImpl(List<? extends AnnotationSignature> annotations, String name, boolean open,
			List<? extends DirectiveSignature> directives, String docComment) {
		super(annotations);
		this.name = name;
		this.open = open;
		this.directives = directives;
		this.docComment = docComment;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public List<? extends DirectiveSignature> getDirectives() {
		return directives;
	}

	@Override
	public String toString() {
		return super.toString() + "module " + name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((directives == null) ? 0 : directives.hashCode());
		result = prime * result + ((docComment == null) ? 0 : docComment.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (open ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleSignatureImpl other = (ModuleSignatureImpl) obj;
		if (directives == null) {
			if (other.directives != null)
				return false;
		} else if (!directives.equals(other.directives))
			return false;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (open != other.open)
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(name);
		out.writeBoolean(open);
		SerialUtils.writeExternalCollection(out, directives);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		name = in.readUTF();
		open = in.readBoolean();
		directives = SerialUtils.readExternalImmutableList(in);
		docComment = (String) in.readObject();
	}

}
