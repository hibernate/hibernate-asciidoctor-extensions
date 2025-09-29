/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.copytoclipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.Location;
import org.asciidoctor.extension.LocationType;

@Location(LocationType.FOOTER)
public class CopyToClipboardProcessor extends DocinfoProcessor {
	@Override
	public String process(Document document) {
		try (
				InputStream is = this.getClass().getResourceAsStream( "/copy/clipboard.min.js" );
				InputStreamReader isr = new InputStreamReader( is, StandardCharsets.UTF_8 );
				BufferedReader reader = new BufferedReader( isr )
		) {
			String clipboardJs = reader.lines().collect( Collectors.joining( "\n" ) );
			return String.format(
					"<script>%s</script>\n<style>%s</style>\n<script>%s</script>",
					clipboardJs,
					"pre.highlight .btn-copy{-webkit-transition:opacity 0.3s ease-in-out;-o-transition:opacity 0.3s ease-in-out;transition:opacity 0.3s ease-in-out;opacity:0;padding:2px 6px;position:absolute;right:4px;top:.225rem;background-color:#fff0;border:none}.listingblock:hover .btn-copy,pre.highlight .btn-copy:focus{opacity:.5}pre.highlight{position:relative}.listingblock code[data-lang]::before{display:block;content:attr(data-lang) '|';right:1.5rem;-webkit-transition:opacity 0.3s ease-in-out;-o-transition:opacity 0.3s ease-in-out;transition:opacity 0.3s ease-in-out;opacity:0}.listingblock:hover code[data-lang]::before{opacity:.5}.tooltip-text{display:block;position:absolute;top:1.5rem;right:4px;background:#444;color:#fff;padding:6px 10px;border-radius:4px;white-space:nowrap;z-index:1;opacity:0;font-size:11px} .tooltip-text span { color: #fff; } .listingblock:hover .tooltip-text.show{opacity:1}",
					"const codes=document.querySelectorAll('pre.highlight > code');let index=0;codes.forEach((code)=>{code.setAttribute(\"id\",\"code\"+index);const block=document.createElement('div');block.className=\"tooltip\";const btn=document.createElement('button');btn.className=\"btn-copy fa-regular fa-copy\";btn.setAttribute(\"data-clipboard-action\",\"copy\");btn.setAttribute(\"data-clipboard-target\",\"#code\"+index);btn.setAttribute(\"title\",\"Copy to clipboard\");btn.setAttribute(\"float-right\",\"true\");code.before(btn);const tooltip=document.createElement('div');tooltip.className=\"tooltip-text\";tooltip.textContent=\"Copied!\";code.before(tooltip);index++});const clipboard=new ClipboardJS('.btn-copy');clipboard.on('success',function(e){e.clearSelection();e.trigger.className=e.trigger.className.replace(\"fa-copy\",\"fa-check\").replace(\"fa-regular \",\"fa \");e.trigger.setAttribute(\"title\",\"Copied!\");e.trigger.nextSibling.classList.toggle(\"show\");e.trigger.blur();setTimeout(function(){e.trigger.className=e.trigger.className.replace(\"fa-check\",\"fa-copy\").replace(\"fa \",\"fa-regular \");e.trigger.setAttribute(\"title\",\"Copy to clipboard\");e.trigger.nextSibling.classList.toggle(\"show\")},1500)})"
			);
		}
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
}
