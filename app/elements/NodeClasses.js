      /**
       * The Edge class
       */
_GlobalNode: function(def){
	this.def = def;
},

_ModelNode: function(def, model){
	this.def = def;	
	this.model = model;
},

_DefNode: function(name, pro){
	this.name= name;
	this.process = pro;
},

_ProcessNode: function(child){
	this.child = child;
},

_ChoiceNode: function(pro1, pro2){
	this.process1 = pro1;,
	this.process2 = pro2;
},

_ParallelNode: function(pro1, pro2){
	this.process1 = pro1;
	this.process2 = pro2;
},

_SequenceNode: function(lab, pro){
	this.label = lab;
	this.process = pro;
},

_LabelNode: function(lab){
	this.label = lab;
},

_NameNode: function(lit){
	this.literal = lit;
},

_ActionNode: function(lit){
	this.literal = lit;
},

_StopNode: function(){
	this.literal = "Stop";
}

