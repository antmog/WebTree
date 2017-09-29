<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>BENIS HEHE</title>

    <spring:url value="/resources/contextmenu/css/contextMenu.css" var="contextMenu"/>
    <link href="${contextMenu}" rel="stylesheet"/>

    <spring:url value="/resources/jstree/themes/default/style.min.css" var="treeCss"/>
    <link href="${treeCss}" rel="stylesheet"/>

</head>
<body>

<h2>Enter developer information</h2>
<div id="kkk">
    <div id="jstree">
    </div>
</div>

<button id="butt">Reload tree</button>

<textarea id="textBox" class="actionBlock"></textarea>
<button id="textOk" class="actionBlock">Ok</button>
<button id="textCancel" class="actionBlock">Cancel</button>

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
                                return '/jdbc/getRoot';
                            } else {
                                return '/jdbc/getChildrenJson/';
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
        $("#butt").click(function () {
            $('#jstree').jstree(true).refresh();
        });
        $('#jstree').on("move_node.jstree", function (e, data) {
            $.ajax({
                type: "get",
                cache: false,
                url: "/jdbc/actionItem/",
                success: function (data) {
                    $('#jstree').jstree(true).refresh();
                    if(data === "true"){
                        alert("Operation succeed.");
                    }else{
                        alert("Operation failed.");
                    }
                },
                data: { id: data.node.id , action: "Dnd", newParent: data.parent, oldParent: data.old_parent },
                dataType: "text"
            });
        });
    });
</script>
<script src="/resources/jstree/jstree.min.js"></script>
<script src="/resources/contextmenu/js/contextMenu.js"></script>
</body>
</html>