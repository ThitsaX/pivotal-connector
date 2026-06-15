

# IndividualTransferResult

Data model for the complex type IndividualTransferResult.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**transferId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**fulfilment** | **String** | Fulfilment that must be attached to the transfer by the Payee. |  [optional] |
|**errorInformation** | [**ErrorInformation**](ErrorInformation.md) |  |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



