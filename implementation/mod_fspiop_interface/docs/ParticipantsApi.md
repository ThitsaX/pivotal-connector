# ParticipantsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**participants1**](ParticipantsApi.md#participants1) | **POST** /participants | Create bulk participant information |
| [**participantsByIDAndError**](ParticipantsApi.md#participantsByIDAndError) | **PUT** /participants/{ID}/error | Return bulk participant information error |
| [**participantsByIDAndType**](ParticipantsApi.md#participantsByIDAndType) | **POST** /participants/{Type}/{ID} | Create participant information |
| [**participantsByTypeAndID**](ParticipantsApi.md#participantsByTypeAndID) | **GET** /participants/{Type}/{ID} | Look up participant information |
| [**participantsByTypeAndID2**](ParticipantsApi.md#participantsByTypeAndID2) | **DELETE** /participants/{Type}/{ID} | Delete participant information |
| [**participantsByTypeAndID3**](ParticipantsApi.md#participantsByTypeAndID3) | **PUT** /participants/{Type}/{ID} | Return participant information |
| [**participantsErrorByTypeAndID**](ParticipantsApi.md#participantsErrorByTypeAndID) | **PUT** /participants/{Type}/{ID}/error | Return participant information error |
| [**participantsSubIdByTypeAndID**](ParticipantsApi.md#participantsSubIdByTypeAndID) | **GET** /participants/{Type}/{ID}/{SubId} | Look up participant information |
| [**participantsSubIdByTypeAndID2**](ParticipantsApi.md#participantsSubIdByTypeAndID2) | **DELETE** /participants/{Type}/{ID}/{SubId} | Delete participant information |
| [**participantsSubIdByTypeAndID3**](ParticipantsApi.md#participantsSubIdByTypeAndID3) | **PUT** /participants/{Type}/{ID}/{SubId} | Return participant information |
| [**participantsSubIdByTypeAndIDPost**](ParticipantsApi.md#participantsSubIdByTypeAndIDPost) | **POST** /participants/{Type}/{ID}/{SubId} | Create participant information |
| [**participantsSubIdErrorByTypeAndID**](ParticipantsApi.md#participantsSubIdErrorByTypeAndID) | **PUT** /participants/{Type}/{ID}/{SubId}/error | Return participant information error |
| [**putParticipantsByID**](ParticipantsApi.md#putParticipantsByID) | **PUT** /participants/{ID} | Return bulk participant information |


<a id="participants1"></a>
# **participants1**
> participants1(accept, contentType, date, fsPIOPSource, participantsPostRequest, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Create bulk participant information

The HTTP request &#x60;POST /participants&#x60; is used to create information in the server regarding the provided list of identities. This request should be used for bulk creation of FSP information for more than one Party. The optional currency parameter should indicate that each provided Party supports the currency.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String accept = "accept_example"; // String | The `Accept` header field indicates the version of the API the client would like the server to use.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ParticipantsPostRequest participantsPostRequest = new ParticipantsPostRequest(); // ParticipantsPostRequest | Participant information to be created.
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.participants1(accept, contentType, date, fsPIOPSource, participantsPostRequest, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participants1");
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
| **accept** | **String**| The &#x60;Accept&#x60; header field indicates the version of the API the client would like the server to use. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **participantsPostRequest** | [**ParticipantsPostRequest**](ParticipantsPostRequest.md)| Participant information to be created. | |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |
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

 - **Content-Type**: application/json
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

<a id="participantsByIDAndError"></a>
# **participantsByIDAndError**
> participantsByIDAndError(ID, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Return bulk participant information error

If there is an error during FSP information creation in the server, the error callback &#x60;PUT /participants/{ID}/error&#x60; is used. The &#x60;{ID}&#x60; in the URI should contain the requestId that was used for the creation of the participant information.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String ID = "ID_example"; // String | The identifier value.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ErrorInformationObject errorInformationObject = new ErrorInformationObject(); // ErrorInformationObject | Details of the error returned.
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.participantsByIDAndError(ID, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsByIDAndError");
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
| **ID** | **String**| The identifier value. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **errorInformationObject** | [**ErrorInformationObject**](ErrorInformationObject.md)| Details of the error returned. | |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |
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

<a id="participantsByIDAndType"></a>
# **participantsByIDAndType**
> participantsByIDAndType(type, ID, contentType, date, fsPIOPSource, accept, participantsTypeIDSubIDPostRequest, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)

Create participant information

The HTTP request &#x60;POST /participants/{Type}/{ID}&#x60; (or &#x60;POST /participants/{Type}/{ID}/{SubId}&#x60;) is used to create information in the server regarding the provided identity, defined by &#x60;{Type}&#x60;, &#x60;{ID}&#x60;, and optionally &#x60;{SubId}&#x60; (for example, &#x60;POST /participants/MSISDN/123456789&#x60; or &#x60;POST /participants/BUSINESS/shoecompany/employee1&#x60;). An ExtensionList element has been added to this reqeust in version v1.1

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    String accept = "accept_example"; // String | The `Accept` header field indicates the version of the API the client would like the server to use.
    ParticipantsTypeIDSubIDPostRequest participantsTypeIDSubIDPostRequest = new ParticipantsTypeIDSubIDPostRequest(); // ParticipantsTypeIDSubIDPostRequest | Participant information to be created.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.participantsByIDAndType(type, ID, contentType, date, fsPIOPSource, accept, participantsTypeIDSubIDPostRequest, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsByIDAndType");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **accept** | **String**| The &#x60;Accept&#x60; header field indicates the version of the API the client would like the server to use. | |
| **participantsTypeIDSubIDPostRequest** | [**ParticipantsTypeIDSubIDPostRequest**](ParticipantsTypeIDSubIDPostRequest.md)| Participant information to be created. | |
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
| **202** | Accepted |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

<a id="participantsByTypeAndID"></a>
# **participantsByTypeAndID**
> participantsByTypeAndID(type, ID, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Look up participant information

The HTTP request &#x60;GET /participants/{Type}/{ID}&#x60; (or &#x60;GET /participants/{Type}/{ID}/{SubId}&#x60;) is used to find out in which FSP the requested Party, defined by &#x60;{Type}&#x60;, &#x60;{ID}&#x60; and optionally &#x60;{SubId}&#x60;, is located (for example, &#x60;GET /participants/MSISDN/123456789&#x60;, or &#x60;GET /participants/BUSINESS/shoecompany/employee1&#x60;). This HTTP request should support a query string for filtering of currency. To use filtering of currency, the HTTP request &#x60;GET /participants/{Type}/{ID}?currency&#x3D;XYZ&#x60; should be used, where &#x60;XYZ&#x60; is the requested currency.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
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
      apiInstance.participantsByTypeAndID(type, ID, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsByTypeAndID");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
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

<a id="participantsByTypeAndID2"></a>
# **participantsByTypeAndID2**
> participantsByTypeAndID2(type, ID, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Delete participant information

The HTTP request &#x60;DELETE /participants/{Type}/{ID}&#x60; (or &#x60;DELETE /participants/{Type}/{ID}/{SubId}&#x60;) is used to delete information in the server regarding the provided identity, defined by &#x60;{Type}&#x60; and &#x60;{ID}&#x60;) (for example, &#x60;DELETE /participants/MSISDN/123456789&#x60;), and optionally &#x60;{SubId}&#x60;. This HTTP request should support a query string to delete FSP information regarding a specific currency only. To delete a specific currency only, the HTTP request &#x60;DELETE /participants/{Type}/{ID}?currency&#x3D;XYZ&#x60; should be used, where &#x60;XYZ&#x60; is the requested currency.  **Note:** The Account Lookup System should verify that it is the Party’s current FSP that is deleting the FSP information.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
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
      apiInstance.participantsByTypeAndID2(type, ID, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsByTypeAndID2");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
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

<a id="participantsByTypeAndID3"></a>
# **participantsByTypeAndID3**
> participantsByTypeAndID3(type, ID, contentType, date, fsPIOPSource, participantsTypeIDPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)

Return participant information

The callback &#x60;PUT /participants/{Type}/{ID}&#x60; (or &#x60;PUT /participants/{Type}/{ID}/{SubId}&#x60;) is used to inform the client of a successful result of the lookup, creation, or deletion of the FSP information related to the Party. If the FSP information is deleted, the fspId element should be empty; otherwise the element should include the FSP information for the Party.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ParticipantsTypeIDPutResponse participantsTypeIDPutResponse = new ParticipantsTypeIDPutResponse(); // ParticipantsTypeIDPutResponse | Participant information returned.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.participantsByTypeAndID3(type, ID, contentType, date, fsPIOPSource, participantsTypeIDPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsByTypeAndID3");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **participantsTypeIDPutResponse** | [**ParticipantsTypeIDPutResponse**](ParticipantsTypeIDPutResponse.md)| Participant information returned. | |
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

<a id="participantsErrorByTypeAndID"></a>
# **participantsErrorByTypeAndID**
> participantsErrorByTypeAndID(type, ID, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Return participant information error

If the server is unable to find, create or delete the associated FSP of the provided identity, or another processing error occurred, the error callback &#x60;PUT /participants/{Type}/{ID}/error&#x60; (or &#x60;PUT /participants/{Type}/{ID}/{SubId}/error&#x60;) is used.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ErrorInformationObject errorInformationObject = new ErrorInformationObject(); // ErrorInformationObject | Details of the error returned.
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.participantsErrorByTypeAndID(type, ID, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsErrorByTypeAndID");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **errorInformationObject** | [**ErrorInformationObject**](ErrorInformationObject.md)| Details of the error returned. | |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |
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

<a id="participantsSubIdByTypeAndID"></a>
# **participantsSubIdByTypeAndID**
> participantsSubIdByTypeAndID(type, ID, subId, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Look up participant information

The HTTP request &#x60;GET /participants/{Type}/{ID}&#x60; (or &#x60;GET /participants/{Type}/{ID}/{SubId}&#x60;) is used to find out in which FSP the requested Party, defined by &#x60;{Type}&#x60;, &#x60;{ID}&#x60; and optionally &#x60;{SubId}&#x60;, is located (for example, &#x60;GET /participants/MSISDN/123456789&#x60;, or &#x60;GET /participants/BUSINESS/shoecompany/employee1&#x60;). This HTTP request should support a query string for filtering of currency. To use filtering of currency, the HTTP request &#x60;GET /participants/{Type}/{ID}?currency&#x3D;XYZ&#x60; should be used, where &#x60;XYZ&#x60; is the requested currency.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String subId = "subId_example"; // String | A sub-identifier of the party identifier, or a sub-type of the party identifier's type. For example, `PASSPORT`, `DRIVING_LICENSE`.
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
      apiInstance.participantsSubIdByTypeAndID(type, ID, subId, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsSubIdByTypeAndID");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **subId** | **String**| A sub-identifier of the party identifier, or a sub-type of the party identifier&#39;s type. For example, &#x60;PASSPORT&#x60;, &#x60;DRIVING_LICENSE&#x60;. | |
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

<a id="participantsSubIdByTypeAndID2"></a>
# **participantsSubIdByTypeAndID2**
> participantsSubIdByTypeAndID2(type, ID, subId, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Delete participant information

The HTTP request &#x60;DELETE /participants/{Type}/{ID}&#x60; (or &#x60;DELETE /participants/{Type}/{ID}/{SubId}&#x60;) is used to delete information in the server regarding the provided identity, defined by &#x60;{Type}&#x60; and &#x60;{ID}&#x60;) (for example, &#x60;DELETE /participants/MSISDN/123456789&#x60;), and optionally &#x60;{SubId}&#x60;. This HTTP request should support a query string to delete FSP information regarding a specific currency only. To delete a specific currency only, the HTTP request &#x60;DELETE /participants/{Type}/{ID}?currency&#x3D;XYZ&#x60; should be used, where &#x60;XYZ&#x60; is the requested currency.  **Note:** The Account Lookup System should verify that it is the Party’s current FSP that is deleting the FSP information.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String subId = "subId_example"; // String | A sub-identifier of the party identifier, or a sub-type of the party identifier's type. For example, `PASSPORT`, `DRIVING_LICENSE`.
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
      apiInstance.participantsSubIdByTypeAndID2(type, ID, subId, contentType, date, fsPIOPSource, accept, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsSubIdByTypeAndID2");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **subId** | **String**| A sub-identifier of the party identifier, or a sub-type of the party identifier&#39;s type. For example, &#x60;PASSPORT&#x60;, &#x60;DRIVING_LICENSE&#x60;. | |
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

<a id="participantsSubIdByTypeAndID3"></a>
# **participantsSubIdByTypeAndID3**
> participantsSubIdByTypeAndID3(type, ID, subId, contentType, date, fsPIOPSource, participantsTypeIDPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)

Return participant information

The callback &#x60;PUT /participants/{Type}/{ID}&#x60; (or &#x60;PUT /participants/{Type}/{ID}/{SubId}&#x60;) is used to inform the client of a successful result of the lookup, creation, or deletion of the FSP information related to the Party. If the FSP information is deleted, the fspId element should be empty; otherwise the element should include the FSP information for the Party.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String subId = "subId_example"; // String | A sub-identifier of the party identifier, or a sub-type of the party identifier's type. For example, `PASSPORT`, `DRIVING_LICENSE`.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ParticipantsTypeIDPutResponse participantsTypeIDPutResponse = new ParticipantsTypeIDPutResponse(); // ParticipantsTypeIDPutResponse | Participant information returned.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.participantsSubIdByTypeAndID3(type, ID, subId, contentType, date, fsPIOPSource, participantsTypeIDPutResponse, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsSubIdByTypeAndID3");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **subId** | **String**| A sub-identifier of the party identifier, or a sub-type of the party identifier&#39;s type. For example, &#x60;PASSPORT&#x60;, &#x60;DRIVING_LICENSE&#x60;. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **participantsTypeIDPutResponse** | [**ParticipantsTypeIDPutResponse**](ParticipantsTypeIDPutResponse.md)| Participant information returned. | |
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

<a id="participantsSubIdByTypeAndIDPost"></a>
# **participantsSubIdByTypeAndIDPost**
> participantsSubIdByTypeAndIDPost(type, ID, subId, contentType, date, fsPIOPSource, accept, participantsTypeIDSubIDPostRequest, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength)

Create participant information

The HTTP request &#x60;POST /participants/{Type}/{ID}&#x60; (or &#x60;POST /participants/{Type}/{ID}/{SubId}&#x60;) is used to create information in the server regarding the provided identity, defined by &#x60;{Type}&#x60;, &#x60;{ID}&#x60;, and optionally &#x60;{SubId}&#x60; (for example, &#x60;POST /participants/MSISDN/123456789&#x60; or &#x60;POST /participants/BUSINESS/shoecompany/employee1&#x60;). An ExtensionList element has been added to this reqeust in version v1.1

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String subId = "subId_example"; // String | A sub-identifier of the party identifier, or a sub-type of the party identifier's type. For example, `PASSPORT`, `DRIVING_LICENSE`.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    String accept = "accept_example"; // String | The `Accept` header field indicates the version of the API the client would like the server to use.
    ParticipantsTypeIDSubIDPostRequest participantsTypeIDSubIDPostRequest = new ParticipantsTypeIDSubIDPostRequest(); // ParticipantsTypeIDSubIDPostRequest | Participant information to be created.
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    try {
      apiInstance.participantsSubIdByTypeAndIDPost(type, ID, subId, contentType, date, fsPIOPSource, accept, participantsTypeIDSubIDPostRequest, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod, contentLength);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsSubIdByTypeAndIDPost");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **subId** | **String**| A sub-identifier of the party identifier, or a sub-type of the party identifier&#39;s type. For example, &#x60;PASSPORT&#x60;, &#x60;DRIVING_LICENSE&#x60;. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **accept** | **String**| The &#x60;Accept&#x60; header field indicates the version of the API the client would like the server to use. | |
| **participantsTypeIDSubIDPostRequest** | [**ParticipantsTypeIDSubIDPostRequest**](ParticipantsTypeIDSubIDPostRequest.md)| Participant information to be created. | |
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
| **202** | Accepted |  -  |
| **400** | Bad Request |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **401** | Unauthorized |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **403** | Forbidden |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **404** | Not Found |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **405** | Method Not Allowed |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **406** | Not Acceptable |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **501** | Not Implemented |  * Content-Length -  <br>  * Content-Type -  <br>  |
| **503** | Service Unavailable |  * Content-Length -  <br>  * Content-Type -  <br>  |

<a id="participantsSubIdErrorByTypeAndID"></a>
# **participantsSubIdErrorByTypeAndID**
> participantsSubIdErrorByTypeAndID(type, ID, subId, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Return participant information error

If the server is unable to find, create or delete the associated FSP of the provided identity, or another processing error occurred, the error callback &#x60;PUT /participants/{Type}/{ID}/error&#x60; (or &#x60;PUT /participants/{Type}/{ID}/{SubId}/error&#x60;) is used.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String type = "type_example"; // String | The type of the party identifier. For example, `MSISDN`, `PERSONAL_ID`.
    String ID = "ID_example"; // String | The identifier value.
    String subId = "subId_example"; // String | A sub-identifier of the party identifier, or a sub-type of the party identifier's type. For example, `PASSPORT`, `DRIVING_LICENSE`.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ErrorInformationObject errorInformationObject = new ErrorInformationObject(); // ErrorInformationObject | Details of the error returned.
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.participantsSubIdErrorByTypeAndID(type, ID, subId, contentType, date, fsPIOPSource, errorInformationObject, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#participantsSubIdErrorByTypeAndID");
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
| **type** | **String**| The type of the party identifier. For example, &#x60;MSISDN&#x60;, &#x60;PERSONAL_ID&#x60;. | |
| **ID** | **String**| The identifier value. | |
| **subId** | **String**| A sub-identifier of the party identifier, or a sub-type of the party identifier&#39;s type. For example, &#x60;PASSPORT&#x60;, &#x60;DRIVING_LICENSE&#x60;. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **errorInformationObject** | [**ErrorInformationObject**](ErrorInformationObject.md)| Details of the error returned. | |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |
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

<a id="putParticipantsByID"></a>
# **putParticipantsByID**
> putParticipantsByID(ID, contentType, date, fsPIOPSource, participantsIDPutResponse, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod)

Return bulk participant information

The callback &#x60;PUT /participants/{ID}&#x60; is used to inform the client of the result of the creation of the provided list of identities.

### Example
```java
// Import classes:
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiClient;
import com.thitsaworks.mojaloop.coreconnector.fspiop.ApiException;
import com.thitsaworks.mojaloop.coreconnector.fspiop.Configuration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.models.*;
import com.thitsaworks.mojaloop.coreconnector.fspiop.api.ParticipantsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ParticipantsApi apiInstance = new ParticipantsApi(defaultClient);
    String ID = "ID_example"; // String | The identifier value.
    String contentType = "contentType_example"; // String | The `Content-Type` header indicates the specific version of the API used to send the payload body.
    String date = "date_example"; // String | The `Date` header field indicates the date when the request was sent.
    String fsPIOPSource = "fsPIOPSource_example"; // String | The `FSPIOP-Source` header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field `FSPIOP-Signature`).
    ParticipantsIDPutResponse participantsIDPutResponse = new ParticipantsIDPutResponse(); // ParticipantsIDPutResponse | Participant information returned.
    Integer contentLength = 56; // Integer | The `Content-Length` header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes).
    String xForwardedFor = "xForwardedFor_example"; // String | The `X-Forwarded-For` header field is an unofficially accepted standard used for informational purposes of the originating client IP address, as a request might pass multiple proxies, firewalls, and so on. Multiple `X-Forwarded-For` values should be expected and supported by implementers of the API.  **Note:** An alternative to `X-Forwarded-For` is defined in [RFC 7239](https://tools.ietf.org/html/rfc7239). However, to this point RFC 7239 is less-used and supported than `X-Forwarded-For`.
    String fsPIOPDestination = "fsPIOPDestination_example"; // String | The `FSPIOP-Destination` header field is a non-HTTP standard field used by the API for HTTP header based routing of requests and responses to the destination. The field must be set by the original sender of the request if the destination is known (valid for all services except GET /parties) so that any entities between the client and the server do not need to parse the payload for routing purposes. If the destination is not known (valid for service GET /parties), the field should be left empty.
    String fsPIOPEncryption = "fsPIOPEncryption_example"; // String | The `FSPIOP-Encryption` header field is a non-HTTP standard field used by the API for applying end-to-end encryption of the request.
    String fsPIOPSignature = "fsPIOPSignature_example"; // String | The `FSPIOP-Signature` header field is a non-HTTP standard field used by the API for applying an end-to-end request signature.
    String FSPIOP_URI = "FSPIOP_URI_example"; // String | The `FSPIOP-URI` header field is a non-HTTP standard field used by the API for signature verification, should contain the service URI. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    String fsPIOPHTTPMethod = "fsPIOPHTTPMethod_example"; // String | The `FSPIOP-HTTP-Method` header field is a non-HTTP standard field used by the API for signature verification, should contain the service HTTP method. Required if signature verification is used, for more information, see [the API Signature document](https://github.com/mojaloop/docs/tree/main/Specification%20Document%20Set).
    try {
      apiInstance.putParticipantsByID(ID, contentType, date, fsPIOPSource, participantsIDPutResponse, contentLength, xForwardedFor, fsPIOPDestination, fsPIOPEncryption, fsPIOPSignature, FSPIOP_URI, fsPIOPHTTPMethod);
    } catch (ApiException e) {
      System.err.println("Exception when calling ParticipantsApi#putParticipantsByID");
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
| **ID** | **String**| The identifier value. | |
| **contentType** | **String**| The &#x60;Content-Type&#x60; header indicates the specific version of the API used to send the payload body. | |
| **date** | **String**| The &#x60;Date&#x60; header field indicates the date when the request was sent. | |
| **fsPIOPSource** | **String**| The &#x60;FSPIOP-Source&#x60; header field is a non-HTTP standard field used by the API for identifying the sender of the HTTP request. The field should be set by the original sender of the request. Required for routing and signature verification (see header field &#x60;FSPIOP-Signature&#x60;). | |
| **participantsIDPutResponse** | [**ParticipantsIDPutResponse**](ParticipantsIDPutResponse.md)| Participant information returned. | |
| **contentLength** | **Integer**| The &#x60;Content-Length&#x60; header field indicates the anticipated size of the payload body. Only sent if there is a body.  **Note:** The API supports a maximum size of 5242880 bytes (5 Megabytes). | [optional] |
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

