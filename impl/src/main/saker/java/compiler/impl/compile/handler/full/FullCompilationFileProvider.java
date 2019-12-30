package saker.java.compiler.impl.compile.handler.full;

import java.util.Collection;
import java.util.Set;

import javax.tools.JavaFileObject.Kind;

import saker.build.file.SakerFile;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMISerialize;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;

public interface FullCompilationFileProvider {
	public interface LocationProvider {
		public SakerPathBytes getJavaInputFile(String classname, Kind kind);

		public SakerPathBytes getInputFile(String packagename, String relativename);

		@RMIWrap(RMIArrayListRemoteElementWrapper.class)
		public Collection<? extends SakerFile> list(String packagename, @RMISerialize Set<Kind> kinds, boolean recurse);
	}

	public LocationProvider getLocation(String name, boolean outputlocation);
}
