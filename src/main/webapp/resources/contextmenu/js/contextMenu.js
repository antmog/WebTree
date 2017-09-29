(function () {
    "use strict";

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //
    // H E L P E R    F U N C T I O N S
    //
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //
    // C O R E    F U N C T I O N S
    //
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Variables.
     */
    var actionBlockActive = "actionBlock-active";
    var textBox = document.querySelector("#textBox");
    var textOk = document.querySelector("#textOk");
    var textCancel = document.querySelector("#textCancel");
    
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
        ButtonListener();
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
            }
        }
    }

    /**
     * Window resize event listener
     */
    function resizeListener() {
        window.onresize = function (e) {
            toggleMenuOff();
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
        if(action === "Delete"){
            get(taskItemInContext,action,textBox.value);
        }
        nodeMenu(taskItemInContext,action);
        console.log(taskItemInContext.getAttribute("id"));
        toggleMenuOff();
    };

    /**
     *
     * @param data
     */
    function nodeMenu(e,action) {
        textBox.value = null;
        if(action === null){
            alert("Wrong action!");
        }else if((action === "Edit")||(action === "Create")){
            textBox.classList.add(actionBlockActive);
            textOk.classList.add(actionBlockActive);
            textCancel.classList.add(actionBlockActive);
            var pos = e.childNodes[1].getBoundingClientRect();
            if(action === "Edit"){
                var leftOffset = e.childNodes[1].childNodes[0].offsetWidth;
                textBox.value = e.childNodes[1].innerText;
            }else{
                var leftOffset = e.childNodes[1].offsetWidth;
                textBox.value = "New Folder";
            }
            textBox.style.left = pos.left + leftOffset + 'px';
            textBox.style.top = pos.top + 'px';

            textOk.style.left = textBox.offsetLeft + textBox.offsetWidth + "px";
            textOk.style.top = textBox.style.top;

            textCancel.style.left = textOk.offsetLeft + textOk.offsetWidth + "px";
            textCancel.style.top = textOk.style.top;
        }
        
        
    }
    
    function get(e,action,nodeName){
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
            data: {id: e.getAttribute("id") , action: action, name: nodeName},
            dataType: "text"
        });
    }
    
    function ButtonListener(){
        textCancel.onclick = function () {
            textBox.classList.remove(actionBlockActive);
            textOk.classList.remove(actionBlockActive);
            textCancel.classList.remove(actionBlockActive);
        }
        textOk.onclick = function () {
            textBox.classList.remove(actionBlockActive);
            textOk.classList.remove(actionBlockActive);
            textCancel.classList.remove(actionBlockActive);
            get(taskItemInContext,action,textBox.value);
        }
    }
    
    /**
     * Run the app.
     */
    init();
})();