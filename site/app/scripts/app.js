window.jQuery = require("jquery");
window.$ = window.jQuery;
require("jquery-resizable-dom/src/jquery-resizable");
require("ace-builds/src-min-noconflict/ace.js");
require("ace-builds/src-min-noconflict/theme-vibrant_ink.js");
require("ace-builds/src-min-noconflict/theme-crimson_editor.js")

$(function() {
    'use strict';

    const app = document.querySelector('#app');
    $(".left-panel").resizable({
        handleSelector: ".splitter",
        resizeHeight: false
    });
    const editor = ace.edit("ace-editor");
    function invert(inverted) {
        const body = $("body");
        const nav = $(".navbar");
        inverted?nav.addClass("navbar-inverse"):nav.removeClass("navbar-inverse");
        inverted?body.addClass("invert"):body.removeClass("invert");
        const theme = "ace/theme/"+(inverted?"vibrant_ink":"crimson_editor");
        editor.setTheme(theme);
        localStorage.setItem("config_invert",inverted);
    }
    $("#darkMode").change(function(){
        invert(this.checked);
    });
    $("#darkMode")[0].checked = localStorage.getItem(("config_invert"));
    invert(localStorage.getItem(("config_invert")));
    window.addEventListener('WebComponentsReady', function() {
        /**
         * The data to use.
         */
        app.automata = {values: [],display:[]};
        app.liveCompiling = true;
        app.fairAbstraction = true;
        app.pruning = false;
        app.nodeSep = 10000;
        app.helpDialogSelectedTab = 0;
        app.currentBuild = {};
        app.previousBuild = {};
        app.previousCode = '';
        app.selectedCtx = 0;
        app.willSaveCookie = true;
        app.graphDefaults = {autoMaxNode: 40, failCount: 10, passCount: 10}
        app.graphSettings = JSON.parse(JSON.stringify(app.graphDefaults));
        app.connected = false;
        app.saveSettings = {currentFile: '', saveCode: true, saveLayout: true};
        app.decoder = new TextDecoder("UTF-8");
        const proto = window.location.protocol.replace("http","").replace(":","");
        app.socket = new ReconnectingWebSocket("ws"+proto+"://" + location.hostname + ":" + location.port + "/socket/");
        app.socket.onmessage = function (msg) {
            let results = JSON.parse(msg.data);
            const data = results.data;
            if (results.event == "compileReturn") {
                if (data.type == "error") {
                    app.showError(data);
                    return;
                }
                app.finalizeBuild(data);
            } else if (results.event = "log") {
                if (data.clear) {
                    if (data.clearAmt != -1) {
                        app.$.console.clear(data.clearAmt);
                    } else {
                        app.$.console.clear();
                    }
                }
                if (data.error) {
                    app.$.console.error(data.message);
                    if (data.location) {
                        const Range = ace.require("ace/range").Range;
                        const editor = app.$.editor._editor.getSession();
                        editor.clearAnnotations();
                        _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
                        const l = data.location;
                        editor.addMarker(new Range(l.lineStart-1, l.colStart, l.lineEnd-1, l.colEnd), "ace_underline");
                        for (let i = l.lineStart; i <= l.lineEnd; i++) {
                            editor.setAnnotations([{row:i-1 ,column: 0, text:data.message,type:"error"}]);
                        }
                    }
                }
                else {
                    app.$.console.log(data.message);
                }
            }
        };
        app.socket.onopen = function () {
            app.connected = true;
            if (app.liveCompiling)
                app.compile();
            $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Online-brightgreen.svg'/>")
        };
        app.socket.onclose = function () {
            app.connected = false;
            app.$.console.log("You have been disconnected from the server.");
            app.$.console.log("As a result, your last compilation may not have completed successfully.");
            $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Offline-red.svg'/>")
        };

        app.compile = function(overrideBuild) {
            const code = app.getCode();
            if(!overrideBuild){
                // if there is nothing to parse then do not continue
                if(code.length === 0){
                    app.$.console.clear();
                    app.automata = {};
                    app.previousCode = '';
                    return;
                }

                // if the code has not changed then do not continue
                if(code === app.previousCode){
                    return;
                }
            }
            app.build(overrideBuild);
        };


        /**
         * Runs the compiler converting the code in the editor into visualisable
         * graphs and calls the renderer.
         */
        app.build = function(override) {
            app.$.console.clear();
            const editor = app.$.editor._editor.getSession();
            editor.clearAnnotations();
            _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
            if (!app.connected) {
                app.$.console.error("Unable to compile: No connection to server.")
            }
            setTimeout(function() {
                if(app.liveCompiling === true || override){
                    app.lastCompileStartTime = (new Date()).getTime();
                    const code = app.$.editor.getCode();
                    const settings = app.getSettings();
                    app.socket.send(JSON.stringify({code:code,context:settings}));
                }
            }.bind(this), 0);
        }
        app.showError = function(error) {
            const Range = ace.require("ace/range").Range;
            const editor = app.$.editor._editor.getSession();
            if (error.stack) {
                app.$.console.error("An exception was thrown that was not related to your script.");
                app.$.console.error(error.message+"\n"+error.stack);
                console.log(error.message+"\n"+error.stack);
            } else {
                app.$.console.error(error.message);
                if (error.location) {
                    const l = error.location;
                    //If colEnd==colStart, add one to colEnd so that a character is highlighted.
                    if (l.colEnd == l.colStart) l.colEnd++;
                    editor.addMarker(new Range(l.lineStart-1, l.colStart, l.lineEnd-1, l.colEnd), "ace_underline");
                    for (let i = l.lineStart; i <= l.lineEnd; i++) {
                        editor.setAnnotations([{row:i-1 ,column: 0, text:error.message,type:"error"}]);
                    }
                }
            }
        };
        app.finalizeBuild = function(results) {
            const editor = app.$.editor._editor.getSession();
            editor.clearAnnotations();
            _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
            const graphs = [];
            const allGraphs = [];
            const skipped = results.skipped;
            console.log(results);
            for(let id in results.processes){
                if(!_.find(skipped, { id: results.processes[id].id })){
                    results.processes[id].id = id;
                    graphs.push(results.processes[id]);
                }

                allGraphs.push(results.processes[id]);
            }
            app.set('automata.values', graphs.reverse());
            app.set('automata.allValues', allGraphs.reverse());
            app.set('automata.analysis',results.analysis);
            app.$.console.clear();
            app.$.console.log('Successfully Compiled!');
            if(results.operations.length !== 0){
                let passed = 0;
                app.$.console.log('Operations:');
                for(let i = 0; i < results.operations.length; i++){
                    const { operation, process1, process2, result, extra } = results.operations[i];
                    const op = process1.ident + ' ' + operation + ' ' + process2.ident + ' = ' + result + ' '+ extra;
                    if(result=="true"){
                        app.$.console.log(op);
                        passed++;
                    } else{
                        app.$.console.error(op);
                    }
                }

                if(passed === results.operations.length){
                    app.$.console.log('All operations passed!');
                }
                else{
                    app.$.console.log(passed + '/' + results.operations.length + ' operations passed');
                }
            }
            if(results.equations.length !== 0){
                let passed = 0;
                app.$.console.log('Equations:');
                for(let i = 0; i < results.equations.length; i++){
                    const { operation, process1, process2, result, extra } = results.equations[i];
                    const op = process1.ident + ' ' + operation + ' ' + process2.ident + ' = ' + result + ' ' + extra;
                    if(result=="true"){
                        app.$.console.log(op);
                        passed++;
                    } else{
                        app.$.console.error(op);
                    }
                }

                if(passed === results.equations.length){
                    app.$.console.log('All equations passed!');
                }
                else{
                    app.$.console.log(passed + '/' + results.equations.length + ' operations passed');
                }
            }
            app.$.console.log("Compiled in: "+(((new Date()).getTime()-app.lastCompileStartTime)/1000)+" seconds");
            _.each(skipped, skip=> {
                if (skip.type != "user")
                    app.$.console.log("Skipped adding "+skip.id+" to the render list, as it has too many "+skip.type+" ("+skip.length +" > "+skip.maxLength+")");
            });
        };
        /**
         * Compiles and builds what has currently been entered into the text-area.
         * Ignores whether or not live compile and build are currently set.
         */
        app.compileAndBuild = function() {
            app.compile(true);
        };

        /**
         * Gets and returns the code from the editor. Strips the code of all whitespace
         * and unnecessary line breaks.
         */
        app.getCode = function() {
            let code = '';
            let temp = app.$.editor.getCode();

            // remove white space and line breaks
            temp = temp.replace(/ /g, '');

            // remove unnecessary whitespace
            const split = temp.split('\n');
            for(let i = 0; i < split.length; i++){
                if(split[i] !== ''){
                    code += split[i] + '\n';
                }
            }

            return code;
        };

        app.getSettings = function() {
            return {
                fairAbstraction: app.fairAbstraction,
                pruning: app.pruning,
                graphSettings: app.graphSettings
            };
        }

        /**
         * Open a text file from the user's computer and set the text-area to
         * the text parsed from the file.
         */
        app.openFile = function() {
            const opener = app.$['open-file'];
            opener.click();
            opener.onchange = function(e) {
                if (opener.value === '') {
                    return;
                }

                // Load file into editor
                const input = e.target;
                app.saveSettings.currentFile = input.files[0];
                app.reloadFile();
                opener.value = '';

                // Enable reload button
                const reload = app.$['reload'];
                reload.disabled = false;
            };
        };

        /**
         * Reload the last used file.
         */
        app.reloadFile = function() {
            if (app.currentFile === '') {
                return;
            }

            const reader = new FileReader();
            reader.onload = function() {
                const text = reader.result.split("visualiser_json_layout:");
                const code = text[0];
                const json = text[1];
                if (json && json.length > 0) {
                    app.$.visualiser.loadJSON(json);
                }
                app.$.editor.setCode(code);
                app.$.editor.focus();
            };
            reader.readAsText(app.saveSettings.currentFile);
        }

        /**
         * Save to code the user has written to their computer (as a download).
         */
        app.downloadFile = function() {
            let filename = app.$.save.getFileName();
            // if filename has not been defined set to untitled
            if(filename === ''){
                filename = 'untitled';
            }
            let output = "";
            if (app.saveSettings.saveCode)
                output+= app.$.editor.getCode();
            if (app.saveSettings.saveLayout) {
                output+="\nvisualiser_json_layout:"
                output+= JSON.stringify(app.$.visualiser.cy.json());
            }
            const blob = new Blob(
                [output],
                {type: 'text/plain;charset=utf-8'});
            saveAs(blob, filename + '.txt');
        };

        /**
         * Opens the help-dialog.
         */
        app.showHelp = function() {
            app.$.help.open();
        };
        app.showSettings = function() {
            app.$.settings.open();
        };
        /**
         * Simple event listener for when the checkbox in ticked.
         * Compile is called if it is.
         */
        app.$['chbx-live-compiling'].addEventListener('iron-change', function() {
            localStorage.setItem("liveCompiling",app.liveCompiling);
            if (app.liveCompiling) {
                app.compile(false);
            }
            app.$.editor.focus();
        });

        $("#settings-dialog")[0].addEventListener('iron-overlay-closed', function() {
            app.saveGraphSettings();
        });
        app.$['chbx-save-cookie'].addEventListener('iron-change', function() {
            localStorage.setItem("willSave",app.willSaveCookie);
        });
        /**
         * Simple event listener for when the user switches tabs.
         * When we switch to index 1 (Diagram), we need to redraw the canvas,
         * as it needs to be showing currently to render.
         * If we switch to the editor, request focus on it.
         */
        app.$['maintabs'].addEventListener('iron-select', function (e) {
            if (app.$.maintabs.selected === 1) {
                app.$.visualiser.redraw();
            } else if (app.$.maintabs.selected === 0) {
                app.$.editor._editor.focus();
            }else if (app.$.maintabs.selected === 2) {
                app.$.modify.redraw();
            }
        });

        /**
         * This is the event which triggers when the user selects an automata from the
         * list to walk down. It sets the root node of this automata, and all automata
         * with this automata as a sub-graph, blue.
         */
        document.addEventListener('automata-walker-start', function(e) {
            const visualisations = Polymer.dom(this).querySelectorAll('automata-visualisation');
            for (let i in visualisations) {
                visualisations[i].setHighlightNodeId(e.detail.node.id);
            }
        });
        /**
         * This is the event which triggers when the user presses the walk
         * button on the walker element. The walker has already checked for the valid
         * edge and thrown any errors. The edge to walk is given in the event argument
         * 'e.detail.edge'.
         */
        document.addEventListener('automata-walker-walk', function(e) {
            const visualisations = Polymer.dom(this).querySelectorAll('automata-visualisation');
            for (let i in visualisations) {
                visualisations[i].setHighlightNodeId(e.detail.edge.to.id);
            }
        });

        /**
         * This is the event which triggers when the text in the text area is changed.
         * Only care about this if the live-compiling check-box is ticked.
         */
        document.addEventListener('text-editor-change', function() {
            localStorage.setItem("editor",encodeURIComponent(app.$.editor.getCode()));
            if (app.liveCompiling && app.connected) {
                app.compile();
            }
        });
        app.willSaveCookie = localStorage.getItem("willSave")!=='false';
        app.liveCompiling = localStorage.getItem("liveCompiling")!=='false';
        app.pruning = localStorage.getItem("pruning")=='true';
        app.fairAbstraction = localStorage.getItem("fairAbstraction")!=='false';
        app.nodeSep = localStorage.getItem("nodeSep") !== null?parseInt(localStorage.getItem("nodeSep")) : app.nodeSep;
        app.graphSettings = localStorage.getItem("graphSettings") !== null?JSON.parse(localStorage.getItem("graphSettings")) :  app.graphSettings;
        app.saveGraphSettings = ()=>localStorage.setItem("graphSettings",JSON.stringify(app.graphSettings));
        if (app.willSaveCookie && localStorage.getItem('editor') != null) {
            app.$.editor.setCode(decodeURIComponent(localStorage.getItem('editor')));
        }
        /**
         * Listen for key presses.
         * Note: Needs to listen for keydown (not keyup) in order to prevent browser default action
         */
        document.addEventListener('keydown',function(e) {
            if (app.$.help.opened()) {
                // CTRL + S
                if (e.keyCode == 83 && e.ctrlKey) {
                    //Even if the help dialog is open, we don't want the default save dialog to show.
                    e.preventDefault();
                }
                return;
            }

            switch (e.keyCode) {
                case 13:
                    // CTRL + ENTER
                    if (e.ctrlKey) {
                        app.compile();
                        e.preventDefault();
                    }
                    break;
                case 79:
                    // CTRL + O
                    if (e.ctrlKey) {
                        app.openFile();
                        e.preventDefault();
                    }
                    break;
                case 83:
                    // CTRL + S
                    if (e.ctrlKey) {
                        app.$.save.open();
                        e.preventDefault();
                    }
                    break;
                case 112:
                    // F1
                    app.$.help.open();
                    e.preventDefault();
                    break;
                default: return;
            }
        });
    });
});
