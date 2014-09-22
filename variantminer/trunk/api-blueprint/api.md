FORMAT: 1A
HOST: http://localhost:8080

# Gemini Backend API

## Introduction
This document describes the web services of the Gemini Backend API; e.g. the communications between the web application and the genetic big data database. This document is aimed at the developers and the power users.

## Getting Started
The ideal is to start with the most complex services: [Variation](#page:variation) and [Variations Search](#page:variation,header:variation-variations-search). Then the other services [table](#page:table) and [group](#page:group) are much simpler.

For each service, the URLs, HTTP status codes and HTTP headers are defined. To view the data payload that is exchanged, just click on the Toggle hyperlink in each of these sections.

## Design choices

1. The primary key of each resources is an arbitrary array of bytes (e.g. a String). This means the key can be changed easily to optimize the performance of specific use cases.
2. The column families in which each column resides are not exposed in the API. Again, this provides the backend with more flexibilities.

## Resource relationships
![Alt Text](http://yuml.me/diagram/scruffy;/class/[Variation]1->1..*[Genotype],[Genotype]0..*<-1[Sample])


<<[services/group.md]
<<[services/table.md]
<<[services/variation.md]
