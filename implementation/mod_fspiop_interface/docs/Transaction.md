

# Transaction

Data model for the complex type Transaction. The Transaction type is used to carry end-to-end data between the Payer FSP and the Payee FSP in the ILP Packet. Both the transactionId and the quoteId in the data model are decided by the Payer FSP in the POST /quotes request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**transactionId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**quoteId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**payee** | [**Party**](Party.md) |  |  |
|**payer** | [**Party**](Party.md) |  |  |
|**amount** | [**Money**](Money.md) |  |  |
|**payeeReceiveAmount** | [**Money**](Money.md) | The amount that the beneficiary will receive. |  [optional] |
|**converter** | **CurrencyConverter** | An optional field which will allow the payer DFSP to specify which DFSP it wants to undertake currency conversion. This is useful incase of if the sender wants the recipient to receive a specified amount of the target currency, but the payer DFSP does not want to undertake the currency conversion. In this case, the amount of the transfer would be expressed in the target currency and the amountType would be set to RECEIVE. |  [optional] |
|**currencyConversion** | [**FxRate**](FxRate.md) | Used by the debtor party if it wants to share information about the currency conversion it proposes to make; or if it is required by scheme rules to share this information. This object contains the amount of the transfer in the source and target currencies, but does not identify the FXP being used. |  [optional] |
|**transactionType** | [**TransactionType**](TransactionType.md) |  |  |
|**note** | **String** | Memo assigned to transaction. |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



