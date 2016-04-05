import com.fuzzylite.hedge.Any;
import com.fuzzylite.hedge.Hedge;
import com.fuzzylite.norm.SNorm;
import com.fuzzylite.norm.TNorm;
import com.fuzzylite.rule.*;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

import java.util.ArrayList;
import java.util.ListIterator;

public class OwaAntecedent extends Antecedent
{
    protected double andness;

    public OwaAntecedent(double andness)
    {
        this.andness = andness;
    }

    public double getAndness()
    {
        return this.andness;
    }

    public void setAndness(double andness)
    {
        this.andness = andness;
    }

    protected void toValueTree(Expression node, Node<String, ArrayList<Double>> values)
    {
        if (node instanceof Proposition) {
            Proposition proposition = (Proposition) node;
            if (!proposition.getVariable().isEnabled()) {
                values.getData().add(0.0);
            }
            if (!proposition.getHedges().isEmpty()) {
                int lastIndex = proposition.getHedges().size();
                ListIterator<Hedge> rit = proposition.getHedges().listIterator(lastIndex);
                Hedge any = rit.previous();
                //if last hedge is "Any", apply hedges in reverse order and return degree
                if (any instanceof Any) {
                    double result = any.hedge(Double.NaN);
                    while (rit.hasPrevious()) {
                        result = rit.previous().hedge(result);
                    }
                    values.getData().add(result);
                }
            }

            double result = Double.NaN;
            if (proposition.getVariable() instanceof InputVariable) {
                InputVariable inputVariable = (InputVariable) proposition.getVariable();
                result = proposition.getTerm().membership(inputVariable.getInputValue());
            } else if (proposition.getVariable() instanceof OutputVariable) {
                OutputVariable outputVariable = (OutputVariable) proposition.getVariable();
                result = outputVariable.fuzzyOutput().activationDegree(proposition.getTerm());
            }
            int lastIndex = proposition.getHedges().size();
            ListIterator<Hedge> reverseIterator = proposition.getHedges().listIterator(lastIndex);
            while (reverseIterator.hasPrevious()) {
                result = reverseIterator.previous().hedge(result);
            }
            values.getData().add(result);
        } else if (node instanceof Operator) {
            Operator operator = (Operator) node;

            if (Rule.FL_AND.equals(operator.getName())) {
                values.setMeta(Rule.FL_AND);

                this.toValueTree(operator.getLeft(), values);
                this.toValueTree(operator.getRight(), values);
            } else if (Rule.FL_OR.equals(operator.getName())) {
                values.setMeta(Rule.FL_OR);

                Node<String, ArrayList<Double>> l = new Node<String, ArrayList<Double>>(values, new ArrayList<Double>());
                Node<String, ArrayList<Double>> r = new Node<String, ArrayList<Double>>(values, new ArrayList<Double>());

                values.setLeft(l);
                values.setRight(r);

                this.toValueTree(operator.getLeft(), l);
                this.toValueTree(operator.getRight(), r);
            } else {
                throw new RuntimeException(String.format(
                        "[syntax error] operator <%s> not recognized",
                        operator.getName()));
            }
        } else {
            throw new RuntimeException("[expression error] unknown instance of Expression");
        }
    }

    protected double process(Node<String, ArrayList<Double>> node)
    {
        if (node.getMeta() == Rule.FL_OR) {
            assert false;

            OWA owa = OWA.MEOWA_FACTORY(2, 1.0 - this.andness);

            double l = this.process(node.getLeft());
            double r = this.process(node.getRight());

            return owa.apply(l, r);
        } else {
            ArrayList<Double> vals = node.getData();
            OWA owa = OWA.MEOWA_FACTORY(vals.size(), this.andness);

            double dvals[] = new double[vals.size()];
            int i = 0;
            for (Double d : vals) {
                dvals[i++] = d;
            }

            return owa.apply(dvals);
        }
    }

    @Override
    public double activationDegree(TNorm conjunction, SNorm disjunction, Expression node) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[antecedent error] antecedent <%s> is not loaded", this.getText()));
        }

        Tree<String, ArrayList<Double>> tree = new Tree<String, ArrayList<Double>>(new ArrayList<Double>());
        this.toValueTree(node, tree.getRoot());
        double r = this.process(tree.getRoot());

        return r;
    }
}
