package saker.java.compiler.impl.compile.handler.invoker.rmi;

import java.io.IOException;

import javax.lang.model.element.Name;

import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;

public class NameRMIWrapper implements RMIWrapper {
	private Name name;

	public NameRMIWrapper() {
	}

	public NameRMIWrapper(Name name) {
		this.name = name;
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		out.writeObject(name.toString());
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		this.name = new IncrementalName((String) in.readObject());
	}

	@Override
	public Object resolveWrapped() {
		return name;
	}

	@Override
	public Object getWrappedObject() {
		throw new UnsupportedOperationException();
	}
}
