

# FxMoney

Data model for the complex type FxMoney; This is based on the type Money but allows the amount to be optional to support FX quotations.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**currency** | **Currency** |  |  |
|**amount** | **String** | The API data type Amount is a JSON String in a canonical format that is restricted by a regular expression for interoperability reasons. This pattern does not allow any trailing zeroes at all, but allows an amount without a minor currency unit. It also only allows four digits in the minor currency unit; a negative value is not allowed. Using more than 18 digits in the major currency unit is not allowed. |  [optional] |



