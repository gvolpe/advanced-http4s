advanced-http4s
===============

Code samples of advanced features of [Http4s](http://http4s.org/) in combination with some features of [Fs2](https://functional-streams-for-scala.github.io/fs2/) not often seen.

## Streaming end to end

- **Server**: Streaming responses end to end, from the `FileService` reading all the directories in your `$HOME` directory to the `FileHttpEndpoint`.
- **Client**: Parsing chunks of the response body produced by the server in a streaming fashion way.

## Compressed responses

- **GZip**: Server and Client.

## Authentication

- **OAuth**: Using Twitter and GitHub APIs.

## Media Type negotiation

- **XML**
- **Json**

## Form Multipart

- **Uploading a file**
