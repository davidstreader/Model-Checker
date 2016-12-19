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
      },
      editorLabel: {
        type: String,
        value: "Add to Editor"
      },
      isExisting: {
        type: Boolean,
        value: false
      },
      hasCompiled: {
        type: Boolean,
        value: false
      }
    },
    //Call compile if added or processName are modified
    observers: ['compile(added.*,processName)'],
    compile: function() {
      //Force upper case
      this.processName = this.processName.toUpperCase();
      const processName = (this.processName==""?"OUTPUT":this.processName);
      this.isExisting = this.getProcessFromCode(processName)!==null;
      //If the new name already exists in the editor, notify the user by changing the button label
      this.editorLabel =  this.isExisting?"Update Process":"Add to Editor";
      if (this.isExisting) {
        const type =_.findWhere(app.automata.allValues,{id:processName}).type;
        $("#process-type-selector")[0].contentElement.selected = this.PROCESS_TYPES.indexOf(type);
      }
      this.hasCompiled = this.added.length>0;
      //If we have no processes, empty the buffer and return
      if (!this.hasCompiled) {
        this.set("compiledResult","");
        return;
      }
      //Create a string with the process type and the name
      let output = $("#process-type-selector")[0].selectedItemLabel + " " + processName + " = ";
      let processes = [];
      let hidden = [];
      //Loop over all processes
      _.each(this.added,function(process) {
        //The stringified version of the current process
        let current = "";
        //If the process has a new name
        if (process.name) {
          //Add the new name and a :
          current+=process.name+":";
        }
        //Add the old name
        current+=process.id;
        //If we have some renamed values
        if (process.renamed.length > 0) {
          let rename = [];
          //Loop over the renaed values
          _.each(process.renamed,function(alphabet) {
            let id = alphabet.id;
            //If the process is renamed, we need to prepend the process name to the id
            if (process.name) {
              id = process.name+"."+id;
            }
            //If hte action is renamed, push it to the rename map
            if (alphabet.renamed)
              rename.push(alphabet.renamed + "/" + id);
            //If it is hidden, push it to the hidden map
            if (alphabet.hidden)
              hidden.push(alphabet.renamed?alphabet.renamed:alphabet.id);
          });
          //If we ended up with some renamed values, collect them and add to the current process
          if (rename.length > 0)
            current += "/{"+rename.join()+"}";
        }
        //Push the current process to the process list
        processes.push(current);
      });
      //Collect up all hidden actions, but only unique ones
      if (hidden.length > 0) hidden = " \\{"+_.uniq(hidden).join()+"}";
      //Set compiled results to the process name + all the added processes collected + hidden+.
      this.set("compiledResult",output+processes.join(" || ")+(hidden || "")+".");
    },
    addToEditor: function() {
      const processName = (this.processName==""?"OUTPUT":this.processName);
      //Dont add anything if there is nothing to add
      if (!this.compiledResult) return;
      const code = app.$.editor.getCode();
      //A regex that will match an entire process including sub processes.
      //By adding the process we are loking for before, we can look up entire processes.
      const process = this.getProcessFromCode(processName);
      //If the process already exists
      if (process != null) {
        //Replace the old version of the process with the new one
        //Note, we need to get rid of the type as its now set by the original process.
        app.$.editor.setCode(code.replace(process, " "+this.compiledResult.replace($("#process-type-selector")[0].selectedItemLabel + " ", "") + "\n"));
        app.$.editor.focus();
        return;
      }
      //It doesnt, append the new process
      app.$.editor.setCode(code+"\n"+this.compiledResult);
      app.$.editor.focus();
    },
    getProcessFromCode:function (processName) {
      const code = app.$.editor.getCode();
      //    B2 = (one:Buff || two:Buff).
      //processName+'\\s*= -> B2 =
      //((?:.|,\n|\r\n?)*? -> Non greedy match up to ( (one:Buff || two:Buff))
      //\.(?:\n|\r\n?|$)) -> a dot followed by a newline or end (.)
      const results = new RegExp("(:?.| |^)"+processName+'\\s*=((?:.|,\n|\r\n?)*?\.(?:\s|$))','g').exec(code);
      //If results isnt null, then we have a match
      if (results) return results[0];
      return null;
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
    },
    addProcess: function() {
      const id = $("#process-modify-selector")[0].selectedItemLabel;
      //Try parsing the subprocess to see if we can import info from it
      const parse = this.parse(id);
      if (parse) {
        //IMport found info
        this.addParsed(parse);
        return;
      }
      //loop over all subkeys from the selected process, then map them to an array with some default states
      this.push("added",{id:id,name:"",renamed:_.keys(_.findWhere(app.automata.allValues,{id:id}).compiledAlphabet).map(id=>{return {id:id,renamed:"",hidden:false};})});
    },
    addParsed: function(parse) {
      //Loop over processes
      for (let id1 in parse.processes) {
        const process = parse.processes[id1];
        //Generate a process formatted for modify
        const orig = {id:process.id,name:process.name||"",renamed:_.keys(_.findWhere(app.automata.allValues,{id:process.id}).compiledAlphabet).map(id=>{
          const val = {id:id,renamed:"",hidden:false};
          if (process.renamed) {
            val.renamed = process.renamed[id] || "";
          }
          return val;
        })};
        //If the process has a hidden section
        if (parse.hidden) {
          const hiddenType = parse.hiddenType;
          //For the exclusive list, we want to start by hiding all
          if (hiddenType == 'ex') {
            orig.renamed.forEach(toHide => toHide.hidden = true);
          }
          //Loop through all the values in the new process
          for (const id in orig.renamed) {
            //Inclusive(hide all in hidden)
            if (hiddenType == 'inc') {
              //If the hidden process has an action with this name
              if (parse.hidden.indexOf(orig.renamed[id].id) !== -1) {
                //Hide the action
                orig.renamed[id].hidden = true;
              }
            } else {
              //Exclusive (hide all not in hidden)
              //If the hidden process has an action with this name
              if (parse.hidden.indexOf(orig.renamed[id].id) !== -1) {
                //show the action
                orig.renamed[id].hidden = false;
              }
            }
          }
        }
        //Push the created process
        this.push("added",orig);
      }

    },
    _onSelection: function (e, detail) {
      this._hasSelection = true;
      const parse = this.parse($("#process-modify-selector")[0].selectedItemLabel);
      this.set("addLabel",parse?"Load Process":"Add Process");
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
      if (process.indexOf("->") > -1 || process.indexOf("~>") > -1) return null;
      //If we split out the =, we end up with the process. we want a list of bisimulated processes.
      let processes = process.split("=")[1].split(/\\|@/)[0].split("||");
      //Loop through and parse
      for (let i in processes) {
        processes[i] = this.parseProcess(processes[i]);
      }
      //Parse the hidden set on the end
      let hidden = process.split(/\\|@/)[1];
      if (hidden) {
        hidden = hidden.replace("{","").replace("}","").split(",")
      }
      return {id:id,processes:processes,hidden:hidden,hiddenType:process.indexOf("@")>-1?'ex':'inc'};
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
