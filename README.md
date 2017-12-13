# Continuation Token

[![Build Status](https://travis-ci.org/spreadshirt/continuation-token.svg?branch=master)](https://travis-ci.org/spreadshirt/continuation-token)

A library for fast, reliable and stateless Web API pagination with Continuation Tokens. It's written in Kotlin, but can be used for both Java and Kotlin services.

# The Approach

A detailed explanation of the approach and the used algorithm can be found in the blog post ["Web API Pagination with Continuation Tokens"](https://blog.philipphauer.de/web-api-pagination-continuation-token/). Some bullet points about continuation tokens are:

- It's a keyset pagination approach.
- The token is a pointer to a certain position within the list of all elements.
- The token is passed to the client in the response body. The client can pass it back to the server as a query parameter in order to receive the next page.
- The token has the format `timestamp_offset_checksum`. 
- The benefits:
    - It's fast because we don't need the expensive `OFFSET` clause.
    - It's reliable because we don't miss any elements and we can't end up in endless loops.
    - It's stateless. No state on the server-side is required. This way, we can easily load balance the requests over our multiple server instances.

# Usage

Add the dependency:

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>jcenter-releases</id>
        <name>jcenter</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.spreadshirt</groupId>
    <artifactId>continuation-token</artifactId>
    <version>VERSION</version>
</dependency>
```

Check out the [JCenter repository](https://jcenter.bintray.com/com/spreadshirt/continuation-token/) for the latest release. 

Basically, we have to do the following things:

1. Parse the continuation token with `ContinuationTokenParser.toContinuationToken()`. Mind the `InvalidContinuationTokenException` that can be thrown.
1. Calculate a so called `QueryAdvice` based on a token (which can be null) and a pageSize. This can be done using `Pagination.calculateQueryAdvice()`.
1. Do the actual database query with a the data of the query advice and the library of your choice. But it's *absolutely important* to mind the following conditions in the query:
    - Use a "greater or *equals*" (`>=`) clause for the timestamp. The elements with exactly the timestamp are also required for the following step.
    - Order by both the timestamp *and the id*.
    - There have to be an index on both timestamp and the id.
1. Pass the query result to `Pagination.createPage()`. It does the skipping, the checksum check and calculates the next continuation token. It finally returns the actual elements and the next token.

Kotlin: 

```kotlin
val token = request.query("continuationToken")?.toContinuationToken()
val pageSize = request.query("pageSize")?.toInt() ?: 100
val queryAdvice = calculateQueryAdvice(token, pageSize)
val sql = """SELECT * FROM designs
    WHERE dateModified >= FROM_UNIXTIME(${queryAdvice.timestamp})
    ORDER BY dateModified asc, id asc
    LIMIT ${queryAdvice.limit};"""
val designs = template.query(sql, this::mapToDesign)
val nextPage = createPage(designs, token, pageSize)
```

Java:

```java
ContinuationToken token = ContinuationTokenParser.toContinuationToken(request.queryParams("continuationToken"));
int pageSize = request.queryParams("pageSize");
QueryAdvice queryAdvice = Pagination.calculateQueryAdvice(token, pageSize);
String sql = format("SELECT * FROM Employees" +
                             " WHERE UNIX_TIMESTAMP(timestamp) >= %d" +
                             " ORDER BY timestamp, id ASC" +
                             " LIMIT %d", queryAdvice.getTimestamp(), queryAdvice.getLimit())
List<Employee> entities = jdbcTemplate.query(sql, this::mapRow);
Page<Employee> page = Pagination.createPage(entities, token, pageSize);
```

The algorithm ensures that you don't miss any element. However, you client may see the same element multiple times if it's changed during the pagination run.

# Demos

Here you can find two example services that are implementing pagination with continuation tokens:

- [Kotlin Demo](/demo-kotlin/)
- [Java Demo](/demo-java/)

# Repository Links

- [Available releases on JCenter](https://jcenter.bintray.com/com/spreadshirt/continuation-token/)
- [Available snapshots on JCenter](https://oss.jfrog.org/simple/oss-snapshot-local/com/spreadshirt/continuation-token/)
- [Continuation-Token Repository on Bintray OSS](https://bintray.com/spreadshirt/library/continuation-token)
