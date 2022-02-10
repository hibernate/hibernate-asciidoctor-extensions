/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.customnumbering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
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

	private static final List<String> LISTING_ROLES = Arrays.asList( "listing", "api" );

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
		for ( StructuralNode node : section.getBlocks() ) {
			if ( "example".equalsIgnoreCase( node.getNodeName() ) ) {
				if ( isListing(node) ) {
					updateBlockCaption( node, "Listing", indexes.getSectionNumber(), indexes.newListingIndex() );
				}
				else {
					updateBlockCaption( node, "Example", indexes.getSectionNumber(), indexes.newExampleIndex() );
				}
			}
			else if ( node instanceof Table ) {
				updateBlockCaption( node, "Table", indexes.getSectionNumber(), indexes.newTableIndex() );
			}
			else if ( node instanceof Section ) {
				processSection( (Section) node, indexes );
			}
		}
	}

	private void updateBlockCaption(StructuralNode block, String title, String sectionNumber, int indexNumber) {
		if ( !( block instanceof RubyObjectWrapper ) ) {
			throw new IllegalStateException( String.format( "%s block is not extended from RubyObjectWrapper. Processor cannot proceed.", title ) );
		}
		RubyObject rubyObject = toRubyObject( (RubyObjectWrapper) block );
		String blockNumber = getBlockNumber( sectionNumber, indexNumber );

		rubyObject.setInstanceVariable( "@caption", RubyString.newString(
				Ruby.getGlobalRuntime(),
				String.format( "%s %s: ", title, blockNumber )
		) );

		rubyObject.setInstanceVariable( "@numeral", RubyString.newString(
				Ruby.getGlobalRuntime(),
				blockNumber
		) );
	}

	private String getBlockNumber(String sectionNumber, int indexNumber) {
		return sectionNumber == NOT_NUMBERED_SECTION_NUMBER ?
				String.format( "%d", indexNumber ) :
				String.format( "%s.%d", sectionNumber, indexNumber );
	}

	private String getSectionNumber(Section section) {
		// return the section number if the section is numbered, return a default global section number if not
		// cannot use section.number() to get the number as for Appendix sections number is a Character and this method
		// will fail
		return section.isNumbered() ? section.getNumeral() : NOT_NUMBERED_SECTION_NUMBER;
	}

	private RubyObject toRubyObject(RubyObjectWrapper block) {
		return (RubyObject) block.getRubyObject();
	}

	private SectionNumberingIndexes getSectionNumberingIndexes(String sectionNumber) {
		return sectionNumberingIndexesMap.computeIfAbsent( sectionNumber, sn -> new SectionNumberingIndexes( sn ) );
	}

	private boolean isListing(StructuralNode block) {
		for ( String listingRole : LISTING_ROLES ) {
			if ( block.getRoles().contains( listingRole ) ) {
				return true;
			}
		}
		return false;
	}

	private static class SectionNumberingIndexes {

		private final String sectionNumber;
		private int listingIndex;
		private int exampleIndex;
		private int tableIndex;

		public SectionNumberingIndexes(String sectionNumber) {
			this.sectionNumber = sectionNumber;
			listingIndex = 1;
			exampleIndex = 1;
			tableIndex = 1;
		}

		public String getSectionNumber() {
			return sectionNumber;
		}

		public int newListingIndex() {
			return listingIndex++;
		}

		public int newExampleIndex() {
			return exampleIndex++;
		}

		public int newTableIndex() {
			return tableIndex++;
		}
	}
}
