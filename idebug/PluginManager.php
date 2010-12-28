<?php

class PluginManager {

    private $_listeners = array();

    public function __construct() {

        $plugins = $this->get_active_plugins();

        if ($plugins) {
            foreach ($plugins as $plugin) {
                if (@file_exists("./plugins/$plugin")) {
                    include_once("./plugins/$plugin");
                    $class = str_replace(".php", "_plugin", $plugin);
                    if (class_exists($class)) {
                        new $class($this);
                    }
                }
            }
        }
    }

    function get_active_plugins() {
        global $block_plugins;
        if ($handle = opendir("./plugins/")) {
            while (false !== ($file = readdir($handle))) {
                if (strpos($file, ".php") && !strpos($block_plugins, str_replace(".php", "", $file))) {
                    $plugins[] = $file;
                }
            }
        }
        closedir($handle);
        return $plugins;
    }

    function register($hook, &$reference, $method) {
        $key = get_class($reference).'->'.$method;
        $this->_listeners[$hook][$key] = array(&$reference, $method); 
    }

    function trigger($hook, $data='') {
        $result = '';
        if (isset($this->_listeners[$hook]) && is_array($this->_listeners[$hook]) && count($this->_listeners[$hook]) > 0) {
            foreach ($this->_listeners[$hook] as $listener) {
                $class =& $listener[0];
                $method = $listener[1];
                if (method_exists($class,$method)) {
                    $result .= $class->$method($data);
                }
            }
        }
        return $result;
    }
}