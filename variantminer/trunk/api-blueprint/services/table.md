# Group Table
A database table.


## Table [/tables]

### GET
Retrieve the list of tables in the database and their columns..

For example:

```
curl http://localhost:8080/tables
```
+ Response 200 (application/json)

    + Body
    
            <<[ resources/tables.json ]

