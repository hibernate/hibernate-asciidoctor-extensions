/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.savepreprocessed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;

/**
 * Preprocessor used to save the preprocessed output of the asciidoctor conversion. It allows to generate a single file
 * integrating all the includes.
 *
 * @author Guillaume Smet
 */
public class SavePreprocessedOutputPreprocessor extends Preprocessor {

	private static final String OUTPUT_FILE = "target/preprocessed/beanvalidation-specification-full.adoc";

	private static final List<String> FILTER_LICENSE_MARKERS = Arrays.asList( "[preface]", "<<<" );
	private static final String COMMENT_MARKER = "//";
	private static final String SOURCE_MARKER = "[source";
	private static final List<String> SECTION_MARKERS = Arrays.asList( "----", "...." );

	public SavePreprocessedOutputPreprocessor(Map<String, Object> config) {
		super( config );
	}

	@Override
	public Reader process(Document document, PreprocessorReader reader) {
		try {
			Path filePath = Paths.get( OUTPUT_FILE );
			Files.createDirectories( filePath.getParent() );
			List<String> lines = filterLines( reader.readLines() );
			Files.write( filePath, lines );
			return newReader( lines );
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to write the preprocessed file " + OUTPUT_FILE, e );
		}
	}

	/**
	 * This is used to filter out the license headers that are just after the markers defined. It also reindents the
	 * source code with spaces to be consistent with the 1.1 spec.
	 */
	private List<String> filterLines(List<String> lines) {
		List<String> filteredLines = new ArrayList<>();

		ParsingState state = ParsingState.NORMAL;
		for ( String line : lines ) {
			switch ( state ) {
				case NORMAL:
					if ( FILTER_LICENSE_MARKERS.contains( line ) ) {
						state = ParsingState.FILTER_LICENSE;
					}
					else if ( line.startsWith( SOURCE_MARKER ) ) {
						state = ParsingState.SOURCE;
					}
					break;
				case SOURCE:
					if ( SECTION_MARKERS.contains( line ) ) {
						state = ParsingState.SOURCE_CONTENT;
					}
					else {
						throw new IllegalStateException( "[source] requires to be followed by a section marker" );
					}
					break;
				case SOURCE_CONTENT:
					if ( SECTION_MARKERS.contains( line ) ) {
						state = ParsingState.NORMAL;
					}
					else {
						filteredLines.add( line.replaceAll( "\t", "    " ) );
						continue;
					}
					break;
				case FILTER_LICENSE:
					if ( line.startsWith( COMMENT_MARKER ) ) {
						continue;
					}
					else {
						state = ParsingState.NORMAL;
					}
					break;
			}
			filteredLines.add( line );
		}

		return filteredLines;
	}

	private enum ParsingState {
		NORMAL, FILTER_LICENSE, SOURCE, SOURCE_CONTENT,
	}

}
