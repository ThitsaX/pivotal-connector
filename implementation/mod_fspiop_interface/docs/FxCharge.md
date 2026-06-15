

# FxCharge

An FXP will be able to specify a charge which it proposes to levy on the currency conversion operation using a FxCharge object.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**chargeType** | **String** | A description of the charge which is being levied. |  |
|**sourceAmount** | [**Money**](Money.md) | The amount of the charge which is being levied, expressed in the source currency. |  [optional] |
|**targetAmount** | [**Money**](Money.md) | The amount of the charge which is being levied, expressed in the target currency. |  [optional] |



