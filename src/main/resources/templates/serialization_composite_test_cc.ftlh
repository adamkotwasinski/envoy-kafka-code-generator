// DO NOT EDIT - THIS FILE WAS GENERATED
// clang-format off
#include "common/common/stack_array.h"

#include "extensions/filters/network/kafka/generated/serialization_composite.h"
#include "extensions/filters/network/kafka/serialization.h"

#include "test/mocks/server/mocks.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"

namespace Envoy {
namespace Extensions {
namespace NetworkFilters {
namespace Kafka {

/**
 * Tests in this class are supposed to check whether serialization operations
 * on composite deserializers are behaving correctly
 */

// helper function
const char* getRawData(const Buffer::OwnedImpl& buffer) {
  uint64_t num_slices = buffer.getRawSlices(nullptr, 0);
  STACK_ARRAY(slices, Buffer::RawSlice, num_slices);
  buffer.getRawSlices(slices.begin(), num_slices);
  return reinterpret_cast<const char*>((slices[0]).mem_);
}

// exactly what is says on the tin:
// 1. serialize expected using Encoder
// 2. deserialize byte array using testee deserializer
// 3. verify result = expected
// 4. verify that data pointer moved correct amount
// 5. feed testee more data
// 6. verify that nothing more was consumed
template <typename BT, typename AT>
void serializeThenDeserializeAndCheckEqualityInOneGo(AT expected) {
  // given
  BT testee{};

  Buffer::OwnedImpl buffer;
  EncodingContext encoder;
  const size_t written = encoder.encode(expected, buffer);

  uint64_t remaining =
      10 *
      written; // tell parser that there is more data, it should never consume more than written
  const uint64_t orig_remaining = remaining;
  const char* data = getRawData(buffer);
  const char* orig_data = data;

  // when
  const size_t consumed = testee.feed(data, remaining);

  // then
  ASSERT_EQ(consumed, written);
  ASSERT_EQ(testee.ready(), true);
  ASSERT_EQ(testee.get(), expected);
  ASSERT_EQ(data, orig_data + consumed);
  ASSERT_EQ(remaining, orig_remaining - consumed);

  // when - 2
  const size_t consumed2 = testee.feed(data, remaining);

  // then - 2 (nothing changes)
  ASSERT_EQ(consumed2, 0);
  ASSERT_EQ(data, orig_data + consumed);
  ASSERT_EQ(remaining, orig_remaining - consumed);
}

// does the same thing as the above test,
// but instead of providing whole data at one, it provides it in N one-byte chunks
// this verifies if deserializer keeps state properly (no overwrites etc.)
template <typename BT, typename AT>
void serializeThenDeserializeAndCheckEqualityWithChunks(AT expected) {
  // given
  BT testee{};

  Buffer::OwnedImpl buffer;
  EncodingContext encoder;
  const size_t written = encoder.encode(expected, buffer);

  const char* data = getRawData(buffer);
  const char* orig_data = data;

  // when
  size_t consumed = 0;
  for (size_t i = 0; i < written; ++i) {
    uint64_t data_size = 1;
    consumed += testee.feed(data, data_size);
    ASSERT_EQ(data_size, 0);
  }

  // then
  ASSERT_EQ(consumed, written);
  ASSERT_EQ(testee.ready(), true);
  ASSERT_EQ(testee.get(), expected);
  ASSERT_EQ(data, orig_data + consumed);

  // when - 2
  uint64_t remaining = 1024;
  const size_t consumed2 = testee.feed(data, remaining);

  // then - 2 (nothing changes)
  ASSERT_EQ(consumed2, 0);
  ASSERT_EQ(data, orig_data + consumed);
  ASSERT_EQ(remaining, 1024);
}

// wrapper to run both tests
template <typename BT, typename AT> void serializeThenDeserializeAndCheckEquality(AT expected) {
  serializeThenDeserializeAndCheckEqualityInOneGo<BT>(expected);
  serializeThenDeserializeAndCheckEqualityWithChunks<BT>(expected);
}

// tests for composite deserializers

<#list composites as composite>
struct CompositeResultWith${composite.arity}Fields {
  <#list composite.elements as idx>
  const std::string field${idx}_;
  </#list>

  <#if (composite.arity > 0)>
  size_t encode(Buffer::Instance& dst, EncodingContext& encoder) const {
    size_t written{0};
    <#list composite.elements as idx>
    written += encoder.encode(field${idx}_, dst);
    </#list>
    return written;
  }
  <#else>
  size_t encode(Buffer::Instance&, EncodingContext&) const {
    return 0;
  }
  </#if>

  <#if (composite.arity > 0)>
  bool operator==(const CompositeResultWith${composite.arity}Fields& rhs) const {
    return <#list composite.elements as idx>field${idx}_ == rhs.field${idx}_<#sep> && </#sep></#list>;
  }
  <#else>
  bool operator==(const CompositeResultWith${composite.arity}Fields&) const {
    return true;
  }
  </#if>
};

typedef CompositeDeserializerWith${composite.arity}Delegates<CompositeResultWith${composite.arity}Fields<#if (composite.arity > 0)>, </#if><#list composite.elements as idx>StringDeserializer<#sep>,</#sep></#list>>
    TestCompositeDeserializer${composite.arity};

<#if (composite.arity > 0)>
TEST(CompositeDeserializerWith${composite.arity}Delegates, EmptyBufferShouldNotBeReady) {
  // given
  const TestCompositeDeserializer${composite.arity} testee{};
  // when, then
  ASSERT_EQ(testee.ready(), false);
}
<#else>
// composite with 0 delegates is special case: it's always ready
TEST(CompositeDeserializerWith${composite.arity}Delegates, EmptyBufferShouldBeReady) {
  // given
  const TestCompositeDeserializer${composite.arity} testee{};
  // when, then
  ASSERT_EQ(testee.ready(), true);
}
</#if>

TEST(CompositeDeserializerWith${composite.arity}Delegates, ShouldDeserialize) {
  const CompositeResultWith${composite.arity}Fields expected{<#list composite.elements as idx>"s${idx}"<#sep>, </#sep></#list>};
  serializeThenDeserializeAndCheckEquality<TestCompositeDeserializer${composite.arity}>(expected);
}

</#list>
} // namespace Kafka
} // namespace NetworkFilters
} // namespace Extensions
} // namespace Envoy
// clang-format on
