
// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
//\
// Een rule heeft een lefthandside (dat is een non temrinal markup node
// Een right hand side: that is een tree.
public class TransitionRule {
    private Tree lefthandside;
    Tree righthandside;

    public TransitionRule(Tree lefthandside, Tree righthandside) {
        this.lefthandside = lefthandside;
        this.righthandside = righthandside;
    }

    public boolean lefthandsideIsApplicableFor(String tag) {
       // System.out.println("Checking with "+lefthandside.tag+" and "+tag);
        // de lefthandside is een tree node zonder kinderen...
        // We kijken of de tag vergelijkbaar is
        if (lefthandside.tag.equals(tag)) {
            return true;
        }
        return false;
    }
}
