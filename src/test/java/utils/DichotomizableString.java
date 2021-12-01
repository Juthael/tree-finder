package utils;

import java.util.Set;

import com.tregouet.tree_finder.algo.unidimensional_sorting.IDichotomizable;

public class DichotomizableString implements IDichotomizable<DichotomizableString> {

	private final String name;
	private final boolean rebutter;
	
	public DichotomizableString(String name) {
		this.name = name;
		rebutter = false;
	}
	
	public DichotomizableString(String name, boolean rebutter) {
		this.rebutter = rebutter;
		String prefix = (rebutter ? "¬" : "");
		this.name = prefix + name;
	}
	
	@Override
	public DichotomizableString buildRebutterOfThis(Set<DichotomizableString> rebutterMinimalLowerBounds) {
		return new DichotomizableString(name, true);
	}
	
	@Override
	public DichotomizableString rebutThisWith(DichotomizableString absorbed) {
		return new DichotomizableString(name + " ABSORB " + absorbed.name , true);
	}

	@Override
	public boolean isRebutter() {
		return rebutter;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (rebutter ? 1231 : 1237);
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
		DichotomizableString other = (DichotomizableString) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rebutter != other.rebutter)
			return false;
		return true;
	}

	@Override
	public void setAsRebutterOf(DichotomizableString rebutted) {
		//do nothing
	}

	@Override
	public DichotomizableString getRebutted() {
		return null;
	}

}
