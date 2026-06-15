

# TransactionType

Data model for the complex type TransactionType.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**scenario** | **TransactionScenario** |  |  |
|**subScenario** | **String** | Possible sub-scenario, defined locally within the scheme (UndefinedEnum Type). |  [optional] |
|**initiator** | **TransactionInitiator** |  |  |
|**initiatorType** | **TransactionInitiatorType** |  |  |
|**refundInfo** | [**Refund**](Refund.md) |  |  [optional] |
|**balanceOfPayments** | **String** | (BopCode) The API data type [BopCode](https://www.imf.org/external/np/sta/bopcode/) is a JSON String of 3 characters, consisting of digits only. Negative numbers are not allowed. A leading zero is not allowed. |  [optional] |



