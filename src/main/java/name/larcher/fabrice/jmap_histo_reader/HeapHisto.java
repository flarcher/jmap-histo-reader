/*
 * Copyright 2017 - Fabrice LARCHER
 */
package name.larcher.fabrice.jmap_histo_reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author larcher
 */
public class HeapHisto implements Iterable<HeapHistoEntry> {

	public HeapHisto() {
		entries = new ArrayList<>();
	}

	public HeapHisto(List<HeapHistoEntry> entries) {
		this.entries = entries;
		for (HeapHistoEntry entry : entries) {
			instanceCount += entry.getInstanceCount();
			memorySize += entry.getMemorySize();
		}
	}

	private int instanceCount;
	private long memorySize;
	private final List<HeapHistoEntry> entries;

	void add(HeapHistoEntry entry) {
		entries.add(entry);
		instanceCount += entry.getInstanceCount();
		memorySize += entry.getMemorySize();
	}

	public int getInstanceCount() {
		return instanceCount;
	}

	public long getMemorySize() {
		return memorySize;
	}

	public HeapHistoEntry get(int index) {
		return entries.get(index);
	}

	@Override
	public Iterator<HeapHistoEntry> iterator() {
		return entries.iterator();
	}
	
}
