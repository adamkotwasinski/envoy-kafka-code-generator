class ${name} : public Request {
public:
	${name}(
		<#list fields as field>
		${field.declaration}<#sep>,</#sep>
		</#list>
	):
	<#if (fields?size > 0)>
		Request{${apiKey}, ${apiVersion}},
		<#list fields as field>
		${field.name}_{${field.name}}<#sep>,</#sep>
		</#list>
	<#else>
		Request{${apiKey}, ${apiVersion}}
	</#if>
	{};

	bool operator==(const ${name}& rhs) const {
	<#if (fields?size > 0)>
		return request_header_ == rhs.request_header_ && <#list fields as field>${field.name}_ == rhs.${field.name}_<#sep> && </#sep></#list>;
	<#else>
		return request_header_ == rhs.request_header_;
	</#if>
	};

protected:
	<#if (fields?size > 0)>
	size_t encodeDetails(Buffer::Instance& dst, EncodingContext& encoder) const override {
		size_t written{0};
		<#list fields as field>
		written += encoder.encode(${field.name}_, dst);
		</#list>
		return written;
	<#else>
	size_t encodeDetails(Buffer::Instance&, EncodingContext&) const override {
		return 0;
	</#if>
	}

	<#if (renderToString)>
	std::ostream& printDetails(std::ostream& os) const override {
		return os << "{"
			<#list fields as field>
			<< "${field.name}=" << ${field.name}_<#sep> << ","</#sep>
			</#list>
			<< "}";
	}

	</#if>
private:
	<#list fields as field>
	const ${field.declaration}_;
	</#list>
};

class ${name}Deserializer:
	public CompositeDeserializerWith${fields?size}Delegates<
		<#list fields>
		${name},
		<#items as field>
		${field.correspondingDeserializerName}<#sep>,</#sep>
		</#items>
		<#else>
		${name}
		</#list>
	>{};

class ${name}Parser : public RequestParser<${name}, ${name}Deserializer> {
public:
	${name}Parser(RequestContextSharedPtr ctx) : RequestParser{ctx} {};
};
