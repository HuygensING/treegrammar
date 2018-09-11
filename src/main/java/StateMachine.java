import java.util.ArrayList;
import java.util.List;

/*
 * author: Ronald Haentjens Dekker
 * date: 11-09-2018
 *
 * in een tree automata willen we een tree bijhouden
 * In een state machine ga je van state naar statae
 * Echter in een tree model zijn er meerdere mogelijke non terminls
 * die je kunt vervangen.
 */
public class StateMachine {
    private Tree completeTree; // tree die we aan het opbouwen zijn
    private Tree pointerToCurrentNode;
    private List<TransitionRule> rules;

    public StateMachine() {
        this.completeTree = new Tree("ROOT");
        this.pointerToCurrentNode = completeTree;
        // nu hebben we nog transitie rules nodig.
        this.rules = new ArrayList<>();
    }

    // bij de state machine komen zaken binnen; input
    // dan moeten we kijek aan den hand van de input of er een transitie rule voor is.
    // zo niet; dan zitten we in een error.
    // input zou eigenlijk tree moeten zijn.
    public void processInput(String tag) {
        // Dan gaan we opzoek naar de transitierule van de huidige state
        // naar een nieuwe op basis van de input.
        // we gaan alle transitierules af.
        // het zou beter zijn om dit te indexeren; maar ok..
        boolean found = false;
        for (TransitionRule rule : rules) {
            if (rule.lefthandsideIsApplicableFor(tag)) {
                found=true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("No transition rule found! Current state: "+this.pointerToCurrentNode.tag+" -> "+tag);
        }
    }

}
