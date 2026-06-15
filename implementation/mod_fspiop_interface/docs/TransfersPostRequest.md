

# TransfersPostRequest

The object sent in the POST /transfers request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**transferId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**payeeFsp** | **String** | FSP identifier. |  |
|**payerFsp** | **String** | FSP identifier. |  |
|**amount** | [**Money**](Money.md) |  |  |
|**ilpPacket** | **String** | Information for recipient (transport layer information). |  |
|**condition** | **String** | Condition that must be attached to the transfer by the Payer. |  |
|**expiration** | **String** | The API data type DateTime is a JSON String in a lexical format that is restricted by a regular expression for interoperability reasons. The format is according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html), expressed in a combined date, time and time zone format. A more readable version of the format is yyyy-MM-ddTHH:mm:ss.SSS[-HH:MM]. Examples are \&quot;2016-05-24T08:38:08.699-04:00\&quot;, \&quot;2016-05-24T08:38:08.699Z\&quot; (where Z indicates Zulu time zone, same as UTC). |  |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



