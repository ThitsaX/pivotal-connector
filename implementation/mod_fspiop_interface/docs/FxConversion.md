

# FxConversion

A DFSP will be able to request a currency conversion, and an FX provider will be able to describe its involvement in a proposed transfer, using a FxConversion object.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**conversionId** | **String** | An end-to-end identifier for the conversion request. |  |
|**determiningTransferId** | **String** | The transaction ID of the transfer on whose success this currency conversion depends. |  [optional] |
|**initiatingFsp** | **String** | The id of the participant who is requesting a currency conversion. |  |
|**counterPartyFsp** | **String** | The ID of the FXP performing the conversion. |  |
|**amountType** | **AmountType** | This is the AmountType for the base transaction - If SEND - then any charges levied by the FXP as part of the transaction will be deducted by the FXP from the amount shown for the target party in the conversion. If RECEIVE - then any charges levied by the FXP as part of the transaction will be added by the FXP to the amount shown for the source party in the conversion. |  |
|**sourceAmount** | [**FxMoney**](FxMoney.md) | The amount to be converted, expressed in the source currency. |  |
|**targetAmount** | [**FxMoney**](FxMoney.md) | The converted amount, expressed in the target currency. |  |
|**expiration** | **String** | The end of the period for which the currency conversion is required to remain valid. |  |
|**charges** | [**List&lt;FxCharge&gt;**](FxCharge.md) | One or more charges which the FXP intends to levy as part of the currency conversion, or which the payee DFSP intends to add to the amount transferred. |  [optional] |
|**extensionList** | [**ExtensionList**](ExtensionList.md) | The extension list for the currency conversion request. |  [optional] |



