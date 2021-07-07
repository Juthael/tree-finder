package com.tregouet.tree_finder.utils;

import org.api.hyperdrive.NArray;

public class NArrayBool extends NArray<Boolean> {

	private final boolean[] data;
	
	public NArrayBool(int[] dimensions) {
		super(dimensions);
		this.data = new boolean[super.size()];
	}

	@Override
	public final Boolean get(int idx) {
		return data[idx];
	}
	
	public final Boolean getBoolean(int idx) {
		return data[idx];
	}	

	public final Boolean getBoolean(int[] coords) {
		return data[super.indexOf(coords)];
	}
	
	public final boolean getPrimitive(int idx) {
		return data[idx];
	}	
	
	public final void set(int idx, boolean value) {
		data[idx] = value;
	}
	
	@Override
	public final void set(int idx, Boolean value) {
		data[idx] = value;
	}
	
	public final void setBoolean(int idx, boolean value) {
		data[idx] = value;
	}
	
	public final void setBoolean(int[] coords, boolean value) {
		data[super.indexOf(coords)] = value;
	}

}
