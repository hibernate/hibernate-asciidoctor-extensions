/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.copytoclipboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

public class CopyToClipboardProcessorTest {

	private static final String CONTENT = "= Test\n:docinfo:\n\n[source,java]\n----\nSystem.out.println( \"test\" );\n----\n";

	@Test
	public void htmlBackendsIncludeCopyToClipboardSupport() {
		for ( String backend : Arrays.asList( "html5", "xhtml5" ) ) {
			assertThat( convert( backend, new ArrayList<>() ) )
					.as( backend )
					.contains( "ClipboardJS", "btn-copy", "tooltip-text" );
		}
	}

	@Test
	public void nonHtmlBackendsAreSkipped() {
		List<LogRecord> logRecords = new ArrayList<>();

		String result = convert( "docbook5", logRecords );

		assertThat( result )
				.doesNotContain( "ClipboardJS", "btn-copy", "tooltip-text" );
		assertThat( logRecords )
				.anySatisfy( logRecord -> {
					assertThat( logRecord.getSeverity() ).isEqualTo( Severity.INFO );
					assertThat( logRecord.getMessage() ).contains( "only applies to HTML documents, skipping" );
				} );
	}

	private static String convert(String backend, List<LogRecord> logRecords) {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		try {
			asciidoctor.javaExtensionRegistry().docinfoProcessor( CopyToClipboardProcessor.class );
			asciidoctor.registerLogHandler( logRecords::add );
			return asciidoctor.convert( CONTENT, Options.builder()
					.backend( backend )
					.standalone( true )
					.safe( SafeMode.SAFE )
					.build() );
		}
		finally {
			asciidoctor.shutdown();
		}
	}
}
