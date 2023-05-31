'use strict';
const express = require('express');
const morgan = require('morgan'); // logging middleware
const { check, body } = require('express-validator'); // validation middleware
const dao = require('./daoChallenge'); // module for accessing the DB
const session = require('express-session'); // enable sessions
const cors = require('cors');

// init express
const PORT = 3001;
const app = express();

// set-up the middlewares
app.use(morgan('dev'));
app.use(express.json());
const corsOptions = { origin: 'http://localhost:3000', credentials: true, };
app.use(cors(corsOptions));

// set up the session
app.use(session({
    secret: 'a secret sentence not to share with anybody and anywhere, used to sign the session ID cookie', resave: false, saveUninitialized: false
}));

//mando il DB completo.
// GET /api/getDB
app.get('/api/getDB', async (req, res) => {
    dao.sendDB().then(rowsDB => res.json(rowsDB)).catch(() => res.status(500).json({ error: `Database error while retrieving rowsDB` }).end())
});

//aggiorno il check
// PUT /api/updateCheck
app.put('/api/updateCheck', async (req, res) => { 
    const vettore = req.body.verify; //req.body ha il vettore di elem da verificare.
    try {
        const promiseMsg = [];
        for (const elem of vettore) {
            promiseMsg.push(dao.updateCheck(elem));
        }
        const results = await Promise.all(promiseMsg);
        const vett2 = results.filter((elem) => elem.id > 0);
        res.status(201).json(vett2).end();
    } catch (err) {
        res.status(503).json({ error: `Database error during the update check.` }).end();
    }
});

//aggiorno lo stato
// PUT /api/updateState
// app.put('/api/updateState', async (req, res) => {
//     dao.updateState(req.body.id,req.body.request_state).then(infoUser => res.json(infoUser)).catch(() => res.status(500).json({ error: `Database error while update state` }).end())
// });

//mando all'app l'hash del messaggio, il sale e l'ID della riga per la verifica.
// GET /api/msg_and_salt
app.get('/api/msg_and_salt', async (req, res) => {
    dao.getMsg_and_salt().then(msg_and_salt => res.json(msg_and_salt)).catch(() => res.status(500).json({ error: `Database error while retrieving msg_and_salt` }).end())
});

//inserisco nel DB HMAC,hash del msg, msg e salt
// POST /api/add_esements
app.post('/api/add_elements', async (req, res) => {
    let elem = req.body;
    dao.addElements(elem).then(elem => res.json(elem)).catch(() => res.status(500).json({ error: `Database error while retrieving elems` }).end())
});

// DELETE /api/delete/:userID
app.delete('/api/delete/:userID',async (req, res) => {
    let userID = req.params.userID;
    try {
        await dao.deleteFromDB(userID);
        res.status(204).end();
    } catch (err) {
        res.status(503).json({ error: `Database error during the deletion of the DB with id: ${userID}.` });
    }
});

app.listen(PORT, () => { console.log(`Server listening at http://localhost:${PORT}/`) });