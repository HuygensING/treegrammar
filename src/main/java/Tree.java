import java.util.ArrayList;
import java.util.List;

/*
// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
 * This class is the tree and the tree node, depending on how you look at it
 * Since trees are recursive and stuff.
 */
public class Tree {
    final String tag;
    private final List<Tree> children;

    public Tree(String root) {
        this.tag = root;
        this.children = new ArrayList<>();
    }

    public Tree(String root, List<String> markup) {
        this.tag = root;
        // convert T to tree nodes
        List<Tree> tempList = new ArrayList<>();
        for (String m : markup) {
            tempList.add(new Tree(m));
        }
        this.children = tempList;
    }
}
