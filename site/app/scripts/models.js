const _ = require("lodash");
let cy,graphIds,graphMap,rendering,connected,graphsToAdd = [];
module.exports = {
    redraw: redraw,
    addGraph: addGraph,
    loadJSON: loadJSON,
    getJSON: ()=>cy.json(),
    updateTheme: function() {
        cy.style()
            .selector(':parent')
            .style({
                "color": app.settings.getSettings().darkTheme?"#ffffff":"#000000"
            })
            .update();
    },
    init: function(){
        graphIds = {};
        graphMap = {};
        const cytoscape = require("cytoscape");
        const cxtmenu = require("cytoscape-cxtmenu");
        const panzoom = require("cytoscape-panzoom");
        const cose = require("cytoscape-cose-bilkent");
        cxtmenu(cytoscape);
        panzoom(cytoscape);
        cose(cytoscape);
        cy = cytoscape({
            container: document.getElementById('svg-parent'),
            style: getCytoscapeStyle()
        });
        cy.cxtmenu( {
            menuRadius: 100,
            selector: '[isParent]',
            commands: [
                {
                    content: 'Explode',
                    select: function (ele) {
                        cy.fit(ele);
                    }
                },
                {
                    content: 'Remove',
                    select: function (ele) {
                        removeGraph(ele);
                    }
                },
                {
                    content: 'Redraw',
                    select: function (ele) {
                        //Keep track of the last position as it gets wiped when we rerun the layout
                        const x =  ele.position("x");
                        const y =  ele.position("y");
                        ele.data("last",{x:x,y:y});
                        applyCose(ele.data("id"),ele);
                    }
                }
            ]
        } );
        cy.on("position pan zoom",()=>{
            saveChanges();
        });
        cy.panzoom();
        $("#add-process").click(function(e){
            //If we are currently rendering, ignore any events
            if (rendering) return;
            addGraph($("#model-process").val(),$("#hide-interrupts")[0].checked);
        });
        $("#clear-process").click(function(e) {
            //If we are currently rendering, ignore any events
            if (rendering) return;
            cy.remove(cy.elements());
            graphIds = {};
            saveChanges();
        });
        $("#add-all-process").click(function(){
            app.console.log("Starting Render.");
            //If we are currently rendering, ignore any events
            if (rendering) return;
            _.each(app.automata.values,graph => addGraph(graph.id,$("#hide-interrupts")[0].checked));
        });
    },

};
function convertGraph(graph, id, hidden) {
    let glGraph = {};
    if (graph.type === 'automata') {
        visualizeAutomata(graph,id, hidden, glGraph);
    }
    return glGraph;
}
function visualizeAutomata(process, graphID, hidden, glGraph) {
    glGraph.interrupts = [];
    let lastBox = graphID;
    // add nodes in automaton to the graph
    const nodes = process.nodes;
    glGraph.nodes = [];
    glGraph.edges = [];
    let interruptId = 1;
    for(let i = 0; i < nodes.length; i++){
        const nid = 'n' + nodes[i].id;
        let type = "fsaState";
        // check if current node is the root node
        if(nodes[i].metaData.startNode){
            type = "fsaStartState";
        }
        if(nodes[i].metaData.isTerminal !== undefined) {
            type = "fsaEndState";
            if (nodes[i].metaData.isTerminal === 'ERROR') {
                type = "fsaErrorState";
            }
        }
        let tooltip;
        const vars = nodes[i].metaData.variables;
        if (vars && Object.keys(vars).length > 0) {
            tooltip = "";
            for (let i in vars) {
                tooltip+=i+"="+vars[i]+", ";
            }
            tooltip = "Variables: <span style='color:blue'>"+tooltip.substr(0,tooltip.length-2).replace(/\$/g,"")+"</span>";
        }
        glGraph.nodes.push({
            group:"nodes",
            data: {id: graphID+nid, label: nodes[i].metaData.label, type: type, tooltip: tooltip, parent: graphID},
        });
    }
    let toEmbed = [];
    // add the edges between the nodes in the automaton to the graph
    const edges = process.edges;
    for(let i = 0; i < edges.length; i++){
        let label = edges[i].label;
        const from = graphID+'n' + edges[i].from;
        const to = graphID+'n' + edges[i].to;
        let tooltip;
        let guard = edges[i].metaData.guard;
        if(guard && guard.hiddenGuardStr && guard.hiddenGuardStr.length > 0){
            label += " "+guard.hiddenGuardStr;
        }

        if (guard) {
            tooltip = "";
            if (guard.varStr.length > 0)
                tooltip += "Variables:<span style='color:blue'>" +guard.varStr+"</span><br/>";
            if (guard.guardStr.length > 0)
                tooltip += "Guard:<span style='color:blue'>" +guard.guardStr+"</span><br/>";
            if (guard.nextStr.length > 0)
                tooltip += "Assigned variables:<span style='color:blue'>" +guard.nextStr+"</span>";
        }
        if (tooltip == "") tooltip = undefined;
        if (edges[i].metaData.interrupt && hidden) {
            const toNode = process.nodeMap[edges[i].to];
            //Destroy all interrupt edges besides the last one.
            if (toNode.incomingEdges.indexOf(edges[i].id) != toNode.incomingEdges.length-1) {
                toEmbed.push(from);
                continue;
            }
            lastBox = _box(glGraph, toEmbed, graphID+"."+(interruptId++),graphID);
            //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
            toEmbed = [ lastBox, _link(lastBox,to, label,tooltip, glGraph, lastBox), to];
            continue;
        }
        toEmbed.push(_link(from,to, label,tooltip,glGraph, lastBox));
        toEmbed.push(from);
        toEmbed.push(to);
    }
}

function _link(source, target, label, tooltip, glGraph, lastBox) {
    glGraph.edges.push({
        group: "edges",
        data: {id:source+"-"+label+"->"+target,label: label, tooltip: tooltip,source: source,target: target, parent: lastBox},
    });
    return source+"->"+target;
}
function _box(glGraph, toEmbed, name, graphID) {
    glGraph.interrupts.push("boxNode"+name);
    //we need to use unshift here, as the parents need to load before the children.
    glGraph.nodes.unshift({
        group:"nodes",
        data: {id: "boxNode"+name, type: 'interrupt', parent: graphID, label: name},
    });
    //Remove embedded cells from the parent and add them to the box
    toEmbed.forEach(embed => {
        let el = _.findWhere(glGraph.nodes,{data:{id: embed}});
        if (!el) {
            el = _.findWhere(glGraph.edges,{data:{id: embed}});
        }
        el.data.parent = "boxNode"+name;
    });
    return "boxNode"+name;
}
function getCytoscapeStyle() {
    return [
        {
            selector: 'node',
            style: {
                'background-color': Colours.grey,
                'label': 'data(label)',
                "text-valign" : "center",
                "text-halign" : "center",
                'font-size': '15',
                'font-weight': 'bold',
                'border-width': '3px',
                'width':"label",
                'height':"label",
                'padding': '10px'
            }
        },
        {
            selector: 'node[type=\'fsaStartState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.blue,
                'border-width': '10px',
            }
        },
        {
            selector: 'node[type=\'fsaEndState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.green,
                'border-width': '10px',
            }
        },
        {
            selector: 'node[type=\'fsaErrorState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.red,
                'border-width': '10px',
            }
        },
        {
            selector: ':parent',
            style: {
                'background-opacity': 0.333,
                "text-valign" : "top",
                "color": app.settings.getSettings().darkTheme?"#ffffff":"#000000"
            }
        },
        {
            selector: 'edge',
            style: {
                'width': 3,
                'line-color': 'black',
                'label': 'data(label)',
                'curve-style': 'bezier',
                'font-size': '15',
                'font-weight': 'bold',
                'target-arrow-color': 'black',
                'target-arrow-shape': 'triangle',
                'text-background-opacity': 1,
                'text-background-color': '#ffffff',
                'text-background-shape': 'rectangle',
                'text-rotation': 'autorotate'
            }
        }
    ]
}
const Colours = {
    red: '#C0392b',
    blue: '#3498DB',
    green: '#2ECC71',
    grey: '#BDC3C7',
    textBackground: 'rgba(255, 255, 255, 0.5)'
};
function saveChanges() {
    if (app.settings.getSettings().autoSave)
        localStorage.setItem("layout", JSON.stringify(cy.json()));
}
function loadJSON(json) {
    app.console.clear();
    app.console.log("Rendering from "+(loading?"Autosave":"File")+" please wait.");
    cy.json(JSON.parse(json));

    const parentNodes = cy.filter(":parent");
    parentNodes.forEach(node => {
        let id = node.id();
        if (id.indexOf(".") > 0) {
            const num = id.substring(id.lastIndexOf(".")+1);
            id = id.substring(0,id.lastIndexOf("."));
            if (graphIds[id]) {
                graphIds[id] = Math.max(graphIds[id], parseInt(num) + 1);
            } else {
                graphIds[id] = parseInt(num)+1
            }
        } else {
            if (!graphIds[id]) {
                graphIds[id] = 1;
            }
        }
    });
    connected = true;
    applyTooltips();
}
function applyTooltips() {
    //TODO: Qtip sucks. We should look for something to replace it.
    // cy.elements("[tooltip]").qtip({
    //     content: function(){ return data().tooltip },
    //     position: {
    //         my: 'top center',
    //         at: 'bottom center'
    //     },
    //     style: {
    //         classes: 'qtip-bootstrap',
    //         tip: {
    //             width: 16,
    //             height: 8
    //         }
    //     }
    // });
}
function convertAndAddGraph(graph,id,hidden) {
    const oldId = id;
    //If there is already a drawn graph with this id,
    //then graphIds contains the next number to append to the label
    if (graphIds[id]) {
        id += "."+graphIds[id];
    }
    const glGraph = convertGraph(graph,id,hidden);
    const interruptLength = (glGraph.interrupts || []).length;
    let x = 20;
    //If there are any parent elements
    if (cy.filter(":parent").length > 1) {
        const prev = _.maxBy(cy.filter(":parent"),g => g.position("x")+(g.width()/2));
        //Work out the bottom of the last element, and then add a 20px buffer, plus some space
        //For interrupts
        x = prev.position("x")+(prev.width()/2)+20;
    }
    //create a new parent
    let parent = {
        group: "nodes",
        data: { id: id, label:id, isParent: true },
        position: { x: 10, y: 10},
    };
    parent = cy.add(parent);
    parent.data("interrupts",interruptLength);
    graphIds[oldId] = (graphIds[oldId] || 0)+1;
    glGraph.nodes.forEach(node =>{
        node.position = { x: x+Math.random()*2, y: Math.random()*2};
        cy.add(node);
    });
    glGraph.edges.forEach(edge =>{
        cy.add(edge);
    });
    applyTooltips();
    applyCose(id, parent);
}
function applyCose(id, node) {
    const nodes = cy.collection('[parent="' + id + '"], [id="' + id + '"]');
    if (node.descendants().length > 1) {
        //Apply the cose-bilkent algorithm to all the elements inside the parent.
        nodes.layout({
            stop: function() {
                layoutStop(node);
            },
            name: 'cose-bilkent',
            fit: false,
            nodeRepulsion: app.nodeSep,
        }).run();
    } else {
        layoutStop(node);
    }
}
function layoutStop(cur) {
    let x = 20;
    if (cur === undefined) return;
    //If last is set, we are rerunning the layout, and we do not want to use normal positioning.
    if (cur.data("last")) {
        const y = cur.data("last").y;
        x = cur.data("last").x;
        cur.descendants().positions((node,i)=>{
            return {y: node.position("y")+y-60,x: node.position("x")+x-60}
        });
        return;
    }
    if (cy.filter(":parent").length > 1) {
        const prev = _.maxBy(cy.filter(":parent"),g => g.position("x")+(g.width()/2));
        //Work out the bottom of the last element, and then add a 20px buffer, plus some space
        //For interrupts
        x = prev.position("x")+(prev.width()/2)+20;
    }
    if (cur.descendants().length > 1) {
        //Move all descendants, and also add some padding to the left of interrupts to make them line up correctly.
        cur.descendants().positions((node, i) => {
            return {y: node.position("y") + 10*cur.data("interrupts"), x: node.position("x") + x + cur.data("interrupts") * 2}
        });
    } else {
        //If there is only one node, we can just set its position and ignore what it was last set to.
        cur.descendants().positions(() => {
            return {y: 0, x: x}
        });
    }
    rendering = false;
    //If there is another graph waiting, add it now.
    if (graphsToAdd.length > 0) {
        const graph = graphsToAdd.pop();
        addGraph(graph.name,graph.hidden);
    }
    saveChanges();
}
function removeGraph(cell){
    //If we are currently rendering, ignore any events
    if (rendering) return;
    cy.remove(cell);
    saveChanges();
}
function addGraph(name, hidden) {
    if (rendering) {
        graphsToAdd.push({name: name, hidden: hidden});
        return;
    }
    rendering = true;
    app.console.clear();
    let graph = _.find(app.automata.values, {id: name});
    convertAndAddGraph(graph,name,hidden);
}
let loading = false;
/**
 * Redraw the automata.
 */
function redraw() {
    if (!loading && app.settings.getSettings().autoSave && localStorage.getItem("layout") !== null) {
        loadJSON(localStorage.getItem("layout"));
    }
    $("#svg-parent").hide().show();
    app.models.updateTheme();
}
