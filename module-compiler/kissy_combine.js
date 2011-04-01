/**
 combined files : 

d:\code\kissy_git\kissy-tools\module-compiler\test\kissy\dom.js
d:\code\kissy_git\kissy-tools\module-compiler\test\kissy\event\base.js
d:\code\kissy_git\kissy-tools\module-compiler\test\kissy\event\ie.js
d:\code\kissy_git\kissy-tools\module-compiler\test\kissy\event.js
d:\code\kissy_git\kissy-tools\module-compiler\test\kissy\overlay.js
**/

KISSY.add("dom", function() {
});


KISSY.add("event/base", function() {
});


KISSY.add("event/ie", function() {
}, {requires:["./base"]});


KISSY.add("event", function() {
}, {requires:["event/ie"]});


KISSY.add("overlay", function() {
}, {requires:["dom", "event"]});



