# envoy-kafka-code-generator
Generator of kafka-filter C++ code for envoy

Run `gradle run --args='ENVOY_DIRECTORY_ABSOLUTE_PATH [RENDER_TO_STRING_METHODS]'` to generate the headers:
* `ENVOY_DIRECTORY_ABSOLUTE_PATH` - absolute path of envoy source (e.g. `/Users/adam/envoy`)
* `RENDER_TO_STRING_METHODS` - optional argument - whether to add print/toString methods to generated classes

## Features
* generation of kafka request headers
* generation of composite deserializers
* tests for above
