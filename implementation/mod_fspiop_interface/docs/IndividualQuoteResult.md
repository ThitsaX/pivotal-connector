

# IndividualQuoteResult

Data model for the complex type IndividualQuoteResult.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**quoteId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**payee** | [**Party**](Party.md) |  |  [optional] |
|**transferAmount** | [**Money**](Money.md) |  |  [optional] |
|**payeeReceiveAmount** | [**Money**](Money.md) |  |  [optional] |
|**payeeFspFee** | [**Money**](Money.md) |  |  [optional] |
|**payeeFspCommission** | [**Money**](Money.md) |  |  [optional] |
|**ilpPacket** | **String** | Information for recipient (transport layer information). |  [optional] |
|**condition** | **String** | Condition that must be attached to the transfer by the Payer. |  [optional] |
|**errorInformation** | [**ErrorInformation**](ErrorInformation.md) |  |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



