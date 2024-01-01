/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.customnumbering;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Marko Bekhta
 */
public class ExampleNumberingTest {

	private static Asciidoctor DOCTOR;

	@BeforeClass
	public static void initAsciidoctor() {
		DOCTOR = Asciidoctor.Factory.create();
		DOCTOR.javaExtensionRegistry().treeprocessor( CustomNumberingProcessor.class );
	}

	@Test
	public void simpleExampleNumberingTest() throws URISyntaxException, IOException {
		File file = Paths.get( ExampleNumberingTest.class.getClassLoader().getResource( "test.asciidoc" ).toURI() ).toFile();
		try ( FileReader reader = new FileReader( file ) ) {
			StringWriter writer = new StringWriter();

			DOCTOR.convert( reader, writer, Options.builder().safe( SafeMode.SAFE ).build() );

			Document convertedDoc = Jsoup.parse( writer.toString() );

			// check that example numbers are constructed correctly
			Elements mainSections = convertedDoc.getElementsByClass( "sect1" );
			int sectionIndex = 1;
			for ( Element section : mainSections ) {
				assertSectionExampleNumbering(
						section.getElementsByClass( "exampleblock" ),
						"Example",
						sectionIndex,
						element -> element.child( 0 ).html()
				);
				assertSectionExampleNumbering(
						section.getElementsByTag( "table" ),
						"Table",
						sectionIndex,
						element -> element.getElementsByTag( "caption" ).html()
				);
				sectionIndex++;
			}
			//check numbering in references:
			List<Element> elements = convertedDoc.getElementsByTag( "p" ).stream()
					.filter( p -> p.html().startsWith( "As we see" ) ) // find a <p> where the reference is present.
					.collect( Collectors.toList() );
			assertThat( elements )
					.as( "should be only one element in collection" )
					.hasSize( 1 );
			assertThat( elements.get( 0 ).getElementsByTag( "a" ).html() )
					.as( "caption" )
					.startsWith( "Example 1.2" );
		}
	}

	private void assertSectionExampleNumbering(Elements elements, String captionStart, int sectionIndex, Function<Element, String> captionRetriver) {
		int index = 1;
		for ( Element element : elements ) {
			String exampleCaptionStart = String.format( "%s %d.%d: ", captionStart, sectionIndex, index++ );
			String caption = captionRetriver.apply( element );
			assertThat( caption )
					.as( "caption" )
					.startsWith( exampleCaptionStart );
		}
	}

}
