// DO NOT EDIT - THIS FILE WAS GENERATED
// clang-format off
#pragma once

#include <algorithm>
#include <memory>
#include <string>
#include <vector>

#include "envoy/buffer/buffer.h"
#include "envoy/common/exception.h"
#include "envoy/common/pure.h"

#include "common/common/byte_order.h"
#include "common/common/fmt.h"

#include "extensions/filters/network/kafka/kafka_types.h"
#include "extensions/filters/network/kafka/serialization.h"

namespace Envoy {
namespace Extensions {
namespace NetworkFilters {
namespace Kafka {

/**
 * This header contains only composite deserializers
 * The basic design is composite deserializer creating delegates DeserializerType1..N
 * Result of type ResponseType is constructed by getting results of each of delegates
 */

<#list composites as composite>
/**
 * Composite deserializer that uses ${composite.arity} deserializer(s)
 * Passes data to each of the underlying deserializers
 * (deserializers that are already ready do not consume data, so it's safe).
 * The composite deserializer is ready when the last deserializer is ready
 * (which means all deserializers before it are ready too)
 * Constructs the result of type ResponseType using { delegate1_.get(), delegate2_.get() ... }
 *
 * @param ResponseType type of deserialized data
<#list composite.elements as idx>
 * @param DeserializerType${idx} deserializer ${idx} (result used as argument ${idx} of ResponseType's ctor)
</#list>
 */
template <
  typename ResponseType<#if (composite.arity > 0)>,</#if>
  <#list composite.elements as idx>
  typename DeserializerType${idx}<#sep>,</#sep>
  </#list>
>
class CompositeDeserializerWith${composite.arity}Delegates : public Deserializer<ResponseType> {
public:

  CompositeDeserializerWith${composite.arity}Delegates(){};

  <#if (composite.arity > 0)>
  size_t feed(const char*& buffer, uint64_t& remaining) {
    size_t consumed = 0;
    <#list composite.elements as idx>
    consumed += delegate${idx}_.feed(buffer, remaining);
    </#list>
    return consumed;
  }
  <#else>
  size_t feed(const char*&, uint64_t&) {
    return 0;
  }
  </#if>

  bool ready() const {
    <#if (composite.arity > 0)>
    return delegate${composite.arity}_.ready();
    <#else>
    return true;
    </#if>
  }

  ResponseType get() const {
    return {
      <#list composite.elements as idx>
      delegate${idx}_.get()<#sep>,</#sep>
      </#list>
    };
  }

protected:
  <#list composite.elements as idx>
  DeserializerType${idx} delegate${idx}_;
  </#list>
};

</#list>
} // namespace Kafka
} // namespace NetworkFilters
} // namespace Extensions
} // namespace Envoy
// clang-format on
