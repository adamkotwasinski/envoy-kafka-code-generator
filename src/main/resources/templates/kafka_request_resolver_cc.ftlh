// DO NOT EDIT - THIS FILE WAS GENERATED
// clang-format off
#include "extensions/filters/network/kafka/generated/requests.h"
#include "extensions/filters/network/kafka/kafka_request.h"
#include "extensions/filters/network/kafka/parser.h"

namespace Envoy {
namespace Extensions {
namespace NetworkFilters {
namespace Kafka {

const RequestParserResolver RequestParserResolver::INSTANCE;

ParserSharedPtr RequestParserResolver::createParser(int16_t api_key, int16_t api_version,
                                                    RequestContextSharedPtr context) const {

<#list requests as request>
  if (${request.apiKey} == api_key && ${request.apiVersion} == api_version) {
    return std::make_shared<${request.name}Parser>(context);
  }
</#list>
  return std::make_shared<SentinelParser>(context);
}

} // namespace Kafka
} // namespace NetworkFilters
} // namespace Extensions
} // namespace Envoy
// clang-format on
