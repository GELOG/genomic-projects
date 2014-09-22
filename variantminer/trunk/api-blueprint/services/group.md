# Group Group
A group collects multiple samples that have similar biochemical properties (e.g. FG1, FG2, FG3, Ctrl).


## Group [/groups]

### GET
Retrieve the list of groups in the population.

For example:

```
curl http://localhost:8080/groups
```

+ Response 200 (application/json)

    + Body
    
            <<[ resources/groups.json ]
