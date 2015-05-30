Helper classes to make Spring JDBC Row Mapping easier using annotations.

The library implements an `AnnotatedRowMapper` class, that is aware of annotated class fields.

This can make automate a mapping process for complex and/bulky JDBC result sets to java objects.

This library is tested only for PostgreSQL JDBC driver and contains code, that is bound to PostgreSQL types and type mappings.

For examples of the usage see the test cases in class `org.valgog.spring.tests.MappingTest`

**Done**:

  * Automatic mapping of annotated fields of primitive and simple types and arrays of these types. (Simple types, in this context, are the database types, that are defined by the PostgreSQL JDBC driver to be mappable to Java classes using `ResultSet.getObject()` method;
  * Mapping of the annotated fields of the super classes being mapped;
  * Ability to customize the mapping from simple database types to arbitrary Java types;
  * Automatic mapping of the arbitrary PostgreSQL types to Java types (using field positions and not field names).

**To Do**:

  * Automatic mapping of the arbitrary PostgreSQL types to Java types (using field names)
  * PostgreSQL Stored Procedure wrapper and executor