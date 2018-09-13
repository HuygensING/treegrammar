import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
 * This class is the tree and the tree node, depending on how you look at it
 * Since trees are recursive and stuff.
 */
public class Tree {
    final String tag;
    final List<Tree> children;
    // een optional als private field hebben schijnt eigenlijk niet goed te zijn.
    final Optional<Tree> parent;


    // Zoveel constructors joh...
    public Tree(String tag) {
        this.tag = tag;
        this.children = new ArrayList<>();
        this.parent = Optional.empty();
    }

    public Tree(String tag, Tree parent) {
        this.tag = tag;
        this.children = new ArrayList<>();
        this.parent = Optional.of(parent);
    }

    // als ik die twee trees merge, dan moet ik parent natuurlijk wel zetten!
    // ah shit een echte graph class gaat dan wel beter worden!
    // nou ja we zien het zo wel!
    public Tree(String root, List<String> markup) {
        this.tag = root;
        this.parent = Optional.empty();
        // convert T to tree nodes
        List<Tree> tempList = new ArrayList<>();
        for (String m : markup) {
            tempList.add(new Tree(m));
        }
        this.children = tempList;
    }

    @Override
    public String toString() {
        return tag + "{ say something here about the children }";
    }

    // I am doing this all wrong!
    public void replaceChild(Tree orig, Tree replace) {
        // Ik moet de child zien te vinden...
        // dat moet ik nu door een list af te gaan.
        // Een insertation ordered map zou hier beter zijn.
        int i = children.indexOf(orig);
        children.remove(i);
        children.add(i, replace);

    }


    // het zou beter zijn om de container (tree) te onderscheiden van de node (dat zou dan zelfs een string kunnen zijn;
    // hoewel strings herhaald kinnen woden. nou ja een simple bakje met waarden dan.
}
