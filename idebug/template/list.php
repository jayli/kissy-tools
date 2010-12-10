<html>
<head>
    <meta charset="utf-8" />
    <title>iDebug --- &copy; 2010 Taobao UED - created by yunqian</title>
    <link rel="stylesheet" href="assets/main.css" />
</head>
<body>
    <h1>
        <a href="%uri%">iDebug</a>
        <span>--- &copy; 2010 Taobao UED - created by yunqian --- 请拖动此 bookmarklet 到你的收藏夹：<a href="javascript:(function(){if(/-min\\./.test(location.href)){location.href = location.href.replace('-min', '');return;}else if(/\\.(css|js)/i.test(location.href)){location.href=location.href.replace(/\\.(css|js)/gi, '.source.$1');return;}window.open('%uri%?url='+location.href);})();">iDebug</a></span>
        <span id="msg"></span>
    </h1>
    %list%
</body>
</html>