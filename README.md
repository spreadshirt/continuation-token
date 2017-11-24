# Continuation Token

Library for fast, reliable and stateless API pagination with Continuation Tokens.

# Start the Demo

You can open both projects in the same IntelliJ Workspace. In this case, IntelliJ's workspace resolutions find the library so you don't have to install it via maven up front.

Otherwise:

```bash
cd continuation-token
./mvnw install
cd ../demo-kotlin
./mvnw package && java -jar target/demo-kotlin*.jar
```

Open `http://localhost:8000/designs?pageSize=3` in your browser an click on the URL in the `nextPage` field in the json payload.

The demo application is a lightweight HTTP service written in Kotlin and powered by [HTTP4K](https://www.http4k.org/). It starts within 600 ms. ;-) 

# TODO

- mvn deploy plumbing
- Linked List
- checksum fallback
- java demo to beautify Kotlin API for Java users