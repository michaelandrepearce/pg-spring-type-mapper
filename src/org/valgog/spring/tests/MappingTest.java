package org.valgog.spring.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.valgog.spring.AnnotatedRowMapper;
import org.valgog.spring.tests.example.ComplexEmbed;
import org.valgog.spring.tests.example.ParentClass;
import org.valgog.spring.tests.example.SimpleClass;
import org.valgog.spring.tests.example.ExtendedClass;
import org.valgog.spring.tests.example.SimpleRowClass;
import org.valgog.spring.tests.example.WithEmbed;

public class MappingTest {
	
	private static final Logger LOG = Logger.getLogger(MappingTest.class);

	private Connection conn;
	
	@Before
	public void setUp() throws Exception {
		// Get connection
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/postgres";
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","postgres");
		this.conn = DriverManager.getConnection(url, props);
		createTestTables(this.conn);
	}

	private void createTestTables(Connection conn) throws SQLException {
		Statement s = conn.createStatement();
		
		final String SQL = 
			"DROP SCHEMA IF EXISTS test CASCADE; \n" +
			"CREATE SCHEMA test; \n" +
			"CREATE TABLE test.simple ( \n" +
			"  id integer, \n" +
			"  name text, \n" +
			"  country_code text, \n" +
			"  last_marks int[], \n" +
			"  tags text[] \n" +
			"); \n" +
			"insert into test.simple \n" +
			"select s.i, 'name' || s.i, 'DE', ARRAY[ (random()*100)::integer,(random()*100)::integer,(random()*100)::integer,(random()*100)::integer ], '{a,b,c,d}'::text[] \n" +
			"from generate_series(1, 100) as s(i);" +
			"CREATE TYPE test.simple_type AS ( \n" +
			"  id integer, \n" +
			"  name text, \n" +
			"  country_code text, \n" +
			"  last_marks int[] \n" +
			"); \n" +
			"CREATE TYPE test.child_child_type AS ( \n" +
			"  id integer\n" +
			"); \n" +
			"CREATE TYPE test.child_type AS ( \n" +
			"  id integer,\n" +
			"  child test.child_child_type,\n" +
			"  children test.child_child_type[]\n" +
			"); \n" +			
			"CREATE TYPE test.with_embed AS ( \n" +
			"  x integer,\n" +
			"  y integer,\n" +
			"  z integer\n" +
			"); \n" +
			"CREATE TYPE test.complex_embed AS ( \n" +
			"  x integer,\n" +
			"  embed test.with_embed\n" +
			"); \n" ;
;
;
		s.execute(SQL);
		conn.commit();
	}
	
	
	@Test
	public final void testMapSimpleRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM test.simple;");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		Integer i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
			assertEquals(i, result.getId());
			assertThat(new String[] {"a","b","c","d"}, is(result.getTags()));
		}
	}

	@Test
	public final void testMapExtendedRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT *, name || ' as full name' as e_full_name FROM test.simple;");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<ExtendedClass> mapper = AnnotatedRowMapper.getMapperForClass(ExtendedClass.class);
		int i = 0;
		while( rs.next() ) {
			ExtendedClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
			assertThat("name" + Integer.toString(i) + " as full name", is(result.getFullName()));
		}
	}
	
	
	@Test()
	public void testSingleMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT 1 as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1,5}'::int4[] as last_marks, '{a,b,c,NULL,e}'::text[] as tags, NULL::text[] as generic_tags");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
			assertEquals( Integer.valueOf(1), result.getId());
			assertThat("Muster", is(result.getName()));
			assertThat("DE", is(result.getCountryCode()));
			assertThat(new int[] {1, 1, 3, 1, 5}, is(result.getLastMarks()));
			assertThat(new String[] {"a","b","c",null,"e"}, is(result.getTags()));
		}

	}	
	
	@Test
	public void testPrimitiveMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT NULL as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1, NULL}'::int4[] as last_marks, '{a,b,c}'::text[] as tags");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			LOG.info(result);
		}

	}	

	@Test()
	public void testRowMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT ROW(1,'a','DE',NULL)::test.simple_type as st");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleRowClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleRowClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleRowClass row = mapper.mapRow(rs, i++);
			assertNotNull(row);
			SimpleClass simpleObject = row.getSimpleObject();
			assertNotNull(simpleObject);
			assertThat(simpleObject.getId(), is( 1 ));
			assertThat(simpleObject.getName(), is ( "a" ));
			assertThat(simpleObject.getCountryCode(), is ( "DE" ));
			assertNull(simpleObject.getLastMarks());
			assertNull(simpleObject.getTags());
		}
	}	
	
	@Test()
	public void testRowMapRowComplex() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT ARRAY[ROW(1, null, null)::test.child_type, ROW(2, ROW(1), null)::test.child_type, ROW(2, ROW(1), ARRAY[ROW(1)]::test.child_child_type[])::test.child_type]::test.child_type[] as children, ARRAY[ROW(1, null, null)::test.child_type, ROW(2, ROW(1), null)::test.child_type, ROW(2, ROW(1), ARRAY[ROW(1)]::test.child_child_type[])::test.child_type]::test.child_type[] as set");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<ParentClass> mapper = AnnotatedRowMapper.getMapperForClass(ParentClass.class);
		int i = 0;
		while( rs.next() ) {
			ParentClass row = mapper.mapRow(rs, i++);
			assertNotNull(row);
			assertThat(3, is(row.getChildren().size()));
			assertThat(3, is(row.getChildrenSet().size()));
			assertNotNull(row.getChildren().get(0).getId());
			assertNull(row.getChildren().get(0).getChild());
			assertNotNull(row.getChildren().get(1).getChild());
			assertNotNull(row.getChildren().get(1).getChild().getId());
			assertNotNull(row.getChildren().get(2).getChildren());
			assertThat(1, is(row.getChildren().get(2).getChildren().size()));
			assertThat(1, is(row.getChildren().get(2).getChildren().get(0).getId()));
		}
	}		
	
	@Test()
	public void testEmbedWithNameNoRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT 1 as x, 2 as y, 3 as z");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<WithEmbed> mapper = AnnotatedRowMapper.getMapperForClass(WithEmbed.class);
		int i = 0;
		while( rs.next() ) {
			WithEmbed result = mapper.mapRow(rs, i++);
			assertNotNull(result.getEmbed());
			assertThat(1, is(result.getEmbed().getX()));
			assertThat(2, is(result.getEmbed().getY()));
			assertThat(3, is(result.getZ()));
		}
	}
	
	
	@Test()
	public void testComplexEmbed() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT 1 as x, ROW(1,2,3)::test.with_embed  as embed");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<ComplexEmbed> mapper = AnnotatedRowMapper.getMapperForClass(ComplexEmbed.class);
		int i = 0;
		while( rs.next() ) {
			ComplexEmbed result = mapper.mapRow(rs, i++);
			assertNotNull(result.getWithEmbed());
			assertThat(1, is(result.getWithEmbed().getEmbed().getX()));
			assertThat(2, is(result.getWithEmbed().getEmbed().getY()));
			assertThat(3, is(result.getWithEmbed().getZ()));
		}
	}		
	

}
