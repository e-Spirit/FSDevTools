package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.SchemaIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.espirit.moddev.cli.api.parsing.parser.SchemaIdentifierParser.CUSTOM_PREFIX_SCHEMA_OPTION;
import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class SchemaIdentifierParserTest {
	@DataPoints
	public static List[] testcases =
			new List[]{Arrays.asList(CUSTOM_PREFIX_SCHEMA_OPTION + ":Products[exportallentities]"),
					Arrays.asList("schema: SimpleSchema"), // just using CUSTOM_PREFIX_SCHEMA_OPTION to keep this list more readable
					Arrays.asList("schema: Products[ExportAllEntities]"),
					Arrays.asList("schema: Products[ExportAllEntities=true]"),
					Arrays.asList("schema: names[ExportAllEntities=false]"),
					Arrays.asList("schema :names[ExportGidMapping]"),
					Arrays.asList("schema :names[ExportGidMapping=false]"),
					Arrays.asList("schema : names[addEntity=entityId]")};

	private SchemaIdentifierParser testling;

	@Before
	public void setUp() {
		testling = new SchemaIdentifierParser();
	}

	@After
	public void resetValidOptions() {
		SchemaIdentifier.resetValidSchemaOptions();
	}

	@Theory
	public void testAppliesTo(List<String> uids) {
		for (String current : uids) {
			boolean appliesTo = testling.appliesTo(current);
			Assert.assertTrue("Parser should apply to string " + current, appliesTo);
		}
	}

	@Test
	public void parse() {
		List<SchemaIdentifier> result = testling.parse(Collections.singletonList("entities:xyz"));
		assertEquals(1, result.size());
		assertEquals(new SchemaIdentifier("xyz", Collections.emptyMap()), result.get(0));
	}

	@Test
	public void parseWithOptions() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(Arrays.asList("option1", "option2", "option3"));
		List<SchemaIdentifier> result = testling.parse(Collections.singletonList("entities:xyz[option1=switch1|option2=switch2|option3=switch3]"));
		assertEquals(1, result.size());
		assertEquals(getExpectedSchemaIdentifier(), result.get(0));
	}

	@Test
	public void parseSchemaOptions() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(Arrays.asList("option1", "option2", "option3WithoutEquals", "option4WithoutValue", "option5", "option6"));
		final String entitiesUidWithOptions = "[option1=switch1|option2=switch2|option3WithoutEquals|option4WithoutValue=|||option5=afterTriplePipe|option6=normal_again]";
		Map<String, String> schemaOptionsMap = SchemaIdentifierParser.parseSchemaOptions(entitiesUidWithOptions);
		assertEquals("switch1", schemaOptionsMap.get("option1"));
		assertEquals("switch2", schemaOptionsMap.get("option2"));
		Assert.assertNull("", schemaOptionsMap.get("option3WithoutEquals"));
		Assert.assertNull("", schemaOptionsMap.get("option4WithoutValue"));
		assertEquals("afterTriplePipe", schemaOptionsMap.get("option5"));
		assertEquals("normal_again", schemaOptionsMap.get("option6"));
	}

	@Test
	public void parseSchemaOptions_exportGidMapping() {
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[exportgidMapping=true]");
			assertEquals(1, options.size());
			assertEquals("true", options.values().iterator().next());
		}
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[exportGidMapping=false]");
			assertEquals(1, options.size());
			assertEquals("false", options.values().iterator().next());
		}
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[EXPORTGIDMAPPING=unknown]");
			assertEquals(1, options.size());
			assertEquals("unknown", options.values().iterator().next());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_invalidOption() {
		SchemaIdentifierParser.parseSchemaOptions("[option1=switch1]");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test1() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions("option1=switch1").size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test2() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions("[option1=switch1").size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test3() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions("option1=switch1]").size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test4() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions(" [option1=switch1]").size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test5() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions("[option1=switch1] ").size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseSchemaOptions_illegalInputFormat_test6() {
		assertEquals(0, SchemaIdentifierParser.parseSchemaOptions(" [option1=switch1] ").size());
	}

	@Test
	public void getSchemaIdentifier() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(Arrays.asList("option1", "option2", "option3"));
		SchemaIdentifier identifierWithOptions = SchemaIdentifierParser.getSchemaIdentifier("xyz[option1=switch1|option2=switch2|option3=switch3]");
		assertEquals(getExpectedSchemaIdentifier(), identifierWithOptions);

		SchemaIdentifier identifierWithoutOptions = SchemaIdentifierParser.getSchemaIdentifier("entitiesUidNoDelimiterBracket");
		assertEquals(new SchemaIdentifier("entitiesUidNoDelimiterBracket", Collections.emptyMap()), identifierWithoutOptions);
	}

	private SchemaIdentifier getExpectedSchemaIdentifier() {
		HashMap<String, String> expectedSchemaOptions = new HashMap<>();
		expectedSchemaOptions.put("option1", "switch1");
		expectedSchemaOptions.put("option2", "switch2");
		expectedSchemaOptions.put("option3", "switch3");
		return new SchemaIdentifier("xyz", expectedSchemaOptions);
	}

	@Test
	public void testDontApplyTo() {
		Assert.assertFalse(testling.appliesTo("asdasd"));
	}

	@Test
	public void testDontApplyToStartsWithSchemaIdentifier() {
		Assert.assertFalse(testling.appliesTo("schemaaasd:asd"));
	}

}
