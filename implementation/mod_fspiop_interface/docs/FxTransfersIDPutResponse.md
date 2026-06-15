

# FxTransfersIDPutResponse

The object sent in the PUT /fxTransfers/{ID} callback.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**fulfilment** | **String** | The fulfilment of the condition specified for the currency conversion. Mandatory if the conversion has been executed successfully. |  [optional] |
|**completedTimestamp** | **String** | Time and date when the conversion was executed. |  [optional] |
|**conversionState** | **TransferState** | The current status of the conversion request. |  |
|**extensionList** | [**ExtensionList**](ExtensionList.md) |  |  [optional] |



