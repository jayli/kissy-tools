/*
 Combined modules by KISSY Module Compiler: 

 dom
 uibase/position
 switch
 event/base
 dom/ie
 event/ie
 event
 overlay
 page/run
*/

KISSY.add("dom", function() {
});
KISSY.add("uibase/position", function() {
});
KISSY.add("switch", function() {
}, {requires:["dom", "uibase/position"]});
KISSY.add("event/base", function() {
},{
    requires:['dom']
});

KISSY.add("dom/ie", function() {
}, {requires:["ua"]});
KISSY.add("event/ie", function() {
}, {requires:["./base",'dom/ie','ua']});

KISSY.add("event", function() {
}, {requires:["event/ie"]});

KISSY.add("overlay", function() {
}, {requires:["dom", "event", "component"]});
KISSY.add("page/run", function() {
}, {requires:["switch", "overlay"]});

