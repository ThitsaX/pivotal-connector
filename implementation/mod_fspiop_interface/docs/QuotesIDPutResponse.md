

# QuotesIDPutResponse

The object sent in the PUT /quotes/{ID} callback.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**transferAmount** | [**Money**](Money.md) |  |  |
|**payeeReceiveAmount** | [**Money**](Money.md) |  |  [optional] |
|**payeeFspFee** | [**Money**](Money.md) |  |  [optional] |
|**payeeFspCommission** | [**Money**](Money.md) |  |  [optional] |
|**expiration** | **String** | The API data type DateTime is a JSON String in a lexical format that is restricted by a regular expression for interoperability reasons. The format is according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html), expressed in a combined date, time and time zone format. A more readable version of the format is yyyy-MM-ddTHH:mm:ss.SSS[-HH:MM]. Examples are \&quot;2016-05-24T08:38:08.699-04:00\&quot;, \&quot;2016-05-24T08:38:08.699Z\&quot; (where Z indicates Zulu time zone, same as UTC). |  |
|**geoCode** | [**GeoCode**](GeoCode.md) |  |  [optional] |
|**ilpPacket** | **String** | Information for recipient (transport layer information). |  |
|**condition** | **String** | Condition that must be attached to the transfer by the Payer. |  |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



