package mc.client.graph;

import mc.processmodels.ProcessModelObject;
import lombok.Getter;
import lombok.Setter;

public class GraphViewNode {
    @Setter
    @Getter
    double x = 0;
    @Setter
    @Getter
    double y = 0;

    public GraphViewNode(double x_, double y_){
        x=x_;
        y=y_;
    }
}
