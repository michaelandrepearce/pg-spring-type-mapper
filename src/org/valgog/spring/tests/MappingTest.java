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

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.valgog.spring.AnnotatedRowMapper;
import org.valgog.spring.tests.example.SimpleClass;
import org.valgog.spring.tests.example.ExtendedClass;
import org.valgog.spring.tests.example.SimpleRowClass;

public class MappingTest {

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
			"); \n" ;
		s.execute(SQL);
		conn.commit();
	}
	
	
	@Test
	public final void testMapSimpleRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM test.simple;");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
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
		
		PreparedStatement ps = conn.prepareStatement("SELECT 1 as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1,5}'::int4[] as last_marks, '{a,b,c,NULL,e}'::text[] as tags");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
			assertEquals(1, result.getId());
			assertThat("Muster", is(result.getName()));
			assertThat("DE", is(result.getCountryCode()));
			assertThat(new int[] {1, 1, 3, 1, 5}, is(result.getLastMarks()));
			assertThat(new String[] {"a","b","c",null,"e"}, is(result.getTags()));
		}

	}	
	
	@Test(expected=SQLException.class)
	public void testPrimitiveMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT NULL as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1, NULL}'::int4[] as last_marks, '{a,b,c}'::text[] as tags");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			mapper.mapRow(rs, i++);
			// should throw an exception
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


}
