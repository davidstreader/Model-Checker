package mc.client.graph;

import mc.processmodels.ProcessModelObject;

public class GraphViewEdge {
    private GraphViewNode from;
    private GraphViewNode to;


    public GraphViewEdge(GraphViewNode from_,GraphViewNode to_){
        to = to_;
        from = from_;

    }
}
