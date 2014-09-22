# Group Sample
One DNA Sample.


## Sample [/samples/{id}/variations]

+ Parameters

    + id      (required, string, `1662`) ... A unique identifier of the sample.

### GET
Retrieve the variations of a `Sample` identified by its *id*.

Return status codes:
- `200` on success
- `404` when the sample is not found

For example:

```
curl http://localhost:8080/samples/1662/variations
```

+ Response 200 (text/tab-separated-values)

    + Body
    
            <<[ resources/sample-variations.tsv ]

+ Response 404
