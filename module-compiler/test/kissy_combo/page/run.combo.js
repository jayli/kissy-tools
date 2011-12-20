/**
 combined files : 

D:\code\kissy_git\kissy-tools\module-compiler\test\kissy_combo\dom.js
D:\code\kissy_git\kissy-tools\module-compiler\test\kissy_combo\uibase\position.js
D:\code\kissy_git\kissy-tools\module-compiler\test\kissy_combo\switch.js
D:\code\kissy_git\kissy-tools\module-compiler\test\kissy_combo\page\run.js
**/

KISSY.add("dom", function() {
});

KISSY.add("uibase/position", function() {
});

KISSY.add("switch", function() {
}, {requires:["dom", "uibase/position"]});

KISSY.add("page/run", function() {
}, {requires:["switch"]});


