package mc.processmodels.petrinet;

import mc.util.expr.Expression;

import java.util.*;

public class ProbabilityDistribution {
    private String id = "";
    public void setId(String s){id=s;}
    public String getId(){return id;}
    private Map<String,Expression> distribution = new TreeMap<>();
    public void addToDistribution(String id, Expression e) {
        distribution.put(id,e);
    }
    public Expression getProbability(String id) { return distribution.get(id);}
    public void clearDistribution() {distribution = new TreeMap<>();}
    public Map<String,Expression> getDistribution(){return distribution;}
    public void setDistribution(Map<String,Expression> dist) {distribution = dist;}
}
