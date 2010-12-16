<?php

@include_once("config.inc");
@include_once("utils.php");
@include_once("PluginManager.php");

$pm = new PluginManager();
// $pm->trigger("demo");

// 更新页面
if (isset($_POST["html"]) && isset($_POST["guid"])) {
    updatePage($_POST["guid"], $_POST["html"]);
    return;
}

if (isset($_GET["demo"]) && isset($_GET["guid"])) {
    $item = getPageByGuid($_GET["guid"]);

    // 编辑页面
    if (isset($_GET["edit"])) {
        $html = getEditHTML($item);
    }

    // 查看页面
    else if (isset($_GET["guid"])) {
        $html = htmlspecialchars_decode($item["html"]);
        /* $html = preg_replace("/<meta.+?charset=\"?([^\"]+?)\".*?>/", "<meta charset=\"utf-8\" />", $html); */

        preg_match("<meta.+?charset=\"?([^\"]+?)\".*?>", $html, $match);
        if ($match[1]) {
            if (strpos(" gbk gb2312 gb18030 ", strtolower(trim($match[1])))) {
                $html = iconv("utf-8", trim($match[1]), $html);
            }
        }

        // responder 的注释功能
        $item["responder"] = preg_replace("/<!--[\\s\\S]*-->/", "", $item["responder"]);

        // 初始化 responder 数据
        if ($item["enableResponder"] === "1") {
            foreach(split("====", $item["responder"]) as $rp) {
                if (trim($rp) !== "") {
                    $rpArr = split("----", $rp);
                    $rpAssets[] = array(
                        trim($rpArr[0]),
                        split("\r|\n", trim($rpArr[1]))
                    );
                }
            }
        }

        // responder
        if ($item["enableResponder"] === "1" && count($rpAssets) > 0) {
            $html = htmlResponse($html, $rpAssets);
        }

        // 还原 combo
        if ($item["decombo"] === "1") {
            $html = htmlDecombo($html);
        }

        // responder
        if ($item["enableResponder"] === "1" && count($rpAssets) > 0) {
            $html = htmlResponse($html, $rpAssets);
        }

        // 还原 min
        if ($item["source"] === "1") {
            $html = str_replace("-min.css", ".css", $html);
            $html = str_replace("-min.js", ".js", $html);
        }

        // responder
        if ($item["enableResponder"] === "1" && count($rpAssets) > 0) {
            $html = htmlResponse($html, $rpAssets);
        }

        // 切换 Hosts
        if ($item["asset"] !== "0" && $item["asset"] !== "") {
            $html = str_replace($assetsHost, $assetsHostEnv[$item["asset"]], $html);
        }

        // 支持相对路径
        // $html = preg_replace("/href=\"(.+?)\"/", "$1", $html);
        preg_match_all("/(href|src)=(\\'|\")(.+?)(\\'|\")/", $html, $matches);
        // foreach ($matches[3] as $url) {
        if (strpos($item["url"], "http://") !== false) {
            $url = $item["url"];
            $url = substr($url, 0, strpos($url, "?") == false ? strlen($url) : strpos($url, "?"));
            $url = substr($url, 0, strrpos($url, "/") == false ? strlen($url) : strrpos($url, "/"))."/";
            for ($i=0; $i<count($matches[3]); $i++) {
                if (strpos($matches[3][$i], "http://") !== false) continue;
                if (strpos($matches[3][$i], "/") === 0) {
                    $html = str_replace($matches[0][$i], $matches[1][$i]."=\"".$url.substr($matches[3][$i], 1)."\"", $html);
                } else {
                    $html = str_replace($matches[0][$i], $matches[1][$i]."=\"".$url.$matches[3][$i]."\"", $html);
                }
                // var_dump($matches[3][$i]);
                // $html = str_replace($matches, "", $html);
            }
        }
        // preg_match("/href=\"(.+?)\"/", $html, $matches);


        // 查看资源情况
        if (isset($_GET["assets"])) {
            $count = 0;
            $assets = getAllAssetsTags($html);
            preg_match_all("/<style.*?>([\\S\\s]*?)<\\/style>/", $html, $matches);
            for ($i=0; $i<count($matches[0]); $i++) {
                $assets["css"][] = array("", "CSS Block", $matches[0][$i]);
            }
            preg_match_all("/<script.*?>([\\S\\s]*?)<\\/script>/", $html, $matches);
            for ($i=0; $i<count($matches[0]); $i++) {
                $assets["js"][] = array("", "JavaScript Block", $matches[0][$i]);
            }

            if (count($assets) > 0) {
                foreach($assets as $k=>$v) {
                    foreach($v as $asset) {
                        if (isset($_GET["css"]) && $k !== "css") continue;
                        if (isset($_GET["js"]) && $k !== "js") continue;

                        $assetSrc = str_replace("-min.$k", ".$k", $asset[1]);
                        $assetContent = $asset[2] ? $asset[2] : iconv("gbk", "utf-8", file_get_contents($assetSrc));
                        $assetContent = htmlspecialchars($assetContent);
                        $viewAllHd .= "<a href=\"#asset-$count\">$assetSrc</a><br>";
                        $viewAllBd .= "<div style=\"border:5px solid #ccc;\">".
                                "<a id=\"asset-$count\">$assetSrc</a> <a href=\"#hd\">回到顶部</a><br><pre>".$assetContent."</pre></div>".
                                "<div style=\"height:100px;background:gray;\"></div>";
                        $count++;
                    }
                }
            }

            $html = parseTemplate("./template/assets.php", array(
                "hd" => $viewAllHd,
                "bd" => $viewAllBd
            ));
        }
    }

    echo $html;
    return;
    
}

// 无参数
if (count($_SERVER["argv"]) === 0) {
    echo "<script>location.href=\"?demo&guid=4d020049ca105&edit\";</script>";
}


