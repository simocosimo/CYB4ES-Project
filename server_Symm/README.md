# Track 9

## Team name: Le Winx

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

## APIs
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
     message: 'ciao', 
     salt: '1234', 
     checkMsg: 'ok' },

},   { IDmsg: 2, 
     HMACmsg: '028d5db0d685e2750a7f9370ff59a919d70a6bed', 
     message: 'prova', 
     salt: '1111', 
     checkMsg: 'w4v'  },
...
]
```

### __Get a message and salt (By checkMsg==w4v)__

URL: `/api/msg_and_salt`

Method: GET

Description: Get the message and salt identified by the checkMsg `w4v`.

Request body: _None_

Response: `200 OK` (success), `404 Not Found` (wrong code), or `500 Internal Server Error` (generic error).

Response body: An object, describing a single message.
```
{
     { IDmsg: 2, 
     HMACmsg: '028d5db0d685e2750a7f9370ff59a919d70a6bed', 
     message: 'prova', 
     salt: '1111', 
     checkMsg: 'w4v'
}

```

### __Add a New Message__

URL: `/api/add_elements`

Method: POST

Description: Add a new message to the list of the messages.

Request body: An object representing a message (Content-Type: `application/json`).
```
{    'hmac': '028d5db0d685e2750a7f9370ff59a919d70a6bed', 
     'message': 'prova', 
     'salt': '1111',
}
```

Response: `201 Created` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: _None_

### __Update the Check__

URL: `/api/updateCheck`

Method: PUT

Description: Update an existing row, identified by its id and hmac.

Request body: An object representing the entire message (Content-Type: `application/json`).
```
{    'hmac': '028d5db0d685e2750a7f9370ff59a919d70a6bed', 
     'id': 3,
}
```

Response: `200 OK` (success) or `503 Service Unavailable` (generic error). If the request body is not valid, `422 Unprocessable Entity` (validation error).

Response body: _None_

### __Delete a Message__

URL: `/api/delete/:userID`

Method: DELETE

Description: Delete an existing message, identified by its id.

Request body: _None_

Response: `204 No Content` (success) or `503 Service Unavailable` (generic error).

Response body: _None_