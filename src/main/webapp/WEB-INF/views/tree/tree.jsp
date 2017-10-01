<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html"/>
    <title>Tree application.</title>

    <spring:url value="resources/contextmenu/css/contextMenu.css" var="contextMenu"/>
    <link href="${contextMenu}" rel="stylesheet"/>

    <spring:url value="resources/jstree/themes/default/style.min.css" var="treeCss"/>
    <link href="${treeCss}" rel="stylesheet"/>
</head>
<body>
<h2>Welcome.Огонь</h2>
<div id="tree-container" class="tree-container">
    <div id="jstree">
    </div>
    <p id = "operation-result" class="operation-result"></p>
</div>

<button id="reload">Reload tree</button>
<button class = "delay0" >Set delay 0</button>
<button class = "delay2" >Set delay 2 sec</button>

<div id="action-block" class="action-block">
    <textarea class="action-block__text-box"></textarea>
    <button class="action-block__text-ok">Ok</button>
    <button class="action-block__text-cancel">Cancel</button>
</div>
<nav id="context-menu" class="context-menu">
    <ul class="context-menu__items">
        <li class="context-menu__item">
            <a href="#" class="context-menu__link" data-action="Create">Create Folder</a>
        </li>
        <li class="context-menu__item">
            <a href="#" class="context-menu__link" data-action="Edit"> Edit Folder</a>
        </li>
        <li class="context-menu__item">
            <a href="#" class="context-menu__link" data-action="Delete"> Delete Folder</a>
        </li>
    </ul>
</nav>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.1/jquery.min.js"></script>
<script>
    $(function () {
        function init() {
            $('#jstree').jstree({
                'plugins': ["sort" , "dnd"],
                'core': {
                    'check_callback': true,
                    'data': {
                        'cache': false,
                        'url': function (node) {
                            if (node.id === '#') {
                                return 'jdbc/getRoot';
                            } else {
                                return 'jdbc/getChildrenJson/';
                            }
                        },
                        'data': function (node) {
                            return {'id': node.id};
                        },
                        'dataType': "json"
                    }
                }
            });
        }
        init();
        $("#reload").click(function () {
            $('#jstree').jstree(true).refresh();
        });
    });
</script>
<script src="resources/jstree/jstree.min.js"></script>
<script src="resources/contextmenu/js/contextMenu.js"></script>
</body>
</html>