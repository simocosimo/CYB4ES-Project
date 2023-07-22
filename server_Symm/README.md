# Track 9

Team members:
* s303333 COSIMO SIMONE
* s305388 INNOCENZI DAVIDE 
* s295491 SIRAGUSA PIERGIUSEPPE

## Instructions

To start the server, after cloning the repository from git you need to install the packages with the command `npm i`, then the server will be ready to be run with the command `node server.js`.

## List of APIs offered by the server

Provide a short description of the API you designed, with the required parameters. Please follow the proposed structure.

* [HTTP Method] [URL, with any parameter]
* [One-line about what this API is doing]
* [A (small) sample request, with body (if any)]
* [A (small) sample response, with body (if any)]
* [Error responses, if any]

## APIs Symmetric Ecryption
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


## APIs Asymmetric Ecryption
Hereafter, we report the designed HTTP APIs, also implemented in the project.

### __Handshake phase__

URL: `/api/asymm/handshake`

Method: POST

Description: creation of a certificate using the public key's device and self signed it using private key's server.

Request body: An object representing a message (Content-Type: `application/json`).

```
{    
     "kpub" : "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5TO/SCYHoeudCfm4FzNH36eTx5D76HYdQGN/lyvKzP1DF2nUyluDq9hDvx8PK5ogppc3wlptD8zdsLJ/    +uNh7UYfCscnIiTY+QYAc51HKM/E1dm+tqk2Xu952F6cQv0klYCuNCoKhwgcLta9p+zSTI0E8Gnptknpt8+FePpHSRNN3Fvcg7vun7jmk2qpweSEBcuK31fZYKXtw2rGIeNo7sGILN5WEdOjE2ShFeY34erzw3n3Nl4iFJ9ZK0hK+79itXrwsZm54n2etIzwlLeHHGfHJbNHMu9GHAd2rv+0VpjYWmOF8VaCFjtzA/8dD3/EuvGzFDvSDfIfbUgJDuIZ1QIDAQAB-----END PUBLIC KEY-----"
}
```

Response: `204 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: _None_