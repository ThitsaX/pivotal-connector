

# FxTransfersPostRequest

The object sent in the POST /fxTransfers request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**commitRequestId** | **String** | An end-to-end identifier for the confirmation request. |  |
|**determiningTransferId** | **String** | The transaction ID of the transfer to which this currency conversion relates, if the conversion is part of a transfer. If the conversion is a bulk currency purchase, this field should be omitted. |  [optional] |
|**initiatingFsp** | **String** | Identifier for the FSP who is requesting a currency conversion. |  |
|**counterPartyFsp** | **String** | Identifier for the FXP who is performing the currency conversion. |  |
|**sourceAmount** | [**Money**](Money.md) | The amount being offered for conversion by the requesting FSP. |  |
|**targetAmount** | [**Money**](Money.md) | The amount which the FXP is to credit to the requesting FSP in the target currency. |  |
|**condition** | **String** | ILP condition received by the requesting FSP when the quote was approved. |  |
|**expiration** | **String** | The API data type DateTime is a JSON String in a lexical format that is restricted by a regular expression for interoperability reasons. The format is according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html), expressed in a combined date, time and time zone format. A more readable version of the format is yyyy-MM-ddTHH:mm:ss.SSS[-HH:MM]. Examples are \&quot;2016-05-24T08:38:08.699-04:00\&quot;, \&quot;2016-05-24T08:38:08.699Z\&quot; (where Z indicates Zulu time zone, same as UTC). |  [optional] |



