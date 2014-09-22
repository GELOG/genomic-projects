# Group Genotype
A genotype is relationship between one sample and one particular variation.


## Genotype [/variations/{id}/genotypes]

+ Parameters

    + id      (required, string, `chr1-034569-001`) ... A unique identifier of the variation.

### GET
Retrieve the list of genotypes in the population for the `Variation` identified by its *id*.

Return status codes:
- `200` on success
- `404` when the variation is not found

For example:

```
curl http://localhost:8080/variations/chr1-034569-001/genotypes
```

+ Response 200 (application/json)

    + Body
    
            <<[ resources/genotypes.json ]

+ Response 404
