package org.valgog.spring.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.valgog.spring.AnnotatedRowMapper;
import org.valgog.spring.tests.example.SimpleClass;

import junit.framework.TestCase;

public class MappingTest extends TestCase {

	private Connection conn;
	
	public MappingTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
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
			"from generate_series(1, 100) as s(i);";
		
		s.execute(SQL);
		conn.commit();
	}

	public void testMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM test.simple;");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
		}

	}

	public void testPrimitiveMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT null as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1, NULL}'::int4[] as last_marks, '{a,b,c}'::text[] as tags");
		ResultSet rs = ps.executeQuery();
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		int i = 0;
		while( rs.next() ) {
			SimpleClass result = mapper.mapRow(rs, i++);
			assertNotNull(result);
		}

	}	
	
}
