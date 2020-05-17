package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.thirdparty.saker.util.io.SerialUtils;

public class OutputBytecodeManipulationOption implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String moduleMainClassInjectValue;
	private String moduleVersionInjectValue;
	private boolean patchEnablePreview;

	public OutputBytecodeManipulationOption() {
	}

	public String getModuleMainClassInjectValue() {
		return moduleMainClassInjectValue;
	}

	public void setModuleMainClassInjectValue(String moduleMainClassInjectValue) {
		this.moduleMainClassInjectValue = moduleMainClassInjectValue;
	}

	public String getModuleVersionInjectValue() {
		return moduleVersionInjectValue;
	}

	public void setModuleVersionInjectValue(String moduleVersionInjectValue) {
		this.moduleVersionInjectValue = moduleVersionInjectValue;
	}

	public boolean isPatchEnablePreview() {
		return patchEnablePreview;
	}

	public void setPatchEnablePreview(boolean patchEnablePreview) {
		this.patchEnablePreview = patchEnablePreview;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(moduleMainClassInjectValue);
		out.writeObject(moduleVersionInjectValue);
		out.writeBoolean(patchEnablePreview);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		moduleMainClassInjectValue = SerialUtils.readExternalObject(in);
		moduleVersionInjectValue = SerialUtils.readExternalObject(in);
		patchEnablePreview = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moduleMainClassInjectValue == null) ? 0 : moduleMainClassInjectValue.hashCode());
		result = prime * result + ((moduleVersionInjectValue == null) ? 0 : moduleVersionInjectValue.hashCode());
		result = prime * result + (patchEnablePreview ? 1231 : 1237);
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
		OutputBytecodeManipulationOption other = (OutputBytecodeManipulationOption) obj;
		if (moduleMainClassInjectValue == null) {
			if (other.moduleMainClassInjectValue != null)
				return false;
		} else if (!moduleMainClassInjectValue.equals(other.moduleMainClassInjectValue))
			return false;
		if (moduleVersionInjectValue == null) {
			if (other.moduleVersionInjectValue != null)
				return false;
		} else if (!moduleVersionInjectValue.equals(other.moduleVersionInjectValue))
			return false;
		if (patchEnablePreview != other.patchEnablePreview)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OutputBytecodeManipulationOption["
				+ (moduleMainClassInjectValue != null
						? "moduleMainClassInjectValue=" + moduleMainClassInjectValue + ", "
						: "")
				+ (moduleVersionInjectValue != null ? "moduleVersionInjectValue=" + moduleVersionInjectValue : "")
				+ "]";
	}

}
