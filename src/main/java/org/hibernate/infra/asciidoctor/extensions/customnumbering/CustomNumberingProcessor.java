/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.customnumbering;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
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
 */
public class CustomNumberingProcessor extends Treeprocessor {

	public CustomNumberingProcessor() {
		super();
	}

	public CustomNumberingProcessor(Map<String, Object> config) {
		super( config );
	}

	@Override
	public Document process(Document document) {
		document.getBlocks().stream()
				.filter( abstractBlock -> "section".equalsIgnoreCase( abstractBlock.getNodeName() ) )
				.forEach( section -> processSection( section, NumberingIndexes.get( getSectionNumber( section ) ) ) );
		return document;
	}

	private void processSection(AbstractBlock section, NumberingIndexes indexes) {
		for ( AbstractBlock block : section.getBlocks() ) {
			if ( "example".equalsIgnoreCase( block.getNodeName() ) ) {
				updateBlockCaption( block, "Example", indexes.getSectionNumber(), indexes.getExample().getAndIncrement() );
			}
			else if ( "table".equalsIgnoreCase( block.getNodeName() ) ) {
				updateBlockCaption( block, "Table", indexes.getSectionNumber(), indexes.getTable().getAndIncrement() );
			}
			else if ( "section".equalsIgnoreCase( block.getNodeName() ) ) {
				processSection( block, indexes );
			}
		}
	}

	private void updateBlockCaption(AbstractBlock block, String title, int sectionNumber, int indexNumber) {
		RubyObject rubyObject = toRubyObject( block );

		rubyObject.setInstanceVariable( "@caption", RubyString.newString(
				Ruby.getGlobalRuntime(),
				String.format( "%s %d.%d: ", title, sectionNumber, indexNumber )
		) );
	}

	private int getSectionNumber(AbstractBlock block) {
		// this would only work if :sectnums: is turned on - otherwise for each section it will reset
		// number to 1
		RubyObject rubyObject = toRubyObject( block );
		return (int) rubyObject.getInstanceVariable( "@number" ).toJava( Integer.class );
	}

	private RubyObject toRubyObject(AbstractBlock block) {
		try {
			Field f = block.delegate().getClass().getDeclaredFields()[1];
			f.setAccessible( true );
			return  (RubyObject) f.get( ( block.delegate() ) ) ;
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException( "Is not able to convert to RubyObject. Processor cannot proceed.", e );
		}
	}

	private static class NumberingIndexes {

		private AtomicInteger example;
		private AtomicInteger table;
		private final int sectionNumber;

		private NumberingIndexes(int sectionNumber) {
			this.sectionNumber = sectionNumber;
			example = new AtomicInteger( 1 );
			table = new AtomicInteger( 1 );
		}

		public AtomicInteger getExample() {
			return example;
		}

		public AtomicInteger getTable() {
			return table;
		}

		public int getSectionNumber() {
			return sectionNumber;
		}

		public static NumberingIndexes get(int sectionNumber) {
			return new NumberingIndexes( sectionNumber );
		}
	}

}
