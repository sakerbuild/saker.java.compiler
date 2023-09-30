package saker.java.compiler.impl.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;

public class RemoteKeyValueLinkedHashMapRMIWrapper implements RMIWrapper {

	private Map<?, ?> map;

	public RemoteKeyValueLinkedHashMapRMIWrapper() {
	}

	public RemoteKeyValueLinkedHashMapRMIWrapper(Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public Object getWrappedObject() {
		return map;
	}

	@Override
	public Object resolveWrapped() {
		return map;
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		Object key = in.readObject();
		if (key == UtilRMIWrapSentinels.NULL_INPUT) {
			map = null;
			return;
		}
		Map<Object, Object> tm = new LinkedHashMap<>();
		while (true) {
			if (key == UtilRMIWrapSentinels.END_OF_OBJECTS) {
				break;
			}
			tm.put(key, in.readObject());
			key = in.readObject();
		}
		this.map = tm;

	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		if (map == null) {
			out.writeObject(UtilRMIWrapSentinels.NULL_INPUT);
			return;
		}

		for (Entry<?, ?> entry : map.entrySet()) {
			out.writeRemoteObject(entry.getKey());
			out.writeRemoteObject(entry.getValue());
		}
		out.writeObject(UtilRMIWrapSentinels.END_OF_OBJECTS);
	}

}
