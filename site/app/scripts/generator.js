const LexerTokens = require("./lexer-tokens");
module.exports = {
    init:init,
    redraw: redraw,
    added: ()=>added
};
let compiledResult = "",added=[];
const selector = $("#process-modify-selector");
const typeSelector = $("#process-type-selector");
const addButton = $("#add-modify");
const importButton = $("#import-modify");
const nameBox = $("#modify-name");
const generateBt = $("#generate-modify");
const clearBt = $("#clear-modify");
const outputBox = $("#generated-text");
const rendered = $("#generated-processes");
let vars = {};
function init() {
    typeSelector.change(compile);
    nameBox.on("input",compile);
    addButton.click(()=>addProcess(false));
    importButton.click(()=>addProcess(true));
    clearBt.click(clear);
    generateBt.click(addToEditor);
}
function clear() {
    added.splice(0,added.length);
    compile(true);
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
    let rename = [];
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
        if (Object.keys(process.renamed).length > 0) {
            let hidden = [];
            //Loop over the renaed values
            _.each(process.renamed,function(alphabet) {
                let id = alphabet.id;
                let renamedId = alphabet.renamed;
                //If the process is renamed, we need to prepend the process name to the id
                if (process.name) {
                    id = process.name+"."+id;
                    renamedId +="."+id;
                }
                //If hte action is renamed, push it to the rename map
                if (alphabet.renamed)
                    rename.push(alphabet.renamed + "/" + id);
                //If it is hidden, push it to the hidden map
                if (alphabet.hidden)
                    hidden.push(alphabet.renamed?renamedId:id);
            });
            //If we ended up with some hidden values, collect them and add to the current process
            if (hidden.length > 0)
                current += "\\{"+hidden.join()+"}";
        }
        //Push the current process to the process list
        processes.push(current);
    });
    let varStr = "";
    if (Object.keys(vars).length > 0) {
        const varsC = [];
        for (const id in vars) {
            if (vars[id]) varsC.push(id);
        }
        if (varsC.length > 0)
            varStr=`\${${varsC.join()}}`;
    }
    let renameStr = "";
    //If we ended up with some renamed values, collect them and add to the current process
    if (rename.length > 0)
        renameStr = "/{"+rename.join()+"}";
    compiledResult = output+processes.join(" || ")+renameStr+varStr+".";
    //Set compiled results to the process name + all the added processes collected + hidden+.
    outputBox.text(compiledResult);
    if (shouldRender && Object.keys(vars).length > 0) {
        renderVars();
    }
}
function renderVars() {
    const form = $(`<form role="form" class="gen-form"></form>`);
    const gp1 = $(`<div class="form-group"></div>`);
    const pname = $(`<label>Variables to hide</label>`);
    form.append(gp1);
    gp1.append(pname);
    const table = $(`<table border="1"></table>`);
    for (const id in vars) {
        const hidden = vars[id];
        const tr = $("<tr></tr>");
        const nametd = $(`<td style="padding: 0 10px;">${id}</td>`);
        const checkTD = $("<td></td>");
        const check = $(`<input type="checkbox" title="Hide variable symbolically" style="margin: 0 10px;"/>`);
        check[0].checked = hidden;
        check.change(function(){
            vars[id] = this.checked;
            compile(false);
        });
        checkTD.append(check);
        tr.append(nametd,checkTD);
        table.append(tr);
    }
    form.append(table);
    rendered.append(form);
}
function render(process) {
    const form = $(`<form role="form" class="gen-form"></form>`);
    const gp1 = $(`<div class="form-group"></div>`);
    const pname = $(`<label>New Process Name (Original: ${process.id})</label>`);
    const removeBt = $(`<button class="btn btn-primary navbar-btn pull-right">Remove</button>`);
    const nameTb = $(`<input type="text" class="form-control" style="padding-left: 20px; float:left" placeholder="${process.id}" value="${process.name}"/>`);
    nameTb.on("input",()=>{
        process.name = nameTb.val();
        compile(false);
    });
    removeBt.click(()=>{
        added.splice(added.indexOf(process),1);
        compile(true);
    });
    form.append(gp1);
    gp1.append(pname);
    gp1.append(nameTb);
    const table = $(`<table border="1"></table>`);
    for (const a in process.renamed) {
        const alphabet = process.renamed[a];
        const renamed = alphabet.renamed || "";
        const tr = $("<tr></tr>");
        const nametd = $(`<td style="padding: 0 10px;">${alphabet.id}&nbsp;&#8209;></td>`);
        const inputTD = $("<td></td>");
        const input = $(`<input type="text" class="form-control" placeholder="Dont rename" value="${renamed}"/>`);
        input.on("input",()=>{
            alphabet.renamed = input.val();
            compile();
        });
        inputTD.append(input);
        const checkTD = $("<td></td>");
        const check = $(`<input type="checkbox" title="Hide edge" style="margin: 0 10px;"/>`);
        check[0].checked = alphabet.hidden;
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
    if (process !== null) {

        //Replace the old version of the process with the new one
        //Note, we need to get rid of the type as its now set by the original process.
        app.editor.setCode(code.replace(process+".", compiledResult.replace(typeSelector.val().toLowerCase() + " ", "")));
        return;
    }
    //It doesnt, append the new process
    app.editor.setCode(code+"\n"+compiledResult);
}
function getProcessFromCode(id) {
    const process = find(id);
    if (!process) return null;
    const loc = process.metaData.location;
    return app.editor.getCode().substring(loc.startIndex,loc.endIndex);
}
function addProcess(isImport) {
    const id = selector.val();
    const process = find(id);
    if (isImport) {
        //Import found info
        importProcess(process);
    } else {
        //loop over all subkeys from the selected process, then map them to an array with some default states
        added.push({
            id: id,
            name: "",
            renamed: generateRenameMap(process)
        });
    }

    process.nodes.forEach(node=>{
        const variables = node.metaData.variables;
        for (let v in variables) {
            v = v.substring(1);
            vars[v] = false;
        }
    });
    const vs = process.metaData.variables;
    console.log(vs);
    for (const v in vs) {
        const variable = vs[v];
        vars[variable]=true;
    }
    compile(true);
}


function generateRenameMap(process) {
    const map = {};
    const alphaMap = process.metaData.alphabet_before_hiding || process.alphabet;
    alphaMap.forEach(alpha => {
        if (alpha.indexOf(".") !== -1) alpha = alpha.split(".")[1];
        map[alpha] = {id: alpha, renamed: "", hidden: false};
    });
    return map;
}
function importProcess(parse) {
    nameBox.val(parse.id);
    //Loop over processes
    for (let id1 in parse.metaData.processList) {
        const process = parse.metaData.processList[id1];
        //Generate a process formatted for modify
        const orig = {
            id: process.id.indexOf(":")===-1?process.id:process.id.split(":")[1],
            name: process.id.indexOf(":")===-1?"":process.id.split(":")[0],
            renamed: generateRenameMap(process)
        };
        process.nodes.forEach(node=>{
            const variables = node.metaData.variables;
            for (let v in variables) {
                v = v.substring(1);
                if (vars[v] === undefined) {
                    vars[v] = false;
                }
            }
        });
        if (process.metaData.relabels) {
            for (const i in process.metaData.relabels) {
                const relabel = process.metaData.relabels[i];
                let old = relabel.oldLabel;
                if (old.indexOf(".") !== -1) {
                    old = old.split(".")[1];
                }
                orig.renamed[old].renamed = relabel.newLabel;
            }
        }
        //If the process has a hidden section
        if (process.metaData.hiding) {
            const hiddenType = process.metaData.hiding.type;
            const hiddenSet = process.metaData.hiding.set.set;
            //For the exclusive list, we want to start by hiding all
            if (hiddenType === 'excludes') {
                orig.renamed.forEach(toHide => toHide.hidden = true);
            }
            //Loop through all the values in the new process
            for (const id in orig.renamed) {
                //Inclusive(hide all in hidden)
                if (hiddenType === 'includes') {
                    //If the hidden process has an action with name
                    if (hiddenSet.indexOf(orig.renamed[id].id) !== -1) {
                        //Hide the action
                        orig.renamed[id].hidden = true;
                    }
                } else {
                    //Exclusive (hide all not in hidden)
                    //If the hidden process has an action with name
                    if (hiddenSet.indexOf(orig.renamed[id].id) !== -1) {
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
function redraw() {}
function removeProcess(id) {
    this.splice("added",this.added.indexOf(id),1);
}
function find(id) {
    return _.find(app.automata.allValues, {id: id});
}
