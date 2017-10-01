(function () {
    "use strict";

    /**
     * Function to check if we clicked inside an element with a particular class
     * name.
     *
     * @param {Object} e The event
     * @param {String} className The class name to check against
     * @return {Boolean}
     */
    function clickInsideElement(e, className) {
        var el = e.srcElement || e.target;

        if (el.classList.contains(className)) {
            return el;
        } else {
            while (el = el.parentNode) {
                if (el.classList && el.classList.contains(className)) {
                    return el;
                }
            }
        }
        return false;
    }

    /**
     * Get's exact position of event.
     *
     * @param {Object} e The event passed in
     * @return {Object} Returns the x and y position
     */
    function getPosition(e) {
        var posx = 0;
        var posy = 0;

        if (!e) var e = window.event;

        if (e.pageX || e.pageY) {
            posx = e.pageX;
            posy = e.pageY;
        } else if (e.clientX || e.clientY) {
            posx = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
            posy = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
        }

        return {
            x: posx,
            y: posy
        }
    }

    // C O R E

    /**
     * Variables.
     */
    var actionBlockActive = "action-block--active";
    var actionBlock = document.querySelector("#action-block");
    var textBox = document.querySelector(".action-block__text-box");
    var textOk = document.querySelector(".action-block__text-ok");
    var textCancel = document.querySelector(".action-block__text-cancel");
    var actionBlockState = 0;
    var operationResult = document.querySelector("#operation-result");

    var action;

    var contextMenuClassName = "context-menu";
    var contextMenuItemClassName = "context-menu__item";
    var contextMenuLinkClassName = "context-menu__link";
    var contextMenuActive = "context-menu--active";

    var taskItemClassName = "jstree-node";
    var taskItemInContext;

    var clickCoords;
    var clickCoordsX;
    var clickCoordsY;
    var menu = document.querySelector("#context-menu");
    var menuItems = menu.querySelectorAll(".context-menu__item");
    var menuState = 0;
    var menuWidth;
    var menuHeight;
    var menuPosition;
    var menuPositionX;
    var menuPositionY;

    var windowWidth;
    var windowHeight;

    /**
     * Initialise our application's code.
     */
    function init() {
        textBox.Multiline = false;
        textBox.AcceptsReturn = false;
        dragAndDropListener();
        buttonListener();
        contextListener();
        clickListener();
        keyupListener();
        resizeListener();
    }

    /**
     * Listens for contextmenu events.
     */
    function contextListener() {
        document.addEventListener("contextmenu", function (e) {
            taskItemInContext = clickInsideElement(e, taskItemClassName);

            if (taskItemInContext) {
                e.preventDefault();
                toggleMenuOn();
                positionMenu(e);
            } else {
                taskItemInContext = null;
                toggleMenuOff();
            }
        });
    }

    /**
     * Listens for click events.
     */
    function clickListener() {
        document.addEventListener("click", function (e) {
            var clickeElIsLink = clickInsideElement(e, contextMenuLinkClassName);
            if (clickeElIsLink) {
                e.preventDefault();
                menuItemListener(clickeElIsLink);
            } else {
                var button = e.which || e.button;
                if (button === 1) {
                    toggleMenuOff();
                }
            }
        });
    }

    /**
     * Listens for keyup events.
     */
    function keyupListener() {
        window.onkeyup = function (e) {
            if (e.keyCode === 27) {
                toggleMenuOff();
                deActivateTextBoxMenu();
            }
        }
    }

    /**
     * Window resize event listener
     */
    function resizeListener() {
        window.onresize = function (e) {
            toggleMenuOff();
            deActivateTextBoxMenu();
        };
    }

    /**
     * Turns the custom context menu on.
     */
    function toggleMenuOn() {
        if (menuState !== 1) {
            menuState = 1;
            menu.classList.add(contextMenuActive);
        }
    }

    /**
     * Turns the custom context menu off.
     */
    function toggleMenuOff() {
        var clickedElement = null;

        if (menuState !== 0) {
            menuState = 0;
            menu.classList.remove(contextMenuActive);
        }
    }

    /**
     * Positions the menu properly.
     *
     * @param {Object} e The event
     */
    function positionMenu(e) {
        clickCoords = getPosition(e);
        clickCoordsX = clickCoords.x;
        clickCoordsY = clickCoords.y;

        menuWidth = menu.offsetWidth + 4;
        menuHeight = menu.offsetHeight + 4;

        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;

        if ((windowWidth - clickCoordsX) < menuWidth) {
            menu.style.left = windowWidth - menuWidth + "px";
        } else {
            menu.style.left = clickCoordsX + "px";
        }

        if ((windowHeight - clickCoordsY) < menuHeight) {
            menu.style.top = windowHeight - menuHeight + "px";
        } else {
            menu.style.top = clickCoordsY + "px";
        }
    }

    /**
     * Dummy action function that logs an action when a menu item link is clicked
     *
     * @param {HTMLElement} link The link that was clicked
     */
    function menuItemListener(link) {
        action = link.getAttribute("data-action");
        if (action === "Delete") {
            var getdata = {id: taskItemInContext.getAttribute("id"), action: action};
            get(getdata);
        }
        nodeMenu(taskItemInContext, action);
        toggleMenuOff();
    }

    /**
     * Generating textBox/textOk/textCancel menu (textbox + 2 buttons) for the selected node (e).
     * @param e
     * @param action action to perform.
     */
    function nodeMenu(e, action) {
        textBox.value = null;
        if (action === null) {
            alert("Wrong action!");
        } else if ((action === "Edit") || (action === "Create")) {
            activateTextBoxMenu();
            var pos = e.childNodes[1].getBoundingClientRect();
            if (action === "Edit") {
                var leftOffset = e.childNodes[1].childNodes[0].offsetWidth;
                textBox.value = e.childNodes[1].innerText;
            } else {
                var leftOffset = e.childNodes[1].offsetWidth;
                textBox.value = "New Folder";
            }
            actionBlock.style.left = pos.left + leftOffset + 'px';
            actionBlock.style.top = pos.top + 'px';
        }
        $(".action-block__text-box").focus();
        textBox.selectionStart = textBox.value.length;
    }

    /**
     * Ajax GET request with "getdata" params
     * getdata = { param:"value",param1:"value" ... };
     * @param getdata
     */
    function get(getdata) {
        $.ajax({
            type: "get",
            cache: false,
            url: "jdbc/actionItem/",
            success: function (data) {
                if (data.result === "true") {
                    $('#jstree').jstree(true).refresh(true);
                    operationResult.innerText = getdata.action + ": Operation succeed.";
                } else {
                    $('#jstree').jstree(true).refresh(true);
                    operationResult.innerText = getdata.action + ": Operation failed.";
                }
                if(data.result === "delay"){
                    window.location.reload();
                }
            },
            data: getdata,
            dataType: "json"
        });
    }

    /**
     * Listener for buttons and checkbox.
     */
    function buttonListener() {
        $(".delay0").click(function () {
            var getdata = {action: "Delay", name: "0"};
            get(getdata);
        });
        $(".delay2").click(function () {
            var getdata = {action: "Delay", name: "2000"};
            get(getdata);
        });
        $('.action-block__text-box').keydown(function (event) {
            if ((event.keyCode === 13) && (actionBlockState !== 0)) {
                sendAction();
                deActivateTextBoxMenu();
            }
        });
        textCancel.onclick = function () {
            deActivateTextBoxMenu();
        };
        textOk.onclick = function () {
            deActivateTextBoxMenu();
            sendAction();
        };
    }
    /**
     * Function, generating data (getdata) for ajax request for Edit/Create functions.
     */
    function sendAction() {
        var getdata = {id: taskItemInContext.getAttribute("id"), action: action, name: textBox.value};
        get(getdata);
    }


    /**
     * Show menu for node.
     */
    function activateTextBoxMenu() {
        if (actionBlockState !== 1) {
            actionBlockState = 1;
            actionBlock.classList.add(actionBlockActive);
        }
    }

    /**
     * Hide menu for node.
     */
    function deActivateTextBoxMenu() {
        if (actionBlockState !== 0) {
            actionBlockState = 0;
            $(".action-block__text-box").blur();
            actionBlock.classList.remove(actionBlockActive);
        }
    }

    /**
     * Listener for drag&drop event + generating data for ajax request.
     */
    function dragAndDropListener() {
        $('#jstree').on("move_node.jstree", function (e, data) {
            var getdata = {id: data.node.id, action: "Dnd", newParent: data.parent, oldParent: data.old_parent};
            get(getdata);
        });
    }

    /**
     * Run the app.
     */
    init();
})();