/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.customnumbering;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.Treeprocessor;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyString;

/**
 * Treeprocessor used to change how example and table captions are numbered.
 * By using this processor numbers will be in format {@code {section_number}.{example_number} },
 * where {@code example_number} is reset for each section.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class CustomNumberingProcessor extends Treeprocessor {

	private static final String NOT_NUMBERED_SECTION_NUMBER = "-1";

	private final Map<String, SectionNumberingIndexes> sectionNumberingIndexesMap = new HashMap<>();

	public CustomNumberingProcessor() {
	}

	public CustomNumberingProcessor(Map<String, Object> config) {
		super( config );
	}

	@Override
	public Document process(Document document) {
		document.getBlocks().stream()
				.filter( block -> block instanceof Section )
				.forEach( block -> processSection( (Section) block, getSectionNumberingIndexes( getSectionNumber( (Section) block ) ) ) );
		return document;
	}

	private void processSection(Section section, SectionNumberingIndexes indexes) {
		for ( AbstractBlock block : section.getBlocks() ) {
			if ( "example".equalsIgnoreCase( block.getNodeName() ) ) {
				updateBlockCaption( block, "Example", indexes.getSectionNumber(), indexes.newExampleIndex() );
			}
			else if ( block instanceof Table ) {
				updateBlockCaption( block, "Table", indexes.getSectionNumber(), indexes.newTableIndex() );
			}
			else if ( block instanceof Section ) {
				processSection( (Section) block, indexes );
			}
		}
	}

	private void updateBlockCaption(AbstractBlock block, String title, String sectionNumber, int indexNumber) {
		RubyObject rubyObject = toRubyObject( block );

		rubyObject.setInstanceVariable( "@caption", RubyString.newString(
				Ruby.getGlobalRuntime(),
				sectionNumber == NOT_NUMBERED_SECTION_NUMBER ?
						String.format( "%s %d: ", title, indexNumber ) :
						String.format( "%s %s.%d: ", title, sectionNumber, indexNumber )
		) );
	}

	private String getSectionNumber(Section section) {
		// return the section number if the section is numbered, return a default global section number if not
		// cannot use section.number() to get the number as for Appendix sections number is a Character and this method
		// will fail
		return section.numbered() ? toRubyObject( section ).getInstanceVariable( "@number" ).toString() : NOT_NUMBERED_SECTION_NUMBER;
	}

	private RubyObject toRubyObject(AbstractBlock block) {
		try {
			Field f = block.delegate().getClass().getDeclaredFields()[1];
			f.setAccessible( true );
			return (RubyObject) f.get( ( block.delegate() ) );
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException( "Is not able to convert to RubyObject. Processor cannot proceed.", e );
		}
	}

	private SectionNumberingIndexes getSectionNumberingIndexes(String sectionNumber) {
		return sectionNumberingIndexesMap.computeIfAbsent( sectionNumber, sn -> new SectionNumberingIndexes( sn ) );
	}

	private static class SectionNumberingIndexes {

		private final String sectionNumber;
		private int exampleIndex;
		private int tableIndex;

		public SectionNumberingIndexes(String sectionNumber) {
			this.sectionNumber = sectionNumber;
			exampleIndex = 1;
			tableIndex = 1;
		}

		public String getSectionNumber() {
			return sectionNumber;
		}

		public int newExampleIndex() {
			return exampleIndex++;
		}

		public int newTableIndex() {
			return tableIndex++;
		}
	}
}
