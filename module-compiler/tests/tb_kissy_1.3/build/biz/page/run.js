/*
 Combined modules by KISSY Module Compiler: 

 biz/x
 biz/y
 biz/page/run
*/

KISSY.add("biz/x", function() {
    return "жпнд";
}, {requires:["overlay", "switchable"]});

KISSY.add("biz/y", function() {
}, {requires:["./x"]});

KISSY.add("biz/page/run", function() {
}, {requires:["../y"]});


