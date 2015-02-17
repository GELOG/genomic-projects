# Avro Notes

The exemples were inspired by:
* https://github.com/bigdatagenomics/adam/blob/master/pom.xml
* http://avro.apache.org/docs/1.7.7/gettingstartedjava.html
* http://avro.apache.org/docs/1.7.7/idl.html
* http://avro.apache.org/docs/1.7.7/spec.html


## Avro Data Types

### Overview

| Data Type | Range                          | Description |
| --------- | ------------------------------ | ----------- |
| null      |  n/a                           | No value (used for optional values). |
| boolean   |  0             @    1          | Binary value |
| int       | -2,147,483,648 @ 2,147,483,647 | 32-bit signed integer |
| long      |      -2^63     @   (2^63-1)    | 64-bit signed integer |
| float     |  32 bits                       | Single precision IEEE 754 floating-point number |
| double    |  64 bits                       | Double precision IEEE 754 floating-point number |
| bytes     |  n/a                           | Sequence of 8-bit unsigned bytes |
| string    |  n/a                           | Unicode character sequence |
| record    |  n/a                           |  |
| enum      |  n/a                           | A fixed list of symbols |
| array     |  n/a                           | A list of items of the same type |
| maps      |  n/a                           | A Key-Value list of items of the same value type. |
| unions    |  n/a                           | Combine multiple possible data types |
| fixed     |  n/a                           | A fixed-size list of bytes |
