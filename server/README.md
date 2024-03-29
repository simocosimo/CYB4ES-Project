# Track 9

Team members:
* s303333 COSIMO SIMONE
* s305388 INNOCENZI DAVIDE 
* s295491 SIRAGUSA PIERGIUSEPPE

## Instructions

To start the server, after cloning the repository from git you need to install the packages with the command `npm i`, then the server will be ready to be run with the command `node server.js`.

## Tables for Symmetric Encryption

Name table = Collection

| Column       | Type         | Description                  |
|--------------|--------------|------------------------------|
| IDmsg        | Integer      | UID                          |
| HMACmsg      | Text         | HMAC of the message          |
| message      | Text         | message                      |
| salt         | Text         | salt                         |
| checkMsg     | Text         | status of the message        |
| DRM          | Text         | DRM ID                       |
| ICCID        | Text         | ICCID                        |

## Tables for Asymmetric Encryption

Name table = certificates

| Column        | Type         | Description                                              |
|---------------|--------------|----------------------------------------------------------|
| K_pub         | Text         | the public key of a device in .pem format                |
| cert          | Text         | the certificate signed with priv_key of the server       |
| id_cert       | Integer      | the serialNumber associated with the certificate & K_pub |

Name table = Asymm_table

| Column        | Type         | Description                                        |
|---------------|--------------|----------------------------------------------------|
| id_msg        | Integer      | UID                                                |
| signature_msg | Text         | signature of the message                           |
| msg           | Text         | message                                            |
| hash_msg      | Text         | hash of the message                                |
| check_msg     | Text         | status of the message                              |
| serialNumber  | Text         | serialNumber associated to a K_pub & a signed Cert |

## List of APIs offered by the server

Provide a short description of the API you designed, with the required parameters. Please follow the proposed structure.

* [HTTP Method] [URL, with any parameter]
* [One-line about what this API is doing]
* [A (small) sample request, with body (if any)]
* [A (small) sample response, with body (if any)]
* [Error responses, if any]

## APIs Symmetric Encryption
Hereafter, we report the designed HTTP APIs, also implemented in the project.

### __List of entry on the DB__

URL: `/api/getDB`

Method: GET

Description: Get all the rows that are stored on the DB.

Request body: _None_

Response: `200 OK` (success) or `500 Internal Server Error` (generic error).

Response body: An array of objects, each describing a message encrypt.
```
[{
     { IDmsg: 1,
     HMACmsg: 'ae6bfa03e559aa6e70db7f884046928237e5504c', 
     hashMsg: '839ebc986212f108e5af9b86bf863b8dcd05340c',
     message: 'ciao', 
     salt: '1234', 
     checkMsg: 'ok',
     DRM : '1C21012220842E401C21012220842E401C21012220842E401C21012220842E40',
     ICCID : '1928374650123456789' },

},   { IDmsg: 2, 
     HMACmsg: '028d5db0d685e2750a7f9370ff59a919d70a6bed', 
     hashMsg: '484d4aa34be8df59475e226f991df5328c1f2d15',
     message: 'prova', 
     salt: '1111', 
     checkMsg: 'w4v',
     DRM : '1C21012220842E401C21012220842E401C21012220842E401C21012220842E40',
     ICCID : '1928374650123456789'  },
...
]
```

### __Get a message and salt (By checkMsg==w4v)__

URL: `/api/msg_and_salt`

Method: GET

Description: Get the IDmsg, message, hash message, salt, DRM and ICCID (if not null) identified by the checkMsg `w4v`.

Request body: _None_

Response: `200 OK` (success), `404 Not Found` (wrong code), or `500 Internal Server Error` (generic error).

Response body: An object or a vec of object, describing a single message or more then ones.
```
{
     IDmsg: 2, 
     message: 'prova', 
     hashMsg: '6279886fde090b3038f267098bcca771a6efa946', 
     salt: '1111',
     DRM : '1C21012220842E401C21012220842E401C21012220842E401C21012220842E40',
     ICCID : '1928374650123456789' 
}

```

### __Add a New Message__

URL: `/api/add_elements`

Method: POST

Description: Add a new message to the list of the messages.

Request body: An object representing a message (Content-Type: `application/json`).
```
{    'hmac': '028d5db0d685e2750a7f9370ff59a919d70a6bed',
     'hashMsg': '6279886fde090b3038f267098bcca771a6efa946',
     'message': 'prova', 
     'salt': '1111',
     'DRM' : '1C21012220842E401C21012220842E401C21012220842E401C21012220842E40',
     'ICCID' : '1928374650123456789'
}
```

Response: `201 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: _None_

### __Update the Check__

URL: `/api/updateCheck`

Method: PUT

Description: Update existing rows, identified by its id and hmac.

Request body: An array of objects representing the entire message (Content-Type: `application/json`).
```
{
     "verify":
     [
          {"hmac": "A3CD41B1B951CEA73F91E1F82BA9B2EC5B12A9A4", "id": 8},
          {"hmac": "DF4F7999D681CB3E3C0F047EDFBD48049F517D6C", "id": 9},
          {"hmac": "5d1ef41191903e02734a3e5e73b3650a9f03ef50", "id": 10},
          {"hmac": "5718114dc87b1aa1be948683ff1b20425f743736", "id": 13},
          ...
     ]
}
```

Response: `200 OK` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: An array of objects representing the messages with check==ok (Content-Type: `application/json`).

```
[
     {"id":8,"message":"ciao","hash":"A3CD41B1B951CEA73F91E1F82BA9B2EC5B12A9A4"},
     {"id":9,"message":"ciao","hash":"A3CD41B1B951CEA73F91E1F82BA9B2EC5B12A9A4"},
     {"id":10,"message":"ciao","hash":"A3CD41B1B951CEA73F91E1F82BA9B2EC5B12A9A4"},
     {"id":13,"message":"prova prova","hash":"A3CD41B1B951CEA73F91E1F82BA9B2EC5B12A9A4"},
     ...
]
```

### __Delete a Message__

URL: `/api/delete/:userID`

Method: DELETE

Description: Delete an existing message, identified by its id.

Request body: _None_

Response: `204 No Content` (success) or `503 Service Unavailable` (generic error).

Response body: _None_


## APIs Asymmetric Encryption
Hereafter, we report the designed HTTP APIs, also implemented in the project.

### __Handshake phase__

URL: `/api/asymm/handshake`

Method: POST

Description: creation of a certificate using the public key's device and self signed it using private key's server.

Request body: An object representing the public key of the client (Content-Type: `application/json`).

```
{    
     "n": "02fe2588c170874672422260c45bc11f1c024eddbd0e656a62e1c8b33cb885835f146472dd92b942401a0ae83a2e3084a7d4b5b46ab0c2048f264ac3e002000c53cdb94d3e26cdb03ba32c94d2c8d07afe00c210f9ed52a22cedff236b0c030e879ebe9ebd9aec69c1d377bca4873add28b6e2e1cbb343d5dd3690006cabdb4851cae204cb278abe980f5ffc02abf9e4d78377f9baf5058d371b9a2e15b1a98aabae1d0ca80f1998bb6720580235e365c5405b54488af8c4844b26cbc972b46500fb4e404e714454ceae55c7f43eea88f58504c67e1618556d0f63bde75e2bff8323c2e0c806e67139adb38397dcd90d445e2e26e78876f3363ae18f3cdc40bd",
     "e": "010001",
}
```

Response: `204 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: An object which describe the serial number of the certificate.

```
{
     "serialNumber":1
}
```

### __Add a New Message__

URL: `/api/asymm/add_elements`

Method: POST

Description: Add a new message to the list of the messages.

Request body: An object representing a message (Content-Type: `application/json`).

```
{
     "msg":"ciao", 
     "serialNumber":1, 
     "signature_msg": "c97cffee6b0aee809aadf4875a0e4667b695b4e1715356b529142218906b0491a6d9b62e7a687250c1936d26725f4f83cde223f4de1034b4ffb08ae45043608af9c2058837920a527eb66b7065822c4c2a49a7a45e83e5065914cc85aaebb9c6b08cde40137751cbc23a4ec8f80668095da1a3804d5267c60eac4ff9a800748c218064e1924649c6efa9e825f5fb583ed667b355d07cc446ffd7dbac2d565f63f1d61605f0fb989b6622a56cccf6a67a704b78b3d1a0f567cb1cc05c2b41c7ed8444a4980883fb9729d626af87a58f60b886635661383a142ea53fd7d88c00cf92b1dc7439f22082b9f78ea782ff13b0b04f66d3155fb384f703572a03265185"
}
```

Response: `201 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: _None_

### __Verify message__

URL: `/api/asymm/verify`

Method: GET

Description: the server checks for messages in w4v state.

Request body: _None_

Response: `201 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: a vector of objects containing id_messages, message and digest in waiting for verification.

```
[
     {
          "id_msg":2,
          "msg": "ciao",
          "hash_msg":"6eda4fcc7685abba4345b5c30dc13885aff7359a51d1877c7c55cf305dd52fc622bcd1a8183ac2e7c7fdc1fc14c3f685"
     }
]
```

### __Update the Check__

URL: `/api/asymm/updateCheck`

Method: PUT

Description: Update existing rows, identified by its id_msg if the signature store into the DB is equal to the signature received.

Request body: An array of objects representing the entire message (Content-Type: `application/json`).

```
{
     "update":
     [
          {
               "id_msg": 1,
               "hash_msg":"6eda4fcc7685abba4345b5c30dc13885aff7359a51d1877c7c55cf305dd52fc622bcd1a8183ac2e7c7fdc1fc14c3f685",
               "signature_msg": "c97cffee6b0aee809aadf4875a0e4667b695b4e1715356b529142218906b0491a6d9b62e7a687250c1936d26725f4f83cde223f4de1034b4ffb08ae45043608af9c2058837920a527eb66b7065822c4c2a49a7a45e83e5065914cc85aaebb9c6b08cde40137751cbc23a4ec8f80668095da1a3804d5267c60eac4ff9a800748c218064e1924649c6efa9e825f5fb583ed667b355d07cc446ffd7dbac2d565f63f1d61605f0fb989b6622a56cccf6a67a704b78b3d1a0f567cb1cc05c2b41c7ed8444a4980883fb9729d626af87a58f60b886635661383a142ea53fd7d88c00cf92b1dc7439f22082b9f78ea782ff13b0b04f66d3155fb384f703572a03265185"
          },
          {
               "id_msg": 2,
               "hash_msg":"6eda4fcc7685abba4345b5c30dc13885aff7359a51d1877c7c55cf305dd52fc622bcd1a8183ac2e7c7fdc1fc14c3f685",
               "signature_msg": "c97cffee6b0aee809aadf4875a0e4667b695b4e1715356b529142218906b0491a6d9b62e7a687250c1936d26725f4f83cde223f4de1034b4ffb08ae45043608af9c2058837920a527eb66b7065822c4c2a49a7a45e83e5065914cc85aaebb9c6b08cde40137751cbc23a4ec8f80668095da1a3804d5267c60eac4ff9a800748c218064e1924649c6efa9e825f5fb583ed667b355d07cc446ffd7dbac2d565f63f1d61605f0fb989b6622a56cccf6a67a704b78b3d1a0f567cb1cc05c2b41c7ed8444a4980883fb9729d626af87a58f60b886635661383a142ea53fd7d88c00cf92b1dc7439f22082b9f78ea782ff13b0b04f66d3155fb384f703572a03265185"
          },
          ...
     ]
}
```

Response: `200 OK` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: An array of objects representing the messages with check==ok (Content-Type: `application/json`).

```
[
     {
          "id_msg":2,
          "msg":"ciao","signature_msg":"c97cffee6b0aee809aadf4875a0e4667b695b4e1715356b529142218906b0491a6d9b62e7a687250c1936d26725f4f83cde223f4de1034b4ffb08ae45043608af9c2058837920a527eb66b7065822c4c2a49a7a45e83e5065914cc85aaebb9c6b08cde40137751cbc23a4ec8f80668095da1a3804d5267c60eac4ff9a800748c218064e1924649c6efa9e825f5fb583ed667b355d07cc446ffd7dbac2d565f63f1d61605f0fb989b6622a56cccf6a67a704b78b3d1a0f567cb1cc05c2b41c7ed8444a4980883fb9729d626af87a58f60b886635661383a142ea53fd7d88c00cf92b1dc7439f22082b9f78ea782ff13b0b04f66d3155fb384f703572a03265185"
     },
     {
          "id_msg":3,
          "msg":"prova","signature_msg":"c97cffee6b0aee809aadf4875a0e4667b695b4e1715356b529142218906b0491a6d9b62e7a687250c1936d26725f4f83cde223f4de1034b4ffb08ae45043608af9c2058837920a527eb66b7065822c4c2a49a7a45e83e5065914cc85aaebb9c6b08cde40137751cbc23a4ec8f80668095da1a3804d5267c60eac4ff9a800748c218064e1924649c6efa9e825f5fb583ed667b355d07cc446ffd7dbac2d565f63f1d61605f0fb989b6622a56cccf6a67a704b78b3d1a0f567cb1cc05c2b41c7ed8444a4980883fb9729d626af87a58f60b886635661383a142ea53fd7d88c00cf92b1dc7439f22082b9f78ea782ff13b0b04f66d3155fb384f703572a03265185"
     }
     ...
]
```
