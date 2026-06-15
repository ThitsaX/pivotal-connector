

# IndividualQuote

Data model for the complex type IndividualQuote.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**quoteId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**transactionId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**payee** | [**Party**](Party.md) |  |  |
|**amountType** | **AmountType** |  |  |
|**amount** | [**Money**](Money.md) |  |  |
|**fees** | [**Money**](Money.md) |  |  [optional] |
|**transactionType** | [**TransactionType**](TransactionType.md) |  |  |
|**note** | **String** | Memo assigned to transaction. |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



