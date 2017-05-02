/*
 * Hibernate Infra - Asciidoctor extensions
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.infra.asciidoctor.extensions.customnumbering;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.asciidoctor.Asciidoctor;
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
			DOCTOR.convert( reader, writer, OptionsBuilder.options().safe( SafeMode.SAFE ).asMap() );

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

		}
	}

	private void assertSectionExampleNumbering(Elements elements, String captionStart, int sectionIndex, Function<Element, String> captionRetriver) {
		int index = 1;
		for ( Element element : elements ) {
			String exampleCaptionStart = String.format( "%s %d.%d: ", captionStart, sectionIndex, index++ );
			String caption = captionRetriver.apply( element );
			Assert.assertTrue( String.format( "Caption should start with %s", exampleCaptionStart ),
					caption.startsWith( exampleCaptionStart )
			);
		}
	}

}
