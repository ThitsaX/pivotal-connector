

# ParticipantsPostRequest

The object sent in the POST /participants request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**requestId** | **String** | Identifier that correlates all messages of the same sequence. The API data type UUID (Universally Unique Identifier) is a JSON String in canonical format, conforming to [RFC 4122](https://tools.ietf.org/html/rfc4122), that is restricted by a regular expression for interoperability reasons. A UUID is always 36 characters long, 32 hexadecimal symbols and 4 dashes (‘-‘). |  |
|**partyList** | [**List&lt;PartyIdInfo&gt;**](PartyIdInfo.md) | List of PartyIdInfo elements that the client would like to update or create FSP information about. |  |
|**currency** | **Currency** |  |  [optional] |



