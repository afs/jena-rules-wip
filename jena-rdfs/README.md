# jena-rdfs

RDFS engine for large data.

The inference engine takes a schema and a data graph. It processes the schema on
startup, independent of the data, to form internal data structures. At runtime,
it dynamically accesses the data together with the pre-processed schema to
implement `Graph.find`.

It assumes the schema and the workspace for the schema fit in memory. Data is
left in the database.
