<html>
<head>
    <meta charset="utf-8" />
    <title>iDebug --- &copy; 2010 Taobao UED - created by yunqian</title>
    <link rel="stylesheet" href="assets/main.css" />
</head>
<body>
    <h1>
        <a href="%uri%">iDebug</a>
        <span>
            --- &copy; 2010 Taobao UED - created by yunqian --- 请拖动此 bookmarklet 到你的收藏夹：
            <a href="javascript:(function(){if(/-min\\./.test(location.href)){location.href = location.href.replace('-min', '');return;}else if(/\\.(css|js)/i.test(location.href)){location.href=location.href.replace(/\\.(css|js)/gi, '.source.$1');return;}window.open('%uri%?url='+location.href);})();">iDebug</a>
            &nbsp;&nbsp;&nbsp;&nbsp;
            启用快捷键:<input type="checkbox" id="shortcut-switcher" checked />&nbsp; | &nbsp;
            Demo 查看模式:<input type="checkbox" id="demomode-switcher" />
        </span>
        <span id="msg"></span>
    </h1>
    <form action="%uri%" class="top-search">
        <input name="list" type="hidden" />
        <input id="search" name="tag" type="search" placeholder="search by tag" />
    </form>
    <form method="post" id="frm" name="frm">
        <div id="left-textarea-cont">
            <textarea id="left-textarea" placeholder="html content" style="width:100%;" rows=22 name=html>%html%</textarea>
        </div>
        <div id="right-textarea-cont">
            <iframe id="right-textarea-iframe" src="?demo&guid=%guid%"></iframe>
            <textarea id="right-textarea" placeholder="auto responder" style="width:100%;" rows=22 name=responder>%responder%</textarea>
            <div><label>开启 responder：</label><input name=enableResponder type=checkbox %enableResponder%></div>
        </div>
        <div style=clear:both;></div>
        <label>源地址：</label><input size=60 readonly value="%url%"><br>
        <label>可用来查找的 tag：</label><input name=tag size=60 value="%tag%"><br>
        <label>还原 Combo：</label><input name=decombo type=checkbox %decombo%><br>
        <label>删除 assets 路径上的 -min：</label><input name=source type=checkbox %source%><br>
        <label>切换 assets host：</label><select name=asset>
            <option value=0>none</option>
            <option value=1>online (a.tbcdn.cn)</option>
            <option value=2>daily</option>
            <option value=3>pre</option></select><br>
        <input type=hidden name=guid value="%guid%">
        <button class="button">保存</button>
        <a href="?demo&guid=%guid%" style=margin-left:10px;>查看 demo</a>
        <a href="?demo&guid=%guid%&assets" style=margin-left:10px;>查看所有 CSS 和 JS</a>
        <a href="javascript:;" id="js-beautify" style=margin-left:10px;>JS Beautify(或格式化 JSON)</a>
        <a href="javascript:;" id="html-beautify" style=margin-left:10px;>HTML Beautify</a>
    </form>

    <script src="assets/jquery-1.4.3.js"></script>
    <script src="assets/beautifier.js"></script>
    <script src="assets/beautify-html.js"></script>
    <script>
    (function($) {

        document.forms["frm"]["asset"].options[parseInt('%asset%', 10)||0].selected = true;

        var shortcutSwitcher = document.getElementById('shortcut-switcher'),
            iframe = $('#right-textarea-iframe')[0];

        $(document).bind('keydown', function(e) {
            if (!shortcutSwitcher.checked) return;
            
            // shift + enter
            if ((e.metaKey || e.ctrlKey) && e.keyCode == 13) {
                saveForm();
                return false;
            }
            // shift + v
            if ((e.shiftKey) && e.keyCode == 86) {
                window.open(location.href.replace('&edit', ''));
                return false;
            }
            // shift + a
            if ((e.shiftKey) && e.keyCode == 65) {
                window.open(location.href.replace('&edit', '&assets'));
                return false;
            }
            // shift + ?
            if ((e.shiftKey) && e.keyCode == 191) {
                $('#facebox, #facebox_overlay').toggle();
                return false;
            }
            // shift + h
            if ((e.shiftKey) && e.keyCode == 72) {
                $('#demomode-switcher')[0].checked = !$('#demomode-switcher')[0].checked;
                toggleDemoMode();
                return false;
            }
            // esc
            if (e.keyCode == 27) {
                $('#facebox, #facebox_overlay').hide();
                return false;
            }
        });

        $('#frm').submit(function() {
            saveForm();
            return false;
        });

        $('#js-beautify').click(function() {
            $('#left-textarea')[0].value = js_beautify($('#left-textarea').val());
        });

        $('#html-beautify').click(function() {
            $('#left-textarea')[0].value = style_html($('#left-textarea').val());
        });

        $('#demomode-switcher').click(function() {
            toggleDemoMode();
        });

        function toggleDemoMode() {
            $(document.body)[$('#demomode-switcher')[0].checked ? 'addClass' : 'removeClass']('mode-demo');
        }

        function message(msg) {
            $('#msg').html(msg).show();
            setTimeout(function() {
                $('#msg').fadeOut();
            }, 1000);
        }

        function saveForm() {
            message("saving...");
            $.post('%uri%', $('#frm').serialize(), function(msg) {
                message(msg === '1' ? 'content saved...' : '<b style="color:red;">error...</b>');
                if ($(document.body).hasClass('mode-demo')) {
                    iframe.contentWindow.location.href = iframe.contentWindow.location.href;
                    $('#left-textarea').focus();
                }
            });
        }

    })(jQuery);
    </script>

    <div id="facebox">
    <div class="popup">
    <div class="content shortcuts">
        <h2>帮助</h2>
        <div class="columns">
            <h3>快捷键</h3>
<dl>
<dt>Shift + A：查看所有 CSS 和 JS</dt>
<dt>Shift + V：查看 Demo</dt>
<dt>Shfit + ?：查看帮助</dt>
<dt>Shfit + H：切换 Demo Mode</dt>
<dt>Ctrl + Enter：保存</dt>
</dl>
        </div>
        <h3>规则示例</h3>
        <div class="columns">
        <pre><code>a.css
----
b.css
====
c.js
----
d.js
e.js</code></pre>
        </div>
        <h3>说明PPT</h3>
        <div class="columns" style="margin-top:10px;">
            <dt><a href="docs/idebug_20101030.pdf">查看</a></dt>
        </div>
    </div>
    </div>
    </div>
    <div id="facebox_overlay"></div>
</body>
</html>