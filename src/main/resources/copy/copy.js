/*
* This is an adapted version of https://github.com/quarkusio/quarkusio.github.io/blob/317db07ca629dc331455692d2976ecece95319cd/assets/javascript/copy.js
*
* NOTE: These styles are minified and added as a simple string to CopyToClipboardProcessor.
* To make changes:
*  - update this file
*  - minify the content
*  - add the minified content to the `CopyToClipboardProcessor`
*
*/
const codes = document.querySelectorAll('pre.highlight > code');
let index = 0;
codes.forEach((code) => {

    code.setAttribute("id", "code" + index);

    const block = document.createElement('div');
    block.className = "tooltip";

    const btn = document.createElement('button');
    btn.className = "btn-copy fa-regular fa-copy";
    btn.setAttribute("data-clipboard-action", "copy");
    btn.setAttribute("data-clipboard-target", "#code" + index);
    btn.setAttribute("title", "Copy to clipboard");
    btn.setAttribute("float-right", "true");
    code.before(btn);

    const tooltip = document.createElement('div');
    tooltip.className = "tooltip-text";
    tooltip.textContent = "Copied!";
    code.before(tooltip);

    index++;
});

const clipboard = new ClipboardJS('.btn-copy');
clipboard.on('success', function (e) {
    e.clearSelection();
    e.trigger.className = e.trigger.className.replace("fa-copy", "fa-check").replace("fa-regular ", "fa ");
    e.trigger.setAttribute("title", "Copied!");
    e.trigger.nextSibling.classList.toggle("show");
    e.trigger.blur();

    setTimeout(function () {
        e.trigger.className = e.trigger.className.replace("fa-check", "fa-copy").replace("fa ", "fa-regular ");
        e.trigger.setAttribute("title", "Copy to clipboard");
        e.trigger.nextSibling.classList.toggle("show");
    }, 1500);
});
