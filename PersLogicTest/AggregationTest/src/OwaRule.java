import com.fuzzylite.Engine;
import com.fuzzylite.Op;
import com.fuzzylite.hedge.Hedge;
import com.fuzzylite.norm.SNorm;
import com.fuzzylite.norm.TNorm;
import com.fuzzylite.rule.Antecedent;
import com.fuzzylite.rule.Consequent;
import com.fuzzylite.rule.Rule;

import java.util.HashMap;
import java.util.StringTokenizer;

public class OwaRule extends Rule
{
    protected OwaAntecedent antecedent;

    protected double andness;

    public OwaRule(double andness)
    {
        super();

        this.andness = andness;

        this.antecedent = new OwaAntecedent(this.andness);
    }

    public double getAndness()
    {
        return this.andness;
    }

    public void setAndness(double andness)
    {
        this.andness = andness;

        this.antecedent.setAndness(andness);
    }

    @Override
    public Antecedent getAntecedent() {
        return antecedent;
    }

    @Override
    public void setAntecedent(Antecedent antecedent) {
        this.antecedent = (OwaAntecedent) antecedent;
    }

    @Override
    public double activationDegree(TNorm conjunction, SNorm disjunction) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format("[rule error] the following rule is not loaded: %s", this.getText()));
        }
        return this.getWeight() * this.antecedent.activationDegree(conjunction, disjunction);
    }

    @Override
    public boolean isLoaded() {
        return this.antecedent.isLoaded() && this.getConsequent().isLoaded();
    }

    @Override
    public void unload() {
        this.antecedent.unload();
        this.getAntecedent().unload();
        this.getHedges().clear();
    }

    @Override
    public void load(String rule, Engine engine) {
        this.setText(rule);
        StringTokenizer tokenizer = new StringTokenizer(rule);
        String token;
        String strAntecedent = "";
        String strConsequent = "";
        double ruleWeight = 1.0;

        final byte S_NONE = 0, S_IF = 1, S_THEN = 2, S_WITH = 3, S_END = 4;
        byte state = S_NONE;
        try {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                int commentIndex = token.indexOf("#");
                if (commentIndex >= 0) {
                    token = token.substring(0, commentIndex);
                }
                switch (state) {
                    case S_NONE:
                        if (Rule.FL_IF.equals(token)) {
                            state = S_IF;
                        } else {
                            throw new RuntimeException(String.format(
                                    "[syntax error] expected keyword <%s>, but found <%s> in rule: %s", Rule.FL_IF, token, rule));
                        }
                        break;

                    case S_IF:
                        if (Rule.FL_THEN.equals(token)) {
                            state = S_THEN;
                        } else {
                            strAntecedent += token + " ";
                        }
                        break;
                    case S_THEN:
                        if (Rule.FL_WITH.equals(token)) {
                            state = S_WITH;
                        } else {
                            strConsequent += token + " ";
                        }
                        break;
                    case S_WITH:
                        try {
                            ruleWeight = Op.toDouble(token);
                            state = S_END;
                        } catch (NumberFormatException ex) {
                            throw ex;
                        }
                        break;

                    case S_END:
                        throw new RuntimeException(String.format(
                                "[syntax error] unexpected token <%s> at the end of rule", token));
                }
            }

            if (state == S_NONE) {
                throw new RuntimeException(String.format("[syntax error] " + (rule.isEmpty() ? "empty" : "ignored") + "rule: %s", rule));
            } else if (state == S_IF) {
                throw new RuntimeException(String.format(
                        "[syntax error] keyword <%s> not found in rule: %s",
                        Rule.FL_THEN, rule));
            } else if (state == S_WITH) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected a numeric value as the weight of the rule: %s",
                        rule));
            }
            this.getAntecedent().load(strAntecedent, this, engine);
            this.getConsequent().load(strConsequent, this, engine);
            this.setWeight(ruleWeight);
        } catch (RuntimeException ex) {
            unload();
            throw ex;
        }
    }

    public static OwaRule parse(String rule, Engine engine, double andness)
    {
        OwaRule result = new OwaRule(andness);
        result.load(rule, engine);
        return result;
    }

    @Override
    public OwaRule clone() throws CloneNotSupportedException {
        OwaRule result = (OwaRule) super.clone();
        result.antecedent = new OwaAntecedent(this.andness);
        result.setConsequent(new Consequent());
        result.setHedges(new HashMap<String, Hedge>(this.getHedges()));
        return result;

    }
}
