package org.valgog.spring.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		String url = "jdbc:postgresql://localhost/test";
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","");
		props.setProperty("ssl","false");
		this.conn = DriverManager.getConnection(url, props);

	}

	public void testMapRow() throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT 1 as id, 'Muster' as name, 'DE' as country_code, '{1,1,3,1}'::int4[] as last_marks, '{a,b,c}'::text[] as tags");
		ResultSet rs = ps.executeQuery();
		
		AnnotatedRowMapper<SimpleClass> mapper = AnnotatedRowMapper.getMapperForClass(SimpleClass.class);
		
		SimpleClass result = mapper.mapRow(rs, 1);
		
		assertNotNull(result);
	}

}
