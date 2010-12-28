<?php

date_default_timezone_set("Asia/Shanghai");

define("SQLITE_FILENAME", "idebug.sqlite");

// 默认开启全部插件，可在此声明禁用的插件
$block_plugins = "";

$assetsHost    = array("a.tbcdn.cn", "assets.daily.taobao.net", "assets.taobaocdn.com");
$assetsHostEnv = array("", "a.tbcdn.cn", "assets.daily.taobao.net", "110.75.14.78");