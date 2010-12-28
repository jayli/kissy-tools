<?php

class Query_plugin
{

    function __construct(&$pluginManager) {

        if (isset($_GET["list"])) {
            $items = getPagesBy($_GET["tag"]);

            $ret = "<ul>";
            foreach($items as $item) {
                $guid = $item["guid"];
                $tag = $item["tag"] == "" ? "no tag" : $item["tag"];
                $url = $item["url"];
                $ret .= "<li><a href=\"?demo&guid=$guid&edit\">$tag</a> <span>($url)</span></li>";
            }
            $ret .= "</ul>";

            echo parseTemplate("./template/list.php", array(
                "list" => $ret,
                "uri"  => "http://".$_SERVER["SERVER_NAME"].$_SERVER["SCRIPT_NAME"]
            ));
            
        }
        
        $pluginManager->register('demo', $this, 'say_hello');
    }

    function say_hello() {
        echo "Query_plugin enabled <br>";
    }

}
