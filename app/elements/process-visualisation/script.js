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
                    },
                    {
                        content: 'Redraw',
                        select: function (ele) {
                            //Keep track of the last position as it gets wiped when we rerun the layout
                            const x =  ele.position("x");
                            const y =  ele.position("y");
                            ele.data("last",{x:x,y:y});
                            _this.applyCose(ele.data("id"),ele);
                        }
                    }
                ]
            } );
            this.cy.on("position pan zoom",()=>{
                _this.saveChanges();
            });
            this.cy.panzoom();
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
                _this.saveChanges();
            });
            //Espand the drawing area to the size of the screen when zooming
            window.addEventListener('resize', function(){
                _this.rescale();
            });
            document.addEventListener('addAll', function(){
                app.$.console.log("Starting Render.");
                //If we are currently rendering, ignore any events
                if (_this.rendering) return;
                _.each(app.get("automata.values"),graph => _this.addGraph(graph.id,app.$.selector.hideInterrupts));
            });
        },
        layoutStop: function(cur) {
            let y = 20;
            if (cur === undefined) return;
            //If last is set, we are rerunning the layout, and we do not want to use normal positioning.
            if (cur.data("last")) {
                y = cur.data("last").y;
                const x = cur.data("last").x;
                cur.descendants().positions((i,node)=>{
                    return {y: node.position("y")+y-60,x: node.position("x")+x-60}
                });
                return;
            }
            if (this.cy.filter(":parent").length > 1) {
                const prev = _.maxBy(this.cy.filter(":parent"),g => g.position("y")+(g.height()/2));
                //Work out the bottom of the last element, and then add a 20px buffer, plus some space
                //For interrupts
                y = prev.position("y")+(prev.height()/2)+20+(10*cur.data("interrupts"));
            }
            if (cur.descendants().length > 1) {
                //Move all descendants, and also add some padding to the left of interrupts to make them line up correctly.
                cur.descendants().positions((i, node) => {
                    return {y: node.position("y") + y, x: node.position("x") + cur.data("interrupts") * 2}
                });
            } else {
                //If there is only one node, we can just set its position and ignore what it was last set to.
                cur.descendants().positions(() => {
                    return {y: y, x: cur.data("interrupts") * 2}
                });
            }
            this.rendering = false;
            //If there is another graph waiting, add it now.
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
            if (!this.connected && app.willSaveCookie && localStorage.getItem("layout") !== null) {
                this.loadJSON(localStorage.getItem("layout"));
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
            app.$.console.log("Rendering from "+(this.connected?"Autosave":"File")+" please wait.");
            this.cy.json(JSON.parse(json));

            const parentNodes = app.$.visualiser.cy.filter(":parent");
            parentNodes.forEach(node => {
                let id = node.id();
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
            this.connected = true;
            this.applyTooltips();
        },
        applyTooltips: function() {
            this.cy.elements("[tooltip]").qtip({
                content: function(){ return this.data().tooltip },
                position: {
                    my: 'top center',
                    at: 'bottom center'
                },
                style: {
                    classes: 'qtip-bootstrap',
                    tip: {
                        width: 16,
                        height: 8
                    }
                }
            });
        },
        convertAndAddGraph: function(graph,id,hidden) {
            const oldId = id;
            //If there is already a drawn graph with this id,
            //then graphIds contains the next number to append to the label
            if (this.graphIds[id]) {
                id += "."+this.graphIds[id];
            }
            const glGraph = convertGraph(graph,id,hidden);
            const interruptLength = (glGraph.interrupts || []).length;
            let y = 20;
            if (this.cy.filter(":parent").length > 0) {
                const prev = _.maxBy(this.cy.filter(":parent"),g => g.position("y")+(g.height()/2));
                //Work out the bottom of the last element, and then add a 20px buffer, plus some space
                //For interrupts
                y = prev.position("y")+(prev.height()/2)+20+(10*interruptLength);
            }
            let parent = {
                group: "nodes",
                data: { id: id, label:id, isParent: true },
                position: { x: 10, y: 10},
            };
            parent = this.cy.add(parent);
            parent.data("interrupts",interruptLength);
            this.graphIds[oldId] = (this.graphIds[oldId] || 0)+1;
            glGraph.nodes.forEach(node =>{
                node.position = { x: 60+Math.random()*2, y: y+Math.random()*2};
                this.cy.add(node);
            });
            glGraph.edges.forEach(edge =>{
                this.cy.add(edge);
            });
            this.applyTooltips();
            this.applyCose(id, parent);
        },
        applyCose : function(id, node) {
            const _this = this;
            const nodes = this.cy.collection('[parent="' + id + '"], [id="' + id + '"]');
            if (node.descendants().length > 1) {
                //Apply the cose-bilkent algorithm to all the elements inside the parent.
                nodes.layout({
                    stop: function() {
                        _this.layoutStop(node);
                    },
                    name: 'cose-bilkent',
                    fit: false,
                    nodeRepulsion: app.nodeSep,
                });
            } else {
                this.layoutStop(node);
            }
        },
        rescale: function() {
            //Get the height of the containing viewport, then set the height of the element to match.
            let screenHeight = $(this.cy.container()).parent().parent().height();
            $(this.cy.container()).height(screenHeight);
        },
    });
})();
