# Design
## High Level Design 
![High_Level_Design](https://github.com/user-attachments/assets/f04cefbe-2ee8-4a65-8606-3488d0caa502)

#### 1. User Accesses Protected Resource

The user attempts to access a protected resource in the **S3-Object-Browser** application but is not logged in.

#### 2. User State Saved in Cache

The application saves the user's state in an in-memory cache, likely to preserve the user's session information and intended actions for post-login redirection.

#### 3. Redirect to OIDC Server

The application redirects the user to the OIDC server to initiate the authentication process.

#### 4. User Logs Into OIDC Server

The user provides credentials (e.g., username and password) to the OIDC server, which is a Spring Boot-based app configured as an OIDC provider.

#### 5. Redirection Back to S3-Object-Browser with Code

After successful authentication, the OIDC server redirects the user back to the S3-Object-Browser app, including an authorization code as part of the redirect URI.

#### 6. Token Exchange

The S3-Object-Browser app exchanges the authorization code received in the previous step for an ID token and an access token at the OIDC server's token endpoint.

#### 7. S3 Metadata Retrieval

The application uses the access token to retrieve metadata about an S3 object from AWS. The access token may be used to authenticate the request to AWS if the AWS resources are protected by OIDC-based access control.

#### 8. Audit Data Storage

Actions such as the user's access and activities are logged into an H2 database for auditing purposes. This typically includes data like access times, accessed resources, and user identifiers.

#### 9. S3 Object Metadata Returned to User

The metadata retrieved from AWS S3 is then returned to the user, completing the interaction flow.


## Sequence Diagrams (Low Level Design)

### s3-object-browser (Resource Server) App Startup Sequence
![s3-object-browser (Resource Server) App Startup ](https://github.com/user-attachments/assets/a9f5f0e6-5902-4661-8a01-0477b6cd6676)



#### 1. RsaKeyService Initialization
- **Action**: The `s3-object-browser` application starts up.
- **Process**:
  - The `RsaKeyService` within the `s3-object-browser` app initializes.

#### 2. Fetch RSA Public Key
- **Action**: The `RsaKeyService` sends a request to the OIDC Server.
- **Process**:
  - The service requests the RSA public key details needed for JWT signature validation.
  - It uses the OIDC Server's `.well-known/oauth2/jwks` endpoint to retrieve the keys.

#### 3. RSA Public Key Details Retrieved
- **Action**: The OIDC Server responds to the `RsaKeyService` request.
- **Process**:
  - The OIDC Server provides the RSA public key details from its JWKS (JSON Web Key Set) endpoint.
  - This key set contains the cryptographic keys used for signing the tokens.

#### 4. Instance Creation and JWT Validation Setup
- **Action**: The `RsaKeyService` processes the received public key data.
- **Process**:
  - An instance of `RSAKey` (Public key) is created for ID token JWT signature validation.
  - This key is then configured within the service to validate the signatures of JWTs provided by the OIDC Server in subsequent authentications.


### Sequence Diagram: User Access to Protected Resources (Authentication process and /object_metadata/filename endpoint)
![User Access Protected Resources (like _object_metadata_file1 txt)](https://github.com/user-attachments/assets/2c63e1da-6a03-42dd-8ba1-7747a25e4ad1)


### Steps:

#### 1. Initial Request
- **User Browser**: Initiates an HTTP request to access a protected resource (e.g., `/object_metadata/file1.txt`).
- **OIDCAuthenticationFilter**: Intercepts the request to check if the user is logged in (checks user session).

#### 2. Authentication Check
- **OIDCAuthenticationFilter**: 
  - Checks if a user attribute exists in the session.
  - If not logged in, it triggers the OIDC login flow by generating random strings for state and nonce and saving these along with the original request in the cache.
  - If logged in, request is forwarded to requested resource's controller 

#### 3. Redirect to OIDC Server
- **User Browser**: Is redirected to the OIDC server's login page via an HTTP redirect initiated by the OIDCAuthenticationFilter.
- **OIDC Server**: Displays the login page where the user submits their credentials.

#### 4. User Authentication
- **User Browser**: Submits credentials to the OIDC Server.

#### 5. Authorization Code Grant
- **OIDC Server**: Upon successful authentication, redirects the user back to the application with an authorization code and state (callback).

#### 6. Authorization Code Exchange
- **OIDCAuthenticationFilter**:
  - Receives the callback request containing the authorization code and state. 
  - Since the **OIDCAuthenticationFilter** is configured to skip user authentication check for /callback, the request is forwared to **OIDCController**

#### 7. Token Retrieval
- **OIDCController**
  - Sends a request to the OIDC server’s token endpoint to exchange the authorization code for an ID token and access token.
- **OIDC Server**:
  - Validates the authorization code, client credentials, and redirect URI.
  - Returns an ID token and access token to the OIDC Authentication Filter.

#### 8. User Session Establishment
- **OIDCController**:
  - Extracts user details from the ID token.
  - Sets user attribute in the HTTP session.

#### 9. Retrieval of Original Request
- **OIDCController**: 
  - Retrieves the originally requested resource (and any saved state data) from the user's state data stored in the cache.

#### 10. Accessing Protected Resource
- **User Browser**: Is redirected back to the originally requested URL (`/object_metadata/file1.txt`).
- **ObjectMetadataController**: Processes the request.
- **AWS**: Retrieves object metadata.

#### 11. Return Metadata
- **ObjectMetadataController**: Returns the object metadata to the User Browser as a JSON response.


### Audit 
#### Sequence Diagram: Audit Log Save
![Audit Log Save ](https://github.com/user-attachments/assets/308cd3af-2998-48fe-ac69-8355262e169d)

#### Steps:

#### 1. Initial Request
- **Logged In User**: Initiates an HTTP request to access object metadata.
  - **URL**: `/object_metadata/file1`

#### 2. Request Handling by Spring Framework
- **Spring Framework (Dispatcher Servlet / RequestMappingHandlerAdapter)**:
  - Receives the HTTP request.
  - Responsible for routing the request to the appropriate controller.

#### 3. AOP Interception
- **RestEndpointAuditAspect**:
  - Intercepts the call with an Around advice before it reaches the `ObjectMetadataController`.
  - This aspect is used for audit logging.

#### 4. Controller Processing
- **ObjectMetadataController**:
  - Processes the request to fetch metadata from an S3 object.
  - Calls the AWS service to get the metadata for `file1.txt`.

#### 5. AWS Interaction
- **AWS**:
  - Returns the requested S3 object metadata to the `ObjectMetadataController`.

#### 6. Audit Logging
- **RestEndpointAuditAspect**:
  - After receiving the S3 object metadata, the audit aspect logs the transaction details.
  - This includes data such as user, accessed object, and timestamp.
  - The audit data is prepared for storage.

#### 7. Data Persistence
- **H2 Database**:
  - Audit data is saved to the H2 database for long-term storage and future audits.

#### 8. Return Response
- **ObjectMetadataController**:
  - Sends the S3 object metadata back to the user as a JSON response.
- **Spring Framework (Dispatcher Servlet / RequestMappingHandlerAdapter)**:
  - Completes the response handling by sending the JSON metadata back to the user.


The Aspect-Oriented Programming approach allows for clean separation of concerns, where the logging logic is decoupled from the business logic, enhancing maintainability and scalability of the application.

#### Sequence Diagram: Audit Log endpoint `/audit_log`

![Audit Log endpoint _audit_log](https://github.com/user-attachments/assets/037e888d-de8a-4a57-92ff-3f10e9fea955)


#### Steps:

#### 1. User Initiates Request
- **Logged In User**:
  - Makes an HTTP request to the `/audit_log` endpoint. This action typically occurs from a web browser or other client interface where the user is already authenticated.

#### 2. Controller Handles Request
- **AuditController**:
  - Receives the request to fetch audit logs.
  - This controller is responsible for handling web requests related to audit logs.

#### 3. Retrieve Audit Logs
- **AuditRepository (H2 DB)**:
  - The `AuditController` invokes the `findAll()` method on the `AuditRepository`.
  - This repository layer interacts with the H2 database to retrieve all audit log entries stored in the database.

#### 4. Return Audit Logs
- **AuditRepository (H2 DB)**:
  - Returns a list of all audit logs to the `AuditController`.
  - The data returned is typically a collection of audit entries that record user activities or system events.

#### 5. Send Response to User
- **AuditController**:
  - Receives the list of audit logs and packages them into a JSON format.
  - Sends the audit logs JSON response back to the logged-in user.


### Health Check 
![Health Check (_health)](https://github.com/user-attachments/assets/0bc8fecc-99fa-47a8-b593-6bfe5569fdac)


