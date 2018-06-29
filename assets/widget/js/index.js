/*Auto generate by UI designer */
(function($) {
    appcan.button("#nav-left", "btn-act", function() {
        alert(1);
        uexDevice.showStatusBar();
        alert(2);
    });
    appcan.button("#nav-right", "btn-act", function() {
        alert(3);
        uexDevice.hideStatusBar();
        alert(4);
    });

})($);

function openTestCase(plugin){
    appcan.window.open("case", "case/unittest.html?"+plugin, 1);
}