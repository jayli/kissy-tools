KISSY.add("dom",function(){

});
KISSY.add("event/base",function(){

});
KISSY.add("event/ie",function(){

},{
    requires:['event/base']
});
KISSY.add("event",function(){

},{
    requires:["event/base","event/ie"]
});

