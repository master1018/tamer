public class FileFilter {
    private static final String LOGTAG = "FileFilter";
    public static boolean ignoreTest(String file) {
        for (int i = 0; i < ignoreTestList.length; i++) {
            if (file.endsWith(ignoreTestList[i])) {
                Log.v(LOGTAG, "File path in list of ignored tests: " + file);
                return true;
            }
        }
        return false;
    }
    public static boolean isNonTestDir(String file) {
        for (int i = 0; i < nonTestDirs.length; i++) {
            if (file.endsWith(nonTestDirs[i])) {
                return true;
            }
        }
        return false;
    }
    public static boolean ignoreResult(String file) {
        for (int i = 0; i < ignoreResultList.size(); i++) {
            if (file.endsWith(ignoreResultList.get(i))) {
                Log.v(LOGTAG, "File path in list of ignored results: " + file);
                return true;
            }
        }
        return false;
    }
    final static Vector<String> ignoreResultList = new Vector<String>();
    static {
        fillIgnoreResultList();
    }
    static final String[] nonTestDirs = {
        ".", 
        "resources", 
        ".svn", 
        "platform"  
    };
    static final String[] ignoreTestList = {
        "editing/selection/move-left-right.html", 
        "fast/js/regexp-charclass-crash.html", 
        "fast/regex/test1.html", 
        "fast/regex/slow.html" 
    };
    static void fillIgnoreResultList() {
        ignoreResultList.add("http/tests/appcache/empty-manifest.html"); 
        ignoreResultList.add("http/tests/appcache/foreign-iframe-main.html"); 
        ignoreResultList.add("http/tests/appcache/manifest-with-empty-file.html"); 
        ignoreResultList.add("storage/database-lock-after-reload.html"); 
        ignoreResultList.add("storage/hash-change-with-xhr.html"); 
        ignoreResultList.add("dom/svg/level3/xpath"); 
        ignoreResultList.add("fast/workers"); 
        ignoreResultList.add("fast/xpath"); 
        ignoreResultList.add("http/tests/eventsource/workers"); 
        ignoreResultList.add("http/tests/workers"); 
        ignoreResultList.add("http/tests/xmlhttprequest/workers"); 
        ignoreResultList.add("storage/domstorage/localstorage/private-browsing-affects-storage.html"); 
        ignoreResultList.add("storage/domstorage/sessionstorage/private-browsing-affects-storage.html"); 
        ignoreResultList.add("storage/private-browsing-readonly.html"); 
        ignoreResultList.add("websocket/tests/workers"); 
        ignoreResultList.add("fast/css/case-transform.html"); 
        ignoreResultList.add("fast/dom/Element/offsetLeft-offsetTop-body-quirk.html"); 
        ignoreResultList.add("fast/dom/Window/Plug-ins.html"); 
        ignoreResultList.add("fast/dom/Window/window-properties.html"); 
        ignoreResultList.add("fast/dom/Window/window-screen-properties.html"); 
        ignoreResultList.add("fast/dom/Window/window-xy-properties.html"); 
        ignoreResultList.add("fast/dom/attribute-namespaces-get-set.html"); 
        ignoreResultList.add("fast/dom/gc-9.html"); 
        ignoreResultList.add("fast/dom/global-constructors.html"); 
        ignoreResultList.add("fast/dom/object-embed-plugin-scripting.html"); 
        ignoreResultList.add("fast/dom/tabindex-clamp.html"); 
        ignoreResultList.add("fast/events/anchor-image-scrolled-x-y.html"); 
        ignoreResultList.add("fast/events/arrow-navigation.html"); 
        ignoreResultList.add("fast/events/capture-on-target.html"); 
        ignoreResultList.add("fast/events/dblclick-addEventListener.html"); 
        ignoreResultList.add("fast/events/drag-in-frames.html"); 
        ignoreResultList.add("fast/events/drag-outside-window.html"); 
        ignoreResultList.add("fast/events/event-view-toString.html"); 
        ignoreResultList.add("fast/events/frame-click-focus.html"); 
        ignoreResultList.add("fast/events/frame-tab-focus.html"); 
        ignoreResultList.add("fast/events/iframe-object-onload.html"); 
        ignoreResultList.add("fast/events/input-image-scrolled-x-y.html"); 
        ignoreResultList.add("fast/events/mouseclick-target-and-positioning.html"); 
        ignoreResultList.add("fast/events/mouseover-mouseout.html"); 
        ignoreResultList.add("fast/events/mouseover-mouseout2.html"); 
        ignoreResultList.add("fast/events/mouseup-outside-button.html"); 
        ignoreResultList.add("fast/events/mouseup-outside-document.html"); 
        ignoreResultList.add("fast/events/onclick-list-marker.html"); 
        ignoreResultList.add("fast/events/ondragenter.html"); 
        ignoreResultList.add("fast/events/onload-webkit-before-webcore.html"); 
        ignoreResultList.add("fast/events/option-tab.html"); 
        ignoreResultList.add("fast/events/window-events-bubble.html"); 
        ignoreResultList.add("fast/events/window-events-bubble2.html"); 
        ignoreResultList.add("fast/events/window-events-capture.html"); 
        ignoreResultList.add("fast/forms/drag-into-textarea.html"); 
        ignoreResultList.add("fast/forms/focus-control-to-page.html"); 
        ignoreResultList.add("fast/forms/focus2.html"); 
        ignoreResultList.add("fast/forms/form-data-encoding-2.html"); 
        ignoreResultList.add("fast/forms/form-data-encoding.html"); 
        ignoreResultList.add("fast/forms/input-appearance-maxlength.html"); 
        ignoreResultList.add("fast/forms/input-select-on-click.html"); 
        ignoreResultList.add("fast/forms/listbox-onchange.html"); 
        ignoreResultList.add("fast/forms/listbox-selection.html"); 
        ignoreResultList.add("fast/forms/onselect-textarea.html"); 
        ignoreResultList.add("fast/forms/onselect-textfield.html"); 
        ignoreResultList.add("fast/forms/plaintext-mode-1.html"); 
        ignoreResultList.add("fast/forms/search-cancel-button-mouseup.html"); 
        ignoreResultList.add("fast/forms/search-event-delay.html"); 
        ignoreResultList.add("fast/forms/select-empty-list.html"); 
        ignoreResultList.add("fast/forms/select-type-ahead-non-latin.html"); 
        ignoreResultList.add("fast/forms/selected-index-assert.html"); 
        ignoreResultList.add("fast/forms/selection-functions.html"); 
        ignoreResultList.add("fast/forms/textarea-appearance-wrap.html"); 
        ignoreResultList.add("fast/forms/textarea-initial-caret-position.html"); 
        ignoreResultList.add("fast/forms/textarea-no-scroll-on-blur.html"); 
        ignoreResultList.add("fast/forms/textarea-paste-newline.html"); 
        ignoreResultList.add("fast/forms/textarea-scrolled-endline-caret.html"); 
        ignoreResultList.add("fast/frames/iframe-window-focus.html"); 
        ignoreResultList.add("fast/frames/frameElement-widthheight.html"); 
        ignoreResultList.add("fast/frames/frame-js-url-clientWidth.html"); 
        ignoreResultList.add("fast/html/tab-order.html"); 
        ignoreResultList.add("fast/js/navigator-mimeTypes-length.html"); 
        ignoreResultList.add("fast/js/string-capitalization.html"); 
        ignoreResultList.add("fast/loader/local-JavaScript-from-local.html"); 
        ignoreResultList.add("fast/loader/local-iFrame-source-from-local.html"); 
        ignoreResultList.add("fast/loader/opaque-base-url.html"); 
        ignoreResultList.add("fast/overflow/scroll-vertical-not-horizontal.html"); 
        ignoreResultList.add("fast/parser/script-tag-with-trailing-slash.html"); 
        ignoreResultList.add("fast/replaced/image-map.html"); 
        ignoreResultList.add("fast/text/plain-text-line-breaks.html"); 
        ignoreResultList.add("profiler"); 
        ignoreResultList.add("svg"); 
    }
}
