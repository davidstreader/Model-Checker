(function() {
  'use strict';
  Polymer({
    is: 'process-modify',
    properties: {
      added: {
        type: Array,
        value: [],
        notify: true
      },
      addLabel: {
        type: String,
        value: "Add Process"
      },
      PROCESS_TYPES: {
        type: Array,
        value: PROCESS_TYPES
      },
      processes: {
        type: Array
      },
      processName: {
        type: String
      },
      _hasSelection: {
        type: Boolean,
        value: false
      },
      _hasProcesses: {
        type: Boolean,
        computed: '_greaterThan(processes.length, 0)'
      },
      _initialSelection: {
        type: String,
        computed: 'initialSelection()'
      },
      compiledResult: {
        type: String
      }
    },
    //Call compile if added or processName are modified
    observers: ['compile(added.*,processName)'],
    compile: function() {
      if (this.added.length === 0) {
        this.set("compiledResult","");
        return;
      }
      //B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in}).
      let output = $("#process-type-selector")[0].selectedItemLabel + " "+(this.processName==""?"OUTPUT":this.processName)+" = ";
      let processes = [];
      let hidden = [];
      _.each(this.added,function(process) {
        let current = "";
        if (process.name) {
          current+=process.name+":";
        }
        current+=process.id;
        if (process.renamed.length > 0) {
          let rename = [];
          _.each(process.renamed,function(alphabet) {
            let id = alphabet.id;
            if (process.name) {
              id = process.name+"."+id;
            }
            if (alphabet.renamed)
              rename.push(alphabet.renamed + "/" + id);
            if (alphabet.hidden)
              hidden.push(alphabet.renamed?alphabet.renamed:alphabet.id);
          });
          if (rename.length > 0)
            current += "/{"+rename.join()+"}";
        }
        processes.push(current);
      });
      if (hidden.length > 0) hidden = " \\{"+_.uniq(hidden).join()+"}";
      this.set("compiledResult",output+processes.join(" || ")+(hidden || "")+".");
    },
    addToEditor: function() {
      //TODO: look if the specified process exists, and replace it if it does.
      //TODO: Figure out the line then replace it.
      //TODO: We do need to take local processes into account though.
      if (!this.compiledResult) return;
      app.$.editor.setCode(app.$.editor.getCode()+"\n"+this.compiledResult);
      app.$.editor.focus();
    },
    clear: function(e) {
      this.set("added",[]);
    },
    removeProcess: function(e) {
      this.splice("added",this.added.indexOf(e.model.process),1);
    },
    _initialSelection: function () {
      return "0";
    },
    /**
     * @returns {!boolean} a > b
     */
    _greaterThan: function (a, b) {
      return a > b;
    },
    ready: function () {
      const _this = this;
      //Expand the drawing area to the size of the screen when zooming
      window.addEventListener('resize', function () {
        _this.rescale();
      });
    },
    addProcess: function() {
      const id = $("#process-modify-selector")[0].selectedItemLabel;
      const parse = this.parse(id);
      const orig = {id:id,name:"",renamed:_.keys(_.findWhere(app.automata.allValues,{id:id}).compiledAlphabet).map(id=>{
        const val = {};
        val.id = id;
        val.renamed = "";
        val.hidden = false;
        return val;
      })};
      if (parse) {
          this.addParsed(parse);
          return;
      }
      this.push("added",orig);
    },
    addParsed: function(parse) {
      for (let id1 in parse.processes) {
        const process = parse.processes[id1];
        const orig = {id:process.id,name:process.name||"",renamed:_.keys(_.findWhere(app.automata.allValues,{id:process.id}).compiledAlphabet).map(id=>{
          const val = {};
          val.id = id;
          val.renamed = "";
          if (process.renamed) {
            val.renamed = process.renamed[id] || "";
          }
          val.hidden = false;
          return val;
        })};
        if (parse.hidden) {
          for (const id in orig.renamed) {
            if (parse.hidden.indexOf(orig.renamed[id].id) !== -1) {
              orig.renamed[id].hidden = true;
            }
          }
        }
        this.push("added",orig);
      }

    },
    _onSelection: function (e, detail) {
      this._hasSelection = true;
      const parse = this.parse($("#process-modify-selector")[0].selectedItemLabel);
      this.set("addLabel",parse?"Load Process":"Add Process");
      const graph = {name: detail.item.dataAutomatonName};
      for (let i in this.display) {
        if (this.display[i].id === graph.name) {
          graph.graph = this.display[i].graph;
          this.fire('change-process', this.display[i]);
          break;
        }
      }

    },
    redraw: function() {
      $("#process-modify-selector")[0].contentElement.selected = null;
      this._hasSelection = false;
      this.set("addLabel","Add Process");
    },
    //A simple parser. While we could just use the normal one, this only matches a subset which is what we want
    //since we only want to convert certain processes
    parse: function(process) {
      const id = process;
      const toMatch="("+Object.keys(Lexer.keywords).concat(Object.keys(Lexer.terminals)).concat(Object.keys(Lexer.functions)).join("|")+"|"+Lexer.operations+")";
      //.replace(/,\r?\n/g,"" -> remove all , followed by a newline. This merges down subprocesses into one line
      process = new RegExp(process+"=.*").exec(app.getCode().replace(/,\r?\n/g,""));
      //if the match doesnt exist then the process named doesnt exist.
      if (!process) return null;
      //get the match
      process = process[0];
      //if process contains any keywords or terminals or functions or operations, it is not a generated process.
      if (!process || process.match(toMatch)) return null;
      process = process.replace(/\s*/g,"").replace("(","").replace(")","");
      //get rid of the . at the end
      process = process.substring(0,process.length-1);
      //It also isnt one if it contains -> or ~>
      if (process.indexOf("->") > -1 || process.indexOf("~>") > -1) return {};
      //If we split out the =, we end up with the process. we want a list of bisimulated processes.
      let processes = process.split("=")[1].split("\\")[0].split("||");
      //Loop through and parse
      for (let i in processes) {
        processes[i] = this.parseProcess(processes[i]);
      }
      //Parse the hidden set on the end
      let hidden = process.split("\\")[1];
      if (hidden) {
        hidden = hidden.replace("{","").replace("}","").split(",")
      }
      return {id:id,processes:processes,hidden:hidden};
    },
    parseProcess: function(process) {
      let proc = {};
      //renamed starts with a /
      let renamed = process.split(/\/(.*)/);
      let id = renamed[0];
      id = id.split(":");
      proc.id = id.length>1?id[1]:id[0];
      if (id.length>1) proc.name = id[0];
      if (renamed.length > 1) {
        proc.renamed = renamed[1].replace("{","").replace("}","").split(",");
        renamed = {};
        for (let i in proc.renamed) {
          const split = proc.renamed[i].split("/");
          renamed[split[1].split(".")[1]] = split[0];
        }
        proc.renamed = renamed;
      }
      return proc;
    }
  });
})();
