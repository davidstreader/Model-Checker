ace.define("ace/theme/example",["require","exports","module","ace/lib/dom"], function(require, exports, module) {

  exports.isDark = false;
  exports.cssClass = "ace-test";
  exports.cssText = `
  .ace-test .ace_gutter {
     background: #f0f0f0;
     color: #333;
  }
  .ace_underline {    
     border-bottom: 1px solid red; 
     position:absolute
  }
  .ace-test .ace_print-margin {
     width: 1px;
     background: #e8e8e8;
  }
  .ace-test .ace_fold {
     background-color: #6B72E6;
  }
  .ace-test {
     background-color: #FFFFFF;
     color: black;
  }
  .ace-test .ace_cursor {
     color: black;
  }
  .ace-test .ace_meta.ace_function{
     color: purple;
  }
  .ace-test .ace_invisible {
     color: rgb(191, 191, 191);
  }
  .ace-test .ace_storage,
  .ace-test .ace_keyword {
     color: blue;
  }
  .ace-test .ace_constant {
     color: rgb(197, 6, 11);
  }
  .ace-test .ace_constant.ace_buildin {
     color: rgb(88, 72, 246);
  }
  .ace-test .ace_constant.ace_language {
     color: rgb(229, 88,246);
  }
  .ace-test .ace_constant.ace_library {
     color: rgb(6, 150, 14);
  }
  .ace-test .ace_invalid {
     background-color: rgba(255, 0, 0, 0.1);
     color: red;
  }
  .ace-test .ace_support.ace_function {
     color: rgb(60, 76, 114);
  }
  .ace-test .ace_support.ace_constant {
     color: rgb(6, 150, 14);
  }
  .ace-test .ace_support.ace_type,
  .ace-test .ace_support.ace_class {
     color: rgb(109, 121, 222);
  }
  .ace-test .ace_keyword.ace_operator {
     color: rgb(104, 118, 135);
  }
  .ace-test .ace_string {
     color: rgb(3, 106, 7);
  }
  .ace-test .ace_comment {
     color: #60695e;
  }
  .ace-test .ace_constant.ace_numeric {
     color: rgb(0, 0, 205);
  }
  .ace-test .ace_variable.ace_action {
     color: rgb(0, 0, 205);
  }
  .ace-test .ace_variable {
     color: rgb(49, 132, 149);
  }
  .ace-test .ace_variable.ace_constant{
     color: rgb(220, 21, 21);
  }
  .ace-test .ace_xml-pe {
     color: rgb(104, 104, 91);
  }
  .ace-test .ace_entity.ace_name.ace_function {
     color: #0000A2;
  }
  .ace-test .ace_heading {
     color: rgb(12, 7, 255);
  }
  .ace-test .ace_list {
     color:rgb(185, 6, 144);
  }
  .ace-test .ace_meta.ace_tag {
     color:rgb(0, 22, 142);
  }
  .ace-test .ace_string.ace_regex {
     color: rgb(255, 0, 0)
  }
  .ace-test .ace_marker-layer .ace_selection {
     background: rgb(181, 213, 255);
  }
  .ace-test.ace_multiselect .ace_selection.ace_start {
     box-shadow: 0 0 3px 0px white;
  }
  .ace-test .ace_marker-layer .ace_step {
     background: rgb(252, 255, 0);
  }
  .ace-test .ace_marker-layer .ace_stack {
     background: rgb(164, 229, 101);
  }
  .ace-test .ace_marker-layer .ace_bracket {
     margin: -1px 0 0 -1px;
     border: 1px solid rgb(192, 192, 192);
  }
  .ace-test .ace_marker-layer .ace_active-line {
     background: rgba(0, 0, 0, 0.07);
  }
  .ace-test .ace_gutter-active-line {
     background-color : #dcdcdc;
  }
  .ace-test .ace_marker-layer .ace_selected-word {
     background: rgb(250, 250, 255);
     border: 1px solid rgb(200, 200, 250);
  }
  .ace-test .ace_indent-guide {
     background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==") right repeat-y;
  }`;
  require("ace/lib/dom").importCssString(exports.cssText, exports.cssClass);
});
