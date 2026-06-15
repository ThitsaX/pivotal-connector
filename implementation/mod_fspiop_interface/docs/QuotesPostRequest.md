

# QuotesPostRequest

The object sent in the POST /quotes request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**quoteId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**transactionId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**transactionRequestId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  [optional] |
|**payee** | [**Party**](Party.md) |  |  |
|**payer** | [**Party**](Party.md) |  |  |
|**amountType** | **AmountType** |  |  |
|**amount** | [**Money**](Money.md) |  |  |
|**fees** | [**Money**](Money.md) |  |  [optional] |
|**transactionType** | [**TransactionType**](TransactionType.md) |  |  |
|**converter** | **CurrencyConverter** | An optional field which will allow the payer DFSP to specify which DFSP it wants to undertake currency conversion. This is useful incase of if the sender wants the recipient to receive a specified amount of the target currency, but the payer DFSP does not want to undertake the currency conversion. In this case, the amount of the transfer would be expressed in the target currency and the amountType would be set to RECEIVE. |  [optional] |
|**currencyConversion** | [**FxRate**](FxRate.md) | Used by the debtor party if it wants to share information about the currency conversion it proposes to make; or if it is required by scheme rules to share this information. This object contains the amount of the transfer in the source and target currencies, but does not identify the FXP being used. |  [optional] |
|**geoCode** | [**GeoCode**](GeoCode.md) |  |  [optional] |
|**note** | **String** | Memo assigned to transaction. |  [optional] |
|**expiration** | **String** | The API data type DateTime is a JSON String in a lexical format that is restricted by a regular expression for interoperability reasons. The format is according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html), expressed in a combined date, time and time zone format. A more readable version of the format is yyyy-MM-ddTHH:mm:ss.SSS[-HH:MM]. Examples are \&quot;2016-05-24T08:38:08.699-04:00\&quot;, \&quot;2016-05-24T08:38:08.699Z\&quot; (where Z indicates Zulu time zone, same as UTC). |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



