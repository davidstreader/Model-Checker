(function() {
  'use strict';
  Polymer({
    is: 'process-visualisation',
    properties: {
      /**
       * The name of the automata.
       */
      name: {
        type: String,
        value: ''
      },

      graph: {
        type: Object,
        value: {}
      }
    },
    attached: function(){
      Polymer.dom.flush();
      this.displayedGraphs = [];
      this.graphIds = {};
      const _this = this;
      this.graphMap = {};
      app.$.selector.locked = false;
      this.cy = cytoscape({
        container: document.getElementById('svg-parent'),
        style: getCytoscapeStyle(),
      });
      this.cy.cxtmenu( {
        menuRadius: 100,
        selector: '[isParent]',
        commands: [
          {
            content: 'Explode',
            select: function (ele) {
              _this.cy.fit(ele);
            }
          },
          {
            content: 'Remove',
            select: function (ele) {
              _this.removeGraph(ele);
            }
          }
        ]
      } );
      this.cy.on("position pan zoom",()=>{
        _this.saveChanges();
      });
      this.cy.panzoom();
      this.cy.on("layoutstop",function() {
        _this.layoutStop();
      });
      document.addEventListener('addProcess', function(e){
        app.$.console.log("Starting Render. While rendering, you can not use the editor tab.");
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        _this.addGraph(app.$.selector.getSelectedName(), app.$.selector.hideInterrupts);
      });
      document.addEventListener('clearProcess',function(e) {
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        //Reset the explosion status since the last exploded item is now gone
        app.$.selector._explosionLabel = "Explode to process";
        _this.cy.remove(_this.cy.elements())
        _this.displayedGraphs = [];
        _this.saveChanges();
      });
      //Espand the drawing area to the size of the screen when zooming
      window.addEventListener('resize', function(){
        _this.rescale();
      });
      document.addEventListener('addAll', function(){
        app.$.console.log("Starting Render. While rendering, you can not use the editor tab.");
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        _.each(app.get("automata.values"),graph => _this.addGraph(graph.id,app.$.selector.hideInterrupts));
      });

    },
    layoutStop: function() {
      let y = 20;
      const cur = this.displayedGraphs[this.displayedGraphs.length-1];
      if (cur === undefined) return;
      if (this.displayedGraphs.length > 1) {
        const prev = _.maxBy(this.displayedGraphs,g => g.parent.position("y")+(g.parent.height()/2));
        //Work out the bottom of the last element, and then add a 20px buffer, plus some space
        //For interrupts
        y = prev.parent.position("y")+(prev.parent.height()/2)+20+(10*cur.interrupts);
      }
      //Move all descendants, and also add some padding to the left of interrupts to make them line up correctly.
      cur.parent.descendants().positions((i,node)=>{
        return {y: node.position("y")+y,x: 50+node.position("x")+cur.interrupts*2}
      });
      this.rendering = false;
      if (this.graphsToAdd.length > 0) {
        const graph = this.graphsToAdd.pop();
        this.addGraph(graph.name,graph.hidden);
      }
      this.saveChanges();
    },
    removeGraph: function(cell){
      //If we are currently rendering, ignore any events
      if (this.rendering) return;
      this.cy.remove(cell);
      this.saveChanges();
    },
    graphsToAdd: [],
    addGraph: function(name, hidden) {
      if (this.rendering) {
        this.graphsToAdd.push({name: name, hidden: hidden});
        return;
      }
      this.rendering = true;
      app.$.console.clear();
      let graph = _.find(app.get("automata.values"), {id: name});
      this.convertAndAddGraph(graph,name,hidden);
    },
    /**
     * Redraw the automata.
     */
    redraw: function() {
      $("#process-name-selector")[0].contentElement.selected = null;
      this.automata = this.graphMap;
      if (!this.loaded && app.willSaveCookie && localStorage.getItem("layout") !== null) {
       // this.loadJSON(localStorage.getItem("layout"));
      }
      this.fire('process-visualisation-rendered');
      this.rescale();
    },
    saveChanges: function() {
      if (app.willSaveCookie)
        localStorage.setItem("layout", JSON.stringify(this.cy.json()));
    },
    loadJSON: function(json) {
      app.$.console.clear();
      app.$.console.log("Rendering from "+(this.loaded?"Autosave":"File")+" please wait.");
      this.cy.json(JSON.parse(json));

      const parentNodes = app.$.visualiser.cy.filter(":parent");
      parentNodes.forEach(node => {
        let id = node.id();
        this.displayedGraphs.push({parent: node});
        if (id.indexOf(".") > 0) {
          const num = id.substring(id.lastIndexOf(".")+1);
          id = id.substring(0,id.lastIndexOf("."));
          if (this.graphIds[id]) {
            this.graphIds[id] = Math.max(this.graphIds[id], parseInt(num) + 1);
          } else {
            this.graphIds[id] = parseInt(num)+1
          }
        } else {
          if (!this.graphIds[id]) {
            this.graphIds[id] = 1;
          }
        }
      });
      this.loaded = true;
    },
    convertAndAddGraph: function(graph,id,hidden) {
      const oldId = id;
      if (this.graphIds[id]) {
        id += "."+this.graphIds[id];
      }
      const glGraph = convertGraph(graph,id,hidden);
      const parent = {
        group: "nodes",
        data: { id: id, label:id, isParent: true },
        position: { x: 10, y: 10},
      };
      this.cy.add(parent);
      this.displayedGraphs.push({parent: this.cy.elements('node[id="'+id+'"]')[0], interrupts: (glGraph.interrupts || []).length});
      this.graphIds[oldId] = (this.graphIds[oldId] || 0)+1;
      glGraph.nodes.forEach(node =>{
        node.position = { x: 10+Math.random(), y: 10+Math.random()};
        this.cy.add(node);
      });
      glGraph.edges.forEach(edge =>{
        this.cy.add(edge);
      });
      if (glGraph.nodes.length > 1) {
        //Apply the cose-bilkent algorithm to all the elements inside the parent.
        this.cy.collection('[parent="' + id + '"], [id="' + id + '"]').layout({
          name: 'cose-bilkent',
          fit: false,
          nodeRepulsion: 10000,
        });
      } else {
        this.layoutStop();
      }
    },
    rescale: function() {
      //Get the height of the containing viewport, then set the height of the element to match.
      let screenHeight = $(this.cy.container()).parent().parent().height();
      $(this.cy.container()).height(screenHeight);
    },
  });
})();
