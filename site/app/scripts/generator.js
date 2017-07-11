const LexerTokens = require("./lexer-tokens");
module.exports = {
    init:init,
    parse:parse,
    redraw: redraw,
    added: ()=>added
};
let compiledResult = "",added=[];
const selector = $("#process-modify-selector");
const typeSelector = $("#process-type-selector");
const addButton = $("#add-modify");
const nameBox = $("#modify-name");
const generateBt = $("#generate-modify");
const clearBt = $("#clear-modify");
const outputBox = $("#generated-text");
const rendered = $("#generated-processes");
function init() {
    selector.change(updateButton);
    typeSelector.change(compile);
    nameBox.on("input",compile);
    addButton.click(addProcess);
    clearBt.click(clear);
    generateBt.click(addToEditor);
}
function clear() {
    added.splice(0,added.length);
    compile(true);
}
function updateButton() {
    const parsed = parse(selector.val());
    addButton.html(`<span class="glyphicon glyphicon-plus"></span> `+(parsed?"Import Process":"Add Process"));
}
function compile(shouldRender) {
    //Force upper case
    let processName = nameBox.val() || "OUTPUT";
    processName = processName.substring(0,1).toUpperCase()+processName.substring(1);
    let isExisting = getProcessFromCode(processName)!==null;
    //If the new name already exists in the editor, notify the user by changing the button label
    let editorLabel =  isExisting?`<span class="glyphicon glyphicon-upload"></span> Update Process`:`<span class="glyphicon glyphicon-play"></span> Add to Editor`;
    generateBt.val(editorLabel);
    if (isExisting) {
        const type =_.find(app.automata.allValues,{id:processName}.type);
        typeSelector.val(type.type.substring(0,1).toUpperCase()+type.type.substring(1));
    }
    let hasCompiled = added.length>0;
    generateBt.prop("disabled",!hasCompiled);
    //If we have no processes, empty the buffer and return
    if (!hasCompiled) {
        rendered.html("");
        outputBox.text(" ");
        return;
    }
    //Create a string with the process type and the name
    let output = typeSelector.val().toLowerCase() + " " + processName + " = ";
    let processes = [];
    let hidden = [];
    if (shouldRender)
        rendered.html("");
    //Loop over all processes
    _.each(added,function(process) {
        if (shouldRender)
            render(process);
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
    compiledResult = output+processes.join(" || ")+(hidden || "")+".";
    //Set compiled results to the process name + all the added processes collected + hidden+.
    outputBox.text(compiledResult);
}
function render(process) {
    const form = $(`<form role="form" class="gen-form"></form>`);
    const gp1 = $(`<div class="form-group"></div>`);
    const pname = $(`<label>New Process Name (Original: ${process.id})</label>`);
    const removeBt = $(`<button class="btn btn-primary navbar-btn pull-right">Remove</button>`);
    const nameTb = $(`<input type="text" class="form-control" style="padding-left: 20px; float:left" placeholder="${process.id}" value="${process.name}"/>`);
    nameTb.on("input",()=>{
        process.name = nameTb.val();
        compile();
    });
    removeBt.click(()=>{
      added.splice(added.indexOf(process),1);
      compile(true);
    });
    form.append(gp1);
    pname.text(`New Process Name (${process.id})`);
    gp1.append(pname);
    gp1.append(nameTb);
    const table = $(`<table border="1"></table>`);
    for (const a in process.renamed) {
        const alphabet = process.renamed[a];
        const renamed = alphabet.renamed || "";
        const tr = $("<tr></tr>");
        const nametd = $(`<td>${alphabet.id}&nbsp;&#8209;>&nbsp;</td>`);
        const inputTD = $("<td></td>");
        const input = $(`<input type="text" class="form-control" placeholder="Dont rename" value="${renamed}"/>`);
        input.on("input",()=>{
            alphabet.renamed = input.val();
            compile();
        });
        inputTD.append(input);
        const checkTD = $("<td></td>");
        const check = $(`<input type="checkbox" value="${alphabet.hidden}" style="margin-left: 10px;"/>`);
        check.change(function(){
            alphabet.hidden = this.checked;
            compile();
        });
        checkTD.append(check);
        tr.append(nametd,inputTD,checkTD);
        table.append(tr);
    }
    form.append(table);
    form.append(removeBt);
    rendered.append(form);
}
function addToEditor() {
    //Dont add anything if there is nothing to add
    if (!compiledResult) return;
    const code = app.editor.getCode();
    let processName = nameBox.val() || "OUTPUT";
    processName = processName.substring(0,1).toUpperCase()+processName.substring(1);
    //A regex that will match an entire process including sub processes.
    //By adding the process we are loking for before, we can look up entire processes.
    const process = getProcessFromCode(processName);
    //If the process already exists
    if (process != null) {
        //Replace the old version of the process with the new one
        //Note, we need to get rid of the type as its now set by the original process.
        app.editor.setCode(code.replace(process+".", compiledResult.replace(typeSelector.val().toLowerCase() + " ", "")));
        return;
    }
    //It doesnt, append the new process
    app.editor.setCode(code+"\n"+compiledResult);
}
function getProcessFromCode(id) {
    if (!app.automata) return null;
    const process = _.find(app.automata.allValues,{id:id});
    if (!process) return null;
    const loc = process.metaData.location;
    //Split into lines
    const code = app.editor.getCode().split(/\n/);
    let endCol = loc.colEnd;
    let procCode = _.drop(code,loc.lineStart-1);
    procCode = _.dropRight(procCode,procCode.length-(loc.lineEnd-loc.lineStart)-1);
    procCode[0] = procCode[0].substring(loc.colStart);
    //If we are dealing with the same line twice, we need to offset the end col by the
    //start col as we just removed it.
    if (loc.lineStart == loc.lineEnd) endCol-=loc.colStart;
    procCode[procCode.length-1] = procCode[procCode.length-1].substring(0,endCol);
    procCode = procCode.join("\n");
    return procCode;
}
function addProcess() {
    const id = selector.val();
    //Try parsing the subprocess to see if we can import info from it
    const parsed = parse(id);
    if (parsed) {
        //IMport found info
        addParsed(parsed);
        compile(true);
        return;
    }
    //loop over all subkeys from the selected process, then map them to an array with some default states
    added.push({id:id,name:"",renamed:Object.values(_.find(app.automata.allValues,{id:id}).alphabet).map(id=>{return {id:id,renamed:"",hidden:false};})});
    compile(true);
}
function addParsed(parse) {
    //Loop over processes
    for (let id1 in parse.processes) {
        const process = parse.processes[id1];

        //Generate a process formatted for modify
        const orig = {
            id: process.id,
            name: process.name || "",
            renamed: Object.values(_.find(app.automata.allValues, {id: process.id}).alphabet).map(id => {
                const val = {id: id, renamed: "", hidden: false};
                if (process.renamed) {
                    val.renamed = process.renamed[id] || "";
                }
                return val;
            })
        };
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
                    //If the hidden process has an action with name
                    if (parse.hidden.indexOf(orig.renamed[id].id) !== -1) {
                        //Hide the action
                        orig.renamed[id].hidden = true;
                    }
                } else {
                    //Exclusive (hide all not in hidden)
                    //If the hidden process has an action with name
                    if (parse.hidden.indexOf(orig.renamed[id].id) !== -1) {
                        //show the action
                        orig.renamed[id].hidden = false;
                    }
                }
            }
        }
        //Push the created process
        added.push(orig);
    }
}
function parseProcess(process) {
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
            const id = split[1].split(".")[1] || split[1];
            renamed[id] = split[0];
        }
        proc.renamed = renamed;
    }
    return proc;
}
function parse(process) {
    const id = process;
    const code = getProcessFromCode(id);
    if (!code) return null;
    const toMatch="("+Object.keys(LexerTokens.keywords).concat(Object.keys(LexerTokens.terminals)).concat(Object.keys(LexerTokens.functions)).join("|")+"|"+LexerTokens.operations+")";
    //Remove newlines and whitespace
    process = code.replace(/[\r\n]/,"");
    //if process contains any keywords or terminals or functions or operations, it is not a generated process.
    if (!process || process.match(toMatch)) return null;
    process = process.split("=")[1];
    //It also isnt one if it contains -> or ~>
    if (process.indexOf("->") > -1 || process.indexOf("~>") > -1) return null;
    //Strip whitespace and brackets
    process = process.replace(/\s*/g,"").replace("(","").replace(")","");
    //We want a list of bisimulated processes.
    let processes = process.split(/\\|@/)[0].split("||");
    //Loop through and parse
    for (let i in processes) {
        processes[i] = parseProcess(processes[i]);
    }
    //Parse the hidden set on the end
    let hidden = process.split(/\\|@/)[1];
    if (hidden) {
        if (hidden.indexOf("{") === -1) {
            const resolvedHidden = new RegExp("set\\s*"+hidden+"\\s*=\\s*{(.*?)}").exec(app.editor.getCode());
            if (resolvedHidden) hidden = resolvedHidden[1];
            else hidden = "";
        }
        hidden = hidden.replace("{","").replace("}","").split(",")
    }
    return {id:id,processes:processes,hidden:hidden,hiddenType:process.indexOf("@")>-1?'ex':'inc'};
}
function redraw() {
    updateButton();
}
function removeProcess(id) {
    this.splice("added",this.added.indexOf(id),1);
}
