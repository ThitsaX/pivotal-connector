

# Party

Data model for the complex type Party.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**partyIdInfo** | [**PartyIdInfo**](PartyIdInfo.md) |  |  |
|**merchantClassificationCode** | **String** | A limited set of pre-defined numbers. This list would be a limited set of numbers identifying a set of popular merchant types like School Fees, Pubs and Restaurants, Groceries, etc. |  [optional] |
|**name** | **String** | Name of the Party. Could be a real name or a nickname. |  [optional] |
|**personalInfo** | [**PartyPersonalInfo**](PartyPersonalInfo.md) |  |  [optional] |
|**supportedCurrencies** | **List&lt;Currency&gt;** | Currencies in which the party can receive funds. |  [optional] |



