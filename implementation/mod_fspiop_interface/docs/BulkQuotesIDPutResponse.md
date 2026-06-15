

# BulkQuotesIDPutResponse

The object sent in the PUT /bulkQuotes/{ID} callback.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**individualQuoteResults** | [**List&lt;IndividualQuoteResult&gt;**](IndividualQuoteResult.md) | Fees for each individual transaction, if any of them are charged per transaction. |  [optional] |
|**expiration** | **String** | The API data type DateTime is a JSON String in a lexical format that is restricted by a regular expression for interoperability reasons. The format is according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html), expressed in a combined date, time and time zone format. A more readable version of the format is yyyy-MM-ddTHH:mm:ss.SSS[-HH:MM]. Examples are \&quot;2016-05-24T08:38:08.699-04:00\&quot;, \&quot;2016-05-24T08:38:08.699Z\&quot; (where Z indicates Zulu time zone, same as UTC). |  |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



