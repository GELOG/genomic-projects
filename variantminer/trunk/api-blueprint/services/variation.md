# Group Variation
One DNA Variation compared to the reference genome. A variation can be uniquely identified by its position on the genome (`chromosome`, `position`), its value on the reference genome (`ref`), and its actual value on the sample genome (`alt`). With SNP variations, the length of `ref` and `alt` is always of 1 nucleotide. However, for INDEL and other variations, the length can be theorically unlimited. This makes the use of `ref` and `alt` unreliable for the primary key since the size of the key must be of fixed size.

Therefore, the primary key, called `variationId` is composed of these components:
- chromosome
- position
- num : a generated number to avoid collisions (e.g. two variations located on the same position).

It has been decided to use a 16 bits number to manage the `num` variable. Therefore allowing over 65,000 collisions.


## Variation [/variations/{id}]

+ Parameters

    + id      (required, string, `chr1-034569-001`) ... A unique identifier of the variation.

### GET
Retrieve a `Variation` identified by its *id*.

Return status codes:
- `200` on success
- `404` when the variation is not found

For example:

```
curl http://localhost:8080/variations/chr1-034569-001
```

+ Response 200 (application/json)

    + Body
    
            <<[ resources/variation.json ]

+ Response 404




## Variations Search [/variations/search/{q}/{?fields,sort,offset,limit}]

+ Parameters

    + q       (required, comma-separated-values, `chrom=1,qual>=80`) ... Specifies the search criterias.
    + fields  (optional, comma-separated-values, `chrom,start,ref`) ... List of fields to return (besides the primary which is always the first field). By default all the fields are returned.
    + sort    (optional, comma-separated-values, `chrom,num_het DESC`) ... **(TO BE IMPLEMENTED LATER)** Field to use for sorting the results. SQL-like syntax with ASC/DESC where ASC is the default.
    + offset  (optional, integer, `1`) ... Paging option. The rank of the first result to return.
    + limit   (optional, integer, `20`) ... Paging option. The maximum number of results to return.

### GET
Search for `variations`.

### The `q` parameter
The `q` parameter is an array of one or more search criterias. Each pair of search criterias is separated by a comma. Each criteria is composed of 3 subparameters: the **field**, the **operator**, and the **operand value**. The format of a criteria obeys the following rules:

Rules for mathematical operators like `<`:
- The space before and after the operator is optional.

Rules for textual operators like `startsWith`:
- The space between the field and the operator is required.

Rules for all operators:
- The trailing slash after the `q` parameter is required.
- The operators and field names are case in-sensitive (e.g. `startsWith` is the same as `startswith`).
- The values however, are case sensitive (e.g. `"Blue"` is different than `"BLUE"`.
- When needed, the values may be quoted with the double quotes (").

See below for a complete list of operators and examples.


### List of operators:
- `a`** < **`b`   ... Field `a` is less than `b`.
- `a`** <= **`b`  ... Field `a` is less than or equal to `b`.
- `a`** = **`b`   ... Field `a` is equal to `b`.
- `a`** != **`b`  ... Field `a` is not equal to than `b`.
- `a`** >= **`b`  ... Field `a` is greater than or equal to `b`.
- `a`** > **`b`   ... Field `a` is greater than `b`.
- `a` **in(**`b`,`c`,`d`**)**      ... Field `a` contains either `b`, `c`, or `d`.
- `a` **notIn(**`b`,`c`,`d`**)**   ... Field `a` does not contains neither `b`, `c`, nor `d`.
- `a` **startsWith(**"`bcd`"**)**  ... Field `a` starts with the prefix `bcd`.
- `a` **endsWith(**"`bcd`"**)**    ... Field `a` ends with the suffix `bcd`.
- `a` **contains(**"`bcd`"**)**    ... Field `a` contains the substring `bcd`.


### Examples - Criterias

Query 1: Listing all the variations with a value of `5` in the field `a`
```
curl http://localhost:8080/variations/search/a=5/
```
or
```
curl http://localhost:8080/variations/search/a = 5/
```

Query 2: Listing all the variations with a value greater or equal to `80` in the field `b`
```
curl http://localhost:8080/variations/search/b>=80/
```

Query 3: Listing all the variations that contains the substring "`hello`" in the filed `c`
```
curl http://localhost:8080/variations/search/c contains("hello")/
```
Note: the spaces entered in the address bar will be automatically URL encoded by the web browers - don't worry about it. For example the URL above will become `http://localhost:8080/variations/search/c%20contains("hello")`

Query 4: Listing all the variations where the field `d` starts with the substring "`chr21`"
```
curl http://localhost:8080/variations/search/d startsWith("chr21")/
```

Query 5: Listing all the variations having all the criterias specified in queries 1 - 4.
```
curl http://localhost:8080/variations/search/a=5,b>=80,c contains("hello"),d startsWith("chr21")/
```

Query 6: Listing all the variations where the field `d` starts with the substring "`chr21`" and returning only 10 results.
```
curl http://localhost:8080/variations/search/d startsWith("chr21")/?limit=10
```

Query 7: Listing all the variations where the field `d` starts with the substring "`chr21`", showing only the fields chrom, numHet and returning only 10 results.
```
curl http://localhost:8080/variations/search/d startsWith("chr21")/?fields=chrom,numHet&limit=10
```

### Examples - Returned fields

Query 8: By default, all fields are returned:
```
curl http://localhost:8080/variations/search/chrom="chr1"/?limit=2

# (Click on the Toggle button in the Response 200 section below, to view the default fields returned)
```

Query 9: Fetching only specific variation fields
```
curl http://localhost:8080/variations/search/chrom="chr1"/?limit=2&fields=id,ref,alt,gene,rsIds,numHomRef,numHet,numHomAlt,sampleId

<<[ resources/variations-basic-fields.json ]
```

Query 10: Fetching only specific variation fields
```
curl http://localhost:8080/variations/search/chrom="chr1"/?limit=2&fields=id,ref,alt,gene,rsIds,numHomRef,numHet,numHomAlt,sampleId,gt,qtQual,group

<<[ resources/variations-genotype-fields.json ]
```
**Note 1:** The values `"18"`, `"75"`, `"95"`, and `"301"` refers here to the sample IDs.

**Note 2:** notice that the `"genotypes"` section is only present when one of its field is requested. In this example, the fields `"gt"` and `"gtQual"` are requested.

**Note 3:** notice that the `"samples"` section is only present when one of its field is requested. In this example, the field `"group"` is requested.


### Automatic detection of primary key
In order to provide the best performance, the API automatically detects when the fields included in the primary key are used within a criteria. When possible it will use these values to do **RANGE SCAN** on the data, instead of doing a **FULL TABLE** scan.

### Automatic detection of columns and columns families
The API automatically detects which columns and columns families to include in the search, based on the parameters received from the client.


### HTTP status codes:
- `200` on success
- `400` when a parameter parsing error occurs

+ Response 200 (application/json)

        <<[ resources/variations-all-fields.json ]

+ Response 400
