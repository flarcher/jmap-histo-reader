/*
 * Copyright 2017 - Fabrice LARCHER
 */
package name.larcher.fabrice.jmap_histo_reader;

import java.util.Objects;

/**
 * A heap histogram entry.
 * @author larcher
 */
public class HeapHistoEntry {

	public HeapHistoEntry(String className, int instanceCount, long memorySize) {
		this.className = Objects.requireNonNull(className);
		this.instanceCount = instanceCount;
		this.memorySize = memorySize;
	}

	private final String className;
	private final int instanceCount;
	private final long memorySize;

	public String getClassName() {
		return className;
	}

	public int getInstanceCount() {
		return instanceCount;
	}

	public long getMemorySize() {
		return memorySize;
	}

	@Override
	public int hashCode() {
		return this.className.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HeapHistoEntry other = (HeapHistoEntry) obj;
		if (!Objects.equals(this.className, other.className)) {
			return false;
		}
		return true;
	}

}
