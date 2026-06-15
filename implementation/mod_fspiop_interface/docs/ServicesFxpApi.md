# ServicesFxpApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**servicesFXPGet**](ServicesFxpApi.md#servicesFXPGet) | **GET** /services/FXP | Obtain a list of the DFSPs in the scheme who provide FXP service |
| [**servicesFXPPut**](ServicesFxpApi.md#servicesFXPPut) | **PUT** /services/FXP |  |
| [**servicesFXPSourceCurrencyTargetCurrencyGet**](ServicesFxpApi.md#servicesFXPSourceCurrencyTargetCurrencyGet) | **GET** /services/FXP/{SourceCurrency}/{TargetCurrency} | Obtain a list of the DFSPs in the scheme who provide FXP service |
| [**servicesFXPSourceCurrencyTargetCurrencyPut**](ServicesFxpApi.md#servicesFXPSourceCurrencyTargetCurrencyPut) | **PUT** /services/FXP/{SourceCurrency}/{TargetCurrency} |  |


<a id="servicesFXPGet"></a>
# **servicesFXPGet**
> servicesFXPGet(contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Obtain a list of the DFSPs in the scheme who provide FXP service

The HTTP request &#x60;GET /services/FXP&#x60; is used to request information about the participants in a scheme who offer currency conversion services.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ServicesFxpApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ServicesFxpApi apiInstance = new ServicesFxpApi(defaultClient);
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    String accept = "accept_example"; // String | The `Accept` header field indicates the version of the API the client would like the server to use.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.servicesFXPGet(contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ServicesFxpApi#servicesFXPGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **accept** | **String**| The &#x60;Accept&#x60; header field indicates the version of the API the client would like the server to use. | |
| **xForwardedFor** | **String**| The &#x60;X-Forwarded-For&#x60; header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple &#x60;X-Forwarded-For&#x60; values should be expected and supported by implementers of the API.  **Note:** An alternative to &#x60;X-Forwarded-For&#x60; is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than &#x60;X-Forwarded-For&#x60;. | [optional] |
| **fsPIOPDestination** | **String**| The &#x60;FSPIOP-Destination&#x60; header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty. | [optional] |
| **fsPIOPEncryption** | **String**| The &#x60;FSPIOP-Encryption&#x60; header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request. | [optional] |
| **fsPIOPSignature** | **String**| The &#x60;FSPIOP-Signature&#x60; header field is a non-HTTP standard field used by the API for applying an end-to-end request signature. | [optional] |
| **FSPIOP_URI** | **String**| The &#x60;FSPIOP-URI&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **fsPIOPHTTPMethod** | **String**| The &#x60;FSPIOP-HTTP-Method&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **202** | Accepted |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

<a id="servicesFXPPut"></a>
# **servicesFXPPut**
> servicesFXPPut(contentType, date, fsPIOPSource, servicesFXPPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)



The callback &#x60;PUT /services/FXP&#x60; is used to inform the requester about participants in a scheme who offer currency conversion services. If no participants offer these services, the return object will be blank.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ServicesFxpApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ServicesFxpApi apiInstance = new ServicesFxpApi(defaultClient);
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ServicesFXPPutResponse servicesFXPPutResponse = new ServicesFXPPutResponse(); // ServicesFXPPutResponse | The list of participant(s) who offer currency conversion services.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.servicesFXPPut(contentType, date, fsPIOPSource, servicesFXPPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ServicesFxpApi#servicesFXPPut");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **servicesFXPPutResponse** | [**ServicesFXPPutResponse**](ServicesFXPPutResponse.md)| The list of participant(s) who offer currency conversion services. | |
| **xForwardedFor** | **String**| The &#x60;X-Forwarded-For&#x60; header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple &#x60;X-Forwarded-For&#x60; values should be expected and supported by implementers of the API.  **Note:** An alternative to &#x60;X-Forwarded-For&#x60; is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than &#x60;X-Forwarded-For&#x60;. | [optional] |
| **fsPIOPDestination** | **String**| The &#x60;FSPIOP-Destination&#x60; header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty. | [optional] |
| **fsPIOPEncryption** | **String**| The &#x60;FSPIOP-Encryption&#x60; header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request. | [optional] |
| **fsPIOPSignature** | **String**| The &#x60;FSPIOP-Signature&#x60; header field is a non-HTTP standard field used by the API for applying an end-to-end request signature. | [optional] |
| **FSPIOP_URI** | **String**| The &#x60;FSPIOP-URI&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **fsPIOPHTTPMethod** | **String**| The &#x60;FSPIOP-HTTP-Method&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

<a id="servicesFXPSourceCurrencyTargetCurrencyGet"></a>
# **servicesFXPSourceCurrencyTargetCurrencyGet**
> servicesFXPSourceCurrencyTargetCurrencyGet(sourceCurrency, targetCurrency, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Obtain a list of the DFSPs in the scheme who provide FXP service

The HTTP request &#x60;GET /services/FXP/{SourceCurrency}/{TargetCurrency}&#x60; is used to request information about the participants in a scheme who offer currency conversion services in a particular currency corridor. The required corridor is specified by giving the ISO 4217 currency code for the SourceCurrency and the TargetCurrency.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ServicesFxpApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ServicesFxpApi apiInstance = new ServicesFxpApi(defaultClient);
    String sourceCurrency = "sourceCurrency_example"; // String | ISO 4217 currency code for the source currency.
    String targetCurrency = "targetCurrency_example"; // String | ISO 4217 currency code for the target currency.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    String accept = "accept_example"; // String | The `Accept` header field indicates the version of the API the client would like the server to use.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.servicesFXPSourceCurrencyTargetCurrencyGet(sourceCurrency, targetCurrency, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ServicesFxpApi#servicesFXPSourceCurrencyTargetCurrencyGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sourceCurrency** | **String**| ISO 4217 currency code for the source currency. | |
| **targetCurrency** | **String**| ISO 4217 currency code for the target currency. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **accept** | **String**| The &#x60;Accept&#x60; header field indicates the version of the API the client would like the server to use. | |
| **xForwardedFor** | **String**| The &#x60;X-Forwarded-For&#x60; header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple &#x60;X-Forwarded-For&#x60; values should be expected and supported by implementers of the API.  **Note:** An alternative to &#x60;X-Forwarded-For&#x60; is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than &#x60;X-Forwarded-For&#x60;. | [optional] |
| **fsPIOPDestination** | **String**| The &#x60;FSPIOP-Destination&#x60; header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty. | [optional] |
| **fsPIOPEncryption** | **String**| The &#x60;FSPIOP-Encryption&#x60; header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request. | [optional] |
| **fsPIOPSignature** | **String**| The &#x60;FSPIOP-Signature&#x60; header field is a non-HTTP standard field used by the API for applying an end-to-end request signature. | [optional] |
| **FSPIOP_URI** | **String**| The &#x60;FSPIOP-URI&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **fsPIOPHTTPMethod** | **String**| The &#x60;FSPIOP-HTTP-Method&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **202** | Accepted |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

<a id="servicesFXPSourceCurrencyTargetCurrencyPut"></a>
# **servicesFXPSourceCurrencyTargetCurrencyPut**
> servicesFXPSourceCurrencyTargetCurrencyPut(sourceCurrency, targetCurrency, contentType, date, fsPIOPSource, servicesFXPSourceCurrencyTargetCurrencyPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)



The callback &#x60;PUT /services/FXP/{SourceCurrency}/{TargetCurrency}&#x60; is used to inform the requester about participants in a scheme who offer currency conversion services. If no participants offer these services, the return object will be blank. The required corridor is specified by giving the ISO 4217 currency code for the SourceCurrency and the TargetCurrency.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ServicesFxpApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ServicesFxpApi apiInstance = new ServicesFxpApi(defaultClient);
    String sourceCurrency = "sourceCurrency_example"; // String | ISO 4217 currency code for the source currency.
    String targetCurrency = "targetCurrency_example"; // String | ISO 4217 currency code for the target currency.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ServicesFXPSourceCurrencyTargetCurrencyPutResponse servicesFXPSourceCurrencyTargetCurrencyPutResponse = new ServicesFXPSourceCurrencyTargetCurrencyPutResponse(); // ServicesFXPSourceCurrencyTargetCurrencyPutResponse | The list of participant(s) who offer currency conversion services.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.servicesFXPSourceCurrencyTargetCurrencyPut(sourceCurrency, targetCurrency, contentType, date, fsPIOPSource, servicesFXPSourceCurrencyTargetCurrencyPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ServicesFxpApi#servicesFXPSourceCurrencyTargetCurrencyPut");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sourceCurrency** | **String**| ISO 4217 currency code for the source currency. | |
| **targetCurrency** | **String**| ISO 4217 currency code for the target currency. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **servicesFXPSourceCurrencyTargetCurrencyPutResponse** | [**ServicesFXPSourceCurrencyTargetCurrencyPutResponse**](ServicesFXPSourceCurrencyTargetCurrencyPutResponse.md)| The list of participant(s) who offer currency conversion services. | |
| **xForwardedFor** | **String**| The &#x60;X-Forwarded-For&#x60; header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple &#x60;X-Forwarded-For&#x60; values should be expected and supported by implementers of the API.  **Note:** An alternative to &#x60;X-Forwarded-For&#x60; is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than &#x60;X-Forwarded-For&#x60;. | [optional] |
| **fsPIOPDestination** | **String**| The &#x60;FSPIOP-Destination&#x60; header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty. | [optional] |
| **fsPIOPEncryption** | **String**| The &#x60;FSPIOP-Encryption&#x60; header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request. | [optional] |
| **fsPIOPSignature** | **String**| The &#x60;FSPIOP-Signature&#x60; header field is a non-HTTP standard field used by the API for applying an end-to-end request signature. | [optional] |
| **FSPIOP_URI** | **String**| The &#x60;FSPIOP-URI&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **fsPIOPHTTPMethod** | **String**| The &#x60;FSPIOP-HTTP-Method&#x60; header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set). | [optional] |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

