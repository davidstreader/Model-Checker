const saveDebounced = app.utils.debounce(saveSettings,250);
module.exports = {
    init:load,
    getSettings:getSettings,
    save:saveDebounced
};
const defaults = {
    liveCompiling: true,
    fairAbstraction: true,
    pruning: false,
    nodeSep: 50,
    autoMaxNode: 40,
    failCount: 10,
    passCount: 10,
    darkTheme: false,
    currentFile: '',
    saveCode: true,
    saveLayout: true,
    autoSave: true
};
let settings = (localStorage.settings && JSON.parse(localStorage.settings)) || defaults;

const nodeSep = $("#nodeSep");
const maxNode = $("#maxNodeCount");
const failCount = $("#failCount");
const passCount = $("#passCount");
const darkMode = $("#darkMode");
const fabs = $("#fairAbs");
const pruning = $("#pruning");
const autoSave = $("#autoSave");
const liveCompiling = $("#liveCompiling");
function load() {
    if (settings.nodeSep > 200) settings.nodeSep = 200;
    saveSettings();
    nodeSep.slider();
    nodeSep.val(settings.nodeSep);
    failCount.val(settings.failCount);
    passCount.val(settings.passCount);
    maxNode.val(settings.autoMaxNode);
    darkMode[0].checked = settings.darkTheme;
    fabs[0].checked = settings.fairAbstraction;
    pruning[0].checked = settings.pruning;
    autoSave[0].checked = settings.autoSave;
    liveCompiling[0].checked = settings.liveCompiling;
    invert(settings.darkTheme);
    if (localStorage.editor && autoSave) {
        app.editor.setCode(decodeURIComponent(localStorage.editor));
    }
}
function invert(inverted) {
    const body = $("body");
    const nav = $(".navbar");
    inverted?nav.addClass("navbar-inverse"):nav.removeClass("navbar-inverse");
    inverted?body.addClass("invert"):body.removeClass("invert");
    const theme = "ace/theme/"+(inverted?"vibrant_ink":"example");
    app.editor.setTheme(theme);
    settings.darkTheme = inverted;
    saveDebounced();
    if (app.models) {
        app.models.updateTheme()
    }
}
function saveSettings() {
    localStorage.setItem("settings",JSON.stringify(settings));
}
function getSettings() {
    return settings;
}
nodeSep.on("slide", function() {
    settings.nodeSep = this.value;
    saveDebounced();
});
maxNode.on('input',()=>{
    settings.autoMaxNode = maxNode.val();
    saveDebounced();
});
failCount.on('input',()=>{
    settings.failCount = failCount.val();
    saveDebounced();
});
passCount.on('input',()=>{
    settings.passCount = passCount.val();
    saveDebounced();
});
fabs.change(function(){
    settings.fairAbstraction = this.checked;
    saveDebounced();
});
autoSave.change(function(){
    settings.autoSave = this.checked;
    saveDebounced();
});
pruning.change(function(){
    settings.pruning = this.checked;
    saveDebounced();
});
liveCompiling.change(function(){
    settings.liveCompiling = this.checked;
    saveDebounced();
});
darkMode.change(function(){
    invert(this.checked);
});
