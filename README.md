advanced-http4s
===============

Code samples of advanced features of [Http4s](http://http4s.org/) in combination with some features of [Fs2](https://functional-streams-for-scala.github.io/fs2/) not often seen.

Streaming end to end
--------------------

- **Server**: Streaming responses end to end, from the `FileService` reading all the directories in your `$HOME` directory to the `FileHttpEndpoint`.
- **StreamClient**: Parsing chunks of the response body produced by the server in a streaming fashion way.

> You'll need two sbt sessions. Run the server in one and after the client in the other to try it out.

Middleware Composition
----------------------

- **GZip**: For compressed responses. Client must set the `Accept Encoding` header to `gzip`.
- **AutoSlash**: To make endpoints work with and without the slash `/` at the end.

> Response compression is verified by `HexNameHttpEndpointSpec`. You can also try it out on Postman or similar.

- **Timeout**: Handling response timeouts with the given middleware.

> The `TimeoutHttpEndpoint` generates a response in a random time to demonstrate the use.

- **NonStreamResponse**: Using the `ChunkAggregator` middleware to wrap the streaming `FileHttpEndpoint` and remove the Chunked Transfer Encoding.

> The endpoint `/v1/nonstream/dirs?depth=3` demonstrates the use case.

Media Type negotiation
----------------------

- **XML** and **Json**: Decoding request body with either of these types for the same endpoint.

> The `JsonXmlHttpEndpoint` demonstrates this use case and it's validated in its spec.

Multipart Form Data
-------------------

- **Server**: The `MultipartHttpEndpoint` is responsible for parsing multipart data with the given multipart decoder.
- **MultipartClient**: Example uploading both text and an image.

> Similar to the streaming example, you'll need to run both Server and MultipartClient to see how it works.

*NOTE: Beware of the creation of `rick.jpg` file in your HOME directory!*

Authentication
--------------

- **Basic Auth**: Using the given middleware as demonstrated by the `BasicAuthHttpEndpoint`.
- **OAuth 2**: Using GitHub as demonstrated by the `GitHubHttpEndpoint`.

-----------------------------------------------------------------------------

fs2 examples
============

In the fs2 package you'll find some practical examples of the few things it's possible to build with this powerful streaming library. This might serve as a starting point, your creativity will do the rest.

fs2.async package
------------------

Apart from the use of the three core types `Stream[F, O]`, `Pipe[F, I, O]` and `Sink[F, I]` you'll find examples of use of the following types:

- `Topic[F, A]`
- `Signal[F, A]`
- `Queue[F, A]`
- `Ref[F, A]`
- `Promise[F, A]`
- `Semaphore[F]`

In addition to the use of some other functions useful in Parallel and Concurrent scenarios.

