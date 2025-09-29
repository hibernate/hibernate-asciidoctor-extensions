/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.sourcecodelanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

public class SourcecodeLanguagePreprocessor extends Preprocessor {

	@Override
	public Reader process(Document document, PreprocessorReader reader) {
		List<String> lines = filterLines( reader.readLines() );
		return newReader( lines );
	}

	private List<String> filterLines(List<String> lines) {
		List<String> filteredLines = new ArrayList<>();
		for ( int i = 0; i < lines.size(); i++ ) {
			String line = lines.get( i );
			if ( line.startsWith( "[source" ) ) {
				String[] source = line.trim().substring( 1, line.length() - 1 ).split( "," );
				if ( source.length > 1 ) {
					String language = source[1];
					String lowerCasedLanguage = language.toLowerCase( Locale.ROOT );
					filteredLines.add( line.replaceFirst( language, lowerCasedLanguage ) );
					if ( !language.equals( lowerCasedLanguage ) ) {
						reportWarning( new StringBuilder( "Found a source block with an uppercase language name. " ), lines, line, i );
					}
				}
				else {
					reportWarning( new StringBuilder( "Found a source block without a language specified. " ), lines, line, i );
				}
			}
			else {
				filteredLines.add( line );
			}
		}

		return filteredLines;
	}

	private void reportWarning(StringBuilder message, List<String> lines, String line, int i) {
		message.append( "Highlighting may not work correctly. Snippet:\n" );
		// There should be at least an opening and closing ---- and maybe a line of code too... but to stay safe:
		for ( int j = 0; j + i < lines.size() && j < 4; j++ ) {
			message.append( "\n\t" ).append( lines.get( j + i ) );
		}
		log( new LogRecord( Severity.WARN, message.toString() ) );
	}
}
