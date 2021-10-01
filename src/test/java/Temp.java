import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

public class Temp {

	public Temp() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IntSortedSet original = new IntAVLTreeSet(new int[] {1, 2, 3});
		IntSortedSet view = original.tailSet(0);
		IntArraySet newSet = new IntArraySet(original.tailSet(0)); 
		view.remove(1);
		System.out.println(original);
		System.out.println(view);
		System.out.print(newSet);
		original.remove(2);
		System.out.println(original);
		System.out.println(view);
		System.out.print(newSet);
	}

}
