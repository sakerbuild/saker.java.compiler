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
package saker.java.compiler.impl.classpath.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredTaskResult;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.JarNestRepositoryBundle;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.exc.BundleLoadingFailedException;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.LocalFileLocation;

public class BundleClassPathEntry implements ClassPathEntry, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleKey bundleKey;
	private Object implementationVersionKey;

	private StructuredTaskResult sourceAttachment;
	private StructuredTaskResult documentationAttachment;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleClassPathEntry() {
	}

	public BundleClassPathEntry(BundleKey bundleKey, Object implementationVersionKey,
			StructuredTaskResult sourceAttachment, StructuredTaskResult documentationAttachment) {
		this.bundleKey = bundleKey;
		this.sourceAttachment = sourceAttachment;
		this.documentationAttachment = documentationAttachment;
		this.implementationVersionKey = implementationVersionKey;
	}

	public BundleClassPathEntry(BundleKey bundleKey, Object implementationVersionKey) {
		this.bundleKey = bundleKey;
		this.implementationVersionKey = implementationVersionKey;
	}

	public void setDocumentationAttachment(StructuredTaskResult documentationAttachment) {
		this.documentationAttachment = documentationAttachment;
	}

	public void setSourceAttachment(StructuredTaskResult sourceAttachment) {
		this.sourceAttachment = sourceAttachment;
	}

	@Override
	public FileLocation getFileLocation() {
		NestBundleClassLoader cl = (NestBundleClassLoader) BundleClassPathEntry.class.getClassLoader();
		BundleStorageView storageview = cl.getBundleStorageConfiguration()
				.getBundleStorageViewForKey(bundleKey.getStorageViewKey());
		if (storageview == null) {
			throw new IllegalArgumentException("Bundle not found for bundle key: " + bundleKey);
		}
		NestRepositoryBundle bundle;
		try {
			bundle = storageview.getBundle(bundleKey.getBundleIdentifier());
		} catch (NullPointerException | BundleLoadingFailedException e) {
			throw new IllegalArgumentException("Bundle not found for bundle key: " + bundleKey, e);
		}
		if (bundle == null) {
			throw new IllegalArgumentException("Bundle not found for bundle key: " + bundleKey);
		}
		if (!(bundle instanceof JarNestRepositoryBundle)) {
			throw new IllegalArgumentException("Unsupported bundle type: " + bundle.getClass().getName());
		}
		JarNestRepositoryBundle jarbundle = (JarNestRepositoryBundle) bundle;
		return LocalFileLocation.create(SakerPath.valueOf(jarbundle.getJarPath()));
	}

	@Override
	public Collection<? extends ClassPathReference> getAdditionalClassPathReferences() {
		return null;
	}

	@Override
	public Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return null;
	}

	@Override
	public Object getAbiVersionKey() {
		return null;
	}

	@Override
	public Object getImplementationVersionKey() {
		return implementationVersionKey;
	}

	@Override
	public StructuredTaskResult getSourceAttachment() {
		return sourceAttachment;
	}

	@Override
	public StructuredTaskResult getDocumentationAttachment() {
		return documentationAttachment;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleKey);
		out.writeObject(implementationVersionKey);
		out.writeObject(sourceAttachment);
		out.writeObject(documentationAttachment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKey = (BundleKey) in.readObject();
		implementationVersionKey = in.readObject();
		sourceAttachment = (StructuredTaskResult) in.readObject();
		documentationAttachment = (StructuredTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentationAttachment == null) ? 0 : documentationAttachment.hashCode());
		result = prime * result + ((implementationVersionKey == null) ? 0 : implementationVersionKey.hashCode());
		result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
		result = prime * result + ((sourceAttachment == null) ? 0 : sourceAttachment.hashCode());
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
		BundleClassPathEntry other = (BundleClassPathEntry) obj;
		if (documentationAttachment == null) {
			if (other.documentationAttachment != null)
				return false;
		} else if (!documentationAttachment.equals(other.documentationAttachment))
			return false;
		if (implementationVersionKey == null) {
			if (other.implementationVersionKey != null)
				return false;
		} else if (!implementationVersionKey.equals(other.implementationVersionKey))
			return false;
		if (bundleKey == null) {
			if (other.bundleKey != null)
				return false;
		} else if (!bundleKey.equals(other.bundleKey))
			return false;
		if (sourceAttachment == null) {
			if (other.sourceAttachment != null)
				return false;
		} else if (!sourceAttachment.equals(other.sourceAttachment))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + bundleKey + "]";
	}
}
