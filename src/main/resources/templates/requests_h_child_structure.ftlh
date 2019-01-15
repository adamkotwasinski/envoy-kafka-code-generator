/* Represents '${originalName}' element in ${owner} */
struct ${name} {
	<#list fields as field>
	const ${field.declaration}_;
	</#list>

	size_t encode(Buffer::Instance& dst, EncodingContext& encoder) const {
		size_t written{0};
		<#list fields as field>
		written += encoder.encode(${field.name}_, dst);
		</#list>
		return written;
	}

	bool operator==(const ${name}& rhs) const {
		return<#list fields as field>
			${field.name}_ == rhs.${field.name}_<#sep> &&</#sep></#list>;
	};

	<#if (renderToString)>
	friend std::ostream& operator<<(std::ostream& os, const ${name}& arg) {
		return os << "{"
			<#list fields as field>
			<#if field.printable()>
			<< "${field.name}=" << arg.${field.name}_<#sep> << ","</#sep>
			</#if>
			</#list>
			<< "}";
	}

	</#if>
};

class ${name}Deserializer:
	public CompositeDeserializerWith${fields?size}Delegates<
		${name},
		<#list fields as field>
		${field.correspondingDeserializerName}<#sep>,</#sep>
		</#list>
	>{};
