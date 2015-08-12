var ParseTreeNode = new ParseTreeNode();

function ParseTreeNode() {};

ParseTreeNode.prototype.GlobalNode = function(def){
  this.def = def;
};

ParseTreeNode.prototype.ModelNode = function(def, model){
  this.def = def;
  this.model = model;
};

ParseTreeNode.prototype.DefNode = function(name, pro){
  this.name= name;
  this.process = pro;
};

ParseTreeNode.prototype.ProcessNode = function(child){
  this.child = child;
};

ParseTreeNode.prototype.ChoiceNode = function(pro1, pro2){
  this.process1 = pro1;
  this.process2 = pro2;
};

ParseTreeNode.prototype.ParallelNode = function(pro1, pro2){
  this.process1 = pro1;
  this.process2 = pro2;
};

ParseTreeNode.prototype.SequenceNode = function(lab, pro){
  this.label = lab;
  this.process = pro;
};

ParseTreeNode.prototype.LabelNode = function(lab){
  this.label = lab;
};

ParseTreeNode.prototype.NameNode = function(lit){
  this.literal = lit;
};

ParseTreeNode.prototype.ActionNode = function(lit){
  this.literal = lit;
};

ParseTreeNode.prototype.StopNode = function(){
  this.literal = "Stop";
};
