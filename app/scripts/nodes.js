
function Node() {};

Node.prototype.GlobalNode: function(def){
  this.def = def;
},

Node.prototype.ModelNode: function(def, model){
  this.def = def;
  this.model = model;
},

Node.prototype.DefNode: function(name, pro){
  this.name= name;
  this.process = pro;
},

Node.prototype.ProcessNode: function(child){
  this.child = child;
},

Node.prototype.ChoiceNode: function(pro1, pro2){
  this.process1 = pro1;
  this.process2 = pro2;
},

Node.prototype.ParallelNode: function(pro1, pro2){
  this.process1 = pro1;
  this.process2 = pro2;
},

Node.prototype.SequenceNode: function(lab, pro){
  this.label = lab;
  this.process = pro;
},

Node.prototype.LabelNode: function(lab){
  this.label = lab;
},

Node.prototype.NameNode: function(lit){
  this.literal = lit;
},

Node.prototype.ActionNode: function(lit){
  this.literal = lit;
},

Node.prototype.StopNode: function(){
  this.literal = "Stop";
},
