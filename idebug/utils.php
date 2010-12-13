<?php

/**
 * 获取 PDO
 * @return PDO
 */
function getPDO() {
    return new PDO("sqlite:".SQLITE_FILENAME, "", "");
}

/**
 * 执行 PDO
 * @param  $sql
 * @return void
 */
function execPDO($sql) {
    $pdo = getPDO();
    $stm = $pdo->exec($sql);
    $pdo = null;
    return $stm;
}

/**
 * 新增页面
 * @return void
 */
function insertPage($html) {

    preg_match("<meta.+?charset=\"?([^\"]+?)\".*?>", stripslashes($html), $match);
    if ($match[1]) {
        if (strpos(" gbk gb2312 gb18030 ", strtolower(trim($match[1])))) {
            $html = iconv(trim($match[1]), "utf-8", $html);
        }
    }
    
    $d = getPDORequiredData($html, uniqid());
    execPDO("INSERT INTO html (guid, html, name, responder, decombo, source, tag, added, url, asset) VALUES (\"".$d["guid"]."\", \"".$d["html"]."\", \"".$d["name"]."\", \"".$d["responder"]."\", \"".$d["decombo"]."\", \"".$d["source"]."\", \"".$d["tag"]."\", \"".$d["date"]."\", \"".$d["url"]."\", \"".$d["asset"]."\")");

    echo "<br>".$d["guid"];
    echo "<script>setTimeout(function(){ location.href = '".$d["uti"]."?demo&guid=".$d["guid"]."&edit'; }, 0);</script>";
}

/**
 * 修改页面
 * @return void
 */
function updatePage($guid, $html) {
    $d = getPDORequiredData($html, $guid);
    $s = execPDO("UPDATE html SET html = \"".$d["html"]."\", name = \"".$d["name"]."\", responder = \"".$d["responder"]."\", enableResponder = \"".$d["enableResponder"]."\", decombo = \"".$d["decombo"]."\", source = \"".$d["source"]."\", tag = \"".$d["tag"]."\", last_modified = \"".$d["date"]."\", asset = \"".$d["asset"]."\" WHERE guid = \"".$d["guid"]."\"");
    echo $s ? "1" : "0";
}

/**
 * 查询页面
 * @param  $tag
 * @return
 */
function getPagesBy($tag = null, $guid = null) {
    $guid = isset($guid) ? "WHERE guid = \"$guid\"" : "";
    $tag = isset($tag) ? "WHERE tag like \"%$tag%\"" : "";
    $pdo = getPDO();
    $stm = $pdo->query("SELECT * FROM html $tag$guid ORDER BY id DESC");
    $pdo = null;
    
    while ($row = $stm->fetch()) { $ret[] = $row; }
    return $ret;
}

/**
 * 通过 guid 查询页面
 * @param  $guid
 * @return bool
 */
function getPageByGuid($guid) {
    $items = getPagesBy(null, $guid);
    return count($items) > 0 ? $items[0] : false;
}

/**
 * 获取 PDO 所需要的数据
 * @param  $html
 * @param  $guid
 * @return
 */
function getPDORequiredData($html, $guid) {
    return array(
        "guid"            => isset($guid) ? $guid : uniqid(),
        "html"            => htmlspecialchars(stripslashes($html)),
        "date"            => date("Ymd G:i:s"),
        "name"            => $_POST["name"] ? $_POST["name"] : $_GET["name"],
        "responder"       => $_POST["responder"],
        "tag"             => $_POST["tag"] ? $_POST["tag"] : $_GET["tag"],
        "url"             => isset($_POST["url"]) ? $_POST["url"] : $_GET["url"],
        "asset"           => $_POST["asset"] ? $_POST["asset"] : $_GET["asset"],
        "decombo"         => $_POST["decombo"] ? "1" : "0",
        "source"          => $_POST["source"] ? "1" : "0",
        "enableResponder" => $_POST["enableResponder"] ? "1" : "0"
    );
}

/**
 * 解析模版
 * @param  $file
 * @param  $data
 * @return void
 */
function parseTemplate($file, $data) {
    if (file_exists($file)) {
        $html = file_get_contents($file);
        foreach ($data as $k=>$v) {
            if (!preg_match("/^\\d+$/", $k)) {
                $html = str_replace("%$k%", $v, $html);
            }
        }
        return $html;
    }
}

/**
 * 获取编辑状态下的 HTML
 * @param  $item
 * @return void
 */
function getEditHTML($item) {
    $item["uri"] = "http://".$_SERVER["SERVER_NAME"].$_SERVER["SCRIPT_NAME"];
    foreach(array("decombo", "source", "enableResponder") as $k) {
        $item[$k] = $item[$k] === "1" ? "checked" : "";
    }

    return parseTemplate("./template/edit.php", $item);
}

/**
 * @param  $html
 * @return 还原 Combo
 */
function htmlDecombo($html) {
    
    $assets = getAllAssetsTags($html);
    $tpl = array(
        "css" => "<link rel=\"stylesheet\" href=\"%source%\" />\r\n",
        "js"  => "<script src=\"%source%\"></script>\r\n"
    );

    foreach($assets as $k=>$v) {
        foreach($v as $asset) {
            if (strpos($asset[1], "??")) {
                $html = str_replace($asset[0], htmlDecomboReplace($asset, $tpl[$k]), $html);
            }
        }
    }

    return $html;
}

function htmlDecomboReplace($str, $tpl) {
    $parts = split("\\?\\?", $str[1]);
    $base = $parts[0];
    $assets = split(",", $parts[1]);
    $ret = "\r\n<!-- decombo: $str[1] -->\r\n";
    foreach ($assets as $asset) {
        $ret .= str_replace("%source%", $base.$asset, $tpl);
    }
    return $ret;
}

/**
 * 实现 responder 功能
 * @param  $html
 * @param  $rpAssets
 * @return void
 */
function htmlResponse($html, $rpAssets) {
    
    $assets = getAllAssetsTags($html);
    $tpl = array(
        "css" => "\r\n<link rel=\"stylesheet\" href=\"%source%\" />",
        "js"  => "\r\n<script src=\"%source%\"></script>"
    );

    foreach($assets as $k=>$v) {
        foreach($v as $asset) {
            foreach($rpAssets as $rpAsset) {
                if (strpos($asset[1], $rpAsset[0]) !== false || strpos($rpAsset[0], $asset[1]) !== false) {
                    // var_dump($asset[1]);
                    $html = str_replace($asset[0], htmlResponseReplace($asset, $rpAsset[1], $tpl[$k]), $html);
                } else {
                    $html = str_replace($rpAsset[0], $rpAsset[1][0], $html);
                }
            }
        }
    }
    
    return $html;
}

function htmlResponseReplace($o, $ns, $tpl) {
    $ret = "<!-- responder: $o[1] --> ";
    foreach ($ns as $n) {
        $ret .= str_replace("%source%", $n, $tpl);
    }
    return $ret;
}

/**
 * 获取所有 assets 标签 (CSS 和 JS)
 * @param  $html
 * @return
 */
function getAllAssetsTags($html) {
    preg_match_all("/<link.+?href=\"(.+?)\".*?>/", $html, $matches);
    for ($i=0; $i<count($matches[0]); $i++) {
        if (strpos($matches[0][$i], "stylesheet")) {
            $ret["css"][] = array($matches[0][$i], $matches[1][$i]);
        }
    }
    preg_match_all("/<script.*?src=\"(.+?)\".*?><\\/script>/", $html, $matches);
    for ($i=0; $i<count($matches[0]); $i++) {
        $ret["js"][] = array($matches[0][$i], $matches[1][$i]);
    }
    return $ret;
}
