

# PartyIdInfo

Data model for the complex type PartyIdInfo. An ExtensionList element has been added to this reqeust in version v1.1

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**partyIdType** | **PartyIdType** |  |  |
|**partyIdentifier** | **String** | Identifier of the Party. |  |
|**partySubIdOrType** | **String** | Either a sub-identifier of a PartyIdentifier, or a sub-type of the PartyIdType, normally a PersonalIdentifierType. |  [optional] |
|**fspId** | **String** | FSP identifier. |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



